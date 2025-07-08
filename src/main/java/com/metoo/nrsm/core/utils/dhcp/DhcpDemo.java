package com.metoo.nrsm.core.utils.dhcp;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.entity.Dhcp;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.repository.init.ResourceReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 11:14
 */
public class DhcpDemo {

//    @ApiModelProperty("租约的开始时间")
//    private String starts;
//    @ApiModelProperty("租约的结束时间")
//    private String ends;
//    private String tstp;
//    private String cltt;
//    private String binding;
//    private String next;
//    private String binding;
//    private String binding;
//    private String binding;
//    private String binding;


    public static void main(String[] args) {


        // 通过ClassLoader读取resources下的文件
        InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd.leases");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//            try {
//                BufferedReader reader = new BufferedReader(new FileReader("file.txt"));
//                String line = reader.readLine();
//                while (line != null) {
//                    System.out.println(line);
//                    line = reader.readLine();
//                }
//                reader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
    }

    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {

        applicationContext = new ClassPathXmlApplicationContext(

                "classpath:application-dev.properties");

    }

    @Test
    public void test() {
        // 通过ClassLoader读取resources下的文件
        InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("./dhcpd/dhcpd.leases");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                Map data = null;
                List dataList = new ArrayList();
                while ((line = reader.readLine()) != null) {
                    if (StringUtil.isNotEmpty(line)) {
                        line = line.trim();
                        String key = this.getKey(line);
                        if (StringUtil.isNotEmpty(key)) {
                            if (key.equals("lease")) {
                                if (data != null) {
                                    dataList.add(data);
                                }
                                data = new HashMap();
                            }
                            parseValue(key, line, data);
                        }

                    }

                }
                System.out.println(dataList);
                Dhcp dhcp = new Dhcp();
                dhcp.setDhcp(JSONObject.toJSONString(dataList));

                IDhcpService dhcpService = (IDhcpService) applicationContext
                        .getBean("IDhcpService");
                dhcpService.save(dhcp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void test1() {
//        String s = "lease 192.168.5.204 {";
//        String str = s.replaceAll("{","\\{");

        String str = "lease 192.168.5.204 {";
        str = escapeExprSpecialWord(str);

        int index1 = MyStringUtils.acquireCharacterPositions(str, " ", 1);

        String str1 = str.substring(0, index1);
        System.out.println(str1);


        int index2 = MyStringUtils.acquireCharacterPositions(str, escapeExprSpecialWord("{"), 1);
        String str2 = str.substring(str1.length(), index2);
        str2 = str2.trim();

        str2.replace("\\", "#");

        System.out.println(str2);

    }

    @Test
    public void test2() {
//        String s = "lease 192.168.5.204 {";
//        String str = s.replaceAll("{","\\{");

        String str = "lease 192.168.5.204 \\{";
//        str = escapeExprSpecialWord(str);

        int index1 = MyStringUtils.acquireCharacterPositions(str, " ", 1);

        String str1 = str.substring(0, index1);
        System.out.println(str1);


        int index2 = MyStringUtils.acquireCharacterPositions(str, "\\{", 1);
//
        String str2 = str.substring(str1.length(), index2 - 1);
        str2 = str2.trim();

        System.out.println(str2);

    }

    @Test
    public void test3() {
//        String s = "lease 192.168.5.204 {";
//        String str = s.replaceAll("{","\\{");

        String str = "starts 1 2024/01/15 03:26:17\\;";
//        str = escapeExprSpecialWord(str);

        int index1 = MyStringUtils.acquireCharacterPositions(str, " ", 2);

        String str1 = str.substring(0, index1);
        System.out.println(str.substring(0, index1 - 1));


        int index2 = MyStringUtils.acquireCharacterPositions(str, "\\;", 1);
//
        String str2 = str.substring(str1.length(), index2 - 1);
        str2 = str2.trim();

        System.out.println(str2);

    }


    @Test
    public void test4() {
//        String s = "lease 192.168.5.204 {";
//        String str = s.replaceAll("{","\\{");

        String str = "hardware ethernet 8e:ab:e9:5c:a7:68\\;";
//        str = escapeExprSpecialWord(str);

        int index1 = MyStringUtils.acquireCharacterPositions(str, " ", 2);

        String str1 = str.substring(0, index1);
        System.out.println(str1);


        int index2 = MyStringUtils.acquireCharacterPositions(str, "\\;", 1);
//
        String str2 = str.substring(str1.length(), index2 - 1);
        str2 = str2.trim();

        System.out.println(str2);

    }


    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword
     * @return
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) { //
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    public static String myEscapeExprSpecialWord(String keyword, String fbs) {
        if (StringUtils.isNotBlank(keyword)) { //
            if (keyword.contains(fbs)) {
                keyword = keyword.replace(fbs, "\\" + fbs);
            }
        }
        return keyword;
    }


    @Test
    public void testParseLineLease() {
        Map data = new HashMap();
        String lineText = "lease 192.168.5.201 {";
        this.parseLineLease(lineText, data);

        System.out.println(data);
        System.out.println(JSONObject.toJSONString(data));

    }


    public void parseLineLease(String lineText, Map data) {

        String text = myEscapeExprSpecialWord(lineText, "{");

        int index1 = MyStringUtils.acquireCharacterPositions(text, " ", 1);

        String text1 = text.substring(0, index1);

        System.out.println(text1);


        int index2 = MyStringUtils.acquireCharacterPositions(text, "\\{", 1);

        String text2 = text.substring(text1.length(), index2 - 1).trim();


        System.out.println(text2);

        data.put(text1, text2);

    }


    @Test
    public void testParseSpecialLineText() {
        Map data = new HashMap();
//        String lineText = "starts 1 2024/01/15 03:33:01;";
//        this.parseSpecialLineText(lineText, data, 2, 1);

        String lineText = "lease 192.168.5.201 {";

        this.parseLineText(lineText, data, 1, 0, "{");

        System.out.println(data);
        System.out.println(JSONObject.toJSONString(data));

    }

    public void parseLineText(String lineText, Map data, int startIndex, int subIndex, String symbol) {

        String text = myEscapeExprSpecialWord(lineText, symbol);

        int index1 = MyStringUtils.acquireCharacterPositions(text, " ", startIndex);

        String text1 = text.substring(0, index1);

        int index2 = MyStringUtils.acquireCharacterPositions(text, "\\" + symbol, 1);

        String text2 = text.substring(text1.length(), index2 - 1);

        data.put(text1.substring(0, index1 - subIndex).trim(), text2.trim());

    }

    public String getKey(String lineText) {

        String[] beginHeads = {"lease", "starts", "ends", "cltt", "binding state", "next binding state", "rewind binding state",
                "hardware ethernet", "uid", "client-hostname"};
        boolean eleFlag = false;
        for (String element : beginHeads) {
            if (lineText.contains(element)) {
                eleFlag = true;
                break;
            }
        }
        if (eleFlag) {
            for (String key : beginHeads) {

                String patten = "^" + key;

                boolean flag = this.parseLineBeginWith(lineText, patten);

                if (flag) {
                    // 保存结果
//                 public void parseLineText(String lineText, Map data, int startIndex, int subIndex, String symbol){
//                if(key.equals("lease")){
//                    this.parseLineText(lineText, );
//                }
                    return key;
                } else {
                    continue;
                }
            }
        }
        return "";
    }

    public void parseValue(String key, String lineText, Map data) {

        switch (key) {
            case "lease":
                this.parseLineText(lineText, data, 1, 0, "{");
                break;
            case "starts":
                this.parseLineText(lineText, data, 2, 1, ";");
                break;
            case "ends":
                this.parseLineText(lineText, data, 2, 1, ";");
                break;
            case "cltt":
                this.parseLineText(lineText, data, 2, 1, ";");
                break;
            case "binding state":
                this.parseLineText(lineText, data, 2, 0, ";");
                break;
            case "next binding state":
                this.parseLineText(lineText, data, 3, 0, ";");
                break;
            case "rewind binding state":
                this.parseLineText(lineText, data, 1, 0, ";");
                break;
            case "hardware ethernet":
                this.parseLineText(lineText, data, 2, 0, ";");
                break;
            case "uid":
                this.parseLineText(lineText, data, 1, 0, ";");
                break;
            case "client-hostname":
                this.parseLineText(lineText, data, 1, 0, ";");
                break;
            default:
                break;
        }
    }

    public boolean parseLineBeginWith(String lineText, String head) {

        if (StringUtil.isNotEmpty(lineText) && StringUtil.isNotEmpty(head)) {
            String patten = "^" + head;

            Pattern compiledPattern = Pattern.compile(patten);

            Matcher matcher = compiledPattern.matcher(lineText);

            while (matcher.find()) {
                return true;
            }
        }
        return false;
    }
}
