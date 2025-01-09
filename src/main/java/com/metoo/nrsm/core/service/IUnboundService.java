package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.entity.Unbound;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IUnboundService {

    boolean add(UnboundDTO instance);
    boolean addDNS(UnboundDTO instance);

    Unbound selectObjByOne(Map params);

    boolean save(Unbound instance);
    boolean open(UnboundDTO instance);
    boolean start() throws Exception;
    boolean stop() throws Exception;
    boolean status() throws Exception;

    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    boolean saveDNS(Unbound instance);

    @Transactional(rollbackFor = Exception.class)  // 强制回滚所有异常
    boolean openAdress(Unbound instance);

    boolean update(UnboundDTO instance);

    boolean delete(Long id);
    boolean deleteDNS(Long id);
    boolean deleteAll(Long id);

}
