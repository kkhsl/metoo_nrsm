package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class IpStatisticsResult extends IdEntity {
    private Long ID;
    private int ACCESS_COUNT;
    private String IP;
    private String NAME;
    private String RANK;
    private Date STATISTICS_TIME;
    private String TYPE;

}
