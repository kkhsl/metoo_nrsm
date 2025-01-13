package com.metoo.nrsm.core.manager.ap.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 14:44
 */

public class DefaultGecoosApi implements GecoosApi {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGecoosApi.class);
    private static CloseableHttpClient httpClient;
    private static String token;
    private String BASE_URL;


    public static String getToken() {
        return token;
    }

    public DefaultGecoosApi(String BASE_URL) {
        this.BASE_URL = BASE_URL;
    }


    @Override
    public void init() {
        if (httpClient == null) {
            BasicCookieStore cookieStore = new BasicCookieStore();
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .build();
        }
    }

    @Override
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception var2) {
                logger.error("close httpclient error!", var2);
            }
        }
    }

    @Override
    public String apiVersion() {
        return null;
    }

    @Override
    public JSONObject call(RequestParams requestParams) {

        // 构造登录请求
        HttpPost loginRequest = new HttpPost(requestParams.getUri());

        System.out.println(getToken());

        loginRequest.setHeader("sysauth", getToken());

        // 设置请求头
        loginRequest.setHeader("Content-Type", "application/json");

        // 设置请求体
        loginRequest.setEntity(new StringEntity(JSON.toJSONString(requestParams.getParams()), ContentType.APPLICATION_JSON));

        // 执行登录请求
        try {
            HttpResponse response = httpClient.execute(loginRequest);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                return jsonObject;
            }else{
                throw new IOException("Post request failed. Status code: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("call exception!", e);
        }
    }

    @Override
    public JSONObject getCall(RequestParams requestParams) {
        String url = requestParams.getUri();
        try {
            url = buildUrl(requestParams.getUri(), requestParams.getParams());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpGet getRequest = new HttpGet(url);

        getRequest.setHeader("sysauth", getToken());


        HttpResponse response = null;
        try {
            response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                return jsonObject;
            } else {
                throw new IOException("GET request failed. Status code: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("call exception!", e);
        }
    }

    public static String buildUrl(String baseUrl, Map<String, Object> params) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(baseUrl);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            uriBuilder.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
        }

        return uriBuilder.build().toString();
    }

    @Override
    public boolean login(String password) {
        token = null;
        // 随机数
        RequestParams randomRequest = RequestBuilder.newBuilder().uri(BASE_URL + "getrandom").build();
        JSONObject randomResponse = this.getCall(randomRequest);
        password = getPassword(randomResponse.get("random").toString(), password);

        RequestParams request = RequestBuilder.newBuilder().paramEntry("password", password).uri(BASE_URL + "sysauth").build();
        JSONObject response = this.call(request);
        String token1 = response.getString("token");
        if (token1 != null && !token1.isEmpty()) {
            token = token1;
            return true;
        } else {
            return false;
        }
    }


    // 从响应体中解析出 Cookie 信息的方法
    private static String getPassword(String random, String password) {
        String originalString = random + password;

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
        }return "";
    }
}
