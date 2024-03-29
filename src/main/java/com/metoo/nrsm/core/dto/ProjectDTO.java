package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;


@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO extends PageDto<Project> {

    private Long id;
    private String name;
    private Date startTime;
    private Date acceptTime;
    private String personLiable;
    private String description;
    private Long userId;
    private String userName;
}
