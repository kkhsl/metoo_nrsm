package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.OsScanMapper;
import com.metoo.nrsm.core.service.IOsScanService;
import com.metoo.nrsm.entity.OsScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Transactional
public class OsScanServiceImpl implements IOsScanService {

    @Resource
    private OsScanMapper osScanMapper;

    public OsScanServiceImpl(OsScanMapper osScanMapper) {
        this.osScanMapper = osScanMapper;
    }

    @Override
    public int insert(OsScan instance) {
        try {
            instance.setCreateTime(new Date());
            return this.osScanMapper.insert(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
