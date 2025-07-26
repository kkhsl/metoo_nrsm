package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.service.IVendorService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Gateway;
import com.metoo.nrsm.entity.PythonScriptParams;
import com.metoo.nrsm.entity.Vendor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping("/admin/gateway")
@RestController
public class GatewayManagerController {

    @Autowired
    private PythonExecUtils pythonExecUtils;

    @Autowired
    private IDeviceTypeService deviceTypeService;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVendorService vendorService;

    @PostMapping("/list")
    private Result list(@RequestBody GatewayDTO dto) {
        Result result = this.gatewayService.selectObjConditionQuery(dto);
        return result;
    }

    @PostMapping("/save")
    private Result save(@RequestBody Gateway instance) {
        Result result = this.gatewayService.save(instance);
        return result;
    }

    @PostMapping("/batch/save")
    private Result batchSave(@RequestBody List<Gateway> devices) {
        if (devices.size() > 0) {
            Result result = this.gatewayService.batchSave(devices);
            return result;
        }
        return ResponseUtil.ok();
    }

    @GetMapping("/modify")
    public Result modify(@RequestParam Long id) {
        Result result = this.gatewayService.modify(id);
        return ResponseUtil.ok(result);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam String ids) {
        Result result = this.gatewayService.delete(ids);
        return result;
    }

    @GetMapping("/test")
    public Result test(@RequestParam String ids) {
        Gateway gateway = gatewayService.selectObjById(Long.valueOf(ids));
        PythonScriptParams params = new PythonScriptParams();

        // 设置设备类型（英文）
        DeviceType deviceType = deviceTypeService.selectObjById(gateway.getDeviceTypeId());
        params.setCommand(Optional.ofNullable(deviceType)
                .map(DeviceType::getNameEn)
                .orElse("switch"));

        // 设置厂商（英文小写）
        Vendor vendor = vendorService.selectObjById(gateway.getVendorId());
        params.setBrand(Optional.ofNullable(vendor)
                .map(Vendor::getNameEn)
                .map(String::toLowerCase)
                .orElse("h3c"));

        // 设置IP地址
        params.setIp(gateway.getIp());

        // 设置协议和端口
        if (gateway.getLoginType().equals("ssh")) {
            params.setProtocol("ssh");
            params.setPort(Integer.parseInt(Optional.ofNullable(gateway.getLoginPort()).orElse(String.valueOf(22))));
        } else {
            params.setProtocol("telnet");
            params.setPort(Integer.parseInt(Optional.ofNullable(gateway.getLoginPort()).orElse(String.valueOf(23))));
        }

        // 设置凭证信息
        params.setUsername(gateway.getLoginName());
        params.setPassword(gateway.getLoginPassword());
        params.setOption("test");
        if (executeTestScript(params).contains("true")){
            return ResponseUtil.ok(true);
        }else if (executeTestScript(params).contains("false")){
            return ResponseUtil.ok(false);
        }else {
            return ResponseUtil.ok(executeTestScript(params));
        }
    }


    /**
     * 执行test脚本
     */
    private String executeTestScript(PythonScriptParams params) {
        String path = Global.TESTPATH + "main.py";
        String[] scriptParams = {
                params.getCommand(),
                params.getBrand(),
                params.getIp(),
                params.getProtocol(),
                String.valueOf(params.getPort()),
                params.getUsername(),
                params.getPassword(),
                params.getOption()
        };

        return pythonExecUtils.execTestPy(path, scriptParams);
    }

}
