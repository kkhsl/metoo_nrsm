package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.DeviceTypeDTO;
import com.metoo.nrsm.core.vo.DeviceTypeVO;
import com.metoo.nrsm.entity.DeviceType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IDeviceTypeService {

    DeviceType selectObjById(Long id);

    DeviceType selectObjByName(String name);

    DeviceType selectObjByType(Integer type);

    Page<DeviceType> selectConditionQuery(DeviceTypeDTO dto);

    List<DeviceType> selectObjByMap(Map params);

    List<DeviceType> selectCountByLeftJoin();

    List<DeviceType> selectDeviceTypeAndTerminalByJoin();

    List<DeviceType> selectCountByJoin();

    List<DeviceType> selectDeviceTypeAndNeByJoin();

    List<DeviceType> selectTerminalCountByJoin();

    List<DeviceType> selectNeByType(Integer type);

    List<DeviceType> selectNeSumByType(Map params);

    List<DeviceType> selectTerminalSumByType(Map params);

    List<DeviceTypeVO> statistics();

   int save(DeviceType instance);

    int update(DeviceType instance);

    int delete(Long id);

    int batcheDel(Long[] ids);

//    String saveAndUpload(DeviceType instance, MultipartFile onlineFile, MultipartFile offlineFile);
    Object saveAndUpload(DeviceType instance, MultipartFile onlineFile, MultipartFile offlineFile);
}

