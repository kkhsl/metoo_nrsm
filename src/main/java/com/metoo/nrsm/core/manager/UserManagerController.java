package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.SaltUtils;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.service.IRoleService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.utils.CommUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.UserVo;
import com.metoo.nrsm.entity.Role;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("用户管理")
@RequestMapping("/admin/user")
@RestController
public class UserManagerController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IUnitService unitService;

    Logger logger = LoggerFactory.getLogger(UserManagerController.class);

    @ApiOperation("用户列表")
    @PostMapping("/list")
    private Object list(@RequestBody(required = false) UserDto dto) {
        if (dto == null) {
            dto = new UserDto();
        }
        Page<User> page = this.userService.selectObjConditionQuery(dto);
        if (page.getResult().size() > 0) {
            for (User user : page.getResult()) {
                Unit unit2 = this.unitService.selectObjById(user.getUnitId());
                if (unit2 != null) {
                    user.setUnitName(unit2.getUnitName());
                }
            }
            return ResponseUtil.ok(new PageInfo<User>(page));
        }
        return ResponseUtil.ok();
    }

    @ApiOperation("用户添加")
    @GetMapping("/add")
    public Object add() {
        Map params = new HashMap();
        Map data = new HashMap();
        params.put("currentPage", 0);
        params.put("pageSize", 1000);
        List<Role> roleList = this.roleService.findObjByMap(params);
        if (roleList.size() > 0) {
            data.put("roleList", roleList);
        }
        List<Unit> unit2s = this.unitService.selectUnitAll();
        data.put("unitList", unit2s);
        return ResponseUtil.ok(data);
    }

    //    @RequiresPermissions(value = {"LK:USER", "LK:USER:MANAGER"})
    @ApiOperation("用户更新")
    @PostMapping("/update")
    public Object update(@RequestBody(required = false) UserDto dto) {
        User user = this.userService.findObjById(dto.getId());
        if (user != null) {
            Map data = new HashMap();
            /*List<Role> userRoleList = this.roleService.findRoleByUserId(user.getId());
            data.put("userRoleList",userRoleList);*/
            UserVo obj = this.userService.findUserUpdate(user.getId());
            data.put("obj", obj);
            Map params = new HashMap();
            params.put("currentPage", 0);
            params.put("pageSize", 1000);
            List<Role> roleList = this.roleService.findObjByMap(params);
            if (roleList.size() > 0) {
                data.put("roleList", roleList);
            }
            List<Unit> unit2s = this.unitService.selectUnitAll();
            data.put("unitList", unit2s);
            return ResponseUtil.ok(data);
        }
        return ResponseUtil.badArgument();
    }

    @ApiOperation("用户保存")
    @PostMapping("/save")
    public Object save(@RequestBody UserDto dto) {
        if (dto != null && dto.getId() != null) {
            if (dto.getUsername() != null && !dto.getUsername().equals("")) {
                User currentUser = ShiroUserHolder.currentUser();
                User user = this.userService.findByUserName(dto.getUsername());
                if (user != null) {
                    if (!currentUser.getUsername().equals(dto.getUsername())) {// 修改用户名强制退出已登录用户
                        dto.setFlag(true);
                    }
                    if (currentUser.getUsername().equals(dto.getUsername())
                            && currentUser.getSex().equals(dto.getSex())
                            && currentUser.getAge().equals(dto.getAge())
                            && StringUtils.isEmpty(dto.getPassword())
                            && StringUtils.isEmpty(dto.getVerifyPassword())) {
                        return ResponseUtil.ok();
                    }
                    if (!user.getId().equals(currentUser.getId())) {
                        return ResponseUtil.fail(400, "用户已存在");
                    }
                } else {
                    dto.setFlag(true);
                }
                if (!StringUtils.isEmpty(dto.getPassword()) || !StringUtils.isEmpty(dto.getVerifyPassword())) {
                    String oldPassword = CommUtils.password(dto.getOldPassword(), currentUser.getSalt());
                    if (!currentUser.getPassword().equals(oldPassword)) {
                        return ResponseUtil.badArgument("旧密码和原始密码不一致");
                    }
                    String newPassword = CommUtils.password(dto.getPassword(), currentUser.getSalt());
                    if (newPassword.equals(oldPassword)) {
                        return ResponseUtil.badArgument("新密码不能和旧密码相同");
                    }
                    if (!dto.getPassword().equals(dto.getVerifyPassword())) {
                        return ResponseUtil.badArgument("新密码和确认密码不一致");
                    }
                    if (dto.getPassword().length() < 6 || dto.getPassword().length() > 20) {
                        return ResponseUtil.badArgument("设置6-20位新密码");
                    }
                    String sale = SaltUtils.getSalt(8);
                    dto.setPassword(CommUtils.password(dto.getPassword(), sale));
                    dto.setSalt(sale);
                    dto.setFlag(true);
                }
                if (dto.getUnitId() == null || "".equals(dto.getUnitId())) {
                    return ResponseUtil.badArgument("请选择所属单位");
                } else {
                    Unit unit = this.unitService.selectObjById(dto.getUnitId());
                    if (unit == null) {
                        return ResponseUtil.badArgument("选择所属单位不存在");
                    }
                }
                if (this.userService.save(dto)) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error();
            }
            return ResponseUtil.badArgument("请输入用户名");
        }
        return ResponseUtil.badArgument("参数错误");
    }

    //    @RequiresPermissions(value = {"LK:USER:MANAGER"})
    @ApiOperation("创建用户")
    @PostMapping("/create")
    public Object persionalSave(@RequestBody UserDto dto) {
        if (dto != null) {
            if (StringUtil.isEmpty(dto.getUsername())) {// 新增时必须验证密码
                return ResponseUtil.badArgument("请输入用户名");
            }
            if (dto.getId() != null && dto.getPassword() == null && dto.getVerifyPassword() == null) {
                return ResponseUtil.badArgument("参数错误");
            }

            // 验证密码参数
            if (dto.getId() == null) {
                if (dto.getId() == null && StringUtils.isEmpty(dto.getPassword())) {
                    return ResponseUtil.badArgument("请输入密码");
                }
                if (dto.getId() == null && StringUtils.isEmpty(dto.getVerifyPassword())) {
                    return ResponseUtil.badArgument("请输入确认密码");
                }
            } else {
                if (!StringUtils.isEmpty(dto.getPassword())) {
                    if (StringUtils.isEmpty(dto.getVerifyPassword())) {
                        return ResponseUtil.badArgument("请输入确认密码");
                    }
                }
                if (!StringUtils.isEmpty(dto.getVerifyPassword())) {
                    if (StringUtils.isEmpty(dto.getPassword())) {
                        return ResponseUtil.badArgument("请输入密码");
                    }
                }
            }

            User user = this.userService.findByUserName(dto.getUsername());
            if (user != null) {
                User currentUser = this.userService.findObjById(dto.getId());
                if (currentUser != null) {// 判断修改时是否为本人
                    if (!user.getId().equals(currentUser.getId())) {
                        return ResponseUtil.fail(400, "用户已存在");
                    }
                } else {
                    return ResponseUtil.fail(400, "用户已存在");
                }
            }
            if (!StringUtils.isEmpty(dto.getPassword())) {
                if (dto.getPassword().length() < 6 || dto.getPassword().length() > 20) {
                    return ResponseUtil.badArgument("设置6-20位新密码");
                } else {
                    String sale = SaltUtils.getSalt(8);
                    String password = CommUtils.password(dto.getPassword(), sale);
                    dto.setPassword(password);
                    dto.setSalt(sale);
                }
            }

            if (dto.getUnitId() == null || "".equals(dto.getUnitId())) {
                return ResponseUtil.badArgument("请选择所属单位");
            } else {
                Unit unit = this.unitService.selectObjById(dto.getUnitId());
                if (unit == null) {
                    return ResponseUtil.badArgument("选择所属单位不存在");
                }
            }
            if (this.userService.save(dto)) {
                // 判断是否为本人
                User currentUser = ShiroUserHolder.currentUser();
                if (currentUser.getUsername().equals(dto.getUsername())) {
                    SecurityUtils.getSubject().logout();
                }/*else{
                    // 退出指定用户

                }*/
                return ResponseUtil.ok();
            }
            return ResponseUtil.error();
        }
        return ResponseUtil.badArgument();
    }


    //    @RequiresPermissions("ADMIN:USER:DELETE")
