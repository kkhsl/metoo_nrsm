package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.dto.AddressPoolDTO;
import com.metoo.nrsm.core.mapper.AddressPoolMapper;
import com.metoo.nrsm.core.service.IAddressPoolService;
import com.metoo.nrsm.core.vo.AddressPoolVO;
import com.metoo.nrsm.entity.nspm.AddressPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AddressPoolServiceImpl implements IAddressPoolService {

    @Autowired
    private AddressPoolMapper addressPoolMapper;

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
        if(addressPool.getId() == null || addressPool.getId().equals("")){
            try {
                addressPool.setAddTime(new Date());
                int i = this.addressPoolMapper.save(addressPool);
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
        }else{
            try {
                int i = this.addressPoolMapper.update(addressPool);
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

    }

    @Override
    public int update(AddressPool addressPool) {
        try {

            int i = this.addressPoolMapper.update(addressPool);
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
    public int delete(Long id) {
        try {
            int i = this.addressPoolMapper.delete(id);
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
    public void write(){
        List<AddressPoolVO> addressPools = this.addressPoolMapper.selectObjToVOByMap(null);
        // 使用流式操作进行过滤并输出结果
        List<AddressPoolVO> filteredEntities = addressPools.stream()
                .filter(e -> e != null && !e.getName().isEmpty()) // 这里根据需要自定义判断条件
                .collect(Collectors.toList());
        try {
            // 创建一个 FileOutputStream 对象，指定文件路径
//            FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\metoo\\dhcp.txt");
            File file = new File("/etc/dhcp/dhcpdtest.conf");
            FileOutputStream fos = new FileOutputStream(file);

            fos.getChannel().position(0).truncate(0); // 清空文件内容

            for (AddressPoolVO entity : filteredEntities) {
                // 有序
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                Class<?> clazz = entity.getClass();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = field.get(entity);
                    if(value != null){
                        map.put(fieldName, value);
                    }
                }
                Collection<Object> values = map.values();
                for (Object value: values){
                    if(value != null && !value.equals("")){
                        byte[] bytes = value.toString().getBytes();
                        fos.write(bytes);
                    }
                }

                fos.write("}\n\n".getBytes());
            }
            // 关闭流
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
