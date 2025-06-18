package com.metoo.nrsm.core.wsapi;

import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.wsapi.utils.NoticeWebsocketResp;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/ws/api/user")
@RestController
public class UserManagerControllerApi {

    @Autowired
    private IUserService userService;
    @Autowired
    private RedisResponseUtils redisResponseUtils;

    @GetMapping
    private Object user(@RequestParam(value = "userId") Long id){
        User user = this.userService.findObjById(id);
        NoticeWebsocketResp rep = new NoticeWebsocketResp();
        if(user != null){
            rep.setNoticeStatus(1);
            rep.setNoticeInfo(user);
            return rep;
        }else{
            rep.setNoticeStatus(0);
        }
        return rep;
    }
}
