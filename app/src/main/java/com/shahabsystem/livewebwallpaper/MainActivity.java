package com.shahabsystem.livewebwallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    LinearLayout l; EditText url; SeekBar sp,cp,dim,zoom,fps; Spinner mode; CheckBox hs;
    TextView tv(String s){ TextView t=new TextView(this); t.setText(s); t.setTextSize(18); t.setTextColor(Color.rgb(235,235,240)); t.setPadding(0,12,0,7); return t; }
    SeekBar bar(int v){ SeekBar s=new SeekBar(this); s.setMax(100); s.setProgress(v); return s; }
    Spinner spin(String[] a){ Spinner s=new Spinner(this); s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a)); return s; }
    Button link(String text,String url){ Button b=new Button(this); b.setText(text); b.setTextSize(17); b.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}catch(Exception ignored){}}); return b; }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(22,16,22,24);
        ScrollView sv=new ScrollView(this); sv.addView(l); setContentView(sv);

        TextView title=tv("LiveWebWallpaper 8.0"); title.setTextSize(25); title.setTypeface(null, android.graphics.Typeface.BOLD); l.addView(title);
        TextView subtitle=tv("تنظیمات حرفه‌ای Live Wallpaper"); subtitle.setTextSize(18); l.addView(subtitle);
        l.addView(tv("حالت نمایش")); mode=spin(new String[]{"Web","Matrix (HTML پیش‌فرض)","Code (HTML پیش‌فرض)"}); l.addView(mode);
        l.addView(tv("آدرس Web (فقط در حالت Web)")); url=new EditText(this); url.setSingleLine(); url.setTextSize(18); url.setTextColor(Color.WHITE); url.setHintTextColor(Color.rgb(150,150,155)); l.addView(url);
        l.addView(tv("سرعت Matrix")); sp=bar(Prefs.speed(this)); l.addView(sp);
        l.addView(tv("سرعت تایپ Code")); cp=bar(Prefs.codeSpeed(this)); l.addView(cp);
        l.addView(tv("FPS — نرخ فریم / مصرف باتری")); fps=bar(Math.max(10,Math.min(60,Prefs.fps(this)))*100/60); l.addView(fps);
        l.addView(tv("کم‌رنگی صفحه")); dim=bar(Prefs.dim(this)); l.addView(dim);
        l.addView(tv("Zoom وب")); zoom=bar((int)((Prefs.zoom(this)-.5f)/1.5f*100)); l.addView(zoom);

        Button color=new Button(this); color.setText("🎨  انتخاب رنگ پس‌زمینه"); color.setTextSize(17); l.addView(color);
        TextView colorPreview=tv("رنگ فعلی: #"+String.format("%06X",(Prefs.bg(this)&0xFFFFFF))); l.addView(colorPreview);
        color.setOnClickListener(v->showColorDialog(colorPreview));

        hs=new CheckBox(this); hs.setText("عدم نمایش ScrollBar"); hs.setTextSize(18); hs.setTextColor(Color.rgb(235,235,240)); hs.setChecked(Prefs.hideScroll(this)); l.addView(hs);
        Button save=new Button(this); save.setText("اعمال تنظیمات و انتخاب Live Wallpaper"); save.setTextSize(17); l.addView(save);
        save.setOnClickListener(v->{
            getSharedPreferences("p",0).edit()
                .putInt("mode",mode.getSelectedItemPosition()).putInt("speed",Math.max(1,sp.getProgress()))
                .putInt("codeSpeed",Math.max(1,cp.getProgress())).putInt("fps",Math.max(10,Math.min(60,fps.getProgress()*60/100)))
                .putInt("dim",dim.getProgress()).putFloat("zoom",.5f+zoom.getProgress()/100f*1.5f)
                .putString("url",url.getText().toString().trim()).putBoolean("hideScroll",hs.isChecked()).apply();
            Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,LiveWallpaperService.class)); startActivity(i);
        });

        l.addView(tv("❤ حمایت از توسعه‌دهنده"));
        l.addView(tv("اگر LiveWebWallpaper برای شما مفید است، با حمایت شما توسعه آن ادامه پیدا می‌کند."));
        l.addView(link("☕ حمایت با قهوه — coffeebede.com/shahabsystem","https://coffeebede.com/shahabsystem"));
        l.addView(link("❤ حمایت مالی — reymit.ir/shahabsystem","https://reymit.ir/shahabsystem"));
        l.addView(link("GitHub — github.com/shahabsystem","https://github.com/shahabsystem"));
        l.addView(link("✉ hamedmohammadinikche@gmail.com","mailto:hamedmohammadinikche@gmail.com"));
        l.addView(tv("🌷 سپاس از حمایت و همراهی شما"));

        mode.setSelection(Prefs.mode(this)); url.setText(Prefs.url(this));
    }

    private void showColorDialog(TextView preview){
        final int old=Prefs.bg(this); final LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(30,10,30,10);
        final SeekBar r=bar(Color.red(old)*100/255), g=bar(Color.green(old)*100/255), bl=bar(Color.blue(old)*100/255);
        box.addView(tv("قرمز"));box.addView(r);box.addView(tv("سبز"));box.addView(g);box.addView(tv("آبی"));box.addView(bl);
        final TextView sample=tv("پیش‌نمایش");box.addView(sample);
        SeekBar.OnSeekBarChangeListener x=new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){int c=Color.rgb(r.getProgress()*255/100,g.getProgress()*255/100,bl.getProgress()*255/100);sample.setText("پیش‌نمایش  #"+String.format("%06X",c&0xFFFFFF));}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}};
        r.setOnSeekBarChangeListener(x);g.setOnSeekBarChangeListener(x);bl.setOnSeekBarChangeListener(x);
        new AlertDialog.Builder(this).setTitle("رنگ پس‌زمینه").setView(box).setPositiveButton("ذخیره",(d,w)->{int c=Color.rgb(r.getProgress()*255/100,g.getProgress()*255/100,bl.getProgress()*255/100);getSharedPreferences("p",0).edit().putInt("bg",c).apply();preview.setText("رنگ فعلی: #"+String.format("%06X",c&0xFFFFFF));}).setNegativeButton("انصراف",null).show();
    }
}
