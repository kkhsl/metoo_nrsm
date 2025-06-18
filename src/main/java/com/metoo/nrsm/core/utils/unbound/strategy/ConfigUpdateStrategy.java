package com.metoo.nrsm.core.utils.unbound.strategy;

import java.io.IOException;
import java.util.List;

/**
 * 策略模式
 */
public interface  ConfigUpdateStrategy {

    List<String> updateConfig(List<String> lines, Object configData) throws IOException;
}
