package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<String> shopIdLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    public static void setCurrentShopId(String shopId) {
        shopIdLocal.set(shopId);
    }

    public static String getCurrentShopId() {
        return shopIdLocal.get();
    }

    public static void removeCurrentShopId() {
        shopIdLocal.remove();
    }
}
