package com.metoo.nrsm.core.utils.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.CopyPropertiesReflect;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.entity.Unit;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

@Slf4j
@Component
public class ApiExecUtils {

    @Autowired
    private IUnitService unitService;
    @Autowired
    private ApiUtils apiUtils;

    public void exec2(){

        try {
            GatherFactory factory = new GatherFactory();
            Gather gather = factory.getGather(Global.TRAFFIC);
            gather.executeMethod();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Date date = new Date();
        String time = DateUtils.getDateTimeWithZeroSeconds(date);
        long currentTime = DateUtils.convertDateStringToTimestamp(time, "yyyy-MM-dd HH:mm:ss");
        String currentTimestamp = String.valueOf(currentTime);
        Map params = new HashMap();
        params.put("hidden", false);
        List<Unit> unitList = this.unitService.selectObjByMapToMonitor(params);
        List<UnitVO> unitVos = new ArrayList<>();
        if(unitList.size() > 0){
            for (Unit unit : unitList) {
                UnitVO unitVO = new UnitVO(unit.getUnitName(), unit.getDepartment(), unit.getArea(),
                        unit.getCity(), time, currentTimestamp,
                        unit.getVfourFlow(), unit.getVsixFlow(), unit.getBroadband_Account());
                unitVos.add(unitVO);
            }
        }

        log.info("Unit list {}", JSONObject.toJSONString(unitVos));

        this.apiUtils.monitorApi(unitVos);

        this.apiUtils.partyApi(unitVos);

    }

}
