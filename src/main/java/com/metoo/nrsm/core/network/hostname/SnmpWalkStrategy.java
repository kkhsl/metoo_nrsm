package com.metoo.nrsm.core.network.hostname;

import com.metoo.nrsm.core.network.jopo.SnmpWalkResult;

public interface SnmpWalkStrategy {
    void execute(SnmpWalkResult result);
}
