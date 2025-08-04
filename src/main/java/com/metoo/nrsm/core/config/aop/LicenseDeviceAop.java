package com.metoo.nrsm.core.config.aop;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ILicenseService;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.entity.License;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Aspect
@Component
public class LicenseDeviceAop {

    @Autowired
    private ILicenseService licenseServicer;
    @Autowired
    private INetworkElementService networkElementService;

    @Around("execution(* com.metoo.nrsm.core.manager.NetworkElementManagerController.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {
            // 检查是否有 MultipartFile 参数
            MultipartFile file = null;
            for (Object arg : pjp.getArgs()) {
                if (arg instanceof MultipartFile) {
                    file = (MultipartFile) arg;
                    break;
                }
            }

            License obj = this.licenseServicer.query().get(0);
            String code = AesEncryptUtils.decrypt(obj.getLicense());
            LicenseVo license = JSONObject.parseObject(code, LicenseVo.class);

            Signature signature = pjp.getSignature();
            String methodName = signature.getName();

            List list = this.networkElementService.selectObjAll();
            switch (methodName) {
                case "save":
                    if (list.size() > license.getLicenseDevice()) {
                        return ResponseUtil.error("授权设备数量已达到最大授权数，禁止上传");
                    }
                    break;
                case "importExcel":
                    // Excel 条数默认为第一个参数
                    // 获取 Excel 文件的数据条目数
                    int excelRowCount = getExcelRowCount(file);
                    log.info("导入的 Excel 条数：{}", excelRowCount);

                    // 这里可以进行进一步的处理，比如记录日志等
                    if (list.size() + excelRowCount > license.getLicenseDevice()) {
                        return ResponseUtil.error("授权设备数量已达到最大授权数，禁止上传");
                    }
                default:
                    return pjp.proceed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return pjp.proceed();
    }

    private int getExcelRowCount(MultipartFile file) {
        int rowCount = -1;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 获取第一个 Sheet
            Sheet sheet = workbook.getSheetAt(0);

            // 遍历每一行
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // 如果行为空，则跳过
                if (row == null) {
                    continue;
                }
                // 遍历每一列
                Iterator<Cell> cellIterator = row.cellIterator();
                boolean emptyRow = true;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    // 如果单元格不为空，则该行不为空行
                    if (cell != null && cell.getCellType() != CellType.BLANK.getCode()) {
                        emptyRow = false;
                        break;
                    }
                }
                // 如果不是空行，则增加行数
                if (!emptyRow) {
                    rowCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowCount;
    }
}
