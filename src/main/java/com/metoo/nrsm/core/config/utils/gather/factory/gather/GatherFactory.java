package com.metoo.nrsm.core.config.utils.gather.factory.gather;

import com.metoo.nrsm.core.config.utils.gather.factory.gather.impl.*;
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
    public Gather getGather(String shapeType) {
        if (shapeType == null) {
            return null;
        }
        if (shapeType.equalsIgnoreCase(Global.TRAFFIC)) {
            if (Global.env.equals("probe")) {
                return new TrafficFactoryImpl();
            }
            if (Global.env.equals("yuehu")) {// yuehu
                return new TrafficFactoryImplYuehu();
            }
            if (Global.env.equals("dev")) {// yingtan
                return new TrafficFactoryImplYingtan();
            }
            if (Global.env.equals("guixi")) {// yingtan
                return new TrafficFactoryImplGuixi();
            }
        } else if (shapeType.equalsIgnoreCase("fileToProbe")) {
            return new GatherOsScanVersin();

        }
        return null;
    }

}
