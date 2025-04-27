package com.metoo.nrsm.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DnsFilterStatePayload {
    private String domain;
    private boolean enable;
}