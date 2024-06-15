package com.metoo.nrsm.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel("拓扑终端")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCount extends IdEntity {

    private Integer v4ip_count;
    private Integer v6ip_count;
    private Integer v4ip_v6ip_count;

}
