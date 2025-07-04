package com.metoo.nrsm.core.utils.dhcp;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.core.utils.date.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:33
 */
@Component
public class Dhcp6Utils {


    public static String getKey(String lineText) {

        String[] beginHeads = {"ia-na", "cltt", "iaaddr", "binding state", "preferred-life", "max-life", "ends"};
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

                boolean flag = parseLineBeginWith(lineText, patten);

                if (flag) {
                    // 保存结果
//                 public void parseLineText(String lineText, Map data, int startIndex, int subIndex, String symbol){
//                if(key.equals("lease")){
//                    parseLineText(lineText, );
//                }
                    return key;
                } else {
                    continue;
                }
            }
        }
        return "";
    }

    public static void parseValue(String key, String lineText, Map data) {
        switch (key) {
            case "ia-na":
                parseLineText(lineText, data, 1, 0, "{");
                break;
            case "cltt":
                parseLineText(lineText, data, 2, 1, ";");
                break;
            case "iaaddr":
                parseLineText(lineText, data, 1, 0, "{");
                break;
            case "binding state":
                parseLineText(lineText, data, 2, 0, ";");
                break;
            case "preferred-life":
                parseLineText(lineText, data, 1, 0, ";");
                break;
            case "max-life":
                parseLineText(lineText, data, 1, 0, ";");
                break;
            case "ends":
                parseLineText(lineText, data, 2, 1, ";");
                break;
            default:
                break;
        }
    }

    public static boolean parseLineBeginWith(String lineText, String head) {

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

    public static void parseLineText(String lineText, Map data, int startIndex, int subIndex, String symbol) {

        String text = myEscapeExprSpecialWord(lineText, symbol);

        int index1 = MyStringUtils.acquireCharacterPositions(text, " ", startIndex);

        String text1 = text.substring(0, index1);

        int index2 = MyStringUtils.acquireCharacterPositions(text, "\\" + symbol, 1);

        String text2 = text.substring(text1.length(), index2 - 1);

        String value = TimeUtils.addZone(text2.trim());


        data.put(text1.substring(0, index1 - subIndex).trim(), value == null ? text2.trim() : value);

    }

    public static String myEscapeExprSpecialWord(String keyword, String fbs) {
        if (StringUtils.isNotBlank(keyword)) { //
            if (keyword.contains(fbs)) {
                keyword = keyword.replace(fbs, "\\" + fbs);
            }
        }
        return keyword;
    }
}
