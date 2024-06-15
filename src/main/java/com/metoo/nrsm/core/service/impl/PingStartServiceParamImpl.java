package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.PingStartMapper;
import com.metoo.nrsm.core.service.IPingStartServiceParam;
import com.metoo.nrsm.entity.PingStartParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 9:57
 */
@Service
@Transactional
public class PingStartServiceParamImpl implements IPingStartServiceParam {

    @Autowired
    private PingStartMapper pingStartMapper;


    @Override
    public PingStartParam selectOneObj() {
        return this.pingStartMapper.selectOneObj();
    }

    @Override
    public boolean update(PingStartParam install) {
        try {
            this.pingStartMapper.update(install);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
