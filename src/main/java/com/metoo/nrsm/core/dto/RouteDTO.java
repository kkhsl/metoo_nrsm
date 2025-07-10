package com.metoo.nrsm.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.RouteEntry;
import lombok.Data;

import java.util.Date;

@Data
public class RouteDTO extends PageDto<RouteEntry> {
    private String uuid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private String name;
}