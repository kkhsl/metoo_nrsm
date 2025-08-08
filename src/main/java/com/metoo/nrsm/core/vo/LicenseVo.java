package com.metoo.nrsm.core.vo;

import com.metoo.nrsm.core.enums.license.BaseVersionType;
import com.metoo.nrsm.core.enums.license.FeatureModule;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Licens验证方式
 * 第一种方式：系统文件
 * 第二种方式：注册表 *
 * 第三种方式：远程服务端
 */
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class LicenseVo {

    @ApiModelProperty("授权时间")
    private Long insertTime;

    @ApiModelProperty("开始时间")
    private Long startTime;
    @ApiModelProperty("结束时间")
    private Long endTime;
    @ApiModelProperty("授权时间")
    private int licenseDay;
    @ApiModelProperty("License类型 0：试用版 1，授权版 2：终身版")
    private String type;
    @ApiModelProperty("License版本号")
    private String licenseVersion;

    // 授权信息
    @ApiModelProperty("授权防火墙")
    private int licenseFireWall;
    @ApiModelProperty("授权路由/交换")
    private int licenseRouter;
    @ApiModelProperty("授权主机数")
    private int licenseHost;
    @ApiModelProperty("授权模拟网关")
    private int licenseUe;
    @ApiModelProperty("授权设备数量")
    private int licenseDevice;
    @ApiModelProperty("AC授权")
    private boolean licenseAC;

    @ApiModelProperty("已使用")
    private int useDay;
    @ApiModelProperty("未使用")
    private int surplusDay;
    @ApiModelProperty("客户信息")
    private String customerInfo;


    @ApiModelProperty("以导入防火墙")
    private int useFirewall;
    @ApiModelProperty("以导入路由交换")
    private int useRouter;
    @ApiModelProperty("以导入主机数")
    private int useHost;
    @ApiModelProperty("以导入模拟网关")
    private int useUe;

    @ApiModelProperty("Probe授权：是否开启扫描")
    private boolean licenseProbe;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("通用版本：0 政务外网 1 教体版：2 ")
    private BaseVersionType baseVersionType;
    @ApiModelProperty("资产测绘：1 流量分析：2 漏洞扫描：3 ")
    private List<FeatureModule> featureModules = new ArrayList<>();

    private List<String> permissionCodeList = new ArrayList();

    // 返回中文名称列表
    public List<String> getFeatureModuleNames() {
        if (featureModules == null) {
            return Collections.emptyList();
        }
        return featureModules.stream()
                .map(FeatureModule::getDesc) // 调用 getDescription() 获取中文名
                .collect(Collectors.toList());
    }

    // 返回中文名称列表
    public String getBaseVersionTypes() {
        if (baseVersionType == null) {
            return "";
        }
        return baseVersionType.getDesc();
    }

}
