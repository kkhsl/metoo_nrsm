package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.PlantRoomDTO;
import com.metoo.nrsm.core.service.IPlantRoomService;
import com.metoo.nrsm.core.service.IRackService;
import com.metoo.nrsm.core.service.IRsmsDeviceService;
import com.metoo.nrsm.core.utils.file.DownLoadFileUtil;
import com.metoo.nrsm.core.utils.poi.ExcelUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.PlantRoomVO;
import com.metoo.nrsm.entity.PlantRoom;
import com.metoo.nrsm.entity.Rack;
import com.metoo.nrsm.entity.RsmsDevice;
import com.metoo.nrsm.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Api("机柜")
@RequestMapping("/admin/rack")
@RestController
public class RackManagerController {

    @Autowired
    private IRackService rackService;
    @Autowired
    private IRsmsDeviceService rsmsDeviceService;
    @Autowired
    private IPlantRoomService plantRoomService;

    @GetMapping("/get")
    public Object get(@PathVariable("id") String id){
        Rack rack = this.rackService.getObjById(Long.parseLong(id));
        return ResponseUtil.ok(rack);
    }

    @ApiOperation("获取机柜信息")
    @RequestMapping("/getRack")
    public Object getTackByDeviceId(@RequestParam(value = "id",required = false) Long id,
                            @RequestParam(value="uuid",required = false) String uuid){
        if(uuid != null){
            RsmsDevice device = this.rsmsDeviceService.getObjByUuid(uuid);
            if(device == null){
                return ResponseUtil.badArgument("设备已删除");
            }
            Object obj = this.rackService.rack(device.getRackId());
            return ResponseUtil.ok(obj);
        }else if(id != null){
            Object obj = this.rackService.rack(id);
            return ResponseUtil.ok(obj);
        }
        return ResponseUtil.ok();
    }


    @ApiOperation("机柜列表")
    @PostMapping("/list")
    public Object list(@RequestBody PlantRoomDTO dto){
        Page<PlantRoom> page = this.plantRoomService.findBySelectAndRack(dto);
        if(page.size() > 0){
            return ResponseUtil.ok(new PageInfo<Rack>(page));
        }
        return ResponseUtil.ok();
    }

    @ApiOperation("添加机柜")
    @GetMapping("/add")
    public Object add(){
        List<PlantRoomVO> plantRoomList = this.plantRoomService.query(null);
        return ResponseUtil.ok(plantRoomList);
    }

    @ApiOperation("更新机柜")
    @GetMapping("/update")
    public Object update(String id){
        Rack rack = this.rackService.getObjById(Long.parseLong(id));
        if(rack != null){
            PlantRoom plantRoom = this.plantRoomService.getObjById(rack.getPlantRoomId());
            List<PlantRoomVO> plantRoomList = this.plantRoomService.query(null);
            Map map = new HashMap();
            map.put("rack", rack);
            map.put("plantRoomList", plantRoomList);
            return ResponseUtil.ok(map);
        }
        return ResponseUtil.badArgument();
    }

