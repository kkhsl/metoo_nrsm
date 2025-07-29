package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.mapper.RoleMapper;
import com.metoo.nrsm.core.mapper.UserMapper;
import com.metoo.nrsm.core.service.IRoleService;
import com.metoo.nrsm.core.service.IUserRoleService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.vo.UserVo;
import com.metoo.nrsm.entity.Role;
import com.metoo.nrsm.entity.User;
import com.metoo.nrsm.entity.UserRole;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private RoleMapper roleMapper;


    @Override
    public User findByUserName(String username) {
        return this.userMapper.findByUserName(username);
    }

    @Override
    public User findRolesByUserName(String username) {
        return null;
    }

    @Override
    public UserVo findUserUpdate(Long id) {
        return this.userMapper.findUserUpdate(id);
    }

    @Override
    public User findObjById(Long id) {
        return this.userMapper.selectPrimaryKey(id);
    }

    @Override
    public Page<User> selectObjConditionQuery(UserDto dto) {
        if (dto == null) {
            dto = new UserDto();
        }
        Page<User> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.userMapper.selectObjConditionQuery(dto);
        return page;
    }


    @Override
    public Page<UserVo> getObjsByLevel(UserDto dto) {
        Page<UserVo> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.userMapper.getObjsByLevel(dto.getGroupLevel());
        return page;
    }

    @Override
    public List<String> getObjByLevel(String level) {
        return null;
    }

    @Override
    public Page<UserVo> query(UserDto dto) {
        Page<UserVo> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.userMapper.query(dto);
        return page;
    }


    @Override
    public boolean save(UserDto dto) {
        User user = null;
        if (dto.getId() == null) {
            user = new User();
            dto.setAddTime(new Date());
        } else {
            user = this.userMapper.selectPrimaryKey(dto.getId());
        }
        BeanUtils.copyProperties(dto, user);
        // 查询组信息

        if (dto.getId() == null) {
            User currentUser = ShiroUserHolder.currentUser();
            Role roleByName = roleMapper.findRoleByName(currentUser.getUserRole());
        if (roleByName.getType()!=null){
            if (roleByName.getType().equals("1")){
                if (dto.getRole_id().length!=0){
                    return false;
                }else {
                    try {
                        this.userMapper.insert(user);

                        String roleName = "";
                        // 批量添加用户角色信息
                        if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                            List<Integer> idList = Arrays.asList(dto.getRole_id());
                            List<Role> roleList = this.roleService.findRoleByIdList(idList);
                            List<UserRole> userRoles = new ArrayList<UserRole>();
                            for (Role role : roleList) {
                                UserRole userRole = new UserRole();
                                userRole.setUser_id(user.getId());
                                userRole.setRole_id(role.getId());
                                userRoles.add(userRole);
                                roleName += role.getName() + ",";
                            }
                            roleName = roleName.substring(0, roleName.lastIndexOf(","));
                            this.userRoleService.batchAddUserRole(userRoles);
                        }
                        try {
                            user.setUserRole(roleName);
                            this.userMapper.update(user);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }else if(roleByName.getType().equals("2")){
                return false;
            }else {
                try {
                    this.userMapper.insert(user);

                    String roleName = "";
                    // 批量添加用户角色信息
                    if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                        List<Integer> idList = Arrays.asList(dto.getRole_id());
                        List<Role> roleList = this.roleService.findRoleByIdList(idList);
                        List<UserRole> userRoles = new ArrayList<UserRole>();
                        for (Role role : roleList) {
                            UserRole userRole = new UserRole();
                            userRole.setUser_id(user.getId());
                            userRole.setRole_id(role.getId());
                            userRoles.add(userRole);
                            roleName += role.getName() + ",";
                        }
                        roleName = roleName.substring(0, roleName.lastIndexOf(","));
                        this.userRoleService.batchAddUserRole(userRoles);
                    }
                    try {
                        user.setUserRole(roleName);
                        this.userMapper.update(user);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }else {
            try {
                this.userMapper.insert(user);

                String roleName = "";
                // 批量添加用户角色信息
                if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                    List<Integer> idList = Arrays.asList(dto.getRole_id());
                    List<Role> roleList = this.roleService.findRoleByIdList(idList);
                    List<UserRole> userRoles = new ArrayList<UserRole>();
                    for (Role role : roleList) {
                        UserRole userRole = new UserRole();
                        userRole.setUser_id(user.getId());
                        userRole.setRole_id(role.getId());
                        userRoles.add(userRole);
                        roleName += role.getName() + ",";
                    }
                    roleName = roleName.substring(0, roleName.lastIndexOf(","));
                    this.userRoleService.batchAddUserRole(userRoles);
                }
                try {
                    user.setUserRole(roleName);
                    this.userMapper.update(user);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        } else {
            User currentUser = ShiroUserHolder.currentUser();
            Role roleByName = roleMapper.findRoleByName(currentUser.getUserRole());
            if (roleByName.getType().equals("1")){
                List<Integer> idList1 = Arrays.asList(dto.getRole_id());
                if (idList1.size()==0 && userMapper.findUserUpdate(dto.getId()).getType()==null){
                    try {
                        String roleName = "";

                        // 批量添加用户角色信息
                        if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                            // 清除用户角色信息
                            this.userRoleService.deleteUserByRoleId(user.getId());
                            List<Integer> idList = Arrays.asList(dto.getRole_id());
                            List<Role> roleList = this.roleService.findRoleByIdList(idList);
                            List<UserRole> userRoles = new ArrayList<UserRole>();
                            for (Role role : roleList) {
                                UserRole userRole = new UserRole();
                                userRole.setUser_id(user.getId());
                                userRole.setRole_id(role.getId());
                                userRoles.add(userRole);
                                roleName += role.getName() + ",";
                            }
                            roleName = roleName.substring(0, roleName.lastIndexOf(","));
                            this.userRoleService.batchAddUserRole(userRoles);
                        }
                        user.setUserRole(roleName);
                        this.userMapper.update(user);


                        if (dto.isFlag() && currentUser.getId().equals(user.getId())) {
                            SecurityUtils.getSubject().logout();
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }else {
                    String name = roleMapper.findRoleById(Long.valueOf(idList1.get(0))).getName();
                    UserVo userUpdate = userMapper.findUserUpdate(dto.getId());
                    if (name.equals(userUpdate.getType())){
                        try {
                            String roleName = "";

                            // 批量添加用户角色信息
                            if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                                // 清除用户角色信息
                                this.userRoleService.deleteUserByRoleId(user.getId());
                                List<Integer> idList = Arrays.asList(dto.getRole_id());
                                List<Role> roleList = this.roleService.findRoleByIdList(idList);
                                List<UserRole> userRoles = new ArrayList<UserRole>();
                                for (Role role : roleList) {
                                    UserRole userRole = new UserRole();
                                    userRole.setUser_id(user.getId());
                                    userRole.setRole_id(role.getId());
                                    userRoles.add(userRole);
                                    roleName += role.getName() + ",";
                                }
                                roleName = roleName.substring(0, roleName.lastIndexOf(","));
                                this.userRoleService.batchAddUserRole(userRoles);
                            }
                            user.setUserRole(roleName);
                            this.userMapper.update(user);


                            if (dto.isFlag() && currentUser.getId().equals(user.getId())) {
                                SecurityUtils.getSubject().logout();
                            }

                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }else {
                        return false;
                    }
                }
            } else if (roleByName.getType().equals("2")){
                UserVo userUpdate = userMapper.findUserUpdate(dto.getId());
                if (!dto.getPassword().equals("") || !userUpdate.getUsername().equals(dto.getUsername()) || !userUpdate.getUnitId().equals(dto.getUnitId())){
                    return false;
                }else {
                    try {
                        String roleName = "";

                        // 批量添加用户角色信息
                        if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                            // 清除用户角色信息
                            this.userRoleService.deleteUserByRoleId(user.getId());
                            List<Integer> idList = Arrays.asList(dto.getRole_id());
                            List<Role> roleList = this.roleService.findRoleByIdList(idList);
                            List<UserRole> userRoles = new ArrayList<UserRole>();
                            for (Role role : roleList) {
                                UserRole userRole = new UserRole();
                                userRole.setUser_id(user.getId());
                                userRole.setRole_id(role.getId());
                                userRoles.add(userRole);
                                roleName += role.getName() + ",";
                            }
                            roleName = roleName.substring(0, roleName.lastIndexOf(","));
                            this.userRoleService.batchAddUserRole(userRoles);
                        }
                        user.setUserRole(roleName);
                        this.userMapper.update(user);


                        if (dto.isFlag() && currentUser.getId().equals(user.getId())) {
                            SecurityUtils.getSubject().logout();
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } else {
                try {
                    String roleName = "";

                    // 批量添加用户角色信息
                    if (dto.getRole_id() != null && dto.getRole_id().length > 0) {
                        // 清除用户角色信息
                        this.userRoleService.deleteUserByRoleId(user.getId());
                        List<Integer> idList = Arrays.asList(dto.getRole_id());
                        List<Role> roleList = this.roleService.findRoleByIdList(idList);
                        List<UserRole> userRoles = new ArrayList<UserRole>();
                        for (Role role : roleList) {
                            UserRole userRole = new UserRole();
                            userRole.setUser_id(user.getId());
                            userRole.setRole_id(role.getId());
                            userRoles.add(userRole);
                            roleName += role.getName() + ",";
                        }
                        roleName = roleName.substring(0, roleName.lastIndexOf(","));
                        this.userRoleService.batchAddUserRole(userRoles);
                    }
                    user.setUserRole(roleName);
                    this.userMapper.update(user);


                    if (dto.isFlag() && currentUser.getId().equals(user.getId())) {
                        SecurityUtils.getSubject().logout();
                    }

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
    }

    @Override
    public boolean update(User user) {
        try {
            this.userMapper.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(User user) {
        // 清除用户角色
        try {
            this.userRoleService.deleteUserByRoleId(user.getId());
            this.userMapper.delete(user.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteByLevel(String level) {
        return this.userMapper.deleteByLevel(level);
    }

    @Override
    public boolean allocation(List<User> list) {
        try {
            this.userMapper.allocation(list);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<User> findObjByIds(Long[] ids) {
        return this.userMapper.findObjByIds(ids);
    }

    @Override
    public List<User> selectObjByMap(Map params) {
        return this.userMapper.selectObjByMap(params);
    }


}
