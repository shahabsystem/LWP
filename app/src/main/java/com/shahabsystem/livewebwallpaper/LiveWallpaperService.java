package com.shahabsystem.livewebwallpaper;

import android.graphics.*;
import android.os.*;
import android.service.wallpaper.WallpaperService;
import android.view.*;
import android.webkit.*;

public class LiveWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){return new EngineImpl();}
    class EngineImpl extends Engine {
        final Handler h=new Handler(Looper.getMainLooper());WebView web;SurfaceHolder holder;boolean visible;long frame=0;
        final Runnable tick=()->{if(visible){drawFrame();schedule();}};
        void schedule(){int m=Prefs.mode(LiveWallpaperService.this),speed=m==1?Prefs.matrixSpeed(LiveWallpaperService.this):Prefs.codeSpeed(LiveWallpaperService.this);h.postDelayed(tick,Math.max(45,720-speed*6L));}
        @Override public void onVisibilityChanged(boolean v){visible=v;if(v)h.post(tick);else h.removeCallbacks(tick);}
        @Override public void onSurfaceCreated(SurfaceHolder sh){super.onSurfaceCreated(sh);holder=sh;if(Prefs.mode(LiveWallpaperService.this)==0)setupWeb();else h.post(tick);}
        @Override public void onSurfaceChanged(SurfaceHolder sh,int f,int w,int he){holder=sh;if(web!=null){web.measure(View.MeasureSpec.makeMeasureSpec(w,1073741824),View.MeasureSpec.makeMeasureSpec(he,1073741824));web.layout(0,0,w,he);}}
        @Override public void onSurfaceDestroyed(SurfaceHolder sh){visible=false;h.removeCallbacksAndMessages(null);if(web!=null){web.stopLoading();web.destroy();web=null;}super.onSurfaceDestroyed(sh);}
        void setupWeb(){
            web=new WebView(LiveWallpaperService.this);web.setBackgroundColor(Prefs.bg(LiveWallpaperService.this));web.setLayerType(View.LAYER_TYPE_HARDWARE,null);
            WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setLoadsImagesAutomatically(true);s.setMediaPlaybackRequiresUserGesture(true);s.setBuiltInZoomControls(false);s.setDisplayZoomControls(false);s.setSupportZoom(false);
            web.setVerticalScrollBarEnabled(!Prefs.hideScroll(LiveWallpaperService.this));web.setHorizontalScrollBarEnabled(!Prefs.hideScroll(LiveWallpaperService.this));web.setOverScrollMode(View.OVER_SCROLL_NEVER);
            web.setInitialScale((int)(Prefs.zoom(LiveWallpaperService.this)*100));web.setWebViewClient(new WebViewClient());web.loadUrl(Prefs.url(LiveWallpaperService.this));
            int w=holder.getSurfaceFrame().width(),he=holder.getSurfaceFrame().height();web.measure(View.MeasureSpec.makeMeasureSpec(w,1073741824),View.MeasureSpec.makeMeasureSpec(he,1073741824));web.layout(0,0,w,he);
        }
        Typeface font(int id){if(id==1)return Typeface.SANS_SERIF;if(id==2)return Typeface.SERIF;return Typeface.MONOSPACE;}
        void drawFrame(){Canvas c=null;try{c=holder.lockCanvas();if(c==null)return;c.drawColor(Prefs.bg(LiveWallpaperService.this));int m=Prefs.mode(LiveWallpaperService.this);if(m==0&&web!=null)web.draw(c);else if(m==1)matrix(c);else code(c);int d=Prefs.dim(LiveWallpaperService.this);if(d>0)c.drawColor(Color.argb(d*255/100,0,0,0));}finally{if(c!=null)holder.unlockCanvasAndPost(c);}}
        void matrix(Canvas c){Paint p=new Paint(1);p.setColor(Color.rgb(0,255,90));p.setTypeface(font(Prefs.matrixFont(LiveWallpaperService.this)));p.setTextSize(Prefs.matrixFont(LiveWallpaperService.this)==3?20:28);int w=c.getWidth(),he=c.getHeight();long step=Math.max(45,720-Prefs.matrixSpeed(LiveWallpaperService.this)*6L);int off=(int)((SystemClock.uptimeMillis()/step)%64);for(int x=8;x<w;x+=32){for(int y=-he+off+(x%64);y<he;y+=32)c.drawText(((x+y+off)&1)==0?"0":"1",x,y,p);}}
        void code(Canvas c){Paint p=new Paint(1);p.setColor(Color.rgb(120,255,170));p.setTypeface(font(Prefs.codeFont(LiveWallpaperService.this)));p.setTextSize(Prefs.codeFont(LiveWallpaperService.this)==3?16:20);String[] lines={"// LIVE CODE","class Wallpaper {","  void render() {","    saveBattery();","    updateDisplay();","  }","}"};long step=Math.max(80,1100-Prefs.codeSpeed(LiveWallpaperService.this)*9L);int shift=(int)((SystemClock.uptimeMillis()/step)%36),y=45;for(String s:lines){c.drawText(s,20,y+shift,p);y+=31;}}
    }
}
