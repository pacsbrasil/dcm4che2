package org.dcm4chee.usr.ui.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    public static String encodePassword(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
            md.update(password.getBytes());
            return new String(md.digest(), "x-base64");
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
