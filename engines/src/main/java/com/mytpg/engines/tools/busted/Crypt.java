package com.mytpg.engines.tools.busted;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stalker-mac on 27.10.16.
 */

public class Crypt {
    public static String md5(String argText)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(argText.getBytes());
            byte[] messageDistest = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (byte b : messageDistest)
            {
                String h = Integer.toHexString(b & 255);
                while (h.length() < 2)
                {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException nsae)
        {
            nsae.printStackTrace();
            return "";
        }
    }

    public static String genUrl(String argAndroidId, String argPlatforme, String argVersion)
    {
        String id = md5(argAndroidId);
        String salt = generateSalt(30);
        String text = new StringBuilder(String.valueOf(id)).append("-").append(argPlatforme).append("-").append(argVersion).toString();
        return new StringBuilder(String.valueOf(text)).append(".").append(salt).append(".").append(calculateKey(text,salt)).toString();
    }

    public static String generateSalt(int argLength)
    {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String pass = "";
        for (int x = 0; x < argLength; x++)
        {
            pass = new StringBuilder(String.valueOf(pass)).append(chars.charAt((int) Math.floor(Math.random() * (double)(chars.length() - 1)))).toString();
        }
        System.out.println(pass);
        return pass;
    }

    public static String calculateKey(String argText, String argSalt)
    {
        return md5(new StringBuilder(String.valueOf(md5(")o)c===3 <3" + md5(argText)))).append(argSalt).toString());
    }
}




