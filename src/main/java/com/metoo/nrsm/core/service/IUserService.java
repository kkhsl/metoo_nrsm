package com.metoo.nrsm.core.service;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.vo.UserVo;
import com.metoo.nrsm.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserService {

    /**
     * 根据Username 查询一个User 对象
     *
     * @param username
     * @return
     */
    User findByUserName(String username);

    User findRolesByUserName(String username);

    UserVo findUserUpdate(Long id);

    User findObjById(Long id);

    Page<User> selectObjConditionQuery(UserDto dto);

    Page<UserVo> getObjsByLevel(UserDto dto);

    List<String> getObjByLevel(String level);

    Page<UserVo> query(UserDto dto);

    void operationLog(String username, String roleName, String des);

    boolean save(UserDto dto);

    boolean update(User user);

    boolean delete(User id);

    boolean deleteByLevel(String level);

    boolean allocation(List<User> list);

    List<User> findObjByIds(Long[] ids);

    List<User> selectObjByMap(Map params);


}
