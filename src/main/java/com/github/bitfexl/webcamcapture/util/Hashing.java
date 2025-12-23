package com.github.bitfexl.webcamcapture.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class Hashing {
    public String md5(byte[] bytes) {
        try {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[] hash = md5.digest(bytes);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String bytesToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString(0xff & b, 16));
        }
        return sb.toString();
    }
}
