package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.dto.AddressPoolIpv6DTO;
import com.metoo.nrsm.core.mapper.AddressPoolIpv6Mapper;
import com.metoo.nrsm.core.service.IAddressPoolIpv6Service;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.utils.io.address.pool.AddressPoolIpv6ConcurrentUtil;
import com.metoo.nrsm.core.vo.AddressPoolIpv6VO;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import com.metoo.nrsm.entity.SysConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-09 15:16
 */
@Service
@Transactional
public class AddressPoolIpv6ServiceImpl implements IAddressPoolIpv6Service {

    @Resource
    private AddressPoolIpv6Mapper addressPoolIpv6Mapper;
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private IDhcpService dhcpService;

    @Override
    public AddressPoolIpv6 selectObjById(Long id) {
        return this.addressPoolIpv6Mapper.selectObjById(id);
    }

    @Override
    public Page<AddressPoolIpv6> selectObjConditionQuery(AddressPoolIpv6DTO dto) {
        if (dto == null) {
            dto = new AddressPoolIpv6DTO();
        }

        Page<AddressPoolIpv6> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.addressPoolIpv6Mapper.selectObjConditionQuery(dto);
        return page;
    }

    @Override
    public List<AddressPoolIpv6> selectObjByMap(Map params) {
        return this.addressPoolIpv6Mapper.selectObjByMap(params);
    }

    @Override
    public List<AddressPoolIpv6VO> selectObjToVOByMap(Map params) {
        return this.addressPoolIpv6Mapper.selectObjToVOByMap(params);
    }

    @Override
    public int save(AddressPoolIpv6 instance) {
        if (instance.getId() == null || instance.getId().equals("")) {
            try {
                instance.setAddTime(new Date());
                int i = this.addressPoolIpv6Mapper.save(instance);


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
        } else {
            try {
//                int i = this.addressPoolIpv6Mapper.update(instance);
//                try {
//                    this.write();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return i;
                // 判断是否是否变化
                AddressPoolIpv6 obj = this.addressPoolIpv6Mapper.selectObjById(instance.getId());
                boolean flag = Md5Crypt.getDiffrent(obj, instance);
                if (!flag) {
                    // 更新应用按钮
                    SysConfig sysconfig = this.sysConfigService.select();
                    sysconfig.setV6_status(true);
                    this.sysConfigService.update(sysconfig);
                    //更新数据
                    int i = this.addressPoolIpv6Mapper.update(instance);
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
    public int update(AddressPoolIpv6 instance) {
        try {
//            int i = this.addressPoolIpv6Mapper.update(instance);
//            try {
//                this.write();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return i;
            AddressPoolIpv6 obj = this.addressPoolIpv6Mapper.selectObjById(instance.getId());
            boolean flag = Md5Crypt.getDiffrent(obj, instance);
            if (!flag) {
                // 更新应用按钮
                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV6_status(true);
                this.sysConfigService.update(sysconfig);
                //更新数据
                int i = this.addressPoolIpv6Mapper.update(instance);
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
            int i = this.addressPoolIpv6Mapper.delete(id);
//            try {
//                this.write();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

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
        List<AddressPoolIpv6VO> addressPoolV6FixedVOS = this.addressPoolIpv6Mapper.selectObjToVOByMap(null);
        AddressPoolIpv6ConcurrentUtil instance = AddressPoolIpv6ConcurrentUtil.getInstance();
        try {
            boolean flag = instance.write(addressPoolV6FixedVOS);
            if (flag) {

                SysConfig sysconfig = this.sysConfigService.select();
                sysconfig.setV6_status(false);
                this.sysConfigService.update(sysconfig);


                String dhcpd = this.dhcpService.checkdhcpd("dhcpd6");
                if (Boolean.valueOf(dhcpd)) {
                    this.dhcpService.dhcpdop("restart", "dhcpd6");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void write() {
//        List<AddressPoolIpv6VO> addressPools = this.addressPoolIpv6Mapper.selectObjToVOByMap(null);
//
//        if(addressPools.size() > 0){
//            AddressPoolIpv6VO default_ipv6 = new AddressPoolIpv6VO();
//            default_ipv6.getDefault();
//            addressPools.add(0, default_ipv6);
//            // 使用流式操作进行过滤并输出结果
//            List<AddressPoolIpv6VO> filteredEntities = addressPools.stream()
//                    .filter(e -> e != null) // 这里根据需要自定义判断条件  && !e.getName().isEmpty()
//                    .collect(Collectors.toList());
//            try {
//                // 创建一个 FileOutputStream 对象，指定文件路径
////                FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp6.txt");
//                FileOutputStream fos = new FileOutputStream(new File("/etc/dhcp/dhcpd6.conf"));
//
//                fos.getChannel().position(0).truncate(0); // 清空文件内容
//
//                for (AddressPoolIpv6VO entity : filteredEntities) {
//                    // 有序
//                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//                    Class<?> clazz = entity.getClass();
//                    for (Field field : clazz.getDeclaredFields()) {
//                        field.setAccessible(true);
//                        String fieldName = field.getName();
//                        Object value = field.get(entity);
//                        if(value != null){
//                            map.put(fieldName, value);
//                        }
//                    }
//                    Collection<Object> values = map.values();
//                    for (Object value: values){
//                        if(value != null && !value.equals("")){
//                            byte[] bytes = value.toString().getBytes();
//                            fos.write(bytes);
//                        }
//                    }
//                    if(MyStringUtils.isNotEmpty(entity.getName())){
//                        fos.write("}\n\n".getBytes());
//                    }else{
//                        fos.write("\n".getBytes());
//                    }
//                }
//                // 关闭流
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
