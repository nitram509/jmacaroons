package com.googlecode.cryptogwt.provider;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

public class JsArrayUtils {
    public static byte[] toByteArray(JsArrayInteger array) {
        byte[] result = new byte[array.length() * 4];
        toByteArray(array, result, 0);
        return result;
    }
    public static byte[] toByteArray(JsArrayInteger array, byte[] result, int offset) {        
        int j = offset;        
        for (int i=0; i < array.length(); i++) {
            int value = array.get(i);         
            result[j++] = (byte) (value >>> 24);
            result[j++] = (byte) (value >>> 16);
            result[j++] = (byte) (value >>> 8);
            result[j++] = (byte) (value);
        }
        return result;
        
    }
    
    public static JsArrayInteger toJsArrayInteger(byte[] array) {
        return toJsArrayInteger(array, 0, array.length);        
    }
    
    public static JsArrayInteger toJsArrayInteger(byte[] array, int offset, int len) {
        assert (len - offset) % 4 == 0;
        JsArrayInteger result = (JsArrayInteger) JavaScriptObject.createArray();
        int j = 0;
        int totalLen = len + offset;
        for (int i=offset; i < totalLen;) {
            int value = 
                (array[i++] << 24) |
                ((array[i++] & 0xff) << 16) |
                ((array[i++] & 0xff) << 8) |
                (array[i++] & 0xff);
            result.set(j++, value);
        }
        return result;
    }
}
