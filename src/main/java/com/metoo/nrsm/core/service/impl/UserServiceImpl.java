package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.mapper.RoleMapper;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.mapper.UserMapper;
import com.metoo.nrsm.core.service.IOperationLogService;
import com.metoo.nrsm.core.service.IRoleService;
import com.metoo.nrsm.core.service.IUserRoleService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.vo.UserVo;
import com.metoo.nrsm.entity.OperationLog;
import com.metoo.nrsm.entity.Role;
import com.metoo.nrsm.entity.User;
import com.metoo.nrsm.entity.UserRole;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

    @Resource
    private UnitMapper unitMapper;

    @Autowired
    @Lazy
    private IOperationLogService operationLogService;


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
    public void operationLog(String username,String roleName,String des) {
        try {
            User currentUser = ShiroUserHolder.currentUser();

            // 获取当前请求的 HttpServletRequest 对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            // 检查是否在 Web 请求上下文中
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                OperationLog instance = new OperationLog();
                instance.setAccount(username);
                instance.setName(roleName);
                instance.setDesc(des);
                if (currentUser.getUnitId()!=null){
                    instance.setDM(String.valueOf(currentUser.getUnitId()));  //unitId
                    instance.setMC(unitMapper.selectObjById(currentUser.getUnitId()).getUnitName());  //unitName
                }

                // 从请求对象中获取客户端 IP
                String clientIP = getClientIP(request);
                instance.setIp(clientIP); // 假设 setIp 接收 String 参数

                this.operationLogService.saveOperationLog(instance);
            } else {


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取客户端真实 IP 的方法（处理代理转发）
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多级代理的情况（取第一个真实 IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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
                    operationLog(currentUser.getUsername(),roleByName.getName(),"无权限新增用户："+dto);
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

                            operationLog(currentUser.getUsername(),roleByName.getName(),"成功新增用户："+dto);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            operationLog(currentUser.getUsername(),roleByName.getName(),"新增用户失败："+dto);
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        operationLog(currentUser.getUsername(),roleByName.getName(),"新增用户失败："+dto);
                        return false;
                    }
                }
            }else if(roleByName.getType().equals("2")){
                operationLog(currentUser.getUsername(),roleByName.getName(),"无权限新增用户："+dto);
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
                        operationLog(currentUser.getUsername(),roleByName.getName(),"成功新增用户："+dto);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        operationLog(currentUser.getUsername(),roleByName.getName(),"新增用户失败："+dto);
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    operationLog(currentUser.getUsername(),roleByName.getName(),"新增用户失败："+dto);
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
                    operationLog(currentUser.getUsername(),roleByName.getName(),"成功新增用户："+dto);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    operationLog(currentUser.getUsername(),roleByName.getName(),"新增用户失败："+dto);
                    return false;

                }
            } catch (Exception e) {
                e.printStackTrace();
                operationLog(currentUser.getUsername(),roleByName.getName(),"新增用户失败："+dto);
                return false;
            }
        }

        } else {
            //修改
            User currentUser = ShiroUserHolder.currentUser();
            Role roleByName = roleMapper.findRoleByName(currentUser.getUserRole());
            if (roleByName.getType().equals("1")){
                List<Integer> idList1 = Arrays.asList(dto.getRole_id());
                if (idList1.size()==0 && userMapper.findUserUpdate(dto.getId()).getUserRole()==null){
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
                        operationLog(currentUser.getUsername(),roleByName.getName(),"成功修改用户："+dto);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }else {
                    String name = roleMapper.findRoleById(Long.valueOf(idList1.get(0))).getName();
                    UserVo userUpdate = userMapper.findUserUpdate(dto.getId());
                    if (name.equals(userUpdate.getUserRole())){
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
                            operationLog(currentUser.getUsername(),roleByName.getName(),"成功修改用户："+dto);

                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            operationLog(currentUser.getUsername(),roleByName.getName(),"修改用户失败："+dto);
                            return false;
                        }
                    }else {
                        operationLog(currentUser.getUsername(),roleByName.getName(),"无权限修改用户："+dto);
                        return false;
                    }
                }
            } else if (roleByName.getType().equals("2")){
                UserVo userUpdate = userMapper.findUserUpdate(dto.getId());
                if (!dto.getPassword().equals("") || !userUpdate.getUsername().equals(dto.getUsername()) || !userUpdate.getUnitId().equals(dto.getUnitId())){
                    operationLog(currentUser.getUsername(),roleByName.getName(),"无权限修改用户："+dto);
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
                        operationLog(currentUser.getUsername(),roleByName.getName(),"成功修改用户："+dto);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        operationLog(currentUser.getUsername(),roleByName.getName(),"修改用户失败："+dto);
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
                    operationLog(currentUser.getUsername(),roleByName.getName(),"成功修改用户："+dto);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    operationLog(currentUser.getUsername(),roleByName.getName(),"修改用户失败："+dto);
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
