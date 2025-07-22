package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.NswitchMapper;
import com.metoo.nrsm.core.service.INswitchService;
import com.metoo.nrsm.entity.Nswitch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-17 17:05
 */
@Service
@Transactional
public class NswitchServiceImpl implements INswitchService {

    @Autowired
    private NswitchMapper nswitchMapper;

    @Override
    public Nswitch selectObjByName(String name) {
        return this.nswitchMapper.selectObjByName(name);
    }

    @Override
    public List<Nswitch> selectObjAll() {
        return this.nswitchMapper.selectObjAll();
    }

    @Override
    public boolean save(Nswitch instance) {
        try {
            this.nswitchMapper.save(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearOrphanedNSwitch() {
        return this.nswitchMapper.clearOrphanedNSwitch();
    }
}
