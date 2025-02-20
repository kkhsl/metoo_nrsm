package com.metoo.nrsm.core.network;

import com.metoo.nrsm.core.network.hostname.SnmpWalkLocalStrategy;
import com.metoo.nrsm.core.network.hostname.SnmpWalkSshStrategy;
import com.metoo.nrsm.core.network.hostname.SnmpWalkStrategy;
import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;

/**
 * 策略模式（Strategy Pattern）
 */
public class SnmpWalkContext {

    private SnmpWalkStrategy strategy;

    public SnmpWalkContext(String executionType) {
        // 根据配置（如 properties）来选择执行方式
        if ("dev".equals(executionType)) {
            this.strategy = new SnmpWalkSshStrategy();
        } else if ("probe".equals(executionType)) {
            this.strategy = new SnmpWalkLocalStrategy();
        } else {
            throw new IllegalArgumentException("Unknown execution type: " + executionType);
        }
    }

    public void execute(SnmpWalkResult result) {
        // 调用具体策略执行
        strategy.execute(result);
    }
}