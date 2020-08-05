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
    private static final String FILE_NAME_CHECKSUM = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String PRODUCT_NAME = "safemc-anticrash";

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress("79.137.50.237", 2115));
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            int packetId = dataInputStream.readInt();
            if(packetId != 0) {
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
            outputStream1.writeUTF(PRODUCT_NAME);
            outputStream1.writeUTF(USERNAME);
            outputStream1.writeUTF(PASSWORD);
            outputStream1.writeUTF(getHash(Files.readAllBytes(new File(FILE_NAME_CHECKSUM).toPath())));
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

                try {
                    System.out.println(dataInputStream1.readUTF());
                } catch (Exception gze) {
                    gze.printStackTrace();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
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
