package com.metoo.nrsm.core.utils.license;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.enums.license.FeatureModule;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.vo.LicenseVo;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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


    public static String encrypt(String content) throws Exception {
        return encrypt(content, Global.AES_KEY);
    }

    public static String decrypt(String encryptStr) {
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

    public static void main(String[] args)  {
        String data = ""+
                "Nh5IJp7B6JBo2vG2S6q5gkvCxmRKCDkxiDS7W7RM80igkZsUnJ715siYVX7Dt0J+kUyVGxVEOm/SxdrKVm604T2Dn0OJn6cNYssSD0LbMjrwiGvxEzZeGIBJDvBfdT1pGT14A72fMvQQCQR40FAZIzXve+YJR4xeHwIGb26+GiZlEN62++SJJdWnjFSW1spmn5uING3g80OAtvXA9vG7i9EJhRQopurvUScRhnxkq3hCqqJtFw4a9cMRxRJFmfdSAbaU9PjzVPkIvkfTbDepPBWkX0FQCi9dBFBYgPkfDMcQyO8p+ceZ//3R/xmJX13s25HH4UE2xRKyAL9UxuGt26LCvwNXw8KYkyhQEDIN6eItPDRSY+LBfR9VbsbvwUm/0HzzDPhIxGQGXFmUnMbjc+/IxI1mV/PXJDg4WiDmQ+JurggjerQRS3CWpxKzwtTvSdAySgjGlYk7Jhb5vOLc4UnnRhFSOm2tM3OsSePMMQQWzXUlvRbOjXcBuY+EGIBwAMfHBxLHRTpaRGb9lbi7JPjYR5M8kL4l84r8sU7qhOvnzJpHHNaXGCdAcTdZ0Ym/JZxSg3fbO8grr5+s+H40B1bK/hMINCQuEIyPprU4GKVrt6lANYFKXnmmX+ekyiRvr83lsVbSTT97P7sdVdqg7Q=="
                ;
        String licenseInfo = AesEncryptUtils.decrypt(data);

        System.out.println("解密后：" + licenseInfo);


        LicenseVo licenseVo = JSONObject.parseObject(licenseInfo, LicenseVo.class);
        List<FeatureModule> featureModule = licenseVo.getFeatureModules();
        System.out.println("a" + FeatureModule.ASSET_SCAN.getDesc());
        if (featureModule != null && featureModule.contains(FeatureModule.ASSET_SCAN) ) {
            System.out.println("包含流量分析或高级扫描功能");
        } else {
            System.out.println("不包含目标功能");
        }
    }
}
