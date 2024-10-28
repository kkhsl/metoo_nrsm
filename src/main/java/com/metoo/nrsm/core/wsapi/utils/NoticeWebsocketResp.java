package com.metoo.nrsm.core.wsapi.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-11 11:12
 */

@Data
@ApiModel("ws通知返回对象")
public class NoticeWebsocketResp<T> {

    @ApiModelProperty(value = "通知状态")
    private Integer noticeStatus;

    @ApiModelProperty(value = "通知类型")
    private String noticeType;

    @ApiModelProperty(value = "通知内容")
    private T noticeInfo;
}
