package com.metoo.nrsm.core.utils.license;

import com.metoo.nrsm.core.utils.Global;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class AesEncryptUtils {

    //可配置到Constant中，并读取配置文件注入,16位,自己定义
//    private static final String KEY = "@NPzwDvPmCJvpYuE";

    //参数分别代表 算法名称/加密模式/数据填充方式
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

//    AES支持三种长度的密钥：128位、192位、256位。

    /**
     * 加密
     *
     * @param content    加密的字符串
     * @param encryptKey key值
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
        byte[] b = cipher.doFinal(content.getBytes("UTF-8"));
        // 采用base64算法进行转码,避免出现中文乱码
        return Base64.encodeBase64String(b);

    }

    /**
     * 解密
     *
     * @param encryptStr 解密的字符串
     * @param decryptKey 解密的key值
     * @return
     * @throws Exception
     */
//    public static String decrypt(String encryptStr, String decryptKey) throws Exception {
//        KeyGenerator kgen = KeyGenerator.getInstance("AES");
//        kgen.init(128);
//        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
//        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
//        // 采用base64算法进行转码,避免出现中文乱码
//        byte[] encryptBytes = Base64.decodeBase64(encryptStr.getBytes("UTF-8"));
//        byte[] decryptBytes = cipher.doFinal(encryptBytes);
//        return new String(decryptBytes);
//    }
    public static String decrypt(String encryptStr, String decryptKey) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            Cipher cipher = null;

            cipher = Cipher.getInstance(ALGORITHMSTR);

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));

            // 采用base64算法进行转码,避免出现中文乱码
            byte[] encryptBytes = Base64.decodeBase64(encryptStr.getBytes("UTF-8"));
            byte[] decryptBytes = cipher.doFinal(encryptBytes);

            return new String(decryptBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String encrypt(String content) throws Exception {
        return encrypt(content, Global.AES_KEY);
    }

    public String decrypt(String encryptStr) {
        return decrypt(encryptStr, Global.AES_KEY);
    }

    public static String encrypt1(String content) throws Exception {
        return encrypt(content, Global.AES_KEY);
    }

    public static String decrypt1(String encryptStr) throws Exception {
        return decrypt(encryptStr, Global.AES_KEY);
    }

//    public static void main(String[] args) throws Exception {
//        String encrypt = encrypt1("4C4C4544-0052-5910-804E-B9C04F464432");
//        String decrypt = decrypt1("4eI6YQ/oWBZJMciu2gAkx4db6vBz1L3TLViuesSoo6kKNecsDZ2LPuSGJRmm8kw1ZiBHGe3gzcg/qKK4E8PO94rZeQFoihzoShvKl3SiY4NAxzaSYa0vnWmOLu2SzartOO+4I8O6VAR+WyUoYa7PBgYSxkaCem8qfpy8Gqyn7iRma2pqwIUGRA+SHGTgC6xE+exB2x4A0KpIZlyHjsNmhgvLnbcsGI/andSiWNJWszSXCa4n6FW7cajy+nkjO8HUqXxFuY9TkZtmOmUrG53D8g==");
//        System.out.println(encrypt);
//        System.out.println(decrypt);
////4eI6YQ/oWBZJMciu2gAkx4db6vBz1L3TLViuesSoo6kKNecsDZ2LPuSGJRmm8kw1ZiBHGe3gzcg/qKK4E8PO94rZeQFoihzoShvKl3SiY4NAxzaSYa0vnWmOLu2SzartOO+4I8O6VAR+WyUoYa7PBgYSxkaCem8qfpy8Gqyn7iRma2pqwIUGRA+SHGTgC6xE+exB2x4A0KpIZlyHjsNmhta8utmgSG+i/CpNcTVXirmXzj1yjpa4/six7w8RAwwGsptBIK1DjRGxqRGrEaPu3USQE/T5t0YS+Vh0rWvy4pPLfNWcWpbQplbaf9g6aHaH
//    }
//

    public static void main(String[] args) throws Exception {
//        Map map=new HashMap<String,String>();
////        map.put("expireTime","1964053510");
//        map.put("systemSN","SYSTEM");
//        LicenseDto dto = new LicenseDto();
//        dto.setStartTime(1704791990000L);
//        dto.setEndTime(1707470390000L);
//        dto.setSystemSN("Y3HaRlRtspphK9W8v/4VUr1l4728jYrkAURR5cGpUTWVwy+8fD5741NkqLnZKEXJ");
////        dto.setType(0);
//        dto.setLicenseVersion("1.0");
//        dto.setType("0");
//        dto.setLicenseFireWall(0);
//        dto.setLicenseRouter(0);
//        dto.setLicenseHost(0);
//        dto.setLicenseUe(0);
//        String content = JSONObject.toJSONString(dto);
//        System.out.println("加密前：" + content);
//
//        String encrypt = encrypt(content, Global.AES_KEY);
//        System.out.println("加密后：" + encrypt);

        String decrypt = decrypt("" +
                        "YtLMTsIwlqMy/88FyrjQsCgQjeulHg7a0XvQ64+9d1748J1Nq9jMhMabyGsS5jfdbh0w+72mCTnCn9xzDYlwIAwS3qnYUYJTgwzhPSJM96oQmY1jEZs7GgbA9W0kzWWO1fRLJvJbPiu72s/63TzJvL2/eKPZjvehJrT9fHXfe6dCqqJtFw4a9cMRxRJFmfdSKzH/Q1oexHy5BMQn9Xf1QBAppQb359fZua4cXxEADpaE2Fuv0RGdWclhBQboX9EZ/WATHuh0gBUG9hR880qCu0WLIXUt86phKkuTupxDkxxwzC8t3Qkiv7n7V6kCVh+o7hj/OIRjbTAy5+ll5KoevfPJAFoK3xKEaqD8Qjn2+wFuNQK1yvVrDa9AlfTv9oS1oaPDC+Qt5RXiKWtUw9u8Rm4+FUVj/chbk+HquNes/XR21DLlJmU3w9wQLWW1vtuTpy6PlNqqWKSnyOv30BVA0A=="
                , Global.AES_KEY);
        System.out.println("解密后：" + decrypt);

    }
}
