package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.SurveyingLogVo;
import com.metoo.nrsm.entity.SurveyingLog;

import java.util.List;
import java.util.Map;

public interface ISurveyingLogService {

    List<SurveyingLog> selectObjByMap(Map params);

    Result insert(SurveyingLog instance);

    int update(SurveyingLog instance);

    Result delete(Integer id);

    int deleteTable();

    /**
     * 获取测绘采集日志
     *
     * @return
     */
    List<SurveyingLogVo> queryLogInfo();

    int createSureyingLog(String name, String beginTime, Integer status, Integer parentId, Integer type);

    void updateSureyingLog(Integer id, Integer status);
}
