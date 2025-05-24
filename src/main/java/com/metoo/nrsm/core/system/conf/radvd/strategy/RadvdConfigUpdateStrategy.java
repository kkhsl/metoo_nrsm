package com.metoo.nrsm.core.system.conf.radvd.strategy;

import com.metoo.nrsm.entity.Radvd;

import java.util.List;

public interface RadvdConfigUpdateStrategy {

    void updateConfig(List<Radvd> radvdList);
}
