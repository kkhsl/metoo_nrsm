package com.metoo.nrsm.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apiguardian.api.API;

import java.util.Date;
import java.util.Objects;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:24
 *
 * 保持表里默认有一条数据
 */
@ApiModel("")
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class PingIpConfig {

    private Long id;

    private Date addTime;

    private Date updateTime;

    @ApiModelProperty("是否启用状态 0：不启用 1：启用")
    private Integer status;

    private String v6ip1;

    private String v6ip2;

    private String v4ip1;

    private String v4ip2;

    private boolean changed;

    // 对比方法
    public boolean isChanged(PingIpConfig dbConfig) {
        return !Objects.equals(this.v6ip1, dbConfig.getV6ip1()) ||
                !Objects.equals(this.v6ip2, dbConfig.getV6ip2()) ||
                !Objects.equals(this.v4ip1, dbConfig.getV4ip1()) ||
                !Objects.equals(this.v4ip2, dbConfig.getV4ip2());
    }


    // 更新状态方法
    public void updateStatusIfChanged(PingIpConfig dbConfig) {
        if (isChanged(dbConfig)) {
            // 假设如果发生了变化，更新状态为 "变化"
            this.setChanged(true);  // 或者其他表示变化的状态
            // 更新数据库逻辑
            // saveOrUpdateDatabase(this); // 假设这是保存或更新数据库的逻辑
        }
    }


    private boolean enabled;
}
