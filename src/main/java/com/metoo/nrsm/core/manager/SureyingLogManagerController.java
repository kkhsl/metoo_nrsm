package com.metoo.nrsm.core.manager;


import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ISurveyingLogService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.SurveyingLogVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/admin/sureying/log")
@RestController
public class SureyingLogManagerController {

    @Autowired
    private ISurveyingLogService surveyingLogService;

    // false true结束轮询
    @GetMapping
    public Result logs(){
        // 获取测绘日志
        List<SurveyingLogVo> surveyingLogList=surveyingLogService.queryLogInfo();
        Map result = new HashMap();
        boolean finish = false;
        result.put("data", surveyingLogList);

        if(surveyingLogList.size() > 0){
            long count = surveyingLogList.stream().filter(surveyingLogVo -> surveyingLogVo.getType() != null
                    && surveyingLogVo.getType() == 10 && (surveyingLogVo.getStatus() == 2 || surveyingLogVo.getStatus() == 3)).count();
            if(count >= 1){
                finish = true;
            }
        }
        result.put("finish", finish);
        return ResponseUtil.ok(result);
    }

}
