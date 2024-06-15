package com.metoo.nrsm.core.utils.license;

import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-11 11:06
 */
@Component
public class Aes {

    private static final String PWD = "ASDFGHJKL";

    /**
     * 加密
     *
     * @param content
     *            需要加密的内容
     * @return
     */
    public static byte[] encrypt2(String content) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(PWD.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content
     *            待解密内容
     * @return
     */
    public static byte[] decrypt2(byte[] content) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(PWD.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }else {
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }

    }

    /**
     * 加密
     *
     * @param content
     *            需要加密的内容
     * @param password
     *            加密密码
     * @return
     */
    public static byte[] encrypt2(String content, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String encrypt(String content) throws Exception {

        byte[] encode = encrypt2(content);

        String code = parseByte2HexStr(encode);

        return code;
    }
    public String decrypt(String encryptStr) throws Exception {

        byte[] decode = parseHexStr2Byte(encryptStr);

        byte[] encode = decrypt2(decode);

        String code = new String(encode, "UTF-8");

        return code;
    }

    public static String encrypt3(String content) throws Exception {

        byte[] encode = encrypt2(content);

        String code = parseByte2HexStr(encode);

        return code;
    }
    public static String decrypt3(String encryptStr) throws Exception {

        byte[] decode = parseHexStr2Byte(encryptStr);

        byte[] encode = decrypt2(decode);

        String code = new String(encode, "UTF-8");

        return code;
    }


    public static void main(String[] args) throws Exception {
        String content = "我是shoneworn";
        String password = "12345678";
        // 加密
        System.out.println("加密前：" + content);
        byte[] encode = encrypt2(content);

        //传输过程,不转成16进制的字符串，就等着程序崩溃掉吧
        String code = parseByte2HexStr(encode);
        System.out.println("密文字符串：" + code);

        byte[] decode = parseHexStr2Byte("BBAF36E6D8313619A3D075E719AEBEA92A989D0A166E225B66CFD4995E0277E12EE6E860D17DE4B2E533F3E40E615EE9C41F5C9D3C784DD8CFF109302D1835567E11F02704D7574EC369DC59EB1CEDB44AD5DA3A457DC78E5809AD715DF8F6A80E329807432CE5DCBA248DDA18651757B55492523ADF415512C0098186DF6A7F236A5E302498EBE6E314678049C5CBE1BBEE5B60776007FC371C43DD13BAE6AD3D572DCFC9AADE5FEB586585EB74751C737F25F6ED118A41BE3B5AE949D9F6BD30FB4EFAA633FEA800EC817E2DF65738EA4ECDBEB540D8033FFB26ED31D5167DB88C10C500AF99FE93209BD12262D45F");
        // 解密
        byte[] decryptResult = decrypt2(decode);


        String code3 = decrypt3("BBAF36E6D8313619A3D075E719AEBEA92A989D0A166E225B66CFD4995E0277E12EE6E860D17DE4B2E533F3E40E615EE9C41F5C9D3C784DD8CFF109302D1835567E11F02704D7574EC369DC59EB1CEDB44AD5DA3A457DC78E5809AD715DF8F6A80E329807432CE5DCBA248DDA18651757B55492523ADF415512C0098186DF6A7F236A5E302498EBE6E314678049C5CBE1BBEE5B60776007FC371C43DD13BAE6AD3D572DCFC9AADE5FEB586585EB74751C737F25F6ED118A41BE3B5AE949D9F6BD30FB4EFAA633FEA800EC817E2DF65738EA4ECDBEB540D8033FFB26ED31D5167DB88C10C500AF99FE93209BD12262D45F");
        System.out.println("解密后：" + code3); //不转码会乱码

    }

}
