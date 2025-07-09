package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Arp;
import lombok.Data;

@Data
public class ArpDTO extends PageDto<Arp> {
    private String uuid;
    private String name;
}