package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.FluxConfigMapper;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.entity.FluxConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class FluxConfigServiceImpl implements IFluxConfigService {

    @Autowired
    private FluxConfigMapper fluxConfigMapper;

    @Override
    public FluxConfig selectObjById(Long id) {
        return this.fluxConfigMapper.selectObjById(id);
    }

    @Override
    public List<FluxConfig> selectObjByMap(Map params) {
        return this.fluxConfigMapper.selectObjByMap(params);
    }

    @Override
    public boolean save(FluxConfig instance) {
       if(instance.getId() == null){
           instance.setAddTime(new Date());
       }
        if(instance.getId() == null){
            try {
                this.fluxConfigMapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }else{
            try {
                this.fluxConfigMapper.update(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean update(FluxConfig instance) {
        try {
            this.fluxConfigMapper.update(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            this.fluxConfigMapper.delete(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void gatherFlux() {


    }
}
