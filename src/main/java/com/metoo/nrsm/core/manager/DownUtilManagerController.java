package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.utils.file.DownLoadFileUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-04 10:45
 */
@RequestMapping("/file")
@RestController
public class DownUtilManagerController {

//    @RequestMapping("/down")
//    public void down(HttpServletRequest request, HttpServletResponse response){
//        String fileName = "1.1.1.2__20240129190749(a374d91ae312406b88b8c57c4fe8b7fd).xlsx";
//        File folder = new File("C:\\Users\\Administrator\\Desktop\\metoo");
//        if (folder.exists()) {
//            File[] files = folder.listFiles();
//            for (File file : files) {
//                if (file.getName().contains(fileName)) {
//                    boolean flag = DownLoadFileUtil.downloadZip(file, response);
//                    break;
//                }
//            }
//        }
//
//    }
}
