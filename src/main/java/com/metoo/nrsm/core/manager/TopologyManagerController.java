package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.TopologyDTO;
import com.metoo.nrsm.core.manager.utils.MacUtils;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/admin/topology")
@RestController
public class TopologyManagerController {

    @Autowired
    private ITopologyService topologyService;
    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IAccessoryService accessoryService;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private IPortService portService;
    @Autowired
    private IPortIpv6Service portIpv6Service;

    @RequestMapping("/list")
    public Object list(@RequestBody(required = false) TopologyDTO dto){
        if(dto == null){
            dto = new TopologyDTO();
        }
        User user = ShiroUserHolder.currentUser();
        Page<Topology> page = this.topologyService.selectConditionQuery(dto);
        if(page.getResult().size() > 0) {
            if(page.getResult().size() == 1){
                // 设置默认拓扑
                try {
                    this.setTopologyDefualt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ResponseUtil.ok(new PageInfo<Topology>(page));
        }
        return ResponseUtil.ok();
    }

    @ApiOperation("拓扑修改名称")
    @GetMapping("/rename")
    public Object rename(Long id, String name){
        if(name == null || name.equals("")){
            return  ResponseUtil.badArgument("拓扑名称不能为空");
        }
        Map params = new HashMap();
        params.put("name", name);
        params.put("NotId", id);
        List<Topology> topologies = this.topologyService.selectObjByMap(params);
        if(topologies.size() > 0){
            return  ResponseUtil.badArgument("拓扑名称已存在");
        }else{
            if(name != null && !name.equals("")){
                Topology topology = this.topologyService.selectObjById(id);
                if(topology != null){
                    topology.setName(name);
                    if(topology.getSuffix() != null && !topology.getSuffix().equals("")){
                        topology.setSuffix(null);
                    }
                    int i = this.topologyService.update(topology);
                    if(i == 1){
                        return ResponseUtil.ok();
                    }else{
                        return ResponseUtil.error();
                    }
                }else{
                    return  ResponseUtil.resourceNotFound();
                }
            }
        }
        return ResponseUtil.badArgument();
    }

    @ApiOperation("拓扑复制")
    @GetMapping("/copy")
    public Object copy(String id, String name, String groupId){
        Map params = new HashMap();
        if(name != null && !name.equals("")){
            params.clear();
            params.put("name", name);
            params.put("NotId", id);
            List<Topology> Topos = this.topologyService.selectObjByMap(params);
            if(Topos.size() > 0){
                return  ResponseUtil.badArgument("拓扑名称已存在");
            }
        }
        params.clear();
        params.put("id", id);
        List<Topology> topologies = this.topologyService.selectObjByMap(params);
        if(topologies.size() > 0){
            Topology copyTopology = topologies.get(0);
            Long returnId = this.topologyService.copy(copyTopology);
            if(returnId != null){
                Topology topology = this.topologyService.selectObjById(Long.parseLong(String.valueOf(returnId)));
                if(topology != null){
                    if(name != null && !name.equals("")){
                        topology.setName(name);
                    }else{
                        String suffix = this.changName(copyTopology.getSuffix(), 1);
                        topology.setSuffix(suffix);
                    }
                    this.topologyService.update(topology);
                    return ResponseUtil.ok();
                }
            }else{
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.badArgument();
    }

    public String changName(String suffix, int num){
        if(suffix == null || suffix.equals("")){
            int number = num;
            if(number == 0){
                number = 1;
            }
            String name = "副本" + " (" + number + ")";
            Topology topology = this.topologyService.selectObjBySuffix(name);
            if(topology != null){
                number ++;
                return this.changName(null, number);
            }
            return name;
        }else{
            int number = num;
            if(number == 0){
                number = 1;
            }
            String name = suffix + " 副本 (" + number + ")";
            Topology topology = this.topologyService.selectObjBySuffix(name);
            if(topology != null){
                number ++;
                return this.changName(suffix, number);
            }
            return name;
        }
    }

    @ApiOperation("保存拓扑")
    @RequestMapping("/save")
    public Object save(@RequestBody(required = false) Topology instance){
        // 校验拓扑名称是否重复
        Map params = new HashMap();
        if(instance.getId() == null
                || instance.getId().equals("")
                ){
            if(StringUtils.isEmpty(instance.getName())){
                return ResponseUtil.badArgument("拓扑名称不能为空");
            }
        }
        if(StringUtils.isNotEmpty(instance.getName())){
            params.put("topologyId", instance.getId());
            params.put("name", instance.getName());
            List<Topology> topologList = this.topologyService.selectObjByMap(params);
            if(topologList.size() > 0){
                return ResponseUtil.badArgument("拓扑名称重复");
            }
        }

        if(instance.getContent() != null && !instance.getContent().equals("")){
            String str = JSONObject.toJSONString(instance.getContent());
            instance.setContent(str);
        }

        int result = this.topologyService.save(instance);
        if(result >= 1){
            // 设置默认拓扑
            try {
                this.setTopologyDefualt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseUtil.ok(result);
        }
        return ResponseUtil.badArgument();
    }

    @DeleteMapping("/delete")
    public Object delete(String ids){
        if(ids != null && !ids.equals("")){
            for (String id : ids.split(",")){
                Topology obj = this.topologyService.selectObjById(Long.parseLong(id));
                if(obj.getIsDefault()){
                    return ResponseUtil.badArgument("拓扑【" + obj.getName() + "】为默认拓扑");
                }
                try {
                    Topology topology = this.topologyService.selectObjById(obj.getId());
                    if(topology != null){
                        int i = this.topologyService.delete(obj.getId());
                        if(i >= 1){
                            // 设置默认拓扑
                            try {
                                this.setTopologyDefualt();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        return ResponseUtil.badArgument();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseUtil.ok("拓扑【" + obj.getName() + "】删除失败");
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    /**
     * 设置乐观锁，多用户同时登陆，避免并发提交
     * @param id
     * @return
     */
    @ApiOperation("设置默认拓扑")
    @RequestMapping("/default")
    public Object isDefault(String id){
        Topology obj = this.topologyService.selectObjById(Long.parseLong(id));
        if(obj != null){
            obj.setIsDefault(true);
            User user = ShiroUserHolder.currentUser();
            List<Topology> topologyList = this.defaultList(user, true);
            if(topologyList.size() > 0){
                Topology topology = topologyList.get(0);
                if(obj == topology){
                    return ResponseUtil.ok();
                }else{
                    topology.setIsDefault(false);
                    this.topologyService.update(topology);
                }
            }
            this.topologyService.update(obj);
            return ResponseUtil.ok();
        }
        return ResponseUtil.ok();
    }

//    @ApiOperation("拓扑信息")
//    @GetMapping("/info")
//    public Object topologyInfo(
//            @RequestParam(value = "id") Long id,
//            @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
//            @RequestParam(value = "time", required = false) Date time){
//        if(id == null){
//            return  ResponseUtil.badArgument();
//        }
//        User user = ShiroUserHolder.currentUser();
//        Topology topology = this.topologyService.selectObjById(id);
//        if(topology != null){
//            if(topology.getContent() != null && !topology.getContent().equals("")){
//                JSONObject content = JSONObject.parseObject(topology.getContent().toString());
//                topology.setContent(content);
//            }
//            return ResponseUtil.ok(topology);
//        }
//        return ResponseUtil.ok();
//    }


    @ApiOperation("拓扑信息")
    @GetMapping("/info")
    public Object topologyInfo(
            @RequestParam(value = "id") Long id,
            @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
            @RequestParam(value = "time", required = false) Date time){
        if(id == null){
            return  ResponseUtil.badArgument();
        }
        List<Topology> topologies = this.selectObjById(id, time);
        if(topologies != null && topologies.size() > 0){
            Topology topology = topologies.get(0);
            if(topology.getContent() != null && !topology.getContent().equals("")){
                JSONObject content = JSONObject.parseObject(topology.getContent().toString());
                topology.setContent(content);
            }
            return ResponseUtil.ok(topology);
        }
        return ResponseUtil.ok();
    }

    public  List<Topology> selectObjById(Long id, Date time){
        Map params = new HashMap();
        List<Topology> topologies = null;
        params.put("id", id);
        if(time == null){
            topologies = this.topologyService.selectObjByMap(params);
        }else{
            params.put("id", id);
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            cal.set(Calendar.SECOND, 99);
            Date date = cal.getTime();
            params.put("time", date);
            topologies = this.topologyService.selectObjHistoryByMap(params);
        }
        return topologies;
    }

    @ApiOperation("默认拓扑")
    @GetMapping("/default/topology")
    public Object defaultTopology(){
        Map map = new HashMap();
        Map params = new HashMap();
        params.put("isDefault", true);
        List<Topology> topologies = this.topologyService.selectObjByMap(params);
        if (topologies.size() > 0){
            Topology topology = topologies.get(0);
            if(topology.getContent() != null && !topology.getContent().equals("")){
                JSONObject content = JSONObject.parseObject(topology.getContent().toString());
                topology.setContent(content);
            }
            map.put("topology", topologies.get(0));
            return ResponseUtil.ok(map);
        }
        return ResponseUtil.ok(map);
    }

    public void setTopologyDefualt(){
        List<Topology> topologies = this.topologyService.selectObjByMap(null);
        if(topologies.size() == 1){
            Topology topology = topologies.get(0);
            topology.setIsDefault(true);
            this.topologyService.update(topology);
        }
    }


    public  List<Topology> defaultList(User user, boolean isDefault){
        Map params = new HashMap();
        params.put("isDefault", true);
        List<Topology> topologies = this.topologyService.selectObjByMap(params);
        return topologies;
    }


    public String uploadFile(@RequestParam(required = false) MultipartFile file){
        String path = Global.TOPOLOGYFILEPATH;
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String fileName1 = DateTools.getCurrentDate(new Date());
        File imageFile = new File(path +  "/" + fileName1 + ext);
        if (!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(imageFile);
            Accessory accessory = new Accessory();
            accessory.setA_name(fileName1);
            accessory.setA_path(path);
            accessory.setA_ext(ext);
            accessory.setA_size((int)file.getSize());
            accessory.setType(4);
            this.accessoryService.save(accessory);
            String picNewName = fileName1 + ext;
            String patha = /*"/opt/nmap/resource" + "/" +*/ "/images/" + picNewName;
            return patha;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean winUploadFile(@RequestParam(required = false) MultipartFile file){
        String path = "C:\\Users\\Administrator\\Desktop\\新建文件夹 (2)";
        String originalName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String picNewName = fileName + ext;
        String imgRealPath = path  + File.separator + picNewName;
        Date currentDate = new Date();
        String fileName1 = DateTools.getCurrentDate(currentDate);
        System.out.println(path + "\\" + fileName1 + ".png");
        File imageFile = new File(path +  "\\" + fileName1 + ".png");
        if (!imageFile.getParentFile().exists()) {
            imageFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(imageFile);
            Accessory accessory = new Accessory();
            accessory.setA_name(picNewName);
            accessory.setA_path(path);
            accessory.setA_ext(ext);
            accessory.setA_size((int)file.getSize());
            accessory.setType(4);
            this.accessoryService.save(accessory);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/query/device/{value}")
    public Object queryDevice(@PathVariable(value = "value") String value){
        Map result = new HashMap();
        Map params = new HashMap();
        if(MacUtils.isValidMacAddress(value)){
            params.clear();
            params.put("mac", value);
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if(terminals.size() > 0){
                Set<String> set = terminals.stream().map(e -> {
                    return e.getMac();
                }).collect(Collectors.toSet());
                result.put("terminal", set);
            }
            return ResponseUtil.ok(result);
        }else{
            if(StringUtils.isNotBlank(value)){
                params.clear();
                if(Ipv4Util.verifyIp(value) && StringUtil.isNotEmpty(value)){
                    params.put("ip", value);
                    List<Port> ports = this.portService.selectObjByMap(params);
                    if(ports.size() > 0){
                        Set<String> set = new HashSet<>();
                        set.add(ports.get(0).getDeviceUuid());
                        result.put("device", set);
                    }
                    params.clear();
                    params.put("v4ip", value);
                    List<Terminal> terminals = this.terminalService.selectObjByMap(params);
                    if(terminals.size() > 0){
                        Set<String> set = terminals.stream().map(e -> {
                            return e.getMac();
                        }).collect(Collectors.toSet());
                        result.put("terminal", set);
                    }
                } else if(Ipv6Util.verifyIpv6(value) && StringUtil.isNotEmpty(value)){
                    params.clear();
                    params.put("v6ip", value);
                    List<Terminal> terminals = this.terminalService.selectObjByMap(params);
                    if(terminals.size() > 0){
                        Set<String> set = terminals.stream().map(e -> {
                            return e.getMac();
                        }).collect(Collectors.toSet());
                        result.put("terminal", set);
                    }else{
                        params.clear();
                        params.put("v6ip", value);
                        List<PortIpv6> portIpv6s = this.portIpv6Service.selectObjByMap(params);
                        if(portIpv6s.size() > 0){
                            Set<String> set = new HashSet<>();
                            set.add(portIpv6s.get(0).getDeviceUuid());
                            result.put("device", set);
                        }
                    }
                }
                return ResponseUtil.ok(result);
            }
        }
        return ResponseUtil.badArgument();
    }


    @ApiOperation("端口信息")
    @GetMapping("/port/{uuid}")
    public Object port(@PathVariable(value = "uuid") String uuid){
        if(Strings.isBlank(uuid)){
            return ResponseUtil.badArgument();
        }
        List list = this.topologyService.getDevicePortsByUuid(uuid);
        return ResponseUtil.ok(list);
    }

}
