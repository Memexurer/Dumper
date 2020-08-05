package pl.memexurer.dumper;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;

public class Dumper {
    private static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("79.137.50.237", 2115);

    private final String productName;
    private final String name;
    private final String password;
    private final File file;

    private Dumper(String productName, String name, String password, File file) {
        this.productName = productName;
        this.name = name;
        this.password = password;
        this.file = file;
    }

    private void connect() {
        Socket socket = new Socket();

        try {
            socket.connect(SERVER_ADDRESS);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            int packetId = dataInputStream.readInt();
            if (packetId != 0) {
                System.out.println("cos sie zjebalo xddd");
                return;
            }

            byte[] bytes = new byte[dataInputStream.readInt()];
            dataInputStream.readFully(bytes);

            byte[] bytez = new byte[dataInputStream.readInt()];
            dataInputStream.readFully(bytez);

            CipherInputStream inputStream = new CipherInputStream(dataInputStream, initCipher(bytes, bytez));
            DataInputStream dataInputStream1 = new DataInputStream(inputStream);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream1 = new DataOutputStream(byteArrayOutputStream);
            outputStream1.writeUTF(productName);
            outputStream1.writeUTF(name);
            outputStream1.writeUTF(password);
            outputStream1.writeUTF(getHash(Files.readAllBytes(file.toPath())));
            outputStream1.writeBoolean(false);
            outputStream1.close();

            outputStream.writeInt(0);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();

            int packetId1 = dataInputStream1.readInt();

            System.out.println(packetId1);
            if (packetId1 == 1) {
                System.out.println("Invalid checksum.");
                return;
            }

            if (packetId1 != 2) {
                System.out.println("bad packet id: " + packetId1);
            } else {
                DumperData dumperData = DumperData.readCycer(dataInputStream1);
                DumperData dumperData1 = DumperData.readCycer(dataInputStream1);

                dumperData.save();
                dumperData1.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Uzycie: dumper (nazwa produktu) (nick) (haslo) (sciezka do jarki launchera)");
            return;
        }

        File launcherJarFile = new File(args[3]);
        if (!launcherJarFile.exists()) {
            System.out.println("Nie znaleziono launchera.");
            return;
        }

        System.out.println("odpalanie tego czegos...");
        new Dumper(args[0], args[1], args[2], launcherJarFile).connect();
    }

    private static String getHash(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(bytes);
        byte[] digestBytes = digest.digest();

        StringBuilder builder = new StringBuilder();

        int i = 0;
        while (i < digestBytes.length) {
            builder.append(Integer.toString((digestBytes[i] & 0xff) | 256, 16).substring(1));
            ++i;
        }

        return builder.toString();
    }

    private static Cipher initCipher(byte[] b1, byte[] b2) throws Exception {
        Cipher cipa = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipa.init(2, new SecretKeySpec(b1, "AES"), new IvParameterSpec(b2));
        return cipa;
    }
}
