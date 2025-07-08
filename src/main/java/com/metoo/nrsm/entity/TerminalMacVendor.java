package com.metoo.nrsm.entity;

import java.io.Serializable;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName metoo_terminal_mac_vendor
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalMacVendor extends IdEntity {

    private Long terminalTypeId;

    private String vendor;

}