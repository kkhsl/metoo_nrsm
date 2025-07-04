package com.metoo.nrsm.core.utils.net;

import com.github.pagehelper.util.StringUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 18:05
 */
public class Ipv4Utils {


    private static final Pattern IP4_PATTERN = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    private static final Pattern IP4_MATCH_PATTERN = Pattern.compile("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    private static final String IP4_MATCH_CIDR = "^((?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(?:(\\/([1-9]|[1-2]\\d|3[0-2])))?)$";
    private static final String IPV4_CIDR = "^(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]" +
            "|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\/([0-9]|[1-2]\\d|3[0-2])$";


    public static boolean isIp4(String ip4) {
        return IP4_PATTERN.matcher(ip4).matches();
    }

    public static void main(String[] args) {
//        String param = "192.168.5.2/32";
//        Integer mask = Integer.parseInt(param.replaceAll(".*/",""));
//        System.out.println(mask);
//        String param2 = "192.168.5.2/32";
//        String ip = param2.replaceAll("/.*","");
//        System.out.println(ip);
//        String param3 = "192.168.5.2/32";
//        String param4 = param3.replaceAll(".","");
//        System.out.println(param4);
        boolean flag = verifyCidr("192.168.0.0/24");
        System.out.println(flag);
    }

    // 校验IP地址格式-cidr
    public static boolean verifyCidr(String param) {
        if (param == "0.0.0.0/0") {
            return false;
        }
        return Pattern.matches(IPV4_CIDR, param);
    }

    /**
     * 校验Ip
     *
     * @param param
     * @return
     */
    public static boolean verifyIp(String param) {
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        // 判断ip地址是否与正则表达式匹配
        if (!param.matches(regex)) {
            return false;
        }
        return true;
    }

    public static String getMatcherIP4(String ipStr) {
        String ip4 = null;
        Matcher m = IP4_MATCH_PATTERN.matcher(ipStr);
        if (m.find()) {
            ip4 = m.group();
        }
        return ip4;
    }

    /**
     * 掩码 转 掩码位
     *
     * @param mask
     * @return
     */
    public static int getMaskBitByMask(String mask) {
        StringBuffer sbf;
        String str;
        int inetmask = 0, count = 0;
        if (StringUtil.isEmpty(mask)) {
            return inetmask;
        }
        String[] ipList = mask.split("\\.");
        for (int n = 0; n < ipList.length; n++) {
            sbf = toBin(Integer.parseInt(ipList[n]));
            str = sbf.reverse().toString();

            // 统计2进制字符串中1的个数
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf('1', i); // 查找 字符'1'出现的位置
                if (i == -1) {
                    break;
                }
                count++; // 统计字符出现次数
            }
            inetmask += count;
        }
        return inetmask;
    }


    @Test
    public void getMaskByMaskBitTest() {
        String mask = this.getMaskByMaskBit(25);
        System.out.println(mask);
    }

    /**
     * 根据掩码获取掩码位
     *
     * @param maskBit
     * @return
     */
    public static String getMaskByMaskBit(int maskBit) {
        int ip = 0xFFFFFFFF << (32 - maskBit);
        String binaryStr = Integer.toBinaryString(ip);
        StringBuffer buffer = new StringBuffer();
        for (int j = 0; j < 4; j++) {
            int beginIndex = j * 8;
            buffer.append(Integer.parseInt(binaryStr.substring(beginIndex, beginIndex + 8), 2)).append(".");
        }
        return buffer.substring(0, buffer.length() - 1);
    }


    public static StringBuffer toBin(int x) {
        StringBuffer result = new StringBuffer();
        result.append(x % 2);
        x /= 2;
        while (x > 0) {
            result.append(x % 2);
            x /= 2;
        }
        return result;
    }

    /**
     * @param mask
     * @return
     */
    public static int getHostNum(Integer mask) {
        if (mask != null) {
            int number = (int) Math.pow(2, 32 - mask);
            return number;
        }
        return 0;
    }


    /**
     * 获取主机地址
     *
     * @param ip
     * @param mask
     * @return
     */
    public List<String> getHost(String ip, String mask) {
        if (ip != null && mask != null) {
            int bitByMask = getMaskBitByMask(mask);
            String[] ipList = ip.split("\\.");
            String ip_host = "";
            StringBuffer ip_network = new StringBuffer();
            for (int n = 0; n < ipList.length; n++) {
                if (n == 3) {
                    ip_host = ipList[n];
                } else {
                    ip_network.append(ipList[n] + ".");
                }
            }
            String ip_net = ip_network.toString();

            // 获取掩码长度
            // 第一个主机ip为网络地址
            // 最后一个主机ip为广播地址
            // 其他主机ip为主机地址
            int length = getHostNum(bitByMask);
            if (length > 0) {
                double host = Math.ceil(Integer.parseInt(ip_host) / length) * length;
                int doubleValue = new Double(host).intValue();
                String networkAddress = ip_network + String.valueOf(doubleValue);
                doubleValue = new Double(host).intValue() + 1;
                List list = new ArrayList<>();
                int n = 0;
                for (int i = doubleValue; i < length + doubleValue - 2; i++) {
                    list.add(ip_net + (doubleValue + n));
                    n++;
                }
                return list;
            }
        }
        return null;
    }

    @Test
    public void test() {
        System.out.println(getNetworkAddress("192.168.6.2", 24));
    }

    /**
     * 获取网络地址
     *
     * @param ip
     * @param mask
     * @return
     */
    public static String getNetworkAddress(String ip, Integer mask) {
        if (ip != null && mask != null) {
            String[] ipList = ip.split("\\.");
            String ip_host = "";
            StringBuffer ip_network = new StringBuffer();
            for (int n = 0; n < ipList.length; n++) {
                if (n == 3) {
                    ip_host = ipList[n];
                } else {
                    ip_network.append(ipList[n] + ".");
                }
            }
            // 获取掩码长度
            // 第一个主机ip为网络地址
            // 最后一个主机ip为广播地址
            // 其他主机ip为主机地址
            int length = getHostNum(mask);
            if (length > 0) {
                double host = Math.ceil(Integer.parseInt(ip_host) / length) * length;
                int doubleValue = new Double(host).intValue();
                String networkAddress = ip_network + String.valueOf(doubleValue);
                return networkAddress;
            }
        }
        return null;
    }

    // 获取网络地址
    public static Map<String, String> getNetworkIpDec(String address, String netmask) {
        Map<String, String> map = new HashMap<String, String>();
        String network = new String();
        String broadcast = new String();
        String[] addresses = address.split("\\.");
        String[] masks = netmask.split("\\.");
        for (int i = 0; i < 4; i++) {
            int opmasksegement = ~Integer.parseInt(masks[i]) & 0xFF;
            //此处有坑，正常的int有32位，
            // 如果此数没有32位的话，就会用0填充前面的数，
            // 从而导致取反0的部分会用1来填充，
            // 用上述方法可以获取想要的部分
            int netsegment = Integer.parseInt(addresses[i]) & Integer.parseInt(masks[i]);
            network = network + String.valueOf(netsegment) + ".";
            broadcast = broadcast + String.valueOf(opmasksegement | netsegment) + ".";
        }
        network = ipConvertDec(network.substring(0, network.length() - 1));
        broadcast = ipConvertDec(broadcast.substring(0, broadcast.length() - 1));
        map.put("network", network);
        map.put("broadcast", broadcast);
        return map;
    }

    @Test
    public void toNumericTest() {
        String ip = "192.168.5.1";
        Long ipDecimalism = toDecimalism(ip);
        System.out.println(ipDecimalism);
    }

    public static Long toDecimalism(String ip) {
        Scanner sc = new Scanner(ip).useDelimiter("\\.");
        Long l = (sc.nextLong() << 24) + (sc.nextLong() << 16) + (sc.nextLong() << 8)
                + (sc.nextLong());
        sc.close();
        return l;
    }


    // 获取网络地址
    public static Map<String, String> getNetworkIp(String address, String netmask) {
        Map<String, String> map = new HashMap<String, String>();
        String network = new String();
        String broadcast = new String();
        String[] addresses = address.split("\\.");
        String[] masks = netmask.split("\\.");
        for (int i = 0; i < 4; i++) {
            int opmasksegement = ~Integer.parseInt(masks[i]) & 0xFF;
            //此处有坑，正常的int有32位，
            // 如果此数没有32位的话，就会用0填充前面的数，
            // 从而导致取反0的部分会用1来填充，
            // 用上述方法可以获取想要的部分
            int netsegment = Integer.parseInt(addresses[i]) & Integer.parseInt(masks[i]);
            network = network + String.valueOf(netsegment) + ".";
            broadcast = broadcast + String.valueOf(opmasksegement | netsegment) + ".";
        }
        map.put("network", network.substring(0, network.length() - 1));
        map.put("broadcast", broadcast.substring(0, broadcast.length() - 1));
        return map;
    }

    @Test
    public void test2() {
        String ip = "192.168.5.191";
        String mask = "24";
        System.out.println(getNetwork(ip, "255.255.0.0"));
    }

    /**
     * 获取IpV4网络地址
     *
     * @param address Ip地址
     * @param mask    掩码
     * @return 网络地址
     */
    public static String getNetwork(String address, String mask) {
        String network = new String();
        String[] addresses = address.split("\\.");
        String[] masks = mask.split("\\.");
        for (int i = 0; i < 4; i++) {
            int opmasksegement = ~Integer.parseInt(masks[i]) & 0xFF;
            //此处有坑，正常的int有32位，
            // 如果此数没有32位的话，就会用0填充前面的数，
            // 从而导致取反0的部分会用1来填充，
            // 用上述方法可以获取想要的部分
            int netsegment = Integer.parseInt(addresses[i]) & Integer.parseInt(masks[i]);
            network = network + String.valueOf(netsegment) + ".";
        }
        network = network.substring(0, network.length() - 1);
        return network;
    }

    /**
     * 获取IpV4网络地址
     *
     * @param address Ip地址
     * @param mask    掩码
     * @return 广播地址
     */
    public static String getBroadcast(String address, String mask) {
        String broadcast = new String();
        String[] addresses = address.split("\\.");
        String[] masks = mask.split("\\.");
        for (int i = 0; i < 4; i++) {
            int opmasksegement = ~Integer.parseInt(masks[i]) & 0xFF;
            //此处有坑，正常的int有32位，
            // 如果此数没有32位的话，就会用0填充前面的数，
            // 从而导致取反0的部分会用1来填充，
            // 用上述方法可以获取想要的部分
            int netsegment = Integer.parseInt(addresses[i]) & Integer.parseInt(masks[i]);
            broadcast = broadcast + String.valueOf(opmasksegement | netsegment) + ".";
        }
        broadcast = broadcast.substring(0, broadcast.length() - 1);
        return broadcast;
    }


//    public static SortedSet sort(List<JSONObject> list){
//        Comparator<JSONObject> ipComparator = new Comparator<JSONObject>() {
//            @Override
//            public int compare(JSONObject obj1, JSONObject obj2) {
//                String ip1 = obj1.getString("subnet");
//                String ip2 = obj2.getString("subnet");
//                return toNumeric(ip1).compareTo(toNumeric(ip2));
//            }
//        };
//        SortedSet<JSONObject> ips = new TreeSet<JSONObject>(ipComparator);
//        for (JSONObject object : list){
//            ips.add(object);
//        }
//
//        return ips;
//    }

    // ip地址排序
    @Test
    public void ipSortTest() {
        List<String> ips = new ArrayList();
        ips.add("192.168.5.1.5");
        ips.add("192.168.5.1.3");
        ips.add("192.168.5.1.8");
        ips.add("192.168.5.1.4");
        ips.add("192.168.5.1.6");
        SortedSet set = this.ipSort(ips);
        System.out.println(set);
    }

    public SortedSet ipSort(List<String> list) {
        Comparator<String> ipComparator = new Comparator<String>() {
            @Override
            public int compare(String obj1, String obj2) {

                return toDecimalism(obj1).compareTo(toDecimalism(obj2));
            }
        };
        SortedSet<String> ips = new TreeSet<>(ipComparator);
        for (String object : list) {
            ips.add(object);
        }
        return ips;
    }


    /**
     * 子网掩码位数转子网掩码
     *
     * @param x
     * @return
     */
    public static String bitMaskConvertMask(int x) {
        int ip = 0xFFFFFFFF << (32 - x);
        String binaryStr = Integer.toBinaryString(ip);
        StringBuffer buffer = new StringBuffer();
        for (int j = 0; j < 4; j++) {
            int beginIndex = j * 8;
            buffer.append(Integer.parseInt(binaryStr.substring(beginIndex, beginIndex + 8), 2)).append(".");
        }
        return buffer.substring(0, buffer.length() - 1);
    }


    @Test
    public void getMaskBitByMaskTest() {
        System.out.println(("255.255.128.0"));
    }


    @Test
    public void ipIsInNetTest() {
        String ip = "192.162.5.5";
        String subnet = "192.168.5.1/17";
        System.out.println(ipIsInNet(ip, subnet));

    }
    /**
     * 判断ip地址是否在指定网段
     *
     * @param ip
     * @return
     */
    /**
     * @param ip     192.168.5.101
     * @param ipArea 192.168.5.0/24
     * @return
     */
    public static boolean ipIsInNet(String ip, String ipArea) {
//        if ("127.0.0.1".equals(ip)) {
//            return true;
//        }
//
//        if(StringUtils.isBlank(ipArea)){
//            return false;
//        }

        String[] ipArray = ipArea.split(",");
        for (String s : ipArray) {
            if (!s.contains("/")) {
                if (s.equals(ip)) {
                    return true;
                }
                continue;
            }

            String[] ips = ip.split("\\.");
            //ip地址的十进制值
            int ipAddress = (Integer.parseInt(ips[0]) << 24)
                    | (Integer.parseInt(ips[1]) << 16)
                    | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
            //掩码（0-32）
            int type = Integer.parseInt(s.replaceAll(".*/", ""));
            //匹配的位数为32 - type位（16进制的1）
            int mask = 0xFFFFFFFF << (32 - type);
            String cidrIp = s.replaceAll("/.*", "");
            //网段ip十进制
            String[] cidrIps = cidrIp.split("\\.");
            int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24)
                    | (Integer.parseInt(cidrIps[1]) << 16)
                    | (Integer.parseInt(cidrIps[2]) << 8)
                    | Integer.parseInt(cidrIps[3]);

            if ((ipAddress & mask) == (cidrIpAddr & mask)) {
                return true;
            }
            continue;
        }
        return false;
    }


