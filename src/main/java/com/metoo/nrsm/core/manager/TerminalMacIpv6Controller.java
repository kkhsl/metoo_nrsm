package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.TerminalMacIpv6Mapper;
import com.metoo.nrsm.entity.TerminalMacIpv6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/terminal/mac/ipv6")
@RestController
public class TerminalMacIpv6Controller {

    @Autowired
    private TerminalMacIpv6Mapper terminalMacIpv6Mapper;

    @PostMapping("/insert")
    public Object insertMac(@RequestBody TerminalMacIpv6 terminalMacIpv6) {
        try {
            terminalMacIpv6Mapper.insertMac(terminalMacIpv6.getMac(), terminalMacIpv6.getIsIPv6());
            return ResponseUtil.ok();
        } catch (Exception e) {
            return ResponseUtil.error("Failed to insert MAC address: " + e.getMessage());
        }
    }

    @GetMapping("/delete")
    public Object deleteMac(String mac){
        try {
            terminalMacIpv6Mapper.deleteMac(mac);
            return ResponseUtil.ok("MAC address deleted successfully: " + mac);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to delete MAC address: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Object updateMac(@RequestBody TerminalMacIpv6 metooMac) {
        try {
            terminalMacIpv6Mapper.updateMac(metooMac.getMac(), metooMac.getIsIPv6());
            return ResponseUtil.ok("MAC address updated successfully: " + metooMac);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to update MAC address: " + e.getMessage());
        }
    }

    @GetMapping("/selectAll")
    public Object selectAllMac() {
        List<TerminalMacIpv6> allMacs = terminalMacIpv6Mapper.getAllMacs();
        return ResponseUtil.ok(allMacs);
    }

    @GetMapping("/selectByMac/{mac}")
    public Object selectByMac(@PathVariable String mac) {
        TerminalMacIpv6 macByMacAddress = terminalMacIpv6Mapper.getMacByMacAddress(mac);
        if (macByMacAddress != null) {
            return ResponseUtil.ok(macByMacAddress);
        } else {
            return ResponseUtil.error("MAC address not found: " + mac);
        }
    }
}