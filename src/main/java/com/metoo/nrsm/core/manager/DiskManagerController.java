package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDiskService;
import com.metoo.nrsm.core.vo.DiskVO;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Disk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/disk")
public class DiskManagerController {

    @Autowired
    private IDiskService diskService;

    @GetMapping("/")
    public Result disk(){
        DiskVO disk = this.diskService.getRootDisk();
        return ResponseUtil.ok(disk);
    }
}
