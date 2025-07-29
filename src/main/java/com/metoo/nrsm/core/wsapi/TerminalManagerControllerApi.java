package com.metoo.nrsm.core.wsapi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.manager.utils.TerminalUtils;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-11 11:10
 */
@Slf4j
@RestController
@RequestMapping("/ws/api/terminal")
public class TerminalManagerControllerApi {

    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;
    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private MacUtils macUtils;
    @Autowired
    private ITerminalCountService terminalCountService;
    @Autowired
    private ITerminalUnitService terminalUnitService;
    @Autowired
    private ITerminalMacIpv6Service terminalMacIpv6Service;
    @Autowired
    private TerminalUtils terminalUtils;

    @ApiOperation("设备 Mac (DT))")
    @GetMapping(value = {"/dt"})
    public NoticeWebsocketResp getObjMac(@RequestParam(value = "requestParams", required = false) String requestParams) {
        NoticeWebsocketResp rep = new NoticeWebsocketResp();

        // 判断requestParams是否为空
        if (StringUtils.isNotEmpty(requestParams)) {
            Map<String, Object> map = JSONObject.parseObject(requestParams, Map.class);
            String sessionId = (String) map.get("sessionId");
            List<String> uuidList = JSONObject.parseObject(String.valueOf(map.get("params")), List.class);
            Map<String, List<Terminal>> result = new HashMap<>();

            String time = (String) map.get("time");
            boolean isHistoricalData = StringUtils.isNotEmpty(time);

            // 遍历UUID列表，处理每一个UUID
            for (String uuid : uuidList) {
                List<Terminal> terminalList = fetchTerminalData(uuid, time, isHistoricalData);
                result.put(uuid, terminalList);
            }

            rep.setNoticeType("101");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(result);

            // 同步到Redis
            this.redisResponseUtils.syncStrRedis(sessionId, JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue), 101);
            return rep;
        }

