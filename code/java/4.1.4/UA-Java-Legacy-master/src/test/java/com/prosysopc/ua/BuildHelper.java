package com.prosysopc.ua;

public class BuildHelper {

    public static boolean isJdk6Toolchain() {
        return "true".equalsIgnoreCase(System.getProperty("useJdk6Toolchain"));
    }
}
