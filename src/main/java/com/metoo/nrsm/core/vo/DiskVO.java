package com.metoo.nrsm.core.vo;

public class DiskVO {

    private String totalSpaceFormatted;
    private String usableSpaceFormatted;
    private String freeSpaceFormatted;
    private String usedSpaceFormatted;

    // Getters 和 Setters
    public String getTotalSpaceFormatted() {
        return totalSpaceFormatted;
    }

    public void setTotalSpaceFormatted(String totalSpaceFormatted) {
        this.totalSpaceFormatted = totalSpaceFormatted;
    }

    public String getUsableSpaceFormatted() {
        return usableSpaceFormatted;
    }

    public void setUsableSpaceFormatted(String usableSpaceFormatted) {
        this.usableSpaceFormatted = usableSpaceFormatted;
    }

    public String getFreeSpaceFormatted() {
        return freeSpaceFormatted;
    }

    public void setFreeSpaceFormatted(String freeSpaceFormatted) {
        this.freeSpaceFormatted = freeSpaceFormatted;
    }

    public String getUsedSpaceFormatted() {
        return usedSpaceFormatted;
    }

    public void setUsedSpaceFormatted(String usedSpaceFormatted) {
        this.usedSpaceFormatted = usedSpaceFormatted;
    }
}