        rep.setNoticeType("101");
        rep.setNoticeStatus(0);
        return rep;
    }

    // 提取的辅助方法，获取终端数据
    private List<Terminal> fetchTerminalData(String uuid, String time, boolean isHistoricalData) {
        Map<String, Object> params = new HashMap<>();
        List<Terminal> terminalList = new ArrayList<>();
        params.put("deviceUuid", uuid);
        List<Terminal> terminals;
        if (isHistoricalData) {
            params.put("time", time);
            terminals = terminalService.selectPartitionTerminalHistory(params);
        } else {
            terminals = terminalService.selectPartitionTerminal(params);
        }

        // 如果有终端数据，加入到列表中
        if (terminals != null && !terminals.isEmpty()) {
            terminalList.addAll(terminals);
        }

        // 查询NSwitch数据
        params.clear();
        params.put("deviceUuid", uuid);
        List<Terminal> nswitchList = new ArrayList<>();
        if (isHistoricalData) {
            params.put("time", time);
            nswitchList = terminalService.selectHistoryNSwitchToTopology(params);
        } else {
            nswitchList = terminalService.selectNSwitchToTopology(params);
        }
        if (nswitchList != null && !nswitchList.isEmpty()) {
            for (Terminal obj : nswitchList) {
                for (Terminal terminal : obj.getTerminalList()) {
                    terminal.setDeviceUuid(obj.getDeviceUuid2());// 恢复实际设备Uuid
                    terminalUtils.completeTerminal(terminal);
                }
            }
            terminalList.addAll(nswitchList);
        }

        // 对所有终端进行补充操作
        for (Terminal terminal : terminalList) {
            terminalUtils.completeTerminal(terminal);
        }

        return terminalList;
    }


    /*
    {"noticeType":"102","userId":"1","time":"","params":{"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa":["6e:2e:10:93:cb:18"],"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa2":["00:50:79:66:68:56","00:50:79:66:68:57"],"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa3":[],"dtGroupDragAddHNFwsjWNtxdRSkiKX7bFFhJaEQHKWa4":[]}}
     */
    @ApiOperation("102：终端")
    @GetMapping("/partition")
    public NoticeWebsocketResp partitionTerminal(@RequestParam(value = "requestParams", required = false) String requestParams) {
        Map<String, Object> requestParam = JSONObject.parseObject(requestParams, Map.class);
        String sessionId = (String) requestParam.get("sessionId");
        Map<String, JSONArray> param = JSONObject.parseObject(String.valueOf(requestParam.get("params")), Map.class);
        Map result = new HashMap();
        if (!param.isEmpty()) {
            Map params = new HashMap();
            if (param.get("time") == null || StringUtil.isEmpty(String.valueOf(param.get("time")))) {
                Set<Map.Entry<String, JSONArray>> keys = param.entrySet();
                for (Map.Entry<String, JSONArray> entry : keys) {
                    if (entry.getValue() != null && entry.getValue().size() > 0) {
                        List list = new ArrayList<>();
                        entry.getValue().stream().forEach(m -> {
                            params.clear();
                            params.put("mac", m);
                            List<Terminal> terminals = this.terminalService.selectCustomPartitionByMap(params);
                            if (terminals.size() > 0) {
                                Terminal terminal = terminals.get(0);
                                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                                if (deviceType != null) {
                                    terminal.setDeviceTypeName(deviceType.getName());
                                    terminal.setDeviceTypeUuid(deviceType.getUuid());
                                }
                                if (StringUtil.isNotEmpty(terminal.getMac())) {
                                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
                                    if (terminalMacIpv6 != null && terminalMacIpv6.getIsIPv6() == 1) {
                                        terminal.setIsIpv6(1);
                                    } else {
                                        terminal.setIsIpv6(0);
                                    }
                                }
                                list.add(terminal);
                            }
                        });
                        result.put(entry.getKey(), list);
                    }
                }
            } else {
                Set<Map.Entry<String, JSONArray>> keys = param.entrySet();
                for (Map.Entry<String, JSONArray> entry : keys) {
                    if (entry.getValue() != null && entry.getValue().size() > 0) {
                        List list = new ArrayList<>();
                        entry.getValue().stream().forEach(m -> {
                            params.clear();
                            params.put("mac", m);
                            params.put("time", param.get("time"));
                            List<Terminal> terminals = this.terminalService.selectCustomPartitionHistoryByMap(params);
                            if (terminals.size() > 0) {
                                Terminal terminal = terminals.get(0);
                                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                                if (deviceType != null) {
                                    terminal.setDeviceTypeName(deviceType.getName());
                                    terminal.setDeviceTypeUuid(deviceType.getUuid());
                                }

                                if (StringUtil.isNotEmpty(terminal.getMac())) {
                                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
                                    if (terminalMacIpv6 != null && terminalMacIpv6.getIsIPv6() == 1) {
                                        terminal.setIsIpv6(1);
                                    } else {
                                        terminal.setIsIpv6(0);
                                    }
                                }
                                list.add(terminal);
                            }
                        });
                        result.put(entry.getKey(), list);
                    }
                }
            }

            NoticeWebsocketResp rep = new NoticeWebsocketResp();
            rep.setNoticeType("102");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(result);
            this.redisResponseUtils.syncStrRedis(sessionId, JSON.toJSONString(result, SerializerFeature.WriteMapNullValue), 102);
            return rep;
        }
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        rep.setNoticeType("102");
        rep.setNoticeStatus(0);
        return rep;
    }

    @ApiOperation("103：终端")
    @GetMapping("/count")
    public NoticeWebsocketResp count(@RequestParam(value = "requestParams", required = false)
                                             String requestParams) {
        Map<String, Object> requestParam = JSONObject.parseObject(requestParams, Map.class);
        String sessionId = (String) requestParam.get("sessionId");
        Map params = new HashMap();
        TerminalCount terminalCount = null;
        if (requestParam.get("time") == null || StringUtil.isEmpty(String.valueOf(requestParam.get("time")))) {
            terminalCount = this.terminalCountService.selectObjByMap(Collections.emptyMap());
        } else {
            params.clear();
            params.put("time", requestParam.get("time"));
            terminalCount = this.terminalCountService.selectHistoryObjByMap(params);
        }
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        rep.setNoticeType("103");
        rep.setNoticeStatus(1);
        rep.setNoticeInfo(terminalCount);
        this.redisResponseUtils.syncStrRedis(sessionId, JSON.toJSONString(terminalCount), 103);
        return rep;
    }

    @ApiOperation("104：单位分区终端")
    @GetMapping("/partition/unit/terminal")
    public NoticeWebsocketResp unitPartitionTerminal(@RequestParam(value = "requestParams", required = false) String requestParams) {
        Map<String, Object> requestParam = JSONObject.parseObject(requestParams, Map.class);
        String sessionId = (String) requestParam.get("sessionId");
        Map map = JSONObject.parseObject(String.valueOf(requestParams), Map.class);
        List<String> list = JSONObject.parseObject(String.valueOf(map.get("params")), List.class);
        Map result = new HashMap();
        if (!map.isEmpty()) {
            Map params = new HashMap();
            List<TerminalUnit> terminalUnitList = new ArrayList<>();
            if (map.get("time") == null || StringUtil.isEmpty(String.valueOf(map.get("time")))) {
                if (list.size() > 0) {
                    // 根据单位查询终端
                    params.clear();
                    params.put("ids", list);
                    terminalUnitList = terminalUnitService.selectObjAndTerminalByMap(params);
                    for (TerminalUnit terminalUnit : terminalUnitList) {
                        int v6_number = 0;
                        int number = 0;
                        if (terminalUnit.getTerminalList().size() > 0) {

                            for (Terminal terminal : terminalUnit.getTerminalList()) {


                                terminalUtils.completeTerminal(terminal);

                                if (terminal.getDeviceTypeId() != null && terminal.getDeviceTypeId() != 24 && terminal.getDeviceTypeId() != 10) {
                                    number += 1;
                                }
                                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                                if (deviceType != null) {
                                    terminal.setDeviceTypeName(deviceType.getName());
                                    terminal.setDeviceTypeUuid(deviceType.getUuid());
                                }

                                if (StringUtil.isNotEmpty(terminal.getMac())) {
                                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
                                    if (terminalMacIpv6 != null && terminalMacIpv6.getIsIPv6() == 1) {
                                        terminal.setIsIpv6(1);
                                        if (terminal.getDeviceTypeId() != null && terminal.getDeviceTypeId() != 24 && terminal.getDeviceTypeId() != 10) {
                                            v6_number += 1;
                                        }
                                    } else {
                                        terminal.setIsIpv6(0);
                                    }
                                }
                            }
                        }

                        BigDecimal percentage = new BigDecimal("0");
                        if (number == 0 || v6_number == 0) {
                        } else {
                            BigDecimal v6 = new BigDecimal(v6_number);
                            BigDecimal num = new BigDecimal(number);
                            // 计算百分比并保留两位小数
                            percentage = v6.divide(num, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                            // 去掉尾随的 .00
                            percentage = percentage.stripTrailingZeros();
                            System.out.println("v6_number 占 number 的百分比: " + percentage.setScale(2, RoundingMode.HALF_UP) + "%");
                        }
                        terminalUnit.setPercentage(percentage);

                    }
                }

            } else {
                if (list.size() > 0) {
                    // 根据单位查询终端
                    params.clear();
                    params.put("time", map.get("time"));
                    params.put("ids", list);
                    terminalUnitList = terminalUnitService.selectObjAndTerminalByMap(params);
                    for (TerminalUnit terminalUnit : terminalUnitList) {

                        int v6_number = 0;
                        int number = terminalUnit.getTerminalList().size();
                        if (terminalUnit.getTerminalList().size() > 0) {
                            // 统计v6终端，后续v6状态改写终端表
                            for (Terminal terminal : terminalUnit.getTerminalList()) {

                                terminalUtils.completeTerminal(terminal);

                                DeviceType deviceType = this.deviceTypeService.selectObjById(terminal.getDeviceTypeId());
                                if (deviceType != null) {
                                    terminal.setDeviceTypeName(deviceType.getName());
                                    terminal.setDeviceTypeUuid(deviceType.getUuid());
                                }

                                if (StringUtil.isNotEmpty(terminal.getMac())) {
                                    TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
                                    if (terminalMacIpv6 != null && terminalMacIpv6.getIsIPv6() == 1) {
                                        terminal.setIsIpv6(1);
                                        v6_number += 1;
                                    } else {
                                        terminal.setIsIpv6(0);
                                    }
                                }
                            }
                        }

                        BigDecimal percentage = new BigDecimal("0");
                        if (number == 0 || v6_number == 0) {
                        } else {
                            BigDecimal v6 = new BigDecimal(v6_number);
                            BigDecimal num = new BigDecimal(number);
                            // 计算百分比并保留两位小数
                            percentage = v6.divide(num, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                            // 去掉尾随的 .00
                            percentage = percentage.stripTrailingZeros();
                            System.out.println("v6_number 占 number 的百分比: " + percentage.setScale(2, RoundingMode.HALF_UP) + "%");
                        }
                        terminalUnit.setPercentage(percentage);
                    }
                }
            }
            NoticeWebsocketResp rep = new NoticeWebsocketResp();
            rep.setNoticeType("104");
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(terminalUnitList);
            this.redisResponseUtils.syncStrRedis(sessionId, JSON.toJSONString(terminalUnitList, SerializerFeature.WriteMapNullValue), 104);
            return rep;
        }

        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        rep.setNoticeType("104");
        rep.setNoticeStatus(0);
        return rep;
    }

}
