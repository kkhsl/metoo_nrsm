package com.metoo.nrsm.core.service.impl;

import com.metoo.nrsm.core.mapper.UserRoleMapper;
import com.metoo.nrsm.core.service.IUserRoleService;
import com.metoo.nrsm.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserRoleServiceImpl implements IUserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public int batchAddUserRole(List<UserRole> userRoles) {
        return this.userRoleMapper.batchAddUserRole(userRoles);
    }

    @Override
    public boolean deleteUserByRoleId(Long id) {
        try {
            this.userRoleMapper.deleteUserByRoleId(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
