package com.googlecode.cryptogwt.util;

import java.util.Arrays;

public class ByteArrayUtils {
   
    public static void xor(byte[] bytes, int offset, byte[] bytesToMix, int mixOffset,
            int len) {
        int bytesLength = offset + len; 
        for (; offset < bytesLength; offset++) {
            bytes[offset] ^= bytesToMix[mixOffset++];
        }
    }
    
    public static void xor(byte[] dest, byte[] bytesToMix) {
        assert dest.length == bytesToMix.length : "different lengths: " + dest.length + " != " +
            bytesToMix.length;
        xor(dest, 0, bytesToMix, 0, dest.length);
    }

    public static byte[] copyOfRange(byte[] bytes, int offset, int len) {
        byte[] result = new byte[len];
        System.arraycopy(bytes, offset, result, 0, len);
        return result;
    }

    public static byte[] toBytes(int integer) {
        byte[] result = new byte[4];
        toBytes(integer, result, 0);
        return result;
    }
    
    public static void toBytes(int integer, byte[] output, int offset) {
        assert output.length - offset >= 4;
        int i = offset;
        output[i++] = (byte) (integer >>> 24);
        output[i++] = (byte) (integer >>> 16);
        output[i++] = (byte) (integer >>> 8);
        output[i++] = (byte) (integer & 0xff);
    }
    
    public static byte[] toBytes(int...integer) {
        byte[] result = new byte[integer.length * 4];
        int offset = 0;
        for (int i=0; i < integer.length; i++) {
            toBytes(integer[i], result, offset);
            offset += 4;
        }
        return result;
    }
    
    public static byte[] toBytes(String s) {
        return toBytes(s.toCharArray());
    }
    
    public static byte[] toBytes(char[] chars) {        
        byte[] result = new byte[chars.length];
        int i = 0;
        for (char c : chars) {
            result[i++] = (byte) c;
        }
        return result;
    }
    
    public static byte[] concatenate(byte[]...bytes) {
        int length = 0;
        for (byte[] array : bytes) length += array.length;
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array : bytes) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static byte[] toBytes(long longValue) {
        return new byte[] {                
                (byte) (longValue >>> 56),
                (byte) (longValue >>> 48),
                (byte) (longValue >>> 40),
                (byte) (longValue >>> 32),                
                (byte) (longValue >>> 24),
                (byte) (longValue >>> 16),
                (byte) (longValue >>> 8),
                (byte) (longValue & 0xff)
        };
    }
    
    public static byte[] hexToBytes(String hex) {        
        hex = removeSpaces(hex); // Remove spaces
        assert hex.length() % 2 == 0 : "must be even number of bytes";
        int resultLen = hex.length()/2;
        byte[] result = new byte[resultLen];
        int j=0;
        for (int i=0; i < resultLen; i++) {            
            result[i] = (byte) (Byte.parseByte(hex.substring(j, ++j), 16) << 4 |
                    Byte.parseByte(hex.substring(j, ++j), 16));
        }
        return result;
    }
    
    private static String removeSpaces(String string) {
        string = string.replaceAll("\\s+", "");
        return string;
    }
    
    public static String toHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }        
        return builder.toString();
    }
    
    public static String toHexString(byte[] bytes, int offset, int length) {
        return toHexString(copyOfRange(bytes, offset, length));
    }
    

    
    /**
     * Get the given bit in the encoded bytes.  Note: bit 0 is assumed to be the LSB.
     * 
     * @param bytes
     * @param bitNr
     * @return
     */
    public static int getBit(byte[] bytes, int bitNr) {               
        int byteNr = bytes.length - (bitNr / 8) - 1;
        int bitNrInByte = bitNr % 8;
        return bytes[byteNr] >>> bitNrInByte & 1; 
        
    }

    public static void setBit(byte[] bytes, int bitNr, int bit) {
        int byteNr = bytes.length - (bitNr / 8) - 1;
        int bitNrInByte = bitNr % 8;
        if (bit != 0) {
            bytes[byteNr] |= 1 << bitNrInByte;
        } else {
            bytes[byteNr] &= ~(1 << bitNrInByte);
        }
    }

    public static String toAsciiString(byte[] output) {
       char[] chars = new char[output.length];
       for (int i = 0; i < output.length; i++) {
           chars[i] = (char) output[i];
       }
       return new String(chars);
    }

    public static int toInteger(byte[] input) {
       return toInteger(input, 0);
    }

    private static int toInteger(byte[] input, int offset) {
        assert offset + 4 <= input.length : "Invalid length " + input.length;
        return ((input[offset++] & 0xff) << 24) | 
            ((input[offset++] & 0xff) << 16) |
            ((input[offset++] & 0xff) << 8) |
            ((input[offset++] & 0xff));
    }

    public static int[] toIntegerArray(byte[] input, int offset, int len) {
        assert len % 4 == 0 : "Must be a multiple of 4 bytes";
        int[] result = new int[len/4];
        toIntegerArray(input, offset, len, result, 0);
        return result;
    }
    
    public static void toIntegerArray(byte[] input, int offset, int len, int[] output, int outputOffset) {
        assert len % 4 == 0 : "Must be a multiple of 4 bytes";
        int outputLen = len / 4;
        assert offset + outputLen < output.length : "Output buffer too short";        
        for (int i=outputOffset; i<outputOffset+outputLen; i++) {
            output[i] = toInteger(input, offset);
            offset += 4;
        }        
    }
    
    public static byte[] repeat(byte b, int nrRepeats) {
        byte[] result = new byte[nrRepeats];
        Arrays.fill(result, b);
        return result;
    }

    public static byte[] copyOf(byte[] bytes) {
        return copyOfRange(bytes, 0, bytes.length);
    }

}
