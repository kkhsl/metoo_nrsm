package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.TerminalAssetDTO;
import com.metoo.nrsm.core.mapper.TerminalAssetMapper;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.ITerminalAssetService;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.TerminalAsset;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TerminalAssetServiceImpl implements ITerminalAssetService {

    @Autowired
    private TerminalAssetMapper TerminalAssetMapper;

    @Override
    public TerminalAsset selectObjById(Long id) {
        return this.TerminalAssetMapper.selectObjById(id);
    }

    @Override
    public Page<TerminalAsset> selectObjByConditionQuery(TerminalAssetDTO instance) {
        if(instance == null){
            instance = new TerminalAssetDTO();
        }
        Page<TerminalAsset> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.TerminalAssetMapper.selectObjByConditionQuery(instance);
        return page;
    }

    @Override
    public List<TerminalAsset> selectObjByMap(Map params) {
        return this.TerminalAssetMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(TerminalAsset instance) {
        if(instance.getId() == null){
            if(instance.getId() == null || instance.getId().equals("")){
                instance.setAddTime(new Date());
                instance.setFrom(1);
                instance.setTag("DT");
                instance.setUuid(UUID.randomUUID().toString());
            }else{
                if(instance.getFrom() != null && Strings.isBlank(instance.getFrom().toString())){
                    instance.setFrom(1);
                }
            }
            try {
                this.TerminalAssetMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                this.TerminalAssetMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(TerminalAsset instance) {
        try {
            this.TerminalAssetMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int delete(Long id) {
        return this.TerminalAssetMapper.delete(id);
    }

}

