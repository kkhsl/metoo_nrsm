package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.entity.Dhcp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:06
 */
@RequestMapping("/admin/dhcp/ftl")
@Controller
public class DhcpManagerFtlController {

    @Autowired
    private IDhcpService dhcpService;


    @GetMapping("/ftl/list")
    public String ftl_list(Model model) {
        List<Dhcp> dhcps = this.dhcpService.selectObjByMap(null);
        model.addAttribute("dhcps", dhcps);
        return "dhcp";
    }

}
