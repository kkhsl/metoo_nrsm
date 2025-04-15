package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 10:41
 */
@ApiModel("流量配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FluxConfig extends IdEntity {

    private String name;

    private String ipv4;

    private String ipv6;

    private String community;

    private String version;

    private String ipv4Oid;

    private List<List<String>> ipv4Oids = new ArrayList<>();

    private String ipv6Oid;

    private List<List<String>> ipv6Oids = new ArrayList<>();

    private Integer update;

}
