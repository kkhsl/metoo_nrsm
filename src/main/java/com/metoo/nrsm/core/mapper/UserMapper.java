package com.metoo.nrsm.core.mapper;

import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.vo.UserVo;
import com.metoo.nrsm.entity.Group;
import com.metoo.nrsm.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据Username 查询一个User 对象
     *
     * @param username
     * @return
     */
    User findByUserName(String username);

    /**
     * 根据用户名查询所有角色
     *
     * @param username
     * @return
     */
    User findRolesByUserName(String username);

    User selectPrimaryKey(Long id);

    List<User> selectObjConditionQuery(UserDto dto);

    List<String> getObjByLevel(List<Group> leves);

    List<User> getObjsByLevel(String level);

    List<UserVo> query(UserDto dto);

    /**
     * 根据用户ID查询用户、角色组、角色信息
     *
     * @param id
     * @return
     */
    UserVo findUserUpdate(Long id);


    List<User> selectObjByMap(Map params);

    /**
     * 保存一个User对象
     *
     * @param user
     */
    int insert(User user);

    int update(User user);

    /**
     * 根据UserID删除一个User对象
     *
     * @param id
     * @return
     */
    int delete(Long id);

    boolean deleteByLevel(String level);

    boolean allocation(List<User> list);

    List<User> findObjByIds(Long[] ids);


}
