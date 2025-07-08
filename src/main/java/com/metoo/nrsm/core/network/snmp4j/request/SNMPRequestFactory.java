package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.entity.NetworkElement;

public class SNMPRequestFactory<T> {

    // 根据网元所选版本，定义使用哪个snmp
    // 实例方法版本
    public T request(NetworkElement networkElement) {
        // 实际实现根据networkElement决定返回类型
        return null; // 这里需要返回实际的T类型对象
    }

    // 静态方法版本（需要额外声明泛型）
//    public static <R> R createRequest(NetworkElement networkElement) {
//        // 实际实现根据networkElement决定返回类型
//        return null; // 这里需要返回实际的R类型对象
//    }

    /**
     * 根据网元创建相应版本的SNMP请求对象
     *
     * @param networkElement 网元信息
     * @param <T>            返回类型
     * @return 具体版本的SNMP请求对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T createRequest(NetworkElement networkElement) {
        switch (networkElement.getVersion()) {
//            case "v1":
//                return (T) new SNMPv1Request();
            case "v2":
                return (T) new SNMPv2Request();
            case "v3":
                return (T) new SNMPv3Request();
            default:
                throw new IllegalArgumentException("Unsupported SNMP version");
        }
    }
}
