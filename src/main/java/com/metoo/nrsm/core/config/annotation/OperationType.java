package com.metoo.nrsm.core.config.annotation;

public enum OperationType {

    /**
     * 查询
     */
    QUERY,

    SAVE("保存"),
    /**
     * 创建 CREATE
     */
    CREATE("创建/修改"),
    /**
     * 批量创建
     */
    BATCH_CREATE,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 批量更新
     */
    BATCH_UPDATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 批量删除
     */
    BATCH_DELETE,
    /**
     * 导入
     */
    IMPORT,
    /**
     * 导出
     */
    EXPORT,
    /**
     * 其他类型
     */
    OTHER;

    private String chineseName;

    OperationType() {
    }

    OperationType(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public static String getChineseName(OperationType operationType) {
        return operationType.getChineseName();
    }

}
