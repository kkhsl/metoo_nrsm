package com.metoo.nrsm.core.config.juc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-27 10:48
 * <p>
 * 测试打断
 */
@Slf4j
public class InterruptDemo {

    private static Map<String, Thread> threadMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {

        String uuuid = "";
        for (int i = 0; i < 3; i++) {
            int a = i;
            Thread t1 = new Thread(() -> {
                while (true) {
                    Thread current = Thread.currentThread();
                    if (current.isInterrupted()) {
                        log.debug("打断退出");
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                        System.out.println(a);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
//                        break;
                        // break 改为 两阶段终止模式
                        // 重置打断标记
                        current.interrupt();
                    }

                }
            });
            t1.start();
            String uuid = UUID.randomUUID().toString();
            System.out.println(uuid);
            uuuid = uuid;
            threadMap.put(uuid, t1);
        }

        Thread.sleep(1000);

        System.out.println(threadMap);

        threadMap.get(uuuid).interrupt();

    }

}
