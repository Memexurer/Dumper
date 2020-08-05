package pl.memexurer.dumper;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DumperData {
    private final String[] strings;
    private final List<byte[]> bytes;

    private DumperData(String[] strings, List<byte[]> bytes) {
        this.strings = strings;
        this.bytes = bytes;
    }

    public static DumperData readCycer(DataInputStream dataInputStream) throws Exception {
        String[] array = new String[dataInputStream.readInt()];
        for (int y = 0; y < array.length; y++)
            array[y] = dataInputStream.readUTF();

        int a = 0;
        int b = dataInputStream.readInt();
        List<byte[]> e = new ArrayList<>();
        while (a < b) {
            int c = dataInputStream.readInt();
            byte[] d = new byte[c];
            dataInputStream.readFully(d);
            e.add(d);
            a++;
        }

        return new DumperData(array, e);
    }

    public void save() throws IOException {
        File saveDirectory = new File("jar");
        saveDirectory.mkdirs();

        int i = 0;
        for (String str : strings) {
            File file = new File(saveDirectory, str.substring(0, str.lastIndexOf('.')).replace('.', '/') + str.substring(str.lastIndexOf('.')));
            file.getParentFile().mkdirs();
            file.createNewFile();

            Files.write(file.toPath(), bytes.get(i++));
        }
    }
}
