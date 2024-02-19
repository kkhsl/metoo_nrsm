package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.Ipv6Mapper;
import com.metoo.nrsm.core.service.Ipv6Service;
import com.metoo.nrsm.entity.nspm.Ipv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HKK
 * @version 1.0
 * @date 2026-02-01 15:31
 */
@Service
//@Transactional
public class Ipv6ServiceImpl implements Ipv6Service {

    @Autowired
    private Ipv6Mapper ipv6Mapper;

    @Override
    public boolean save(Ipv6 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.ipv6Mapper.save(instance);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }
        return false;
    }

    @Override
    public boolean removeDuplicates() {
        return this.ipv6Mapper.removeDuplicates();
    }

    @Override
    public boolean truncateTable() {
        try {
            this.ipv6Mapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }
}
