package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:10
 */
@ApiModel("")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dhcp extends IdEntity {

    private String dhcp;

    private String lease;
    private String starts;
    private String ends;
    private String tstp;
    private String cltt;
    private String binding_state;
    private String next_binding_state;
    private String rewind_binding_state;
    private String hardware_ethernet;
    private String uid;
    //    private String client_host_name;
    private String client_hostname;

    private String set_vendor_class_identifier;

}
