package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.ThresholdConfigMapper;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.ThresholdConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


@RequestMapping("/admin/threshold")
@RestController
public class ThresholdManagerController {

    @Resource
    private ThresholdConfigMapper thresholdConfigMapper;

    // 获取所有配置
    @GetMapping("/select")
    public Result list() {
        List<ThresholdConfig> thresholdConfigs = thresholdConfigMapper.selectAll();
        if (thresholdConfigs.size()==0){
            return ResponseUtil.ok(null);
        }
        return ResponseUtil.ok(thresholdConfigs.get(0));
    }

    @GetMapping("/save")
    public Result save(@RequestParam String value) {
        ThresholdConfig thresholdConfig = new ThresholdConfig();
        List<ThresholdConfig> thresholdConfigs = thresholdConfigMapper.selectAll();
        if (thresholdConfigs.size()!=0){
            thresholdConfig.setId(thresholdConfigs.get(0).getId());
            thresholdConfig.setNum(value);
            int update = thresholdConfigMapper.update(thresholdConfig);
            if (update>0){
                return ResponseUtil.ok();
            }else {
                return ResponseUtil.error();
            }
        }else {
            thresholdConfig.setNum(value);
            int insert = thresholdConfigMapper.insert(thresholdConfig);
            if (insert>0){
                return ResponseUtil.ok();
            }else {
                return ResponseUtil.error();
            }
        }
    }



}
