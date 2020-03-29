package com.kraigs.android.micamp.extras;

public class SharedPrefs {

    static String college;

    public SharedPrefs(){}

    public SharedPrefs(String college) {
        SharedPrefs.college = college;
    }

    public static String getCollege() {
        return college;
    }

}
