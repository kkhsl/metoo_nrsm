package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.manager.utils.TestUtils;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@RequestMapping("/admin/test")
@RestController
public class TestController {


    // 周末和休息时间（17:周末和休息时间（17:30-8:30）按上述范围的1/10取值30-8:30）按上述范围的1/10取值
    public String getlow(double min, double max) {

        Random random = new Random();

        // Generate a random double between min (inclusive) and max (exclusive)
        double randomValue = min + (max - min) * random.nextDouble();

        // Divide randomValue by ten
        double result = randomValue;

        System.out.println("isWeekend()" + isWeekend());

        System.out.println("timeCheck()" + timeCheck());

        if(isWeekend() || timeCheck()){
            result = result / 10.0;
        }

        // Format result to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedResult = df.format(result);
        return formattedResult;
    }

    // 封装判断是否为周末的方法
    public static boolean isWeekend() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    // 17:30-8:30

    @Test
    public void isInTime() {
//        LocalTime currentTime = LocalTime.now();
        LocalTime currentTime = LocalTime.of(8, 30);
        LocalTime endTime = LocalTime.of(17, 30);

        if (isAfter1730(currentTime) || isBefore830(currentTime)) {
            System.out.println("当前时间在17:30之后或8:30之前");
        } else {
            System.out.println("当前时间不在指定范围内");
        }
    }

    public boolean timeCheck(){
        LocalTime currentTime = LocalTime.now();
        if (isAfter1730(currentTime) || isBefore830(currentTime)) {
            System.out.println("当前时间在17:30之后或8:30之前");
            return true;
        } else {
            System.out.println("当前时间不在指定范围内");
            return false;
        }
    }

    public static boolean isAfter1730(LocalTime currentTime) {
        LocalTime endTime = LocalTime.of(17, 30);
        return currentTime.isAfter(endTime);
    }

    public static boolean isBefore830(LocalTime currentTime) {
        LocalTime startTime = LocalTime.of(8, 30);
        return currentTime.isBefore(startTime);
    }
}


