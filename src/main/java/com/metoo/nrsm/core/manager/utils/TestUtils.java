package com.metoo.nrsm.core.manager.utils;

import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.service.IVlanService;
import com.metoo.nrsm.entity.User;
import com.metoo.nrsm.entity.Vlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-04 15:08
 */
@Component
public class TestUtils {

    @Autowired
    private IUserService userService;
    @Autowired
    private IVlanService vlanService;

    @Transactional
    public void test(){

        // 更新用户表
        UserDto userDTO = new UserDto();
        userDTO.setUsername("testAdmin");
        this.userService.save(userDTO);
        // 更新用户表表
        User user = this.userService.findByUserName("testAdmin");
        user.setPassword("123456");
        this.userService.update(user);

    }
}
