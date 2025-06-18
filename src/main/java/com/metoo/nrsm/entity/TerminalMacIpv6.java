package com.metoo.nrsm.entity;

import java.io.Serializable;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalMacIpv6 extends IdEntity {

    private String mac;

    private Integer isIPv6;

}