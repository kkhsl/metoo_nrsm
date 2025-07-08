package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.PlantRoomDTO;
import com.metoo.nrsm.core.service.IPlantRoomService;
import com.metoo.nrsm.core.service.IRackService;
import com.metoo.nrsm.core.service.IRsmsDeviceService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.PlantRoomVO;
import com.metoo.nrsm.entity.PlantRoom;
import com.metoo.nrsm.entity.Rack;
import com.metoo.nrsm.entity.RsmsDevice;
import com.metoo.nrsm.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api("机房")
@RequestMapping("/admin/plant/room")
@RestController
public class PlantRoomManagerController {

    @Autowired
    private IPlantRoomService plantRoomService;
    @Autowired
    private IRackService rackService;
    @Autowired
    private IRsmsDeviceService rsmsDeviceService;

    @ApiOperation("机房列表")
    @RequestMapping("/list")
    public Object list(@RequestBody PlantRoomDTO dto) {
        Page<PlantRoom> page = this.plantRoomService.selectConditionQuery(dto);
        if (page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<PlantRoom>(page));
        }
        return ResponseUtil.ok();
    }

    @GetMapping("/rackList")
    public Object rackList(@RequestParam("id") Long id) {
        PlantRoom plantRoom = this.plantRoomService.getObjById(id);
        Rack rack = new Rack();
        rack.setPlantRoomId(plantRoom.getId());
        List<Rack> rackList = this.rackService.query(rack);
        // 遍历得到rack的使用信息
        List list = new ArrayList();
        for (Rack obj : rackList) {
            list.add(this.rackService.rack(obj.getId()));
        }
        return ResponseUtil.ok(list);
    }


    @ApiOperation("机柜卡片")
    @GetMapping("/cart")
    public Object cart() {
        List<PlantRoom> plantRoomList = this.plantRoomService.selectObjByCard();
        return ResponseUtil.ok(plantRoomList);
    }

    @ApiOperation("/新增或更新机房")
    @RequestMapping("/save")
    public Object save(@RequestBody PlantRoom instance) {
        if (instance == null) {
            return ResponseUtil.badArgument();
        }
        if (StringUtils.isEmpty(instance.getName())) {
            return ResponseUtil.badArgument("机房名称不能为空");
        }
        if (!StringUtils.isEmpty(instance.getName())) {
            Map params = new HashMap();
            params.put("name", instance.getName());
            params.put("plantRoomId", instance.getId());
            List<PlantRoomVO> plantRooms = this.plantRoomService.selectVoByMap(params);
            if (plantRooms.size() > 0) {
                return ResponseUtil.badArgumentRepeatedName();
            }
        }
        int flag = this.plantRoomService.save(instance);
        if (flag != 0) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("机房保存失败");
    }

    @ApiOperation("删除机房")
    @DeleteMapping("/del")
    public Object del(@RequestParam(value = "id") String id) {
        PlantRoom instance = this.plantRoomService.getObjById(Long.parseLong(id));
        if (instance == null) {
            return ResponseUtil.badArgument("机房不存在");
        }
        if (instance.getDeleteStatus() == 1) {
            return ResponseUtil.badArgument("默认机房不允许删除");
        }
        Map params = new HashMap();
        params.put("plantRoomId", instance.getId());
        List<Rack> racks = this.rackService.selectObjByMap(params);
        if (racks.size() > 0) {
            return ResponseUtil.badArgument("删除失败，请先删除机柜");
        }
//        // 更新机柜到默认机房
//        updateRack(instance.getId());
        int flag = this.plantRoomService.delete(Long.parseLong(id));
        if (flag != 0) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("机房删除失败");
    }

    @ApiOperation("批量删除机房")
    @DeleteMapping("/batch/del")
    public Object batchDel(@RequestParam(value = "ids") String ids) {
        String[] l = ids.split(",");
        List<String> list = Arrays.asList(l);
        for (String id : list) {
            PlantRoom instance = this.plantRoomService.getObjById(Long.parseLong(id));
            if (instance == null) {
                return ResponseUtil.badArgument("机房不存在");
            }
            if (instance.getDeleteStatus() == 1) {
                return ResponseUtil.badArgument("默认机房不允许删除");
            }
//        // 更新机柜到默认机房
//        updateRack(instance.getId());
            Map params = new HashMap();
            params.put("plantRoomId", instance.getId());
            List<Rack> racks = this.rackService.selectObjByMap(params);
            if (racks.size() > 0) {
                return ResponseUtil.badArgument("删除失败，请先删除[" + instance.getName() + "]机柜");
            }
        }
        int flag = this.plantRoomService.batchDel(ids);
        if (flag != 0) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("机房删除失败");
    }

    public void updateRack(Long plantRoomId) {
        // 查询预置机房
        PlantRoom plantRoom = new PlantRoom();
        plantRoom.setDeleteStatus(1);
        List<PlantRoomVO> vo = this.plantRoomService.query(plantRoom);
        // 查询所有机柜
        Rack rack = new Rack();
        rack.setPlantRoomId(plantRoomId);
        List<Rack> rackList = this.rackService.query(rack);
        for (Rack obj : rackList) {
            obj.setPlantRoomId(vo.get(0).getId());
            this.rackService.update(obj);
        }
        User user = ShiroUserHolder.currentUser();
        Map params = new HashMap();
        params.put("userId", user.getId());
        params.put("plantRoomId", plantRoomId);
        // 更新设备
        List<RsmsDevice> rsmsDevices = this.rsmsDeviceService.selectObjByMap(params);
        for (RsmsDevice rsmsDevice : rsmsDevices) {
            rsmsDevice.setPlantRoomId(vo.get(0).getId());
            try {
                this.rsmsDeviceService.update(rsmsDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
