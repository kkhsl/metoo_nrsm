package com.metoo.nrsm.core.config.utils;

import java.lang.reflect.Field;

public class CopyPropertiesReflect {


    public static void copyPropertiesExceptId(Object source, Object target) {
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("id".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) { // 跳过 null 值

                    Field targetField = target.getClass().getDeclaredField(field.getName());
                    targetField.setAccessible(true);
                    targetField.set(target, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
