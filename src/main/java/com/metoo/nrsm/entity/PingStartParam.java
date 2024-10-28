package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-19 9:56
 */
@ApiModel("")
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class PingStartParam {

    private Long id;
    private String param;
}
