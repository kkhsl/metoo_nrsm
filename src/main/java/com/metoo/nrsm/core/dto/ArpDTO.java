package com.metoo.nrsm.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Arp;
import lombok.Data;

import java.util.Date;

@Data
public class ArpDTO extends PageDto<Arp> {
    private String uuid;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
}