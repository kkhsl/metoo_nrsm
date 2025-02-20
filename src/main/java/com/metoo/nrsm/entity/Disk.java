package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Disk extends IdEntity {

    private String rootDirectory;
    private long totalSpace;
    private long usableSpace;
    private long freeSpace;
    private long usedSpace;
}
