package com.shahabsystem.livewebwallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Live wallpaper renderer. Matrix and Code use the user's supplied HTML files from assets. */
public class LiveWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() { return new WallpaperEngine(); }

    private final class WallpaperEngine extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private WebView webView;
        private boolean visible;
        private boolean surfaceReady;
        private int loadedMode = -1;
        private String loadedUrl = "";
        private float loadedZoom = -1f;

        private final Runnable frameRunnable = new Runnable() {
            @Override public void run() {
                if (!visible || !surfaceReady) return;
                drawFrame();
                int fps = Math.max(10, Math.min(60, Prefs.fps(context())));
                handler.postDelayed(this, Math.max(1L, 1000L / fps));
            }
        };

        private android.content.Context context() { return LiveWallpaperService.this; }

        @Override public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            surfaceReady = true;
            updateWebContent(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height(), true);
        }

        @Override public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            layoutWebView(width, height);
        }

        @Override public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;
            if (isVisible) {
                handler.removeCallbacks(frameRunnable);
                handler.post(frameRunnable);
            } else {
                handler.removeCallbacks(frameRunnable);
            }
        }

        @Override public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false;
            surfaceReady = false;
            handler.removeCallbacks(frameRunnable);
            destroyWebView();
            super.onSurfaceDestroyed(holder);
        }

        private String sourceForMode(int mode) {
            if (mode == 1) return "file:///android_asset/Matrix4.html";
            if (mode == 2) return "file:///android_asset/Code9.html";
            return Prefs.url(context());
        }

        private void updateWebContent(int width, int height, boolean force) {
            int mode = Math.max(0, Math.min(2, Prefs.mode(context())));
            String source = sourceForMode(mode);
            float zoom = Prefs.zoom(context());
            boolean changed = force || webView == null || mode != loadedMode || !source.equals(loadedUrl)
                    || Math.abs(zoom - loadedZoom) > 0.001f;

            if (webView == null) createWebView(width, height);
            layoutWebView(width, height);

            webView.setBackgroundColor(Prefs.bg(context()));
            webView.setVerticalScrollBarEnabled(!Prefs.hideScroll(context()));
            webView.setHorizontalScrollBarEnabled(!Prefs.hideScroll(context()));

            if (changed) {
                loadedMode = mode;
                loadedUrl = source;
                loadedZoom = zoom;
                webView.loadUrl(source);
            }
        }

        private void createWebView(int width, int height) {
            if (webView != null) return;
            webView = new WebView(context());
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setLoadsImagesAutomatically(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setBuiltInZoomControls(false);
            settings.setDisplayZoomControls(false);
            settings.setSupportZoom(false);
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(false);
            settings.setOffscreenPreRaster(true);
            webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            webView.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) {
                    applyWebSettings(view);
                }
            });
            layoutWebView(width, height);
        }

        private void applyWebSettings(WebView view) {
            view.setBackgroundColor(Prefs.bg(context()));
            view.setVerticalScrollBarEnabled(!Prefs.hideScroll(context()));
            view.setHorizontalScrollBarEnabled(!Prefs.hideScroll(context()));
            float z = Prefs.zoom(context());
            view.setInitialScale(Math.max(50, Math.min(250, Math.round(z * 100f))));
        }

        private void layoutWebView(int width, int height) {
            if (webView == null) return;
            webView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            webView.layout(0, 0, width, height);
        }

        private void destroyWebView() {
            if (webView == null) return;
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
            loadedMode = -1;
            loadedUrl = "";
            loadedZoom = -1f;
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas == null) return;
                canvas.drawColor(Prefs.bg(context()));
                updateWebContent(canvas.getWidth(), canvas.getHeight(), false);
                if (webView != null) {
                    webView.invalidate();
                    webView.draw(canvas);
                }
                int dim = Math.max(0, Math.min(100, Prefs.dim(context())));
                if (dim > 0) {
                    dimPaint.setStyle(Paint.Style.FILL);
                    dimPaint.setColor(Color.argb(dim * 255 / 100, 0, 0, 0));
                    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), dimPaint);
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
