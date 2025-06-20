package com.metoo.nrsm.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.TaskController;
import com.metoo.nrsm.core.mapper.SurveyingLogMapper;
import com.metoo.nrsm.core.service.ISurveyingLogService;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.enums.LogStatusType;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.SurveyingLogVo;
import com.metoo.nrsm.entity.SurveyingLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurveyingLogServiceImpl implements ISurveyingLogService {

    @Resource
    private SurveyingLogMapper surveyingLogMapper;

    @Override
    public List<SurveyingLog> selectObjByMap(Map params) {
        List<SurveyingLog> logs = this.surveyingLogMapper.selectObjByMap(params);
        return logs;
    }

    @Override
    public Result insert(SurveyingLog instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(DateTools.getCreateTime());
            try {
                int i = this.surveyingLogMapper.insert(instance);
                return i >= 0 ? ResponseUtil.ok() : ResponseUtil.saveError();
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.saveError();
            }
        } else {
            try {
                int i = this.surveyingLogMapper.update(instance);
                return i >= 0 ? ResponseUtil.ok() : ResponseUtil.saveError();
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtil.saveError();
            }
        }
    }

    @Override
    public int update(SurveyingLog instance) {
        try {
            return this.surveyingLogMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Result delete(Integer id) {
        try {
            int i = this.surveyingLogMapper.delete(id);
            return i >= 0 ? ResponseUtil.ok() : ResponseUtil.deleteError();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.deleteError();
        }
    }

    @Override
    public int deleteTable() {
        try {
            return this.surveyingLogMapper.deleteTable();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<SurveyingLogVo> queryLogInfo() {
        return null;
    }

    @Override
    public int createSureyingLog(String name, String beginTime, Integer status, Integer parentId, Integer type) {
        SurveyingLog surveyingLog = new SurveyingLog()
                .addTime(DateTools.getCreateTime())
                .name(name).beginTime(beginTime)
                .status(status)
                .type(type);
        if (null != parentId) {
            surveyingLog.setParentId(parentId);
        }
        if (null != status && status.equals(LogStatusType.FAIL.getCode())) {
            surveyingLog.setEndTime(DateTools.getCreateTime());
        }
        this.insert(surveyingLog);
        return surveyingLog.getId();
    }

    @Override
    public void updateSureyingLog(Integer id, Integer status) {
        SurveyingLog surveyingLog = new SurveyingLog();
        surveyingLog.setId(id);
        surveyingLog.endTime(DateTools.getCreateTime()).status(status);
        this.update(surveyingLog);
    }

}
