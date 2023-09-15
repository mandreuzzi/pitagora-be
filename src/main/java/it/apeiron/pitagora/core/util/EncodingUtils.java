package it.apeiron.pitagora.core.util;

import java.nio.charset.StandardCharsets;
import org.bson.internal.Base64;
import org.springframework.util.DigestUtils;

public class EncodingUtils {

    public static String calcHash(String dataBase64) {
        byte[] bytes = Base64.decode(_extractDataBase64(dataBase64));
        return calcHash(bytes);
    }

    public static String calcHash(byte[] bytes) {
        return DigestUtils.md5DigestAsHex(bytes);
    }
    public static byte[] base64toByteArray(String dataBase64) {
        return Base64.decode(EncodingUtils._extractDataBase64(dataBase64));
    }

    private static String _extractDataBase64(String base64) {
        return base64.substring(base64.indexOf("base64,") + 7);
    }

    public static String encodeBase64(String data) {
        return java.util.Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeBase64(byte[] data) {
        return Base64.encode(data);
    }
    public static String decodeBase64(String data) {
        return new String(java.util.Base64.getDecoder().decode(data));
    }
}
