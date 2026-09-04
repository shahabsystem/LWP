package com.shahabsystem.livewebwallpaper;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.WallpaperService;
import android.view.*;
import android.webkit.*;

public class LiveWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){ return new EngineImpl(); }

    class EngineImpl extends Engine {
        final Handler h=new Handler(Looper.getMainLooper());
        WebView web; SurfaceHolder holder; boolean visible;
        final Runnable tick=()->{ if(visible){ drawOverlay(); h.postDelayed(tick, 1000); }};

        @Override public void onVisibilityChanged(boolean v){ visible=v; if(v){h.post(tick);}else{h.removeCallbacks(tick);} }
        @Override public void onSurfaceCreated(SurfaceHolder sh){super.onSurfaceCreated(sh);holder=sh; setup();}
        @Override public void onSurfaceChanged(SurfaceHolder sh,int f,int w,int he){holder=sh; if(web!=null) web.layout(0,0,w,he);}
        @Override public void onSurfaceDestroyed(SurfaceHolder sh){visible=false;h.removeCallbacksAndMessages(null);if(web!=null){web.stopLoading();web.destroy();web=null;}super.onSurfaceDestroyed(sh);}

        void setup(){
            int mode=Prefs.mode(LiveWallpaperService.this);
            if(mode==0) setupWeb(); else { web=null; h.post(tick); }
        }
        void setupWeb(){
            web=new WebView(LiveWallpaperService.this);
            web.setBackgroundColor(Prefs.bg(LiveWallpaperService.this));
            web.setLayerType(View.LAYER_TYPE_HARDWARE,null);
            WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
            s.setLoadsImagesAutomatically(true); s.setMediaPlaybackRequiresUserGesture(true);
            s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false);
            web.setInitialScale((int)(Prefs.zoom(LiveWallpaperService.this)*100));
            web.setWebViewClient(new WebViewClient());
            web.loadUrl(Prefs.url(LiveWallpaperService.this));
            int w=holder.getSurfaceFrame().width(), he=holder.getSurfaceFrame().height();
            web.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(he,View.MeasureSpec.EXACTLY));
            web.layout(0,0,w,he);
        }
        void drawOverlay(){
            Canvas c=null; try{
                c=holder.lockCanvas(); if(c==null)return;
                c.drawColor(Prefs.bg(LiveWallpaperService.this));
                int mode=Prefs.mode(LiveWallpaperService.this);
                if(mode==0 && web!=null) web.draw(c);
                else if(mode==1) drawMatrix(c);
                else drawCode(c);
                if(Prefs.dim(LiveWallpaperService.this)>0){
                    int a=(int)(Prefs.dim(LiveWallpaperService.this)*255/100f);
                    c.drawColor(Color.argb(a,0,0,0));
                }
            }finally{if(c!=null)holder.unlockCanvasAndPost(c);}
        }
        void drawMatrix(Canvas c){
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setTypeface(Typeface.MONOSPACE);p.setTextSize(28);p.setColor(Color.rgb(0,255,80));
            int w=c.getWidth(),he=c.getHeight(),t=(int)(SystemClock.uptimeMillis()/70);
            for(int x=12;x<w;x+=32)for(int y=((x*7+t)%he)-he;y<he;y+=32)c.drawText(String.valueOf((x+y+t)%2),x,y,p);
        }
        void drawCode(Canvas c){
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setTypeface(Typeface.MONOSPACE);p.setTextSize(18);p.setColor(Color.rgb(120,255,160));
            String[] lines={"// LIVE CODE","while (true) {","  renderWallpaper();","  saveBattery();","  if (visible)","    update();","}"};
            int y=45,shift=(int)((SystemClock.uptimeMillis()/900)%20);
            for(String s:lines){c.drawText(s,20,y+shift,p);y+=30;}
        }
    }
}
