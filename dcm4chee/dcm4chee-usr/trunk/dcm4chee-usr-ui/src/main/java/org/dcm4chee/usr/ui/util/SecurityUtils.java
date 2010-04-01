package org.dcm4chee.usr.ui.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.dcm4che2.base64.Base64Encoder;

public class SecurityUtils {

    public static String encodePassword(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md.update(password.getBytes());
        return new String(Base64Encoder.encode(md.digest()));
    }
}
