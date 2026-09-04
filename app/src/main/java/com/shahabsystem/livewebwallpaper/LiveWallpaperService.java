package com.shahabsystem.livewebwallpaper;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class LiveWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() { return new WallpaperEngine(); }

    private final class WallpaperEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private WebView web;
        private boolean visible;
        private boolean surfaceReady;

        @Override public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            surfaceReady = true;
            createWebView(holder);
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (web != null) { web.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)); web.layout(0,0,width,height); }
        }
        @Override public void onVisibilityChanged(boolean v) {
            visible = v;
            if (web != null) web.evaluateJavascript("window.setWallpaperActive(" + v + ");", null);
            if (v && web == null && surfaceReady) createWebView(getSurfaceHolder());
        }
        @Override public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false; surfaceReady = false;
            destroyWebView();
            super.onSurfaceDestroyed(holder);
        }
        private void createWebView(SurfaceHolder holder) {
            if (web != null) return;
            web = new WebView(LiveWallpaperService.this);
            web.setBackgroundColor(Prefs.bg(LiveWallpaperService.this));
            web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            WebSettings s = web.getSettings();
            s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setLoadsImagesAutomatically(true);
            s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false); s.setSupportZoom(false);
            s.setAllowFileAccess(true); s.setAllowContentAccess(false);
            web.setVerticalScrollBarEnabled(!Prefs.hideScroll(LiveWallpaperService.this));
            web.setHorizontalScrollBarEnabled(!Prefs.hideScroll(LiveWallpaperService.this));
            web.setOverScrollMode(View.OVER_SCROLL_NEVER);
            web.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) { applySettings(); }
            });
            int w = holder.getSurfaceFrame().width(), h = holder.getSurfaceFrame().height();
            web.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
            web.layout(0,0,w,h);
            if (Prefs.mode(LiveWallpaperService.this) == 0) {
                web.setInitialScale((int)(Prefs.zoom(LiveWallpaperService.this) * 100f));
                web.loadUrl(Prefs.url(LiveWallpaperService.this));
            } else {
                web.loadUrl("file:///android_asset/wallpaper.html");
            }
        }
        private void applySettings() {
            if (web == null) return;
            String bg = String.format(Locale.US, "#%06X", 0xFFFFFF & Prefs.bg(LiveWallpaperService.this));
            String accent = String.format(Locale.US, "#%06X", 0xFFFFFF & Prefs.accent(LiveWallpaperService.this));
            String js = String.format(Locale.US,
                    "if(window.applySettings){window.applySettings({mode:%d,matrixSpeed:%d,codeSpeed:%d,fontIndex:%d,accent:'%s',bg:'%s',dim:%d});}",
                    Prefs.mode(LiveWallpaperService.this), Prefs.matrixSpeed(LiveWallpaperService.this), Prefs.codeSpeed(LiveWallpaperService.this),
                    Prefs.mode(LiveWallpaperService.this)==1?Prefs.matrixFont(LiveWallpaperService.this):Prefs.codeFont(LiveWallpaperService.this), accent,bg,Prefs.dim(LiveWallpaperService.this));
            web.evaluateJavascript(js, null);
            web.evaluateJavascript("if(window.setWallpaperActive){window.setWallpaperActive("+visible+");}", null);
        }
        private void destroyWebView() {
            if (web != null) { web.stopLoading(); web.loadUrl("about:blank"); web.destroy(); web = null; }
        }
    }
}
