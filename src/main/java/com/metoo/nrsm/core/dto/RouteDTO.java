package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.RouteEntry;
import lombok.Data;

@Data
public class RouteDTO extends PageDto<RouteEntry> {
    private String uuid;
    private String name;
}