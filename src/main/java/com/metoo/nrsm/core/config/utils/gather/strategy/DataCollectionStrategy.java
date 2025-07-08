package com.metoo.nrsm.core.config.utils.gather.strategy;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 14:40
 * <p>
 * 策略模式
 */
public interface DataCollectionStrategy {

    void collectData(Context context);
}
