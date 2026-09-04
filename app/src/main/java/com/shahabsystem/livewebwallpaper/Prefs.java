package com.shahabsystem.livewebwallpaper;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private Prefs() {}
    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }
    public static String url(Context c) { return p(c).getString("url", "https://example.com"); }
    public static float zoom(Context c) { return p(c).getFloat("zoom", 1.0f); }
    public static int dim(Context c) { return p(c).getInt("dim", 0); }
    public static int bg(Context c) { return p(c).getInt("bg", 0xFF000000); }
    public static int mode(Context c) { return p(c).getInt("mode", 0); }
    public static void save(Context c, String url, float zoom, int dim, int bg, int mode) {
        p(c).edit().putString("url", url).putFloat("zoom", zoom).putInt("dim", dim).putInt("bg", bg).putInt("mode", mode).apply();
    }
}
