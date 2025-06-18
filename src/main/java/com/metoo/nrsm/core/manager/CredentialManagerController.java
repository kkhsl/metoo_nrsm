package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.CredentialDTO;
import com.metoo.nrsm.core.service.ICredentialService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.Credential;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/credential")
public class CredentialManagerController {

    @Autowired
    private ICredentialService credentialService;

    @ApiOperation("凭据列表")
    @PostMapping("/list")
    public Object list(@RequestBody(required = true) CredentialDTO dto){
        if(dto == null){
            dto = new CredentialDTO();
        }
        Page<Credential> page = this.credentialService.selectObjByConditionQuery(dto);
        if(page.getResult().size() > 0){

            return ResponseUtil.ok(new PageInfo<Credential>(page));
        }
        return ResponseUtil.ok();
    }

    @ApiOperation("凭据添加")
    @PostMapping("/save")
    public Object save(@RequestBody Credential instance){
        if(StringUtils.isEmpty(instance.getName())){
            return ResponseUtil.badArgument("凭据名不能为空");
        }
        if(StringUtils.isEmpty(instance.getLoginName())){
            return ResponseUtil.badArgument("登录名不能为空");
        }
//        if(MyStringUtils.isEmpty(instance.getLoginPassword())){
//            return ResponseUtil.badArgument("登录密码不能为空");
//        }
        if(instance.isTrafficPermit()){
            if(StringUtil.isEmpty(instance.getEnableUserName())){
                return ResponseUtil.badArgument("通行用户名不能为空");
            }
            if(StringUtil.isEmpty(instance.getEnablePassword())){
                return ResponseUtil.badArgument("通行密码不能为空");
            }
        }else{
            instance.setEnablePassword(null);
            instance.setEnableUserName(null);
        }
        int i = this.credentialService.save(instance);
        if(i >= 1){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.error();
        }
    }

    @ApiOperation("删除/批量删除")
    @DeleteMapping
    public Object delete(@RequestParam(required = false, value = "id") Long id,@RequestParam(required = false, value = "ids") Long[] ids){
        if(ids != null && ids.length > 0){
            int i = this.credentialService.batchesDel(ids);
            if(i >= 1){
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.error();
            }
        }else  if(id != null && !id.equals("")){
            int i = this.credentialService.delete(id);
            if(i >= 1){
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.badArgument();
    }

}
