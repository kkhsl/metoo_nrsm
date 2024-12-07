package com.metoo.nrsm.core.config.filter;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.service.ILicenseService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.License;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LicenseFilter implements Filter {

    // 定义不需要过滤的路径
    private final List<String> excludedPaths
            = Arrays.asList(
                "/nrsm/admin/gather/mac",
                "/nrsm/admin/gather/flux",
                "/nrsm/admin/ac/stasearch",
                "/nrsm/admin/ac/sync",
                "/nrsm/admin/ac/apsearch",
                "/nrsm/admin/subnet/comb",
                "/nrsm/admin/gather/ipv4/thread",
                "/nrsm/admin/subnet/ipv6",
                "/nrsm/admin/analysis",
                "/nrsm/admin/index/nav",
                "/nrsm/license/systemInfo",
                "/nrsm/license/query",
                "/nrsm/license/update",
                "/nrsm/admin/unbound",
                "/nrsm/license/update",
                "/nrsm/admin/test/flow",
                "/nrsm/admin/unbound",
                "/nrsm/admin/ping/ip/config",
              "/nrsm/admin/interface/info"
            );

    @Autowired
    private ILicenseService licenseService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        // 检测授权码
        License license = this.licenseService.detection();
        if(license != null && license.getStatus() == 0 && license.getFrom() == 0){
            chain.doFilter(servletRequest, servletResponse);
        }else{
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            System.out.println(request.getRequestURI());
            System.out.println(request.getRequestURL());
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            if (
                    excludedPaths.stream().anyMatch(request.getRequestURI()::startsWith)) {
                // 如果在排除列表中，则直接放行
                chain.doFilter(request, response);
            } else {
                // 如果不在排除列表中，则执行过滤器的逻辑
                // 此处可以编写你的过滤器逻辑
                String message = "未授权";
                if(license != null){
                    switch (license.getStatus()){
                        case 1:
                            message = "未授权";
                            break;
                        case 2:
                            message = "授权已过期";
                            break;
                    }
                }
                Result result = new Result(413, message);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().print(JSONObject.toJSONString(result));
            }


        }
      }

    @Override
    public void destroy() {

    }
}
