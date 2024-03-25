package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.UserRole;

import java.util.List;

public interface IUserRoleService {

    int batchAddUserRole(List<UserRole> userRoles);

    boolean deleteUserByRoleId(Long id);
}
