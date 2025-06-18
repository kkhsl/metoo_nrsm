package com.metoo.nrsm.core.config.utils.gather.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-25 10:57
 */
@Data
public class Parent {

    //    @Value("${py.version}")
    private String version = "python3";

    //    @Value("${py.path}")
    @ApiModelProperty("文件绝对路径")
    private String path = "/opt/sqlite/script/";

}
