package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IGradWeightService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.GradeWeight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-30 15:30
 */
@RequestMapping("/admin/grade/weight")
@RestController
public class GradeWeightManagerController {

    @Autowired
    private IGradWeightService gradWeightService;

    @GetMapping
    public Result list() {
        GradeWeight gradWeight = this.gradWeightService.selectObjOne();
        return ResponseUtil.ok(gradWeight);
    }

    @PutMapping
    public Object update(@RequestBody GradeWeight instance) {
        BigDecimal sum = instance.getFlux().add(instance.getTerminal()).add(instance.getNe());
        if (sum.compareTo(new BigDecimal(10)) > 0) {
            return ResponseUtil.badArgument("权重之和大于10");
        }
        if (sum.compareTo(new BigDecimal(10)) < 0) {
            return ResponseUtil.badArgument("权重之和小于10");
        }
        boolean flag = this.gradWeightService.update(instance);
        return ResponseUtil.ok(flag);
    }

}
