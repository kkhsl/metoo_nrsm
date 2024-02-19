package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.Ipv4Mapper;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.entity.nspm.Arp;
import com.metoo.nrsm.entity.nspm.Ipv4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:31
 */
@Service
//@Transactional
public class Ipv4ServiceImpl implements Ipv4Service {

    @Autowired
    private Ipv4Mapper ipv4Mapper;

    @Override
    public List<Ipv4> joinSelectObjAndIpv6() {
        return this.ipv4Mapper.joinSelectObjAndIpv6();
    }

    @Override
    public boolean save(Ipv4 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.ipv4Mapper.save(instance);
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
        return this.ipv4Mapper.removeDuplicates();
    }

    @Override
    public boolean truncateTable() {
        try {
            this.ipv4Mapper.truncateTable();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }
}