    /**
     * 判断Ip地址范围大小
     *
     * @param ip1
     * @param ip2
     * @return
     */
    public static int compareIP(String ip1, String ip2) {
        BigInteger ipValue1 = ipToBigInteger(ip1);
        BigInteger ipValue2 = ipToBigInteger(ip2);

        if (ipValue1 != null && ipValue2 != null) {
            return ipValue1.compareTo(ipValue2);
        }

        return 0;
    }

    public static BigInteger ipToBigInteger(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            byte[] bytes = inetAddress.getAddress();
            return new BigInteger(1, bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getRealIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null) {
            ip = ip.contains(",") ? ip.split(",")[0] : ip;
        } else {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }


    /**
     * ip地址转十进制
     *
     * @param
     */
    public static String ipConvertDec(String ip) {
        if (ip == null || ip.equals("")) {
            return null;
        }
        if (ip.equals("0.0.0.0")) {

        } else {
            boolean isIp = verifyIp(ip);
            if (!isIp) {
                return null;
            }
        }
        String[] split = ip.split("\\.");
        Long rs = 0L;
        for (int i = 0, j = split.length - 1; i < split.length; j--, i++) {
            Long intIp = Long.parseLong(split[i]) << 8 * j;
            rs = rs | intIp;
        }
        return rs.toString();
    }

    /**
     * 十进制转ip
     */
    public static String decConvertIp(Long rs) {
//        Long rs=3232235777L;
        String[] ipString = new String[4];
        for (int i = 0, j = 3; i < 4; j--, i++) {
            // 每 8 位为一段，这里取当前要处理的最高位的位置
            int pos = i * 8;
            // 取当前处理的 ip 段的值   rs=3232235777
            long and = rs & (255 << pos);
            // 将当前 ip 段转换为 0 ~ 255 的数字，注意这里必须使用无符号右移
            ipString[j] = String.valueOf(and >>> pos);
        }
        String join = String.join(".", ipString);
        return join;
    }

    // 获取子网列表
    public static String[] getSubnetList(String ip, int mask) {
        if (!StringUtil.isEmpty(ip) && mask >= 1 && mask <= 32) {
            SubnetUtils utils = new SubnetUtils(ip + "/" + mask);
            String[] allIps = utils.getInfo().getAllAddresses();
            return allIps;
        }
        return null;
    }

}
