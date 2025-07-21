package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.RiskPortConfigMapper;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.RiskPortConfig;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RequestMapping("/admin/riskPort")
@RestController
public class RiskPortManagerController {

    @Resource
    private RiskPortConfigMapper portConfigMapper;

    // 获取所有配置
    @GetMapping("/select")
    public Result list() {
        List<RiskPortConfig> riskPortConfigs = portConfigMapper.selectAll();
        if (riskPortConfigs.size()==0){
            return ResponseUtil.ok(null);
        }
        return ResponseUtil.ok(riskPortConfigs.get(0));
    }

    @GetMapping("/save")
    public Result save(@RequestParam String ports) {
        RiskPortConfig riskPortConfig = new RiskPortConfig();
        List<RiskPortConfig> riskPortConfigs = portConfigMapper.selectAll();
        if (riskPortConfigs.size()!=0){
            riskPortConfig.setId(riskPortConfigs.get(0).getId());
            riskPortConfig.setPortValue(ports);
            int update = portConfigMapper.update(riskPortConfig);
            if (update>0){
                return ResponseUtil.ok();
            }else {
                return ResponseUtil.error();
            }
        }else {
            riskPortConfig.setPortValue(ports);
            int insert = portConfigMapper.insert(riskPortConfig);
            if (insert>0){
                return ResponseUtil.ok();
            }else {
                return ResponseUtil.error();
            }
        }
    }



}
