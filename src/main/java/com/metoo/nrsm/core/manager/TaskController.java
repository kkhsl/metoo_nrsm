package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.ssh.excutor.ExecutorDto;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IProbeService;
import com.metoo.nrsm.core.service.ISurveyingLogService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.enums.LogStatusType;
import com.metoo.nrsm.core.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@RestController
@RequestMapping("/test/task")
public class TaskController {

    private final IProbeService probeService;
    private final ISurveyingLogService surveyingLogService;

    public TaskController(IProbeService probeService, ISurveyingLogService surveyingLogService){
        this.probeService = probeService;
        this.surveyingLogService = surveyingLogService;
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> runningTask = null;
    private final Object taskLock = new Object();// 专门用于同步任务状态操作

    /**
     * 启动测绘
     * @return
     */
    @GetMapping("/start")
    public Result startTask() throws Exception {
        synchronized (taskLock) {
            // 开始测绘
            if (isTaskRunning()) {
                return ResponseUtil.error("任务已在运行中，请勿重复启动");
            }

            surveyingLogService.deleteTable();

            int cjLogId = surveyingLogService.createSureyingLog("采集模块检测", DateTools.getCreateTime(), LogStatusType.init.getCode(), null, 1);
            if(!CFScanner()){
                surveyingLogService .updateSureyingLog(cjLogId, LogStatusType.FAIL.getCode());
                throw new Exception("采集模块检测出错");
            }else{
                Thread.sleep(5000);
                surveyingLogService.updateSureyingLog(cjLogId, LogStatusType.SUCCESS.getCode());
            }
            int scLogId = surveyingLogService.createSureyingLog("扫描模块检测", DateTools.getCreateTime(), LogStatusType.init.getCode(), null, 2);
            if(!existOSScannerFile()){
                surveyingLogService.updateSureyingLog(scLogId, LogStatusType.FAIL.getCode());
                throw new Exception("扫描模块检测出错");
            }else{
                Thread.sleep(5000);
                surveyingLogService.updateSureyingLog(scLogId, LogStatusType.SUCCESS.getCode());
            }

            Callable<Void> task = () -> {
                try {
                    // 实际任务逻辑
                    gatherData();
                } catch (InterruptedException e) {
                    log.info("任务被正常中断");
                    Thread.currentThread().interrupt(); // 恢复中断状态
                } catch (Exception e) {
                    log.error("任务执行异常", e);
                }
                return null;
            };

            runningTask = executorService.submit(task);
            log.info("任务启动成功");
            return ResponseUtil.error("任务已启动");
        }
    }


    /**
     * 实际业务方法 - 模拟数据采集
     */
    private void gatherData() throws InterruptedException {
        probeService.scanByTerminal();
    }

    // 检测扫描设备是否可用
    public boolean CFScanner(){
        // 1.查询程序是否存在
        // 2.判断程序是否可用
        String filePath = Global.cf_scanner_path;
        String fileName = Global.cf_scanner_name.replace("./", "");
        return fileExists(filePath, fileName);
    }


    public boolean existOSScannerFile() {
        for (int i = 1; i <= 5; i++) {
            String directoryPath = Global.os_scanner + i;
            String fileName = Global.os_scanner_name;
            // 构建完整的文件路径
            File directory = new File(directoryPath);
            File file = new File(directory, fileName.replace("./", ""));
            // 检查文件是否存在
            if (file.exists() && file.isFile() /*&& (fileName.contains(".exe") ? file.canExecute() : true)*/) {
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 停止任务
     */
    @GetMapping("/stop")
    public String stopTask(){
        synchronized (taskLock){
            if(!isTaskRunning()){
                return "没有任务在执行";
            }

            // 尝试取消任务（true表示终端正在执行的任务）
            boolean cancelled = runningTask.cancel(true);
            if(cancelled){
                log.info("任务取消成功");
            }else{
                log.warn("任务取消失败");
            }
            runningTask =  null;
            return "已发送停止请求";
        }
    }

    /**
     * 查看任务状态
     */
    public boolean isTaskRunning(){
        return runningTask != null && !runningTask.isDone();
    }


    // 查询文件是否存在
    public boolean fileExists(String filePath, String fileName){
        return Files.exists(Paths.get(filePath).resolve(fileName)); // 安全拼接路径
    }
}
