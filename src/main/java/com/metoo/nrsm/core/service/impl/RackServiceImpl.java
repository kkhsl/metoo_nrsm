package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.RackDTO;
import com.metoo.nrsm.core.mapper.PlantRoomMapper;
import com.metoo.nrsm.core.mapper.RackMapper;
import com.metoo.nrsm.core.service.IRackService;
import com.metoo.nrsm.core.service.IRsmsDeviceService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Rack;
import com.metoo.nrsm.entity.RsmsDevice;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class RackServiceImpl implements IRackService {

    @Autowired
    private RackMapper rackMapper;
    @Autowired
    private PlantRoomMapper plantRoomMapper;
    @Autowired
    private IRsmsDeviceService rsmsDeviceService;

    @Override
    public Rack getObjById(Long id) {
        return this.rackMapper.getObjById(id);
    }

    @Override
    public Rack selectObjByName(String name) {
        return this.rackMapper.selectObjByName(name);
    }

    @Override
    public Page<Rack> findBySelect(RackDTO instance) {
//        User user = ShiroUserHolder.currentUser();
//        instance.setUserId(user.getId());
        Page<Rack> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.rackMapper.findBySelect(instance);
        return page;
    }

    @Override
    public List<Rack> query(Rack instance) {
        if (instance == null) {
            instance = new Rack();
        }
//        User user = ShiroUserHolder.currentUser();
//        instance.setUserId(user.getId());
        return this.rackMapper.query(instance);
    }

    @Override
    public List<Rack> selectObjByMap(Map params) {
//        User user = ShiroUserHolder.currentUser();
//        params.put("userId", user.getId());
        return this.rackMapper.selectObjByMap(params);
    }

    @Override
    public int save(Rack instance) {
        if (instance.getId() == null) {
            instance.setAddTime(new Date());
            return this.rackMapper.save(instance);
        } else {
            return this.rackMapper.update(instance);
        }
    }

    @Override
    public int batchInsert(List<Rack> rackList) {
        try {
            for (Rack instance : rackList) {
                instance.setAddTime(new Date());
                User user = ShiroUserHolder.currentUser();
                instance.setUserId(user.getId());
            }
            return this.rackMapper.batchInsert(rackList);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int update(Rack instance) {
        return this.rackMapper.update(instance);
    }

    @Override
    public int delete(Long id) {
        return this.rackMapper.delete(id);
    }

    @Override
    public Object batchDel(String ids) {
        String[] l = ids.split(",");
        List<String> list = Arrays.asList(l);
        for (String id : list) {
            Rack instance = this.rackMapper.getObjById(Long.parseLong(id));
            if (instance == null) {
                return ResponseUtil.badArgument("机柜不存在");
            }
            // 设备
//            User user = ShiroUserHolder.currentUser();
            Map params = new HashMap();
            params.put("rackId", instance.getId());
//            params.put("userId", user.getId());
            List<RsmsDevice> rsmsDevices = this.rsmsDeviceService.selectObjByMap(params);
            for (RsmsDevice rsmsDevice : rsmsDevices) {
                rsmsDevice.setRackId(null);
                rsmsDevice.setRackName(null);
                try {
                    this.rsmsDeviceService.update(rsmsDevice);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        int flag = this.rackMapper.batchDel(ids);

        if (flag != 0) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("机柜删除失败");
    }

    @Override
    public Object rack(Long id) {
        Map map = new HashMap();
        Rack rack = this.rackMapper.getObjById(id);
        // 优化 使用JDK新特性
        // 判断是否开启反面
        if (rack instanceof Rack) {
            map.put("rack", rack);
            map.put("front", this.getRack(rack, false));
            map.put("back", rack.getRear() ? this.getRack(rack, true) : null);
        }
        return map;
    }

    @Override
    public boolean verifyRack(Rack rack, Integer start, Integer size, boolean rear, Long id) {
        boolean flag = false;
        if (rack != null) {
            int current = start;
            // 查询设备，根据设备位置排序
            Map params = new HashMap();
            params.put("rear", rear);
            params.put("rackId", rack.getId());
            params.put("start", 0);
            params.put("size", 0);
            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
            // 查询空闲位置，一级已使用设备
            if (rsmsDeviceList.size() > 0) {
                for (RsmsDevice device : rsmsDeviceList) {
                    if (device.getId().equals(id)) {
                        flag = true;
                        continue;
                    }
                    // 判断在前面
                    if (device.getStart() - current > 0 && device.getStart() - current >= size) {// 上次与本次间隔
                        flag = true;
                        continue;
                    } else if (device.getStart() - current < 0 && (device.getStart() - 1 + device.getSize()) < current) {// 上次与本次间隔
                        flag = true;
                        continue;
                    } else {
                        return false;
                    }
                }
            } else {
                flag = true;
            }
        }
        return flag;
    }

    public List getRack(Rack rack, boolean rear) {
        if (rack instanceof Rack) {
            List list = new ArrayList();
            int current = 1;
            int lentght = rack.getSize();
            // 查询设备，根据设备位置排序(正面)
            Map params = new HashMap();
            params.put("rear", rear);
            params.put("rackId", rack.getId());
            params.put("start", 0);
            params.put("size", 0);
            List<RsmsDevice> rsmsDeviceList = this.rsmsDeviceService.selectObjByMap(params);
            // 查询空闲位置，一级已使用设备
            if (rsmsDeviceList.size() > 0) {
                for (RsmsDevice device : rsmsDeviceList) {
                    // 判断首次
                    if (device.getStart() - current > 0) {// 上次与本次间隔
                        // 首先记录间隔
                        Map middle = this.json1(current, device.getStart() - current, null);
                        list.add(middle);
                    }
                    Map middle = this.json1(device.getStart(), device.getSize(), device);
                    list.add(middle);
                    current = device.getSize() + device.getStart();// 当前位置
                    lentght = rack.getSize() - (device.getSize() + device.getStart() - 1);
                }
            }
            if (lentght >= 1) {
                Map end = this.json1(current, lentght, null);
                list.add(end);
            }
            return list;
        }
        return null;
    }

    public Map json(int current, int length, RsmsDevice rsmsDevice) {
        Map map = new HashMap();
        List list = new ArrayList();
        for (int i = current; i > current - length; i--) {
            list.add(i);
        }
        map.put("floor", list);
        map.put("deviceInfo", rsmsDevice);
        return map;
    }

    public Map json1(int current, int length, RsmsDevice rsmsDevice) {
        Map map = new HashMap();
        List list = new ArrayList();
        for (int i = current; i < current + length; i++) {
            list.add(i);
        }
        map.put("floor", list);
        map.put("deviceInfo", rsmsDevice);
        return map;
    }

    // 根据起始位置和长度，判断当前位置是否存在设备

}
