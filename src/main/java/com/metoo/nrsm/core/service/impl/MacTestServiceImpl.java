package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.MacTestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MacTestServiceImpl {
    private static final int MAX_ITERATIONS = 100;

    @Autowired
    private MacTestMapper macTestMapper;

    @Transactional
    public void executeFullProcess() {
        try {
            // 1. 执行存储过程
            macTestMapper.callRemoteIPAndPort();

            int iteration = 0;
            boolean moreThanTwoExists = true;

            // 循环直到没有分组记录数>2的条目或达到最大迭代次数
            while (moreThanTwoExists && iteration < MAX_ITERATIONS) {
                iteration++;

                // 检查是否存在分组记录数>2的条目
                int count = macTestMapper.countMultiRecordDevices();
                if (count > 2) {
                    // 执行2-5步
                    macTestMapper.updateToAEForMultiples();
                    macTestMapper.updateToDEForMultiples();
                    macTestMapper.swapAEtoDEForMultiples();
                    macTestMapper.updateToXEForMultiples();
                } else if (count == 2) {
                    macTestMapper.updatePairedDEForTwo();
                    moreThanTwoExists = false;
                } else {
                    moreThanTwoExists = false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}