package com.metoo.nrsm.core.system.utils;

import org.springframework.stereotype.Component;

@Component
public class OsHelper {

    public enum OSType {
        WINDOWS, LINUX, MAC, OTHER
    }

    public OSType getOperatingSystemType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OSType.LINUX;
        } else if (osName.contains("mac")) {
            return OSType.MAC;
        } else {
            return OSType.OTHER;
        }
    }

    public boolean isWindows() {
        return getOperatingSystemType() == OSType.WINDOWS;
    }

    public boolean isLinux() {
        return getOperatingSystemType() == OSType.LINUX;
    }
}
