package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class BackupSql extends IdEntity {

    @ApiModelProperty("文件名称")
    private String name;
    @ApiModelProperty("文件大小")
    private String size;

}
