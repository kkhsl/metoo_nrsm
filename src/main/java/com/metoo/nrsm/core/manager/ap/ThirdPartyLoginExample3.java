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
public class ThirdPartyLoginExample3 {

    public static void main(String[] args) throws Exception {
        // 创建 HttpClient 实例
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(new BasicCookieStore()) // 设置默认的 Cookie 存储
                .build();

        // 第三方登录接口 URL
        String loginUrl = "http://192.168.5.205:60650/api/sysauth";

        // 准备登录表单数据
        List<NameValuePair> loginParams = new ArrayList<>();
        loginParams.add(new BasicNameValuePair("password", "589b83d41b5cb2c6b8ddf947449e13d9"));

        // 创建 POST 请求
        HttpPost loginRequest = new HttpPost(loginUrl);
        loginRequest.setEntity(new UrlEncodedFormEntity(loginParams));

        // 执行登录请求
        HttpResponse loginResponse = httpClient.execute(loginRequest);

        // 打印登录响应
        System.out.println("Login Response Status: " + loginResponse.getStatusLine());

        // 检查登录是否成功
        if (loginResponse.getStatusLine().getStatusCode() == 200) {
            // 登录成功，保存 Cookie
            // 之后的请求会自动带上这个 Cookie
            System.out.println("Login successful. Saving cookie...");

            // 获取响应体
            HttpEntity entity = loginResponse.getEntity();
            String responseBody = EntityUtils.toString(entity);
            System.out.println(responseBody);

            // Ac控制器

            // 创建 GET 请求
            HttpGet otherApiRequest = new HttpGet("http://192.168.5.205:60650/api/apsearch?numperpage=10&pagenum=1&reverse=&sortkey=&searchkey=&withstatus=&withversion=&withmodel=&template=&withcustom=&withreg=");
            // 执行其他接口请求
            HttpResponse otherApiResponse = httpClient.execute(otherApiRequest);

            // 打印其他接口响应
            System.out.println("Other API Response Status: " + otherApiResponse.getStatusLine());

            // 检查响应状态码
            if (otherApiResponse.getStatusLine().getStatusCode() == 200) {
                // 获取响应实体
                String otherApiResponseContent = EntityUtils.toString(otherApiResponse.getEntity());
                System.out.println("Other API Response Content: " + otherApiResponseContent);
            } else {
                System.out.println("Failed to call other API.");
            }

        } else {
            // 登录失败
            System.out.println("Login failed.");
        }

        // 关闭 HttpClient
        httpClient.close();
    }
}
