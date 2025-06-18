package com.metoo.nrsm.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.FlowStatistics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-03 9:45
 */
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class FlowStatisticsDTO  extends PageDto<FlowStatistics> {

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date startTime;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date endTime;
}
