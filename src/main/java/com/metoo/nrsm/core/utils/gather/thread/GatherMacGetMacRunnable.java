package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.service.impl.MacServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Mac;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherMacGetMacRunnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private String hostname;

    private CountDownLatch latch;

    public GatherMacGetMacRunnable() {
    }

    public GatherMacGetMacRunnable(NetworkElement networkElement, Date date, String hostname, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.hostname = hostname;
        this.latch = latch;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        log.info(Thread.currentThread().getName() + ": getmac.py + : " + networkElement.getIp());

        PythonExecUtils pythonExecUtils = (PythonExecUtils) ApplicationContextUtils.getBean("pythonExecUtils");

        String path = Global.PYPATH + "getmac.py";
        String[] params = {networkElement.getIp(), networkElement.getVersion(),
                networkElement.getCommunity()};
        String result = pythonExecUtils.exec2(path, params);
        if(StringUtil.isNotEmpty(result)){
            try {
                List<Mac> array = JSONObject.parseArray(result, Mac.class);
                if (array.size() > 0) {
                    List<Mac> list = new ArrayList();
                    MacServiceImpl macService = (MacServiceImpl) ApplicationContextUtils.getBean("macServiceImpl");
                    array.forEach(e -> {
                        if ("3".equals(e.getType())) {
                            e.setAddTime(date);
                            e.setDeviceIp(networkElement.getIp());
                            e.setDeviceName(networkElement.getDeviceName());
                            e.setHostname(hostname);
                            String patten = "^" + "00:00:5e";
                            boolean flag = this.parseLineBeginWith(e.getMac(), patten);
                            if (flag) {
                                e.setTag("LV");
                            }
                            list.add(e);
                        }
                    });
                    if(list.size() > 0){
                        macService.batchSaveGather(list);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(latch != null){
                    latch.countDown();
                }
            }
        }else{
            if(latch != null){
                latch.countDown();
            }
        }
    }

    /**
     * 判断Mac是否以某个规则开始
     * @param lineText
     * @param head
     * @return
     */
    public boolean parseLineBeginWith(String lineText, String head){

        if(StringUtil.isNotEmpty(lineText) && StringUtil.isNotEmpty(head)){
            String patten = "^" + head;

            Pattern compiledPattern = Pattern.compile(patten);

            Matcher matcher = compiledPattern.matcher(lineText);

            while(matcher.find()) {
                return true;
            }
        }
        return false;
    }

}
