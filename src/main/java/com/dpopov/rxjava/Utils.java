package com.dpopov.rxjava;

public final class Utils {

    public static void printMethodStart(final String methodName) {
        System.out.printf("========== [%s started] ========== \n", methodName);
    }
    public static void printSeparator() {
        System.out.println("===================================");
    }


    public static void log(final Class clazz, final String s) {
        // todo: use logger
        System.out.printf("[%s] %s \n", clazz.getSimpleName(), s);
    }

}