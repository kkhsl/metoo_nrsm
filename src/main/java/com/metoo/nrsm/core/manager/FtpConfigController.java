package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.FtpConfigMapper;
import com.metoo.nrsm.core.service.impl.FtpConfigServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FtpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/ftp")
public class FtpConfigController {

    @Autowired
    private FtpConfigServiceImpl ftpConfigService;

    @Autowired
    private FtpConfigMapper ftpConfigMapper;

    /**
     * 新增FTP配置
     */
    @PostMapping("/save")
    public Result save(@RequestBody FtpConfig dto) {
        Result ftpConfig = ftpConfigService.createFtpConfig(dto);
        return ftpConfig;
    }


    @GetMapping("/list")
    public Result getAllActive() {
        Result result = ftpConfigService.selectAllQuery();
        return result;
    }

    @DeleteMapping("/delete")
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public Result delete(@RequestParam String ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseUtil.badArgument("参数错误");
        }

        List<Long> checkIds = new ArrayList<>();
        for (String idStr : ids.split(",")) {
            try {
                checkIds.add(Long.parseLong(idStr.trim()));
            } catch (NumberFormatException e) {
                return ResponseUtil.badArgument("非法ID格式: " + idStr);
            }
        }

        List<FtpConfig> unitsToDelete = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long id : checkIds) {
            FtpConfig unit = ftpConfigMapper.selectById(Math.toIntExact(id));
            if (unit == null) {
                errors.add("ftp账号不存在: " + id);
                continue;
            }
            // 没有错误则添加待删除列表
            if (errors.isEmpty()) {
                unitsToDelete.add(unit);
            }
        }

        for (FtpConfig ftpConfig : unitsToDelete) {
            ftpConfig.setDeleteStatus(true);
            ftpConfigService.updateFtpConfig(ftpConfig);
        }

        if (!errors.isEmpty()) {
            return ResponseUtil.badArgument(String.join("; ", errors));
        }
        return ResponseUtil.ok("删除成功");
    }
}