package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDomainService;
import com.metoo.nrsm.entity.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/admin/domain")
@RestController
public class DomainManagerController {

    @Autowired
    private IDomainService domainService;

    @GetMapping
    public Object list() {
        Map params = new HashMap();
        List<Domain> domains = this.domainService.selectObjByMap(params);
        return ResponseUtil.ok(domains);
    }

    @PostMapping
    public Object save(@RequestBody Domain domain) {
        if (domain.getId() != null && !"".equals(domain.getId())) {
            Domain obj = this.domainService.selectObjById(domain.getId());
            if ("默认二层域".equals(obj.getName())) {
                return ResponseUtil.badArgument("默认二层域不能修改");
            }
        }
        if ("默认二层域".equals(domain.getName())) {
            return ResponseUtil.badArgument("名称不能为默认二层域");
        }
        if (domain.getName() == null || domain.getName().equals("")) {
            return ResponseUtil.badArgument("名称不能为空");
        } else {
            Map params = new HashMap();
            params.put("domainId", domain.getId());
            params.put("name", domain.getName());
            List<Domain> domains = this.domainService.selectObjByMap(params);
            if (domains.size() > 0) {
                return ResponseUtil.badArgument("名称重复");
            }
        }
        int result = this.domainService.save(domain);
        if (result >= 1) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping
    public Object delete(String ids) {
        for (String id : ids.split(",")) {
            Domain domain = this.domainService.selectObjById(Long.parseLong(id));
            if (domain == null) {
                return ResponseUtil.badArgument();
            }
            if ("默认二层域".equals(domain.getName())) {
                return ResponseUtil.badArgument("默认二层域不能删除");
            }
            int i = this.domainService.delete(Long.parseLong(id));
            if (i <= 0) {
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.ok();
    }

    @GetMapping("/domain")
    public Object domain() {
        List<Domain> domains = this.domainService.selectDomainAndVlanProceDureByMap(null);
        return ResponseUtil.ok(domains);
    }

}
