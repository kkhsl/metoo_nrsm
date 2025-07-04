package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.DeviceTypeDTO;
import com.metoo.nrsm.core.mapper.DeviceTypeMapper;
import com.metoo.nrsm.core.mapper.VendorMapper;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.file.UploadFileUtil;
import com.metoo.nrsm.core.vo.DeviceTypeVO;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;


@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceTypeServiceImpl implements IDeviceTypeService {

    @Autowired
    private DeviceTypeMapper deviceTypeMapper;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Resource
    private VendorMapper vendorMapper;

    @Override
    public DeviceType selectObjById(Long id) {
        return this.deviceTypeMapper.selectObjById(id);
    }

    @Override
    public DeviceType selectObjByName(String name) {
        return this.deviceTypeMapper.selectObjByName(name);
    }

    @Override
    public DeviceType selectObjByType(Integer type) {
        return this.deviceTypeMapper.selectObjByType(type);
    }

    @Override
    public Page<DeviceType> selectConditionQuery(DeviceTypeDTO dto) {
        Page<DeviceType> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.deviceTypeMapper.selectConditionQuery(dto);
        return page;
    }

    @Override
    public List<DeviceType> selectObjByMap(Map params) {
        return this.deviceTypeMapper.selectObjByMap(params);
    }

    @Override
    public List<DeviceType> getDeviceTypeWithVendors() {
        Map<String, Object> params = new HashMap<>();
        params.put("diff", 0);
        params.put("orderBy", "sequence");
        params.put("orderType", "DESC");
        List<DeviceType> deviceTypes = deviceTypeMapper.selectObjByMap(params);

        // 为每个设备类型查询关联的品牌（带顺序）
        deviceTypes.forEach(deviceType -> {
            List<Vendor> vendors = vendorMapper.selectByDeviceType(deviceType.getId());
            deviceType.setVendors(vendors);
        });
        return deviceTypes;
    }


    @Override
    public List<DeviceType> selectCountByLeftJoin() {
        return this.deviceTypeMapper.selectCountByLeftJoin();
    }

    @Override
    public List<DeviceType> selectDeviceTypeAndTerminalByJoin() {
        return this.deviceTypeMapper.selectDeviceTypeAndTerminalByJoin();
    }

    @Override
    public List<DeviceType> selectCountByJoin() {
        return this.deviceTypeMapper.selectCountByJoin();
    }

    @Override
    public List<DeviceType> selectDeviceTypeAndNeByJoin() {
        return this.deviceTypeMapper.selectDeviceTypeAndNeByJoin();
    }

    @Override
    public List<DeviceType> selectTerminalCountByJoin() {
        return this.deviceTypeMapper.selectTerminalCountByJoin();
    }

    @Override
    public List<DeviceType> selectNeByType(Integer type) {
        return this.deviceTypeMapper.selectNeByType(type);
    }

    @Override
    public List<DeviceType> selectNeSumByType(Map params) {
        return this.deviceTypeMapper.selectNeSumByType(params);
    }

    @Override
    public List<DeviceType> selectTerminalSumByType(Map params) {
        return this.deviceTypeMapper.selectTerminalSumByType(params);
    }

    @Override
    public List<DeviceTypeVO> statistics() {
        return this.deviceTypeMapper.statistics();
    }

    @Override
    public int save(DeviceType instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            instance.setUuid(UUID.randomUUID().toString());
        }
        if (instance.getId() == null || instance.getId().equals("")) {

            //设置回滚点,只回滚以下异常
            Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
            try {
                int i = this.deviceTypeMapper.save(instance);
                int n = 10 / 0;
                return i;
            } catch (Exception e) {
                e.printStackTrace();//手工回滚异常
                TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
                return 0;
            }
        } else {
            try {
                return this.deviceTypeMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(DeviceType instance) {
        try {
            return this.deviceTypeMapper.update(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            return this.deviceTypeMapper.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int batcheDel(Long[] ids) {
        try {
            return this.deviceTypeMapper.batcheDel(ids);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Object saveAndUpload(DeviceType instance, MultipartFile onlineFile, MultipartFile offlineFile) {
        String uuid = UUID.randomUUID().toString();
        if (instance.getId() == null || instance.getId().equals("")) {
            instance.setAddTime(new Date());
            instance.setUuid(uuid);
        } else {
            DeviceType deviceType = this.deviceTypeMapper.selectObjById(instance.getId());
            uuid = deviceType.getUuid();
        }
        //设置回滚点,只回滚以下异常
        Object savePoint = TransactionAspectSupport.currentTransactionStatus().createSavepoint();
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                this.deviceTypeMapper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
                return ResponseUtil.error("保存失败");
            }
        } else {
            try {
                this.deviceTypeMapper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
                return ResponseUtil.error("保存失败");
            }
        }
        if (uuid != null) {
            if (onlineFile != null) {
                try {
                    uploadFileUtil.uploadFile(onlineFile, uuid, Global.WEBTERMINALPATH);
                } catch (Exception e) {
                    e.printStackTrace();
                    TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
                    this.uploadFileUtil.deleteFile(uuid, Global.WEBTERMINALPATH);
                    return ResponseUtil.badArgument("在线图片上传失败");
                }
            }
            if (offlineFile != null) {
                try {
                    uploadFileUtil.uploadFile(offlineFile, uuid + "_0", Global.WEBTERMINALPATH);
                } catch (Exception e) {
                    e.printStackTrace();
                    TransactionAspectSupport.currentTransactionStatus().rollbackToSavepoint(savePoint);
                    this.uploadFileUtil.deleteFile(uuid, Global.WEBTERMINALPATH);
                    this.uploadFileUtil.deleteFile(uuid + "_0", Global.WEBTERMINALPATH);
                    return ResponseUtil.badArgument("离线图片上传失败");
                }
            }
        }
        return ResponseUtil.ok();
    }
}
