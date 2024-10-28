package com.metoo.nrsm.core.manager.ap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 11:09
 */
public class ThirdPartyLoginExample2 {

    public static void main(String[] args) throws IOException {
        // 创建Cookie存储
        CookieStore cookieStore = new BasicCookieStore();

        // 创建HTTP客户端
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {

            // 发送登录请求
            HttpPost loginRequest = new HttpPost("http://192.168.5.205:60650/api/sysauth");

            // 设置请求参数
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("password", "a8fe9196931d3f0601a95cce9200fb08"));
            // 设置登录请求参数
            loginRequest.setEntity(new UrlEncodedFormEntity(params));

            // 执行登录请求
            HttpResponse loginResponse = httpClient.execute(loginRequest);

            HttpEntity entity = loginResponse.getEntity();
            String responseBody2 = EntityUtils.toString(entity);
            System.out.println(responseBody2);

            // 检查登录响应中是否包含Set-Cookie头部
            // 如果有，保存Cookie
            List<Cookie> cookies = cookieStore.getCookies();
            // 使用保存的Cookie调用其他接口
            HttpGet otherRequest = new HttpGet("https://example.com/other");
            HttpResponse otherResponse = httpClient.execute(otherRequest);

            // 处理其他接口的响应
            String responseBody = EntityUtils.toString(otherResponse.getEntity());
            System.out.println(responseBody);
        }
    }
}
