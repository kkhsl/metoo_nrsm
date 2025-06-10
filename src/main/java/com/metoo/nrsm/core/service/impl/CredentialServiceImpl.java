package com.metoo.nrsm.core.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.CredentialDTO;
import com.metoo.nrsm.core.mapper.CredentialMapper;
import com.metoo.nrsm.core.service.ICredentialService;
import com.metoo.nrsm.core.service.ISysConfigService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.entity.Credential;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CredentialServiceImpl implements ICredentialService {

    @Autowired
    private CredentialMapper credentialMaaper;
    @Autowired
    private ISysConfigService sysConfigService;
//    @Autowired
//    private NodeUtil nodeUtil;
    @Autowired
    private IUserService userService;


    @Override
    public Credential getObjById(Long id) {
        return this.credentialMaaper.getObjById(id);
    }

    @Override
    public Credential getObjByName(String name) {
        return this.credentialMaaper.getObjByName(name);
    }

    @Override
    public List<Credential> query() {
        return null;
    }

    @Override
    public int save(Credential instance) {
        if(instance.getId() == null || instance.getId().equals("")){
            instance.setAddTime(new Date());
            String uuid = UUID.randomUUID().toString().replace("-", "");
            instance.setUuid(uuid);
            User user = ShiroUserHolder.currentUser();
            instance.setUserId(user.getId());
        }
        if(instance.getId() == null || instance.getId().equals("")){
            try {
                return this.credentialMaaper.save(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }else{
            try {
                return this.credentialMaaper.update(instance);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    @Override
    public int update(Credential instance) {
        return this.credentialMaaper.update(instance);
    }

    @Override
    public int delete(Long id) {
        return this.credentialMaaper.delete(id);
    }

    @Override
    public int batchesDel(Long[] ids) {
        return this.credentialMaaper.batchesDel(ids);
    }

//    @Override
//    public Map<String, String> getUuid(TopoCredentialDto dto) {
//        SysConfig sysConfig = this.sysConfigService.select();
//        String token = sysConfig.getNspmToken();
//        if(token != null){
//            String url = "/push/credential/getall";
//            Object result = this.nodeUtil.postBody(dto, url, token);
//            JSONObject json = JSONObject.parseObject(result.toString());
//            if(json.get("content") != null){
//                JSONObject content = JSONObject.parseObject(json.get("content").toString());
//                if(content.get("list") != null) {
//                    List list = new ArrayList();
//                    JSONArray arrays = JSONArray.parseArray(content.get("list").toString());
//                    for (Object array : arrays) {
//                        JSONObject credential = JSONObject.parseObject(array.toString());
//                        if(credential.get("uuid") != null && credential.get("name").equals(dto.getName())){
//                            Map map = new HashMap();
//                            map.put("uuid", credential.get("uuid").toString());
//                            map.put("credentialId", credential.get("id").toString());
//                            return map;
//                        }
//                    }
//                }
//            }
//
//    }
//     return null;
//    }

    @Override
    public Page<Credential> getObjsByLevel(Credential instance) {
        if(instance.getBranchLevel() == null || instance.getBranchLevel().equals("")){
            User currentUser = ShiroUserHolder.currentUser();
            User user = this.userService.findByUserName(currentUser.getUsername());
            instance.setBranchLevel(user.getGroupLevel());
        }
        Page<Credential> page = PageHelper.startPage(instance.getCurrentPage(), instance.getPageSize());
        this.credentialMaaper.getObjsByLevel(instance);
        return page;
    }

    @Override
    public List<Credential> getAll() {
        return this.credentialMaaper.getAll();
    }

    @Override
    public Page<Credential> selectObjByConditionQuery(CredentialDTO dto) {
        if(dto == null){
            dto = new CredentialDTO();
        }
        Page<Credential> page = PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        this.credentialMaaper.selectConditionQuery(dto);
        return page;
    }
    @Override
    public List<Credential> selectObjByIdQuery(CredentialDTO dto) {
        if(dto == null){
            dto = new CredentialDTO();
        }
        return this.credentialMaaper.selectConditionQuery(dto);
    }

}
