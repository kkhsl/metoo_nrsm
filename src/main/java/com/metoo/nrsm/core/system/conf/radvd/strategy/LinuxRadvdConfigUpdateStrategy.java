package com.metoo.nrsm.core.system.conf.radvd.strategy;

import com.metoo.nrsm.entity.Radvd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LinuxRadvdConfigUpdateStrategy implements RadvdConfigUpdateStrategy {

    private static final String CONFIG_FILE_PATH = "/etc/radvd.conf"; // 配置文件路径

    @Override
    public void updateConfig(List<Radvd> radvdList) {
        try {
            StringBuilder configContent = new StringBuilder();

            for (Radvd radvd : radvdList) {
                configContent.append("#").append(radvd.getName()).append("\n")
                        .append("interface ").append(radvd.getInterfaceName()).append(" {\n")
                        .append("    AdvSendAdvert on;\n")
                        .append("    MinRtrAdvInterval 30;\n")
                        .append("    MaxRtrAdvInterval 100;\n")
                        .append("    AdvManagedFlag on;\n")
                        .append("    AdvOtherConfigFlag on;\n")
                        .append("    AdvLinkMTU 1500;\n")
                        .append("    AdvDefaultLifetime 0;\n")
                        .append("    prefix ").append(radvd.getIpv6Prefix()).append(" {\n")
                        .append("        AdvOnLink on;\n")
                        .append("        AdvAutonomous on;\n")
                        .append("        AdvValidLifetime 86400;\n")
                        .append("        AdvPreferredLifetime 3600;\n")
                        .append("    };\n")
                        .append("};\n");
            }

            // 写入配置文件
            Files.write(Paths.get(CONFIG_FILE_PATH), configContent.toString().getBytes());
            System.out.println("Linux配置文件已更新：/etc/radvd.conf");

        } catch (IOException e) {
            System.err.println("更新配置文件失败: " + e.getMessage());
        }
    }

}
