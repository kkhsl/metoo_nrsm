package com.metoo.nrsm.core.config.utils.gather.factory.gather;

import com.metoo.nrsm.core.config.utils.gather.factory.gather.impl.GatherOsScanVersin;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.impl.TrafficFactoryImpl;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.impl.TrafficFactoryImplYuehu;
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
            if(Global.env.equals("probe")){
                return new TrafficFactoryImpl();
            } if(Global.env.equals("dev")){// yuehu
                return new TrafficFactoryImplYuehu();
            }
        } else if(shapeType.equalsIgnoreCase("fileToProbe")){
            return new GatherOsScanVersin();

        }
        return null;
    }

}
