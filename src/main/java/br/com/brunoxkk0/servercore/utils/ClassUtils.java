package br.com.brunoxkk0.servercore.utils;

public class ClassUtils {

    public static String getCallerPackage(){
        String pkg = getCallerClass(3);
        return pkg.substring(0, pkg.lastIndexOf("."));
    }

    public static String getCallerPackage(int offset){
        String pkg = getCallerClass(offset);
        return pkg.substring(0, pkg.lastIndexOf("."));
    }

    public static String getCallerClass(int offset){
        return Thread.currentThread().getStackTrace()[offset].getClassName();
    }

    public static String getCallerClass(){
        return Thread.currentThread().getStackTrace()[2].getClassName();
    }

    public static String getPackage(Class<?> clazz){
        return clazz.getPackage().getName();
    }

}
