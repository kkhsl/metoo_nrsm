package com.metoo.nrsm.core.manager.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.IProbeService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.api.EncrypUtils;
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

    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        return date;
    }

    public String getEncryptedDataByUnit() {
        String encryptedData = "";
        // 查询全部单位
        List<Unit> unitList = unitService.selectUnitAll();
        if(unitList.size() > 0){
            for (Unit unit : unitList) {
                // 获取测绘数据
                String data = getSureyingData(unit);
                log.info("单位：{} {}", unit.getUnitName(), data);
                // 推送数据到远程

            }

        }
        return encryptedData;
    }

    public String getSureyingData(Unit unit){
        Map data = new HashMap();
        data.put("areaInfo", new HashMap<>());
        data.put("deviceInfo", new ArrayList<>());
        data.put("terminalInfo", new ArrayList<>());
        data.put("probe", new ArrayList<>());

        // 查询 区域信息
        AreaVO areaInfo = new AreaVO(unit.getUnitId(), unit.getCityName(), unit.getCountyName(), unit.getUnitName(), getDate());
        // 查询设备
        List<NetworkElement> deviceInfo = getDevice();
        // 查询终端
        List<Terminal> terminalInfo = getTerminalByUnit(unit.getId());
        // 查询probe数据
        List<Probe> probe = getProbe(unit.getId());
        data.put("areaInfo", areaInfo);
        data.put("deviceInfo", deviceInfo);
        data.put("terminalInfo", terminalInfo);
        data.put("probe", probe);

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
