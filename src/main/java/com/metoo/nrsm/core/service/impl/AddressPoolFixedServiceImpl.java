package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.dto.AddressPoolFixedDTO;
import com.metoo.nrsm.core.mapper.AddressPoolFixedMapper;
import com.metoo.nrsm.core.service.IAddressPoolFixedService;
import com.metoo.nrsm.core.service.IAddressPoolService;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.utils.io.address.pool.AddressPoolIpv4ConcurrentUtil;
import com.metoo.nrsm.core.vo.AddressPoolFixedVO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolFixed;
import com.metoo.nrsm.entity.SysConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class AddressPoolFixedServiceImpl implements IAddressPoolFixedService {

    @Resource
    private AddressPoolFixedMapper addressPoolFixedMapper;
    @Autowired
    private IAddressPoolService addressPoolService;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IDhcpService dhcpService;

    @Override
    public AddressPoolFixed selectObjById(Long id) {
        return this.addressPoolFixedMapper.selectObjById(id);
    }

    @Override
    public Page<AddressPoolFixed> selectObjConditionQuery(AddressPoolFixedDTO dto) {
        if (dto == null) {
            dto = new AddressPoolFixedDTO();
        }

        Page<AddressPoolFixed> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.addressPoolFixedMapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<AddressPoolFixed> selectObjByMap(Map params) {
        return this.addressPoolFixedMapper.selectObjByMap(params);
    }

    @Override
    public List<AddressPoolFixedVO> selectObjToVOByMap(Map params) {
        return this.addressPoolFixedMapper.selectObjToVOByMap(params);
    }

    @Override
    public int save(AddressPoolFixed instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            try {
                instance.setAddTime(new Date());
                int i = this.addressPoolFixedMapper.save(instance);
                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV4_status(true);
                this.sysConfigService.update(sysconfig);

                return i;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {

                AddressPoolFixed obj = this.addressPoolFixedMapper.selectObjById(instance.getId());
                boolean flag = Md5Crypt.getDiffrent(obj, instance);
                if(!flag){
                    // 更新应用按钮
                    SysConfig sysconfig = this.sysConfigService.select();
                    sysconfig.setV4_status(true);
                    this.sysConfigService.update(sysconfig);
                    //更新数据
                    int i = this.addressPoolFixedMapper.update(instance);
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
    public int update(AddressPoolFixed instance) {
        try {
            AddressPoolFixed obj = this.addressPoolFixedMapper.selectObjById(instance.getId());
            boolean flag = Md5Crypt.getDiffrent(obj, instance);
            if(!flag){
                // 更新应用按钮
                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV4_status(true);
                this.sysConfigService.update(sysconfig);
                //更新数据
                int i = this.addressPoolFixedMapper.update(instance);
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
            int i = this.addressPoolFixedMapper.delete(id);
            SysConfig sysconfig = this.sysConfigService.select();
            sysconfig.setV4_status(true);
            this.sysConfigService.update(sysconfig);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void write() {
        List<AddressPoolVO> addressPoolVOList = this.addressPoolService.selectObjToVOByMap(null);
        AddressPoolIpv4ConcurrentUtil instance = AddressPoolIpv4ConcurrentUtil.getInstance();
        try {
            boolean flag = instance.write(addressPoolVOList);

            SysConfig sysconfig = this.sysConfigService.select();
            sysconfig.setV4_status(false);
            this.sysConfigService.update(sysconfig);

            if(flag){
                String dhcpd = this.dhcpService.checkdhcpd("dhcpd");
                if(Boolean.valueOf(dhcpd)){
                    this.dhcpService.dhcpdop("restart", "dhcpd");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
