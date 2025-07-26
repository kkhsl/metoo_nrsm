package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.mapper.AddressPoolMapper;
import com.metoo.nrsm.core.service.IAddressPoolService;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.utils.io.address.pool.AddressPoolIpv4ConcurrentUtil;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.SysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AddressPoolServiceImpl implements IAddressPoolService {

    @Autowired
    private AddressPoolMapper addressPoolMapper;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IDhcpService dhcpService;

//    @Lazy // 延迟固定地址池依赖加载，避免循环依赖
//    @Autowired
//    private IAddressPoolFixedService addressPoolFixedService;

    @Override
    public AddressPool selectObjById(Long id) {
        return this.addressPoolMapper.selectObjById(id);
    }

    @Override
    public Page<AddressPool> selectObjConditionQuery(AddressPoolDTO dto) {
        if (dto == null) {
            dto = new AddressPoolDTO();
        }

        Page<AddressPool> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.addressPoolMapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<AddressPool> selectObjByMap(Map params) {
        return this.addressPoolMapper.selectObjByMap(params);
    }

    @Override
    public List<AddressPoolVO> selectObjToVOByMap(Map params) {
        return this.addressPoolMapper.selectObjToVOByMap(params);
    }

    @Override
    public int save(AddressPool addressPool) {
        if (addressPool.getId() == null || addressPool.getId().equals("")) {
            try {
                addressPool.setAddTime(new Date());
                int i = this.addressPoolMapper.save(addressPool);
                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV4_status(true);
                this.sysConfigService.update(sysconfig);
                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            try {
                AddressPool obj = this.addressPoolMapper.selectObjById(addressPool.getId());
                boolean flag = Md5Crypt.getDiffrent(obj, addressPool);
                if (!flag) {
                    // 更新应用按钮
                    SysConfig sysconfig = this.sysConfigService.select();
                    sysconfig.setV4_status(true);
                    this.sysConfigService.update(sysconfig);
                    //更新数据
                    int i = this.addressPoolMapper.update(addressPool);
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
    public int update(AddressPool addressPool) {
        try {

//            int i = this.addressPoolMapper.update(addressPool);
//            try {
//                this.write();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            // 判断是否是否变化
            AddressPool obj = this.addressPoolMapper.selectObjById(addressPool.getId());
            boolean flag = Md5Crypt.getDiffrent(obj, addressPool);
            if (!flag) {
                // 更新应用按钮
                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV4_status(true);
                this.sysConfigService.update(sysconfig);
                //更新数据
                int i = this.addressPoolMapper.update(addressPool);
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
            int i = this.addressPoolMapper.delete(id);
            SysConfig sysconfig = this.sysConfigService.select();
            sysconfig.setV4_status(true);
            this.sysConfigService.update(sysconfig);
            try {
                this.write();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void write() {
        List<AddressPoolVO> addressPoolVOList = this.addressPoolMapper.selectObjToVOByMap(null);
        AddressPoolIpv4ConcurrentUtil instance = AddressPoolIpv4ConcurrentUtil.getInstance();
        try {
            // 是否可采用双重校验锁；可以不用，instance，单例线程安全，多个线程修改v4_status为false，并不影响
            boolean flag = instance.write(addressPoolVOList);
            if (flag) {
                try {
                    SysConfig sysconfig = this.sysConfigService.select();
                    sysconfig.setV4_status(false);
                    this.sysConfigService.update(sysconfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String dhcpd = this.dhcpService.checkdhcpd("dhcpd");
                if (Boolean.valueOf(dhcpd)) {
                    this.dhcpService.dhcpdop("restart", "dhcpd");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void write(){
//        List<AddressPoolVO> addressPoolVOList = this.addressPoolMapper.selectObjToVOByMap(null);
//        AddressPoolIpv4ConcurrentUtil instance = AddressPoolIpv4ConcurrentUtil.getInstance();
//        try {
//            // 是否可采用双重校验锁
//            SysConfig sysconfig = this.sysConfigService.select();
//            if(sysconfig.isV4_status()){
//                synchronized(AddressPoolServiceImpl.class){
//                    if(sysconfig.isV4_status()){
//                        boolean flag = instance.write(addressPoolVOList);
//                        if(flag){
//                            try {
//                                SysConfig sysconfig1 = this.sysConfigService.select();
//                                sysconfig1.setV4_status(false);
//                                this.sysConfigService.update(sysconfig1);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            String dhcpd = this.dhcpService.checkdhcpd("dhcpd");
//                            if(Boolean.valueOf(dhcpd)){
//                                this.dhcpService.dhcpdop("restart", "dhcpd");
//                            }
//                        }
//                    }
//
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
