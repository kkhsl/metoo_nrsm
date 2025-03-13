package com.metoo.nrsm.core.manager;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.TrafficDataMapper;
import com.metoo.nrsm.core.network.ssh.SnmpHelper;

import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.TrafficData;
import com.metoo.nrsm.entity.UnitSubnet;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.AbstractMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/terminal/unit")
public class TerminalUnitManagerController {

    @Resource
    private TerminalUnitMapper terminalUnitMapper;

    @Resource
    private TrafficDataMapper trafficDataMapper;




    @GetMapping("/selectAll")
    public Result selectAll() {
        try {
            List<UnitSubnet> unitSubnets = terminalUnitMapper.selectAll();
            if (unitSubnets == null || unitSubnets.isEmpty()) {
                return ResponseUtil.ok(unitSubnets);
            }
            return ResponseUtil.ok(unitSubnets);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to retrieve data: " + e.getMessage());
        }
    }

    @PostMapping("/saveAll")
    @Transactional
    public Result saveAll(@RequestBody List<UnitSubnet> unitSubnets) {
        if (unitSubnets == null || unitSubnets.isEmpty()) {
            return ResponseUtil.error("Input data cannot be empty");
        }

        try {
            for (UnitSubnet unitSubnet : unitSubnets) {
                if (unitSubnet.getId() == null) {
                    // ID 为空，执行插入操作
                    terminalUnitMapper.insert(unitSubnet);
                } else {
                    // ID 存在，检查是否已存在
                    UnitSubnet subnet = terminalUnitMapper.findById(unitSubnet.getId());
                    if (subnet == null) {
                        terminalUnitMapper.insert(unitSubnet);
                    } else {
                        terminalUnitMapper.update(unitSubnet);
                    }
                }
            }
            return ResponseUtil.ok("UnitSubnet added/updated successfully");
        } catch (Exception e) {
            return ResponseUtil.error("Failed to save data: " + e.getMessage());
        }
    }


    @DeleteMapping("/deleteAll")
    public Result deleteAll(@RequestParam(required = true) Long id) {
        try {
            UnitSubnet subnet = terminalUnitMapper.findById(id);
            if (subnet == null) {
                return ResponseUtil.error("UnitSubnet with ID " + id + " does not exist");
            }
            terminalUnitMapper.deleteById(id);
            return ResponseUtil.ok("UnitSubnet deleted successfully");
        } catch (Exception e) {
            return ResponseUtil.error("Failed to delete: " + e.getMessage());
        }
    }


    //@GetMapping("/getTraffic")

}
