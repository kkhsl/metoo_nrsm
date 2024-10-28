package com.metoo.nrsm.core.manager.ap;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 11:01
 */
public class ThirdPartyLoginExample {

//    public static void main(String[] args) throws Exception {
//        // 调用第三方登录接口
//        String loginUrl = "https://example.com/login";
//        HttpClient client = new Httpcli.newHttpClient();
//        HttpRequest loginRequest = HttpRequest.newBuilder()
//                .uri(URI.create(loginUrl))
//                .build();
//        HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
//
//        // 保存登录状态（保存Cookie）
//        Map<String, List<String>> headers = loginResponse.headers().map();
//        List<String> setCookieHeaders = headers.get("Set-Cookie");
//        String cookie = setCookieHeaders.get(0); // 假设第一个Set-Cookie头部包含了我们需要的Cookie
//        HttpCookie httpCookie = HttpCookie.parse(cookie).get(0);
//
//        // 调用其他接口，使用保存的Cookie
//        String otherApiUrl = "https://example.com/other-api";
//        HttpRequest otherApiRequest = HttpRequest.newBuilder()
//                .uri(URI.create(otherApiUrl))
//                .header("Cookie", httpCookie.toString()) // 将保存的Cookie添加到请求头中
//                .build();
//        HttpResponse<String> otherApiResponse = client.send(otherApiRequest, HttpResponse.BodyHandlers.ofString());
//        System.out.println(otherApiResponse.body());
//    }
}
