package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;


@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class NetworkElementDto extends PageDto<NetworkElement> {

    private String filter;
    private Long id;
    private Integer deleteStatus;
    private String ip;
    private String deviceName;
    private Long groupId;
    private Set<Long> groupIds;
    private String groupName;
    private Long deviceTypeId;
    private String deviceTypeName;
    private String vendorId;
    private String vendorName;
    private String description;
    private Long userId;
    private String userName;
    private boolean display;

}
