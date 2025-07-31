package com.metoo.nrsm.core.manager.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.IProbeService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.api.EncrypUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.AreaVO;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sun.nio.ch.Net;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class UnitDataUtils {

    private final IUnitService unitService;
    private final ITerminalService terminalService;
    private final INetworkElementService networkElementService;
    private final IProbeService probeService;

    public UnitDataUtils(IUnitService unitService, ITerminalService terminalService, INetworkElementService networkElementService,
                         IProbeService probeService){
        this.unitService = unitService;
        this.terminalService = terminalService;
        this.networkElementService = networkElementService;
        this.probeService = probeService;
    }

    /**
     * 根据单位分组获取区域、设备、终端、probe数据。
     * 此方法将所有单位的数据加密后按单位名称返回，单位名称作为键，加密后的数据作为值。
     *
     * @return 返回一个包含所有单位加密数据的列表
     */
    public List<Map<String, String>> getEncryptedDataByUnit() {
        List<Map<String, String>> encryptedDataList = new ArrayList<>();
        List<Unit> unitList = unitService.selectUnitAll();
        if (!unitList.isEmpty()) {
           unitList.forEach(unit -> {
               // 获取该单位的加密数据
               String encryptedData = getSureyingData(unit);
               Map<String, String> unitDataMap = new HashMap<>();
               unitDataMap.put(unit.getUnitName(), encryptedData);
               encryptedDataList.add(unitDataMap);
           });
        }
        return encryptedDataList;
    }

    /**
     * 该方法根据单位获取区域、设备、终端和探针数据，
     * 然后将这些数据进行加密并返回加密后的 JSON 字符串。
     *
     * @param unit 当前单位对象
     * @return 返回加密后的 JSON 字符串，包含区域、设备、终端和探针数据。
     */
    public String getSureyingData(Unit unit){
        // 创建数据容器，存储相关的区域信息、设备信息、终端信息和探针信息
        Map<String, Object> data = new HashMap<>();

        // 区域信息
        AreaVO areaInfo = new AreaVO(
                unit.getUnitId(),
                unit.getCityName(),
                unit.getCountyName(),
                unit.getUnitName(),
                DateTools.getCurrentDateTime()
        );
        // 添加区域信息
        data.put("areaInfo", areaInfo);

        // 获取设备信息
        List<NetworkElement> deviceInfo = getDevice();
        data.put("deviceInfo", deviceInfo);

        // 获取终端信息
        List<Terminal> terminalInfo = getTerminalByUnit(unit.getId());
        data.put("terminalInfo", terminalInfo);

        // 获取探针数据
        List<Probe> probe = getProbe(unit.getId());
        data.put("probe", probe);

        // 将数据转换为 JSON 字符串，并加密后返回
        // SerializerFeature.WriteMapNullValue 确保空值字段也会写入 JSON
        return EncrypUtils.encrypt(JSONObject.toJSONString(data, SerializerFeature.WriteMapNullValue));
    }

    public List<Terminal> getTerminalByUnit(Long unitId){
        Map params = new HashMap();
        params.put("unitId", unitId);
        return terminalService.selectObjByMap(params);
    }

    public List<NetworkElement> getDevice(){
        Map params = new HashMap();
        params.put("nswitch", false);
        return networkElementService.selectObjByMap(params);
    }

    private List<Probe> getProbe(Long unitId){
       Map params = new HashMap();
       params.put("unitId", unitId);
       return probeService.selectObjByMap(params);
    }
}
