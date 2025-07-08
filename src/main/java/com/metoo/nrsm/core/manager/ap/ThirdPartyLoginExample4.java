package com.metoo.nrsm.core.manager.ap;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 11:09
 */
public class ThirdPartyLoginExample4 {

    public static void main(String[] args) throws Exception {

        BasicCookieStore cookieStore = new BasicCookieStore();

        String password = "";

        // 创建 HttpClient 实例
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        // 其他接口的 URL
        String getrandom = "http://192.168.5.205:60650/api_back/getrandom";

        // 创建 GET 请求
        HttpGet getrandomApiRequest = new HttpGet(getrandom);

        // 执行其他接口请求
        HttpResponse getrandomApiResponse = httpClient.execute(getrandomApiRequest);
        // 打印其他接口响应
        System.out.println("Other API Response Status: " + getrandomApiResponse.getStatusLine());
        // 检查登录响应状态码
        if (getrandomApiResponse.getStatusLine().getStatusCode() == 200) {
            // 获取响应体
            HttpEntity entity = getrandomApiResponse.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            password = getPassword(jsonObject.get("random").toString());

        } else {
        }


        // 第三方登录接口 URL
        String loginUrl = "http://192.168.5.205:60650/api_back/sysauth";

        // 构造登录请求
        HttpPost loginRequest = new HttpPost(loginUrl);

        // 设置请求头
        loginRequest.setHeader("Content-Type", "application/json");

        // 构造登录参数，这里假设参数是一个 JSON 格式的字符串
        String jsonParams = "{\"password\": \"" + password + "\"}";

        // 设置请求体
        loginRequest.setEntity(new StringEntity(jsonParams));

        // 执行登录请求
        HttpResponse loginResponse = httpClient.execute(loginRequest);

        // 检查登录响应状态码
        if (loginResponse.getStatusLine().getStatusCode() == 200) {
            // 获取响应体
            HttpEntity entity = loginResponse.getEntity();
            String responseBody = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String token = jsonObject.get("token").toString();
            // 在这里可以处理登录成功后的逻辑
            System.out.println("Login successful.");
            System.out.println("Response body: " + responseBody);


            // 其他接口的 URL
            String apsearch = "http://192.168.5.205:60650/api_back/apsearch?numperpage=10&pagenum=1&reverse=&sortkey=&searchkey=&withstatus=&withversion=&withmodel=&template=&withcustom=&withreg=";

            // 创建 GET 请求
            HttpGet apsearchApiRequest = new HttpGet(apsearch);

            apsearchApiRequest.setHeader("sysauth", token);

            // 执行其他接口请求
            HttpResponse gapsearchApiResponse = httpClient.execute(apsearchApiRequest);
            if (gapsearchApiResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity gapsearchApiResponseEntity = gapsearchApiResponse.getEntity();
                String re = EntityUtils.toString(gapsearchApiResponseEntity);
                JSONObject asd = JSONObject.parseObject(re);

            }

        } else {
            // 登录失败
            System.out.println("Login failed.");
        }

        // 关闭 HttpClient
        httpClient.close();
    }

    // 从响应体中解析出 Cookie 信息的方法
    private static String getPassword(String random) {
        String originalString = random + "admin";

        try {
            // 创建 MessageDigest 实例并指定算法为 MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将字符串转换为字节数组
            byte[] bytes = originalString.getBytes();

            // 更新摘要信息
            md.update(bytes);

            // 计算摘要
            byte[] digest = md.digest();

            // 将摘要转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String encryptedString = hexString.toString();
            System.out.println("Original String: " + originalString);
            System.out.println("Encrypted String (MD5): " + encryptedString);
            return encryptedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
