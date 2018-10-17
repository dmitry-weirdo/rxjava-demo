package com.dpopov.rxjava;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static String formatDate() {
        return formatDate( new Date() );
    }
    public static String formatDate(final Date date) {
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss:SSS");
        return format.format(date);
    }
}