    @ApiOperation("保存机柜")
    @RequestMapping("/save")
    public Object save(@RequestBody Rack instance){
        if(instance == null){
            return ResponseUtil.badArgument();
        }
        if(instance.getSize() == null || instance.getSize().equals("")){
            return ResponseUtil.badArgument("请选择机柜大小");
        }else if(instance.getSize() == 0){
            return ResponseUtil.badArgument("择机柜大小不能小于等于0");
        }

        Map params = new HashMap();
        // 验证名称唯一性
        if(instance.getName() != null && !instance.getName().isEmpty()){
            params.clear();
            params.put("rackId", instance.getId());
            params.put("rackName", instance.getName());
            params.put("plantRoomId", instance.getPlantRoomId());
            List<Rack> rackList = this.rackService.selectObjByMap(params);
            if(rackList.size() > 0){
                return ResponseUtil.badArgumentRepeatedName();
            }
        }
        PlantRoom obj = this.plantRoomService.getObjById(instance.getPlantRoomId());

        if(obj == null){
            return ResponseUtil.badArgument("请选择机房");
        }

        // 验证资产编号唯一性
        if(instance.getAsset_number() != null && !instance.getAsset_number().isEmpty()){
            params.clear();
            params.put("rackId", instance.getId());
            params.put("asset_number", instance.getAsset_number());
            List<Rack> rackList = this.rackService.selectObjByMap(params);
            if(rackList.size() > 0){
                Rack rack = rackList.get(0);
                return ResponseUtil.badArgument("资产编号与(" + rack.getPlantRoomName() + ":" + rack.getName() + ")重复");
            }
        }


        int flag = this.rackService.save(instance);
        if (flag != 0){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("机柜保存失败");
    }

    @ApiOperation("删除机柜")
    @DeleteMapping("/del")
    public Object del(@RequestParam(value = "id") String id){
        Rack instance = this.rackService.getObjById(Long.parseLong(id));
        if(instance == null){
            return ResponseUtil.badArgument("机柜不存在");
        }
        // 设备
//        User user = ShiroUserHolder.currentUser();
        Map params = new HashMap();
        params.put("rackId", instance.getId());
//        params.put("userId", user.getId());
        List<RsmsDevice> rsmsDevices = this.rsmsDeviceService.selectObjByMap(params);
        for (RsmsDevice rsmsDevice : rsmsDevices){
            rsmsDevice.setRackId(null);
            rsmsDevice.setRackName(null);
            try {
                this.rsmsDeviceService.update(rsmsDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int flag = this.rackService.delete(Long.parseLong(id));
        if (flag >= 1){

            return ResponseUtil.ok();
        }
        return ResponseUtil.error("机柜删除失败");
    }

    @ApiOperation("批量删除机柜")
    @DeleteMapping("/batch/del")
    public Object batchDel(@RequestParam(value = "ids") String ids){
        return this.rackService.batchDel(ids);
    }

    @ApiOperation("设备批量导入")
    @PostMapping("/import")
    public Object importExcel(@RequestPart("file") MultipartFile file) throws Exception {
        if(!file.isEmpty()){
            String fileName = file.getOriginalFilename().toLowerCase();
            String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
            if (suffix.equals("xlsx") || suffix.equals("xls")) {
                List<Rack> racks = ExcelUtils.readMultipartFile(file, Rack.class);
                // 校验表格数据是否符号要求
                String tips = "";
                for (Rack rack : racks) {
                    if(!rack.getRowTips().isEmpty()){
                        tips = rack.getRowTips();
                        break;
                    }
                }
                if(!tips.isEmpty()){
                    return ResponseUtil.badArgument(tips);
                }
                if(racks.size() > 0){
                    String msg = "";
                    Map params = new HashMap();
                    List<Rack> rackList = new ArrayList<>();
                    for (int i = 0; i < racks.size(); i++) {
                        Rack obj = racks.get(i);
                        if(obj.getName()  == null || obj.getName().equals("")){
                            msg = "第" + (i + 2) + "行,机柜名不能为空";
                            break;
                        }else{
                            params.clear();
                            params.put("name", obj.getName());
                            List<RsmsDevice> deviceList = this.rsmsDeviceService.selectObjByMap(params);
                            if(deviceList.size() > 0){
                                msg = "第" + (i + 2) + "行, 机柜已存在";
                                break;
                            }
                        }
                        if(obj.getSize() == null || obj.getSize().equals("")){
                            msg = "第" + (i + 2) + "行,机柜大小不能为空";
                            break;
                        }else if(obj.getSize() <= 0){
                            msg = "第" + (i + 2) + "行,机柜大小不能为小于1";
                            break;
                        }
                        // 机房
                        if(obj.getPlantRoomName()!= null && !obj.getPlantRoomName().equals("")){
                            params.clear();
                            params.put("name", obj.getPlantRoomName());
                            List<PlantRoom> plantRooms = this.plantRoomService.selectObjByMap(params);
                            if(plantRooms.size() >= 1){
                                PlantRoom plantRoom = plantRooms.get(0);
                                obj.setPlantRoomId(plantRoom.getId());
                                obj.setPlantRoomName(plantRoom.getName());
                            }else{
                                msg = "第" + (i + 2) + "行,机房不存在";
                                break;
                            }
                        }
                        // 验证资产编号唯一性
                        if(obj.getAsset_number() != null && !obj.getAsset_number().isEmpty()){
                            params.clear();
                            params.put("asset_number", obj.getAsset_number());
                            params.put("rackId", obj.getId());
                            List<Rack> rackList1 = this.rackService.selectObjByMap(params);
                            if(rackList1.size() > 0){
                                Rack rsmsDevice = rackList1.get(0);
                                return ResponseUtil.badArgument("资产编号与(" + rsmsDevice.getName() + ")机柜重复");
                            }
                        }
                        rackList.add(obj);
                    }

                    if(msg.isEmpty()){
                        int i = this.rackService.batchInsert(rackList);
                        if(i >= 0){
                            return ResponseUtil.ok();
                        }else{
                            return ResponseUtil.error();
                        }
                    }else{
                        return ResponseUtil.badArgument(msg);
                    }
                }else{
                    return ResponseUtil.badArgument("文件无数据");
                }
            }else{
                return ResponseUtil.badArgument("文件格式错误，请使用标准模板上传");
            }
        }
        return ResponseUtil.badArgument("文件不存在");
    }

    @Value("${batchImportRackFileName}")
    private String batchImportRackFileName;
    @Value("${batchImportFilePath}")
    private String batchImportFilePath;

    @ApiOperation("下载机柜批量上传模板")
    @GetMapping("/downTemp")
    public Object downTemplate(HttpServletResponse response) throws UnsupportedEncodingException {
        boolean flag = DownLoadFileUtil.downloadTemplate(this.batchImportFilePath, this.batchImportRackFileName, response);
        if(flag){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.error();
        }
    }


}
