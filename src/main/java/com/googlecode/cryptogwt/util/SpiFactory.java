package com.googlecode.cryptogwt.util;


import java.security.NoSuchAlgorithmException;

public interface SpiFactory<T> {
    T create(Object constructorParam) throws NoSuchAlgorithmException;
}
