package com.shahabsystem.livewebwallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Random;

public class LiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private final class WallpaperEngine extends Engine {

        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();

        private WebView webView;
        private boolean visible;
        private boolean surfaceReady;
        private float[] matrixDrops = new float[0];
        private int matrixColumns;
        private long lastFrameTime;
        private int codeLine;
        private int codePosition;

        private final String[] codeLines = {
                "// LIVE CODE",
                "const wallpaper = {",
                "  mode: \"creative\",",
                "  battery: \"optimized\",",
                "  render() {",
                "    animate();",
                "    updateDisplay();",
                "  },",
                "  status: \"online\"",
                "};",
                "// building something new..."
        };

        private final Runnable frameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!visible || !surfaceReady) {
                    return;
                }
                drawFrame();
                handler.postDelayed(this, 16L);
            }
        };

        private android.content.Context context() {
            return LiveWallpaperService.this;
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            surfaceReady = true;
            SurfaceHolder h = getSurfaceHolder();
            int width = h.getSurfaceFrame().width();
            int height = h.getSurfaceFrame().height();
            initMatrix(width, height);
            if (Prefs.mode(context()) == 0) {
                createWebView(width, height);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            initMatrix(width, height);
            layoutWebView(width, height);
        }

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;
            if (isVisible) {
                lastFrameTime = SystemClock.uptimeMillis();
                handler.removeCallbacks(frameRunnable);
                handler.post(frameRunnable);
            } else {
                handler.removeCallbacks(frameRunnable);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            visible = false;
            surfaceReady = false;
            handler.removeCallbacks(frameRunnable);
            destroyWebView();
            super.onSurfaceDestroyed(holder);
        }

        private void initMatrix(int width, int height) {
            int fontSize = Math.max(14, Math.min(22, width / 55));
            matrixColumns = Math.max(1, width / fontSize + 1);
            matrixDrops = new float[matrixColumns];
            for (int i = 0; i < matrixColumns; i++) {
                matrixDrops[i] = -random.nextInt(Math.max(10, height / fontSize));
            }
        }

        private void createWebView(int width, int height) {
            if (webView != null) {
                return;
            }

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

            webView.setBackgroundColor(Prefs.bg(context()));
            webView.setVerticalScrollBarEnabled(!Prefs.hideScroll(context()));
            webView.setHorizontalScrollBarEnabled(!Prefs.hideScroll(context()));
            webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.setInitialScale((int) (Prefs.zoom(context()) * 100f));
                    view.invalidate();
                }
            });

            layoutWebView(width, height);
            webView.loadUrl(Prefs.url(context()));
        }

        private void layoutWebView(int width, int height) {
            if (webView == null) {
                return;
            }
            webView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            );
            webView.layout(0, 0, width, height);
        }

        private void destroyWebView() {
            if (webView == null) {
                return;
            }
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas == null) {
                    return;
                }

                canvas.drawColor(Prefs.bg(context()));

                int mode = Prefs.mode(context());
                if (mode == 0) {
                    drawWeb(canvas);
                } else if (mode == 1) {
                    drawMatrix(canvas);
                } else {
                    drawCode(canvas);
                }

                int dim = Prefs.dim(context());
                if (dim > 0) {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.argb(dim * 255 / 100, 0, 0, 0));
                    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawWeb(Canvas canvas) {
            if (webView == null) {
                createWebView(canvas.getWidth(), canvas.getHeight());
                return;
            }
            layoutWebView(canvas.getWidth(), canvas.getHeight());
            webView.invalidate();
            webView.draw(canvas);
        }

        private void drawMatrix(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int fontSize = Math.max(14, Math.min(22, width / 55));

            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(fontSize);
            int accent = Prefs.accent(context());
            paint.setColor(accent);
            paint.setShadowLayer(7f, 0f, 0f, accent);

            long now = SystemClock.uptimeMillis();
            if (lastFrameTime == 0L) {
                lastFrameTime = now;
            }
            float delta = Math.min(50L, now - lastFrameTime) / 16f;
            lastFrameTime = now;

            float speed = 0.25f + (Prefs.speed(context()) / 100f) * 1.9f;

            for (int i = 0; i < matrixColumns; i++) {
                float y = matrixDrops[i] * fontSize;
                paint.setAlpha(130 + random.nextInt(110));

                String character;
                int type = random.nextInt(10);
                if (type < 8) {
                    character = random.nextBoolean() ? "0" : "1";
                } else {
                    String symbols = "<>/{}[]$#@";
                    character = String.valueOf(symbols.charAt(random.nextInt(symbols.length())));
                }

                canvas.drawText(character, i * fontSize, y, paint);
                matrixDrops[i] += speed * delta;

                if (y > height + fontSize * 20f && random.nextFloat() > 0.96f) {
                    matrixDrops[i] = -random.nextInt(30);
                }
            }

            paint.clearShadowLayer();
            paint.setAlpha(255);
        }

        private void drawCode(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float scale = Math.max(0.75f, Math.min(1.25f, width / 700f));
            float left = width * 0.04f;
            float top = height * 0.14f;
            float panelWidth = width * 0.92f;
            float panelHeight = height * 0.72f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(190, 8, 10, 12));
            canvas.drawRoundRect(left, top, left + panelWidth, top + panelHeight, 18f, 18f, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.argb(60, 255, 255, 255));
            canvas.drawRoundRect(left, top, left + panelWidth, top + panelHeight, 18f, 18f, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(Color.argb(70, 255, 255, 255));
            canvas.drawRect(left, top + 46f, left + panelWidth, top + 47f, paint);

            paint.setColor(Color.rgb(85, 85, 85));
            for (int i = 0; i < 3; i++) {
                canvas.drawCircle(left + 18f + i * 16f, top + 23f, 4f, paint);
            }

            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(13f * scale);
            int lineHeight = (int) (25f * scale);
            int maxLines = Math.max(1, (int) ((panelHeight - 100f) / lineHeight));

            StringBuilder shown = new StringBuilder();
            for (int i = 0; i < codeLine && i < codeLines.length; i++) {
                shown.append(codeLines[i]).append('\n');
            }
            if (codeLine < codeLines.length) {
                shown.append(codeLines[codeLine], 0, Math.min(codePosition, codeLines[codeLine].length()));
            }

            String[] visibleLines = shown.toString().split("\\n", -1);
            float textX = left + 60f;

            paint.setColor(Color.rgb(62, 62, 62));
            for (int i = 0; i < Math.min(maxLines, visibleLines.length); i++) {
                canvas.drawText(String.valueOf(i + 1), left + 12f, top + 75f + i * lineHeight, paint);
            }

            paint.setColor(Color.argb(220, 200, 200, 200));
            for (int i = 0; i < Math.min(maxLines, visibleLines.length); i++) {
                canvas.drawText(visibleLines[i], textX, top + 75f + i * lineHeight, paint);
            }

            paint.setColor(Prefs.accent(context()));
            canvas.drawText("● LIVE", left + 14f, top + panelHeight - 14f, paint);

            paint.setColor(Color.rgb(80, 80, 80));
            canvas.drawText("UTF-8", left + panelWidth - 55f, top + panelHeight - 14f, paint);

            // سرعت تایپ واقعی: بر اساس تنظیم کاربر، نه یک تایمر ثابت.
            long interval = Math.max(18L, 150L - Prefs.codeSpeed(context()) * 1.3L);
            long now = SystemClock.uptimeMillis();
            if (now - lastFrameTime >= interval) {
                lastFrameTime = now;
                if (codeLine < codeLines.length) {
                    if (codePosition < codeLines[codeLine].length()) {
                        codePosition++;
                    } else if (codeLine < codeLines.length - 1) {
                        codeLine++;
                        codePosition = 0;
                    } else {
                        codeLine = 0;
                        codePosition = 0;
                    }
                }
            }
        }
    }
}
