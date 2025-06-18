package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.AddressPoolV6FixedDTO;
import com.metoo.nrsm.core.mapper.AddressPoolV6FixedMapper;
import com.metoo.nrsm.core.service.IAddressPoolV6FixedService;
import com.metoo.nrsm.core.service.IAddressPoolService;
import com.metoo.nrsm.core.service.IAddressPoolV6FixedService;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.utils.io.address.pool.AddressPoolIpv4ConcurrentUtil;
import com.metoo.nrsm.core.utils.io.address.pool.AddressPoolIpv6ConcurrentUtil;
import com.metoo.nrsm.core.vo.AddressPoolV6FixedVO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import com.metoo.nrsm.entity.AddressPoolV6Fixed;
import com.metoo.nrsm.entity.SysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 9:50
 */
@Service
@Transactional
public class AddressPoolV6FixedServiceImpl implements IAddressPoolV6FixedService {

    @Resource
    private AddressPoolV6FixedMapper addressPoolV6FixedMapper;
    @Autowired
    private ISysConfigService sysConfigService;

    @Override
    public AddressPoolV6Fixed selectObjById(Long id) {
        return this.addressPoolV6FixedMapper.selectObjById(id);
    }

    @Override
    public Page<AddressPoolV6Fixed> selectObjConditionQuery(AddressPoolV6FixedDTO dto) {
        if (dto == null) {
            dto = new AddressPoolV6FixedDTO();
        }

        Page<AddressPoolV6Fixed> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.addressPoolV6FixedMapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<AddressPoolV6Fixed> selectObjByMap(Map params) {
        return this.addressPoolV6FixedMapper.selectObjByMap(params);
    }

    @Override
    public List<AddressPoolV6FixedVO> selectObjToVOByMap(Map params) {
        return this.addressPoolV6FixedMapper.selectObjToVOByMap(params);
    }

    @Override
    public int save(AddressPoolV6Fixed instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            try {
                instance.setAddTime(new Date());
                int i = this.addressPoolV6FixedMapper.save(instance);

                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV6_status(true);
                this.sysConfigService.update(sysconfig);

//                try {
//                    this.write();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
//                int i = this.addressPoolV6FixedMapper.update(instance);
//                try {
//                    this.write();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return i;
                // 判断是否是否变化
                AddressPoolV6Fixed obj = this.addressPoolV6FixedMapper.selectObjById(instance.getId());
                boolean flag = Md5Crypt.getDiffrent(obj, instance);
                if(!flag){
                    // 更新应用按钮
                    SysConfig sysconfig = this.sysConfigService.select();
                    sysconfig.setV6_status(true);
                    this.sysConfigService.update(sysconfig);
                    //更新数据
                    int i = this.addressPoolV6FixedMapper.update(instance);
                    return i;
                }
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(AddressPoolV6Fixed instance) {
        try {
//            int i = this.addressPoolV6FixedMapper.update(instance);
//            try {
//                this.write();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return i;
            AddressPoolV6Fixed obj = this.addressPoolV6FixedMapper.selectObjById(instance.getId());
            boolean flag = Md5Crypt.getDiffrent(obj, instance);
            if(!flag){
                // 更新应用按钮
                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV6_status(true);
                this.sysConfigService.update(sysconfig);
                //更新数据
                int i = this.addressPoolV6FixedMapper.update(instance);
                return i;
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int delete(Long id) {
        try {
            int i = this.addressPoolV6FixedMapper.delete(id);
            // 更新应用按钮
            SysConfig sysconfig = this.sysConfigService.select();
            sysconfig.setV6_status(true);
            this.sysConfigService.update(sysconfig);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void write() {
//        List<AddressPoolV6FixedVO> addressPoolV6FixedVOS = this.addressPoolV6FixedMapper.selectObjToVOByMap(null);
//        AddressPoolIpv6ConcurrentUtil instance = AddressPoolIpv6ConcurrentUtil.getInstance();
//        try {
//            instance.write(addressPoolV6FixedVOS);
//
//            SysConfig sysconfig = this.sysConfigService.select();
//            sysconfig.setV4_status(false);
//            this.sysConfigService.update(sysconfig);
//
//            boolean flag = instance.write(addressPoolVOList);
//            if(flag){
//                String dhcpd = this.dhcpService.checkdhcpd("dhcpd");
//                if(Boolean.valueOf(dhcpd)){
//                    this.dhcpService.dhcpdop("restart", "dhcpd");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
