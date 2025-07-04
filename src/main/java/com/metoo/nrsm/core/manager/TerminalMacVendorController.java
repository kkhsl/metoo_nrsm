package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.TerminalMacVendorMapper;
import com.metoo.nrsm.entity.TerminalMacVendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/terminalMacVendor")
@RestController
public class TerminalMacVendorController {

    @Autowired
    private TerminalMacVendorMapper terminalMacVendorMapper;

    @PostMapping("/insert")
    public Object insertTerminalMacVendor(@RequestBody TerminalMacVendor terminalMacVendor) {
        try {
            terminalMacVendorMapper.insert(terminalMacVendor);
            return ResponseUtil.ok("MacVendor inserted successfully: " + terminalMacVendor);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to insert MacVendor: " + e.getMessage());
        }
    }

    @GetMapping("/delete")
    public Object deleteTerminalMacVendor(String vendor) {
        try {
            terminalMacVendorMapper.deleteByVendor(vendor);
            return ResponseUtil.ok("MacVendor deleted successfully: " + vendor);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to delete MacVendor: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Object updateByMacVendor(@RequestBody TerminalMacVendor terminalMacVendor) {
        try {
            terminalMacVendorMapper.updateByMacVendor(terminalMacVendor);
            return ResponseUtil.ok("MacVendor updated successfully: " + terminalMacVendor);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to update MacVendor: " + e.getMessage());
        }
    }

    @GetMapping("/updateByType")
    public Object updateByType(int oldType, int newType) {
        try {
            terminalMacVendorMapper.updateByMacType(oldType, newType);
            return ResponseUtil.ok("MacType updated successfully: ");
        } catch (Exception e) {
            return ResponseUtil.error("Failed to updated MacType: " + e.getMessage());
        }
    }


    @GetMapping("/selectAll")
    public Object selectAllTerminalMacVendor(String vendor) {
        List<TerminalMacVendor> terminalMacVendor = terminalMacVendorMapper.selectAllVendor(vendor);
        return ResponseUtil.ok(terminalMacVendor);
    }

    @GetMapping("/selectByMacVendor/{vendor}")
    public Object selectByMacVendor(@PathVariable String vendor) {
        TerminalMacVendor terminalMacVendor = terminalMacVendorMapper.selectByVendor(vendor);
        if (terminalMacVendor != null) {
            return ResponseUtil.ok(terminalMacVendor);
        } else {
            return ResponseUtil.error("MacVendor not found: " + vendor);
        }
    }
}