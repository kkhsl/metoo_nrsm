package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.service.Ipv6Service;
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.Ipv6;
import com.metoo.nrsm.entity.NetworkElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@RequestMapping("/admin/arp")
@RestController
public class ArpManagerController {

}
