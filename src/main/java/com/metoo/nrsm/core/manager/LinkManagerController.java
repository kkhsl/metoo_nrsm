package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.LinkDTO;
import com.metoo.nrsm.core.service.ILinkService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.Link;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;

@ApiOperation("链路管理")
@RestController
@RequestMapping("/admin/link")
public class LinkManagerController {

    @Autowired
    private ILinkService linkService;

    @GetMapping
    public Object list(@RequestBody(required = false) LinkDTO dto){
        if(dto == null){
            dto = new LinkDTO();
        }

        Page<Link> page = this.linkService.selectObjConditionQuery(dto);
        if(page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<Link>(page));
        }
        return ResponseUtil.ok();
    }

    @PostMapping
    public Object save(@RequestBody(required = false) Link instance){
        if(checkObjAllFieldsIsNull(instance)){
            return ResponseUtil.ok();
        }
        int i = this.linkService.save(instance);
        if(i >= 1){
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    public static boolean checkObjAllFieldsIsNull(Object object) {
        if (null == object) {
            return true;

        }
        try {
            for (Field f : object.getClass().getDeclaredFields()) {
                f.setAccessible(true);

                System.out.print(f.getName() + ":");

                System.out.println(f.get(object));

                if (f.get(object)!= null && StringUtils.isNotBlank(f.get(object).toString())) {
                    return false;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @DeleteMapping
    public Object delete(@RequestParam(required = false, value = "id") String id,
                         @RequestParam(required = false, value = "ids") Long[] ids){
        if(ids != null && ids.length > 0){
            int i = this.linkService.batchesDel(ids);
            if(i >= 1){
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.badArgument();
            }
        }else  if(id != null && !id.equals("")){
            int i = this.linkService.delete(Long.parseLong(id));
            if(i >= 1){
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.badArgument();
            }
        }
        return ResponseUtil.badArgument();
    }

}
