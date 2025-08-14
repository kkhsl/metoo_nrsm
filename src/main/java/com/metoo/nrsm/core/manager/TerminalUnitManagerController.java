package com.metoo.nrsm.core.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.UnitSubnet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/admin/terminal/unit")
public class TerminalUnitManagerController {

    @Resource
    private TerminalUnitMapper terminalUnitMapper;
    @Resource
    private UnitMapper unitMapper;

    @Value("${netflow.asset.add.url}")
    private String assetAddUrl;

    @Value("${netflow.asset.delete.url}")
    private String assetDeleteUrl;


    @Autowired
    private RestTemplate restTemplate;


    @GetMapping("/selectAll")
    public Result selectAll() {
        try {
            List<UnitSubnet> unitSubnets = terminalUnitMapper.selectAll();
            if (unitSubnets == null || unitSubnets.isEmpty()) {
                return ResponseUtil.ok(unitSubnets);
            }
            return ResponseUtil.ok(unitSubnets);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to retrieve data: " + e.getMessage());
        }
    }

    @PostMapping("/saveAll")
    @Transactional
    public Result saveAll(@RequestBody List<UnitSubnet> unitSubnets) {
        if (unitSubnets == null || unitSubnets.isEmpty()) {
            return ResponseUtil.error("Input data cannot be empty");
        }

        // 用于收集同步失败的资产组信息
        List<String> syncFailures = new ArrayList<>();

        try {
            for (UnitSubnet unitSubnet : unitSubnets) {
                UnitNewDTO unitNewDTO = new UnitNewDTO();
                unitNewDTO.setId(unitSubnet.getUnitId());
                List<Unit> units = unitMapper.selectObjConditionQuery(unitNewDTO);
                if (units.size() > 1) {
                    //一个部门多网段

                } else {
                    unitSubnet.setUnitName(units.get(0).getUnitName());
                    unitSubnet.setAddTime(new Date());
                    if (unitSubnet.getId() == null) {
                        // ID为空，执行插入操作
                        terminalUnitMapper.insert(unitSubnet);
                        // 同步新增资产组
                        if (!syncAssetGroup(unitSubnet)) {
                            syncFailures.add(unitSubnet.getUnitName());
                        }
                    } else {
                        // ID存在，检查是否已存在
                        UnitSubnet subnet = terminalUnitMapper.findById(unitSubnet.getId());
                        if (subnet == null) {
                            terminalUnitMapper.insert(unitSubnet);
                            // 同步新增资产组
                            if (!syncAssetGroup(unitSubnet)) {
                                syncFailures.add(unitSubnet.getUnitName());
                            }
                        } else {
                            terminalUnitMapper.update(unitSubnet);
                            // 更新操作同步资产组

                        }
                    }
                }
            }

            // 处理同步结果
            if (syncFailures.isEmpty()) {
                return ResponseUtil.ok("UnitSubnet added/updated and assets synced successfully");
            } else {
                String errorMsg = "UnitSubnet saved but asset sync failed for: " +
                        String.join(", ", syncFailures);
                return ResponseUtil.error(errorMsg);
            }
        } catch (Exception e) {
            return ResponseUtil.error("Failed to save data: " + e.getMessage());
        }
    }

    // 资产组同步方法
    private boolean syncAssetGroup(UnitSubnet unitSubnet) {
        try {
            // 构建资产组请求
            Map<String, String> assetRequest = new HashMap<>();
            assetRequest.put("ip_v4_range", unitSubnet.getIpv4());
            assetRequest.put("ip_v6_range", unitSubnet.getIpv6());
            assetRequest.put("name", unitSubnet.getUnitName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(assetRequest, headers);

            // 调用资产组接口
            ResponseEntity<String> response = restTemplate.postForEntity(
                    assetAddUrl, request, String.class);

            // 解析响应JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);

            // 检查响应code是否为0（成功）
            Integer code = (Integer) responseMap.get("code");
            if (code != null && code == 0) {
                return true;
            }

            // 记录错误信息
            String errorMsg = (String) responseMap.get("message");
            log.error("资产组同步失败: {} - {}", unitSubnet.getName(), errorMsg);
            return false;

        } catch (Exception e) {
            log.error("资产组接口调用异常: {}", e.getMessage());
            return false;
        }
    }


    @DeleteMapping("/deleteAll")
    @Transactional
    public Result deleteAll(@RequestParam(required = true) Long id) {
        try {
            UnitSubnet subnet = terminalUnitMapper.findById(id);
            if (subnet == null) {
                return ResponseUtil.error("ID为 " + id + " 的单位网段不存在");
            }

            UnitNewDTO unitNewDTO = new UnitNewDTO();
            unitNewDTO.setId(subnet.getUnitId());
            List<Unit> units = unitMapper.selectObjConditionQuery(unitNewDTO);

            if (units.isEmpty()){
                terminalUnitMapper.deleteById(id);
                return ResponseUtil.ok("单位网段删除成功");
            }else {
                subnet.setUnitName(units.get(0).getUnitName());
                // 删除单位网段
                terminalUnitMapper.deleteById(id);
                // 删除资产组
                if (!deleteAssetGroup(subnet.getUnitName())) {
                    return ResponseUtil.error("单位网段删除成功,资产组删除失败");
                }
                return ResponseUtil.ok("单位网段及资产组删除成功");
            }

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseUtil.error("删除失败: " + e.getMessage());
        }
    }

    // 资产组删除方法
    private boolean deleteAssetGroup(String assetGroupName) {
        try {
            // 构建资产组删除URL
            String deleteUrl = assetDeleteUrl.replace("{name}",
                    URLEncoder.encode(assetGroupName, StandardCharsets.UTF_8.toString()));

            // 设置HTTP头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(headers);

            // 调用资产组删除接口
            ResponseEntity<String> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    request,
                    String.class
            );

            // 解析响应
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);

            // 检查响应code是否为0（成功）
            Integer code = (Integer) responseMap.get("code");
            if (code != null && code == 0) {
                return true;
            }

            // 记录错误信息
            String errorMsg = (String) responseMap.get("message");
            log.error("资产组删除失败: {} - {}", assetGroupName, errorMsg);
            return false;

        } catch (Exception e) {
            log.error("资产组删除接口调用异常: {}", e.getMessage());
            return false;
        }
    }


}
