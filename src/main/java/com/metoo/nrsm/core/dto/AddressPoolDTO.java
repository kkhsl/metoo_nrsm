package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.AddressPool;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-22 10:20
 */
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class AddressPoolDTO extends PageDto<AddressPoolIpv6> {
    private Long id;
    private Date addTime;
    private String username;
    private String password;
    private String verifyPassword;
    private String oldPassword;
    @ApiModelProperty("用户年龄")
    private Integer age;
    @ApiModelProperty("用户性别")
    private Integer sex;
    private String salt;
    @ApiModelProperty("角色ID字符串")
    private Integer[] role_id;
    @ApiModelProperty("默认0：普通用户 1：管理员 ")
    private String type;
    @ApiModelProperty("是否强制退出用户")
    private boolean flag;
    @ApiModelProperty("用户邮箱")
    private String email;
    @ApiModelProperty("手机号码")
    private String mobile;
    @ApiModelProperty("备注")
    private String note;
    @ApiModelProperty("组ID")
    private Long groupId;
    private Long[] userIds;
    private String groupLevel;


}
