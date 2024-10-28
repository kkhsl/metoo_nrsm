package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.entity.Dhcp;

import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:07
 */

public interface IDhcpHistoryService {

//    @Autowired
//    private DhcpDao dhcpDao;
//    @Autowired
//    private MongoTemplate mongoTemplate;

    // 保存dhcp
//    public void save(Dhcp dhcp){
//        this.dhcpDao.save(dhcp);
//    }


    Dhcp selectObjById(Long id);

    Dhcp selectObjByLease(String lease);

    Page<Dhcp> selectConditionQuery(DhcpDto dto);

    List<Dhcp> selectObjByMap(Map params);

    boolean save(Dhcp instance);

    boolean update(Dhcp instance);

    boolean delete(Long id);

    int truncateTable();

    int batchInsert();

}
