package com.metoo.nrsm.core.manager.ap;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.manager.ap.utils.*;
import com.metoo.nrsm.entity.ac.AcAction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 11:09
 */

@RestController
public class ThirdPartyLoginExample5 {

    public static void main(String[] args) throws Exception {
        GecoosApi gecoosApi = new DefaultGecoosApi("http://192.168.5.205:60650/api/");
        gecoosApi.login("admin");

        RequestParams requestParams = RequestBuilder.newBuilder().uri("apsearch")
                .paramEntry("numperpage", 10)
                .paramEntry("pagenum", 1)
                .paramEntry("reverse", "")
                .paramEntry("sortkey", "")
                .paramEntry("withstatus", "")
                .paramEntry("withversion", "")
                .paramEntry("withmodel", "")
                .paramEntry("template", "")
                .paramEntry("withcustom", "")
                .paramEntry("withreg", "").build();

        JSONObject jsonObject = gecoosApi.getCall(requestParams);
        System.out.println(jsonObject);

    }


    @PostMapping("aaa")
    public void get(@RequestBody AcAction instance){

        JSONObject jsonObject = GecossApiUtil.getCall(GecossApiUtil.parseParam(instance, "apsearch"));
        System.out.println(jsonObject);
    }

}
