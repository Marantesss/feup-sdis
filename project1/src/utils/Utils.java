package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class Utils {

    public static String CRLF = "\r\n";

    public static byte hexCR = 0xD;

    public static byte hexLF = 0xA;

    /**
     * 
     * @param message
     * @return
     */
    public static byte[] getHeader(byte[] message) {
        int headerLength;
        // find the two CRLF flags
        for (headerLength = 0; headerLength < message.length; headerLength++) {
            if (message[headerLength] == hexCR && message[headerLength + 1] == hexLF && message[headerLength + 2] == hexCR && message[headerLength + 3] == hexLF)
                break;
        }

        return Arrays.copyOfRange(message, 0, headerLength);
    }

    /**
     * 
     * @param message
     * @return
     */
    public static byte[] getBody(byte[] message) {
        int headerLength;
        // find the two CRLF flags
        for (headerLength = 0; headerLength < message.length; headerLength++) {
            if (message[headerLength] == hexCR && message[headerLength + 1] == hexLF && message[headerLength + 2] == hexCR && message[headerLength + 3] == hexLF)
                break;
        }

        return Arrays.copyOfRange(message, headerLength + 4, message.length);
    }

    /**
     * 
     * @param data
     * @return
     */
    public static byte[] getSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return hash;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Exception thrown for incorrect algorithm " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param hash
     * @return
     */
    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * 
     * @param arrayA
     * @param arrayB
     * @return
     */
    public static byte[] concatenateArrays(byte[] arrayA, byte[] arrayB) {
        byte[] arrayC = new byte[arrayA.length + arrayB.length];
		System.arraycopy(arrayA, 0, arrayC, 0, arrayA.length);
		System.arraycopy(arrayB, 0, arrayC, arrayA.length, arrayB.length);
		return arrayC;
    }

    /**
     * 
     * @param low
     * @param high
     * @return
     */
    public static int getRandomNumber(int low, int high) {
        Random rd = new Random();
        return rd.nextInt(high - low + 1) + high;
    }

    /**
     * 
     * @return
     */
    public static char getCharSeparator() {
		String os = System.getProperty("os.name");

		if (os.toLowerCase().contains("win"))
			return '\\';
		
		return '/';
	}	

}
