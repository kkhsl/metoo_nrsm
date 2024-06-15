package com.metoo.nrsm.core.config.aop.idempotent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 16:11
 */
@RequestMapping("/idempotent")
@RestController
public class TestIdempotentController {

    @NotRepeat
    @GetMapping
    public List<String> orderList() {
        // 查询列表
        return Arrays.asList("Order_A", "Order_B", "Order_C");
        // throw new RuntimeException("参数错误");
    }
}
