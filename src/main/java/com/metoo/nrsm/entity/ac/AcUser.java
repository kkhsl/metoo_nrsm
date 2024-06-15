package com.metoo.nrsm.entity.ac;

import com.metoo.nrsm.core.dto.ParamsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-05-14 16:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcUser extends ParamsDTO {

    private Integer numperpage;
    private Integer pagenum;
    private String reverse;
    private String sortkey;
    private String groupid;
    private String searchkey;
    private String withradio;
}
