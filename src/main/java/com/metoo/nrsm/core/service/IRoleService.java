package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.RoleDto;
import com.metoo.nrsm.entity.Role;

import java.util.List;
import java.util.Map;

public interface IRoleService {

    /**
     * 根据角色id查询指定角色
     * @param id
     * @return
     */
    Role findRoleById(Long id);

    Role findObjByName(String name);

    List<Role> findRoleByType(String type);

    /**
     *根据用户id查询用所有角色信息
     * @param user_id
     * @return
     */
    List<Role> findRoleByUserId(Long user_id);

    List<Role> findRoleByRoleGroupId(Long role_group_id);

    List<Role> findRoleByResId(Long res_id);

    List<Role> findRoleByIdList(List<Integer> list);

    Role selectByPrimaryUpdae(Long id);

    boolean save(RoleDto instance);

    Object update(RoleDto instance);

    Page<Role> query(RoleDto dto);

    List<Role> findObjByMap(Map params);

    boolean delete(Long id);

    int batchUpdateRoleGroupId(Map map);
}
