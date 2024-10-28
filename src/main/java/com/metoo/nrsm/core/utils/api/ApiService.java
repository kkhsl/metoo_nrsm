package com.metoo.nrsm.core.utils.api;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-29 21:11
 */
@Slf4j
@Service
public class ApiService {

    private final RestTemplate restTemplate;


    public static void main(String[] args) {
//
//        String randomStr = "aaa";
//
//        try {
//            System.out.println(sha1(randomStr));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//
        ApiService apiService = new ApiService(new RestTemplate());
        apiService.sendDataToMTO(JSON.toJSONString("test"));
    }

    @Autowired
    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendDataToMTO(String data) {
        RestTemplate restTemplate = new RestTemplate();

//        String url = "http://127.0.0.1:8930/api/nrsm/traffic/data/{traffic}";
//
        String url = "http://175.6.37.154:10000/api/nrsm/traffic/data/{traffic}";

        String response = restTemplate.getForObject(url, String.class, data);

        log.info("Trffix api 175 ============================== " + response);

        System.out.println(response);
    }

    public String callThirdPartyApi(String apiUrl, JsonRequest request) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();

        String nonce = "asdf";
        String timestamp = String.valueOf(System.currentTimeMillis());
        headers.set("nonce", nonce);
        headers.set("timestamp", timestamp);
        try {
            String signature = sha1(timestamp + nonce);
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        headers.setContentType(MediaType.APPLICATION_JSON);

        // 设置请求体参数
        HttpEntity<JsonRequest> requestBody = new HttpEntity<>(request, headers);

        // 发起POST请求，并获取响应
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestBody,
                String.class
        );
        // 返回响应内容
        return response.getBody();
    }

    public String callThirdPartyApiT(String apiUrl, JindustryUnitRequest jindustryUnitRequest) {

        log.info("====================== http://59.52.34.196:6001/apisix/blade-ipv6/industryUnit");

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();


        headers.set("nonce", jindustryUnitRequest.getNonce());
        headers.set("timestamp", jindustryUnitRequest.getTimestamp());
        try {
            String signature = sha1(jindustryUnitRequest.getTimestamp() + jindustryUnitRequest.getNonce());
            headers.set("signature", signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("charset", "UTF-8"); // 设置字符集编码为 UTF-8


        // 设置请求体参数 jindustryUnitRequest-进行国安加密
        try {
            String param = EncrypUtils.encrypt(jindustryUnitRequest.getData());
            HttpEntity<String> requestBody = new HttpEntity<>(param, headers);

            // 发起POST请求，并获取响应
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestBody,
                    String.class
            );


            log.info("api Body=================================" + response.getBody());

            // 返回响应内容
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("======================== error" + e.getMessage());
        }
        return "";

    }


    public static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(input.getBytes());
        String str = encodeHex(digest);
//        return Hex.toHexString(digest);
        return str;

//        StringBuilder sb = new StringBuilder();
//        for (byte b : digest) {
//            sb.append(String.format("%02x", b));
//        }
//        return sb.toString();
    }

    private static final char[] HEX_CODE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encodeHex(byte[] bytes) {
        StringBuilder r = new StringBuilder(bytes.length * 2);
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            r.append(HEX_CODE[b >> 4 & 15]);
            r.append(HEX_CODE[b & 15]);
        }

        return r.toString();
    }
}