//    @RequiresPermissions(value = {"LK:USER:MANAGER"})
    @ApiOperation("用户删除")
    @RequestMapping("/delete")
    public Object delete(@RequestBody UserDto dto) {
        User user = this.userService.findObjById(dto.getId());
        if (user != null) {
            // 判断用户是否为管理员
            if (user.getType() == 1) {
                return ResponseUtil.badArgument("删除失败");
            }
            user.setDeleteStatus(-1);
            this.userService.update(user);
            // 清空用户直播间
            Map params = new HashMap();
            params.put("pageSize", 0);
            params.put("currentPage", 0);
            params.put("userId", user.getId());


            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    //    @RequiresPermissions(value = {"LK:USER"}) // 创建帐号应分配个人中心权限
    @ApiOperation("个人中心")
    @RequestMapping("/personal")
    public Object personal() {
        User user = ShiroUserHolder.currentUser();
        if (user == null) {
            return ResponseUtil.unlogin();
        }
        Unit unit2 = this.unitService.selectObjById(user.getUnitId());
        if (unit2 != null) {
            user.setUnitName(unit2.getUnitName());
        }
        return ResponseUtil.ok(user);
    }

    @ApiOperation("修改密码")
    @RequestMapping("/editPassword")
    public Object editPaswork(@RequestBody UserDto dto) {
        if (dto != null && dto.getId() != null) {
            User currentUser = ShiroUserHolder.currentUser();
            User user = this.userService.findObjById(dto.getId());
            if (currentUser.getUsername().equals(user.getUsername())) {
                if (!StringUtils.isEmpty(dto.getPassword()) || !StringUtils.isEmpty(dto.getVerifyPassword())) {
                    String oldPassword = CommUtils.password(dto.getOldPassword(), currentUser.getSalt());
                    if (!currentUser.getPassword().equals(oldPassword)) {
                        return ResponseUtil.badArgument("旧密码和原始密码不一致");
                    }
                    String newPassword = CommUtils.password(dto.getPassword(), currentUser.getSalt());
                    if (newPassword.equals(oldPassword)) {
                        return ResponseUtil.badArgument("新密码不能和旧密码相同");
                    }
                    if (!dto.getPassword().equals(dto.getVerifyPassword())) {
                        return ResponseUtil.badArgument("新密码和确认密码不一致");
                    }
                    if (dto.getPassword().length() < 6 || dto.getPassword().length() > 20) {
                        return ResponseUtil.badArgument("设置6-20位新密码");
                    }
                    String sale = SaltUtils.getSalt(8);
                    dto.setPassword(CommUtils.password(dto.getPassword(), sale));
                    dto.setSalt(sale);
                    dto.setFlag(true);
                    if (this.userService.save(dto)) {
                        Subject subject = SecurityUtils.getSubject();
                        subject.logout();
                        return ResponseUtil.ok();
                    }
                } else {

                    return ResponseUtil.badArgument("请输入密码或确认密码");
                }
            }
            return ResponseUtil.badArgument();

        }
        return ResponseUtil.badArgument("请输入用户ID");
    }


}
