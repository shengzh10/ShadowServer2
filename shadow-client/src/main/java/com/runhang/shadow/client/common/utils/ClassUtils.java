package com.runhang.shadow.client.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ClassUtils
 * @Description 类操作工具
 * @Date 2019/4/30 9:38
 * @author szh
 **/
@Slf4j
public class ClassUtils {

    private static final String MAIN_PACKAGE_NAME = "com.runhang.shadow.client.";
    // 动态类的包名
    private static final String ENTITY_PACKAGE_NAME = MAIN_PACKAGE_NAME + "device.entity";

    // getter & setter 选项
    public static final String METHOD_GETTER = "get";
    public static final String METHOD_SETTER = "set";

    /**
     * 获取getter或setter方法名称
     *
     * @param propertyName 属性名
     * @param method 方法类型：ClassGenerateUtils.METHOD_GETTER 或 ClassGenerateUtils.METHOD_SETTER
     * @return 方法名
     */
    public static String getGetterSetterName(String propertyName, String method) {
        String upperPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return method + upperPropertyName;
    }

    /**
     * 设置bean的属性值
     *
     * @param obj       bean对象
     * @param fieldName 属性名
     * @param value     属性值
     * @return          是否成功
     */
    public static boolean setValue(Object obj, String fieldName, Object value) {
        Class clazz = obj.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取bean的属性值
     *
     * @param obj       bean对象
     * @param fieldName 属性名称
     * @return          属性值
     */
    public static Object getValue(Object obj, String fieldName) {
        if (null == obj || StringUtils.isEmpty(fieldName)) {
            return null;
        }
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * @Description 获取bean的所有属性名及值
     * @param obj bean
     * @return 属性名及对应值
     * @author szh
     * @Date 2019/6/26 16:58
     */
    public static Map<String, Object> getValueMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (null == obj) {
            return map;
        }

        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                if (f.getType() == java.util.List.class || f.getType() == java.util.ArrayList.class) {
                    if (f.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) f.getGenericType();
                        String genericName = ((Class<?>) pt.getActualTypeArguments()[0]).getSimpleName();
                        String fieldName = "List<" + genericName + ">";
                        map.put(fieldName, f.get(obj));
                    }
                } else {
                    map.put(f.getName(), f.get(obj));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return map;
    }

    /**
     * @Description 获取所有entity名
     * @return entity类名
     * @author szh
     * @Date 2019/6/18 10:34
     */
    public static List<String> getAllEntityName() {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = ENTITY_PACKAGE_NAME.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if ("file".endsWith(type)) {
                fileNames = getClassNameByFile(url.getPath(), null, true);
            }
        }
        return fileNames;
    }

    /**
     * @Description 获取路径下所有类名
     * @param filePath 路径
     * @param className 类名
     * @param childPackage 是否获取子包下类名
     * @return 类名
     * @author szh
     * @Date 2019/6/18 10:35
     */
    private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
        List<String> myClassName = new ArrayList<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (null != childFiles) {
            for (File childFile : childFiles) {
                if (childFile.isDirectory()) {
                    if (childPackage) {
                        myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
                    }
                } else {
                    String childFilePath = childFile.getPath();
                    if (childFilePath.endsWith(".class")) {
                        childFilePath = childFilePath.substring(childFilePath.indexOf("\\entity") + 8, childFilePath.lastIndexOf("."));
                        myClassName.add(childFilePath);
                    }
                }
            }
        }
        return myClassName;
    }

}
