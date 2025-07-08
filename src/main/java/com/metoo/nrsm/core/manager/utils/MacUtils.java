package com.metoo.nrsm.core.manager.utils;

import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.IMacVendorService;
import com.metoo.nrsm.core.utils.string.MyStringUtils;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.MacVendor;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MacUtils {

    @Autowired
    private IMacVendorService macVendorService;
    @Autowired
    private IDeviceTypeService deviceTypeService;

    private static final String MAC_ADDRESS_PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";

    public static boolean isValidMacAddress(String macAddress) {
        // 正则表达式匹配MAC地址格式
        return macAddress.matches(MAC_ADDRESS_PATTERN);
    }

    public static String getMac(String mac) {
        if (isValidMacAddress(mac)) {
            // 方法二：使用正则表达式匹配并提取前三段
            String[] sections = mac.split(":|-");
            String firstThreeSectionsRegex = sections[0] + ":" + sections[1] + ":" + sections[2];
            return firstThreeSectionsRegex;
        }
        return "";
    }


    public List<Mac> macJoint(List<Mac> macs) {
        if (macs != null && macs.size() > 0) {
            for (Mac mac : macs) {
                if (mac.getMac() != null && !mac.getMac().equals("")) {
                    String macAddr = mac.getMac();
                    int index = MyStringUtils.acquireCharacterPositions(macAddr, ":", 3);
                    if (index != -1) {
                        macAddr = macAddr.substring(0, index);
                        Map params = new HashMap();
                        params.clear();
                        params.put("mac", macAddr);
                        List<MacVendor> macVendors = this.macVendorService.selectObjByMap(params);
                        if (macVendors.size() > 0) {
                            MacVendor macVendor = macVendors.get(0);
                            mac.setVendor(macVendor.getVendor());
                        }
                    }
                }
            }
        }
        return macs;
    }

    public void macVendor(Mac mac) {
        if (mac != null) {
            if (mac.getMac() != null && !mac.getMac().equals("")) {
                String macAddr = mac.getMac();
                int index = MyStringUtils.acquireCharacterPositions(macAddr, ":", 3);
                if (index != -1) {
                    macAddr = macAddr.substring(0, index);
                    Map params = new HashMap();
                    params.clear();
                    params.put("mac", macAddr);
                    List<MacVendor> macVendors = this.macVendorService.selectObjByMap(params);
                    if (macVendors.size() > 0) {
                        MacVendor macVendor = macVendors.get(0);
                        mac.setVendor(macVendor.getVendor());
                    }
                }
            }
        }
    }

    public List<Terminal> terminalJoint(List<Terminal> terminals) {
        if (terminals != null && terminals.size() > 0) {
            for (Terminal terminal : terminals) {
                if (terminal.getMac() != null && !terminal.getMac().equals("")) {
                    String macAddr = terminal.getMac();
                    int index = MyStringUtils.acquireCharacterPositions(macAddr, ":", 3);
                    if (index != -1) {
                        macAddr = macAddr.substring(0, index);
                        Map params = new HashMap();
                        params.clear();
                        params.put("mac", macAddr);
                        List<MacVendor> macVendors = this.macVendorService.selectObjByMap(params);
                        if (macVendors.size() > 0) {
                            MacVendor macVendor = macVendors.get(0);
                            terminal.setMacVendor(macVendor.getVendor());
                        }
                    }
                }
            }
        }
        return terminals;
    }

    public void terminalSetMacVendor(Terminal terminal) {
        if (terminal != null) {
            if (terminal.getMac() != null && !terminal.getMac().equals("")) {
                String macAddr = terminal.getMac();
                int index = MyStringUtils.acquireCharacterPositions(macAddr, ":", 3);
                if (index != -1) {
                    macAddr = macAddr.substring(0, index);
                    Map params = new HashMap();
                    params.clear();
                    params.put("mac", macAddr);
                    List<MacVendor> macVendors = this.macVendorService.selectObjByMap(params);
                    if (macVendors.size() > 0) {
                        MacVendor macVendor = macVendors.get(0);
                        terminal.setMacVendor(macVendor.getVendor());
                    }
                }
            }
        }
    }

    public void terminalAssetSetMacVendor(TerminalAsset terminal) {
        if (terminal != null) {
            if (terminal.getMac() != null && !terminal.getMac().equals("")) {
                String macAddr = terminal.getMac();
                int index = MyStringUtils.acquireCharacterPositions(macAddr, ":", 3);
                if (index != -1) {
                    macAddr = macAddr.substring(0, index);
                    Map params = new HashMap();
                    params.clear();
                    params.put("mac", macAddr);
                    List<MacVendor> macVendors = this.macVendorService.selectObjByMap(params);
                    if (macVendors.size() > 0) {
                        MacVendor macVendor = macVendors.get(0);
                        terminal.setMacVendor(macVendor.getVendor());
                    }
                }
            }
        }
    }

//    public void writerType(List<Mac> macs){
//        if(macs.size() > 0){
//            Map params = new HashMap();
//            macs.stream().forEach(e -> {
//                params.clear();
//                params.put("mac", e.getMac());
//                List<Terminal> terminals = this.terminalService.selectObjByMap(params);
//                if(terminals.size() > 0){
//                    Terminal terminal = terminals.get(0);
//                    if(terminal.getOnline()){
//                        e.setOnline(true);
//                    }
////                    if(terminal.getTerminalTypeId() != null){
////                        TerminalType terminalType = this.terminalTypeService.selectObjById(terminal.getTerminalTypeId());
////                        e.setTerminalTypeName(terminalType.getName());
////                    }
//                    if(terminal.getDeviceTypeId() != null){
//                        DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
//                        e.setDeviceTypeName(deviceType.getName());
//                    }
//                }
//            });
//        }
//    }

    public static void main(String[] args) {
        String macAddr = "50:0:0:26:0:2";
        String mac = supplement(macAddr);
        System.out.println(mac);
//        String[] strs = macAddr.split(":");
//        StringBuffer stringBuffer = new StringBuffer();
//        int i = 1;
//        for(String str : strs){
//            if(str.length() == 1){
//                stringBuffer.append(0).append(str);
//            }else{
//                stringBuffer.append(str);
//            }
//            if(i < strs.length){
//                stringBuffer.append(":");
//            }
//            i++;
//        }
//        System.out.println(stringBuffer.toString());
    }

    public List<Mac> supplements(List<Mac> macs) {
        if (macs != null && macs.size() > 0) {
            for (Mac mac : macs) {
                if (mac.getMac() != null && !mac.getMac().equals("")) {
                    String macAddr = mac.getMac();
                    int one_index = macAddr.indexOf(":");
                    if (one_index != -1) {
                        String[] strs = macAddr.split(":");
                        StringBuffer stringBuffer = new StringBuffer();
                        int i = 1;
                        for (String str : strs) {
                            if (str.length() == 1) {
                                stringBuffer.append(0).append(str);
                            } else {
                                stringBuffer.append(str);
                            }
                            if (i < str.length()) {
                                stringBuffer.append(":");
                            }
                            i++;
                        }
                    }
                }
            }
        }
        return macs;
    }

    public static String supplement(String macAddr) {
        int one_index = macAddr.indexOf(":");
        if (one_index != -1) {
            String[] strs = macAddr.split(":");
            StringBuffer stringBuffer = new StringBuffer();
            int i = 1;
            for (String str : strs) {
                if (str.length() == 1) {
                    stringBuffer.append(0).append(str);
                } else {
                    stringBuffer.append(str);
                }
                if (i < strs.length) {
                    stringBuffer.append(":");
                }
                i++;
            }
            macAddr = stringBuffer.toString();
        }
        return macAddr;
    }
}
