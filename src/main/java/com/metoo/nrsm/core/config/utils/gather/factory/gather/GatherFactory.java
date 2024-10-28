package com.metoo.nrsm.core.config.utils.gather.factory.gather;

import com.metoo.nrsm.core.config.utils.gather.factory.gather.impl.TrafficFactoryImpl;
import com.metoo.nrsm.core.utils.Global;
import org.springframework.stereotype.Component;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-24 16:43
 */
@Component
public class GatherFactory {

    // 非线程安全
    public Gather getGather(String shapeType){
        if(shapeType == null){
            return null;
        }
        if(shapeType.equalsIgnoreCase(Global.TRAFFIC)){
//            return new TrafficByGatewayFactoryImpl();
            return new TrafficFactoryImpl();
        }
        return null;
    }

}
