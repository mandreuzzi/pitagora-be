package it.apeiron.pitagora.core.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.SneakyThrows;

public class IOUtils {

    @SneakyThrows
    public static byte[] zip(InputStream is, String zipEntryName) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        ZipOutputStream zipOut = new ZipOutputStream(os);
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while((length = is.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        zipOut.close();
        is.close();
        os.close();

        return os.toByteArray();
    }

    @SneakyThrows
    public static byte[] unzip(InputStream is) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
//            File newFile = new File(destDir);
//            if (zipEntry.isDirectory()) {
//                if (!newFile.isDirectory() && !newFile.mkdirs()) {
//                    throw new IOException("Failed to create directory " + newFile);
//                }
//            } else {
////                 fix for Windows-created archives
//                File parent = newFile.getParentFile();
//                if (!parent.isDirectory() && !parent.mkdirs()) {
//                    throw new IOException("Failed to create directory " + parent);
//                }

            // write file content
            int len;
            while ((len = zis.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.close();
//            }
            zipEntry = zis.getNextEntry();
//            }

        }
        zis.closeEntry();
        zis.close();
        return os.toByteArray();
    }
}
