package com.metoo.nrsm.core.view;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.RoleMapper;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.IOperationLogService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.utils.CaptchaUtil;
import com.metoo.nrsm.core.utils.CookieUtil;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.OperationLog;
import com.metoo.nrsm.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@Api(description = "登录控制器")
@RestController
@RequestMapping(value = "/buyer")
public class LoginController {

    Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private IUserService userService;
    @Autowired
    private IOperationLogService operationLogService;

    @Resource
    private UnitMapper unitMapper;

    @Autowired
    private RoleMapper roleMapper;

    @ApiOperation("登录")
    @RequestMapping("/login")
    public Object login(HttpServletRequest request, HttpServletResponse response,
                        String username, String password, @ApiParam("验证码") String captcha, String isRememberMe) {
        String msg = "";
        // 通过安全工具类获取 Subject
        Subject subject = SecurityUtils.getSubject();

        // 获取当前已登录用户
        Session session = SecurityUtils.getSubject().getSession();
        log.info("SESSIONID：" + session.getId());
        String sessionCaptcha = (String) session.getAttribute("captcha");
        session.getStartTimestamp();
        if (StringUtils.isEmpty(username)) {
            return ResponseUtil.badArgument("用户名必填");
        }
        if (StringUtils.isEmpty(password)) {
            return ResponseUtil.badArgument("密码必填");
        }
        if (StringUtils.isEmpty(captcha)) {
            return ResponseUtil.badArgument("验证码必填");
        }
        if (!org.springframework.util.StringUtils.isEmpty(captcha) && !StringUtils.isEmpty(sessionCaptcha)) {
            if (sessionCaptcha.toUpperCase().equals(captcha.toUpperCase())) {
                boolean flag = true;// 当前用户是否已登录
                if (subject.getPrincipal() != null && subject.isAuthenticated()) {
                    String userName = subject.getPrincipal().toString();
                    if (userName.equals(username)) {
                        flag = false;
                    }
                }
                if (flag) {
                    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
                    try {
                        if (isRememberMe != null && isRememberMe.equals("1")) {
                            token.setRememberMe(true);
                            // 或 UsernamePasswordToken token = new UsernamePasswordToken(username,password,true);
                        } else {
                            token.setRememberMe(false);
                        }
                        subject.login(token);
                        session.removeAttribute("captcha");
//                        Cookie cookie = new Cookie("access_token", this.sysConfigService.select().getNspmToken().trim());
//                        cookie.setMaxAge(43200);
//                        cookie.setPath("/");
//                        cookie.setDomain("192.168.5.101");
//                        response.addCookie(cookie);
                        User user = this.userService.findByUserName(username);


                        // 登录日志
                        try {
                            OperationLog instance = new OperationLog();
                            instance.setAccount(username);
                            if (user.getUnitId()!=null){
                                instance.setDM(String.valueOf(user.getUnitId()));  //unitId
                                instance.setMC(unitMapper.selectObjById(user.getUnitId()).getUnitName());  //unitName
                            }
                            if(user.getUserRole()!=null){
                                instance.setName(user.getUserRole());
                            }
                            instance.setIp(Ipv4Util.getRealIP(request));
                            this.operationLogService.saveLoginLog(instance);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return ResponseUtil.ok(user.getId());
                        //  return "redirect:/index.jsp";
                    } catch (UnknownAccountException e) {
                        e.printStackTrace();
                        msg = "用户名错误";
                        System.out.println("用户名错误");
                        return new Result(410, msg);
                    } catch (IncorrectCredentialsException e) {
                        e.printStackTrace();
                        msg = "密码错误";
                        System.out.println("密码错误");
                        return new Result(420, msg);
                    }
                } else {
                    User user = this.userService.findByUserName(username);
                    return new Result(200, "用户已登录", user.getId());
                }
            } else {
                return new Result(430, "验证码错误");
            }
        } else {
            return new Result(400, "验证码已过期");
        }
    }

    private static Integer i = 0;

    @GetMapping("/test")
    public Integer captchaa(HttpServletRequest request, HttpServletResponse response) throws IOException {
        i++;
        if (i == 1) {
            try {
                Thread.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {


        //设置响应头信息，通知浏览器不要缓存
        response.setHeader("Expires", "-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "-1");
        response.setContentType("image/jpeg");

        // 获取验证码
        String code = CaptchaUtil.getRandomCode();

        // 将验证码输入到session中，用来验证
        HttpSession session = request.getSession();
        log.info("SESSIONID：" + session.getId());

        session.setAttribute("captcha", code);

        this.removeAttrbute(session, "captcha");

        // 输出到web页面
        ImageIO.write(CaptchaUtil.genCaptcha(code), "jpg", response.getOutputStream());
    }

    public void removeAttrbute(final HttpSession session, final String attrName) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                // 删除session中存的验证码
                session.removeAttribute(attrName);
                timer.cancel();
            }
        }, 5 * 60 * 1000); //5 * 60 * 1000
    }

    @RequestMapping("/logout")
    public Object logout(HttpServletRequest request, HttpServletResponse response) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.getPrincipal() != null) {
            // 清除cookie
            subject.logout(); // 退出登录
            CookieUtil.removeCookie(request, response, "JSESSIONID");
            return ResponseUtil.ok();
        } else {
            return new Result(401, "log in");
        }
    }


}

