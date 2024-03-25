package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.RsmsDeviceDTO;
import com.metoo.nrsm.core.vo.RsmsDeviceVo;
import com.metoo.nrsm.entity.RsmsDevice;

import java.util.List;
import java.util.Map;

public interface IRsmsDeviceService {

    RsmsDevice getObjById(Long id);

    RsmsDevice getObjByUuid(String uuid);

    RsmsDevice getObjAndProjectById(Long id);

    Page<RsmsDevice> selectConditionQuery(RsmsDeviceDTO instance);

    List<RsmsDeviceVo> selectNameByMap(Map map);

    List<RsmsDevice> selectObjByMap(Map map);

    int save(RsmsDevice instance);

    int update(RsmsDevice instance);

    int delete(Long id);

    int batchDel(String ids);

    int batchInsert(List<RsmsDevice> rsmsDevices);
}
