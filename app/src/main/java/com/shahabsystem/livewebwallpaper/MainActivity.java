package com.shahabsystem.livewebwallpaper;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import android.app.Activity;

public class MainActivity extends Activity {
    EditText url; SeekBar zoom, dim, matrixSpeed, codeSpeed; Spinner mode, matrixFont, codeFont;
    CheckBox fullscreen, hideScroll; Button bg, accent; int bgColor, accentColor;
    final String[] fonts={"Monospace","Sans Serif","Serif","Small Monospace"};

    @Override public void onCreate(Bundle b){super.onCreate(b); build(); load();}
    TextView tv(String s,float z){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setPadding(0,8,0,8);return t;}
    void build(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(24,18,24,24);
        ScrollView sv=new ScrollView(this); sv.addView(box); setContentView(sv);
        box.addView(tv("LiveWebWallpaper",27)); box.addView(tv("Web • Matrix • Code  |  کم‌مصرف و قابل تنظیم",14));
        box.addView(tv("آدرس صفحه وب",15));
        url=new EditText(this); url.setHint("https://example.com"); url.setSingleLine(); url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI); box.addView(url);
        box.addView(tv("نوع پس‌زمینه",15)); mode=spinner(new String[]{"صفحه وب","ماتریکس متحرک","تایپ کد"}); box.addView(mode);
        box.addView(tv("بزرگ‌نمایی / کوچک‌نمایی وب",14)); zoom=seek(0,100,33); box.addView(zoom);
        box.addView(tv("کم‌رنگ کردن محتوا",14)); dim=seek(0,90,0); box.addView(dim);
        bg=new Button(this); box.addView(bg); bg.setOnClickListener(v->colorDialog(false));
        accent=new Button(this); box.addView(accent); accent.setOnClickListener(v->colorDialog(true));
        box.addView(tv("تنظیمات ماتریکس",18)); box.addView(tv("سرعت ماتریکس",14)); matrixSpeed=seek(1,100,55); box.addView(matrixSpeed); matrixFont=spinner(fonts); box.addView(matrixFont);
        box.addView(tv("تنظیمات تایپ کد",18)); box.addView(tv("سرعت تایپ کد",14)); codeSpeed=seek(1,100,55); box.addView(codeSpeed); codeFont=spinner(fonts); box.addView(codeFont);
        fullscreen=new CheckBox(this); fullscreen.setText("حالت تمام‌سطح Wallpaper"); box.addView(fullscreen);
        hideScroll=new CheckBox(this); hideScroll.setText("عدم نمایش ScrollBar"); box.addView(hideScroll);
        Button apply=new Button(this); apply.setText("ذخیره و انتخاب Live Wallpaper"); box.addView(apply); apply.setOnClickListener(v->{save();choose();});
        Button support=new Button(this); support.setText("♥ حمایت از توسعه‌دهنده"); box.addView(support); support.setOnClickListener(v->supportDialog());
    }
    Spinner spinner(String[] a){Spinner s=new Spinner(this);s.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,a));return s;}
    SeekBar seek(int min,int max,int p){SeekBar s=new SeekBar(this);s.setMax(max-min);s.setProgress(p-min);s.setTag(min);return s;}
    int val(SeekBar s){return s.getProgress()+(Integer)s.getTag();}
    void colorDialog(boolean accentMode){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(24,10,24,10);
        SeekBar r=seek(0,255,Color.red(accentMode?accentColor:bgColor)),g=seek(0,255,Color.green(accentMode?accentColor:bgColor)),b=seek(0,255,Color.blue(accentMode?accentColor:bgColor));
        l.addView(tv("قرمز",13));l.addView(r);l.addView(tv("سبز",13));l.addView(g);l.addView(tv("آبی",13));l.addView(b);
        new AlertDialog.Builder(this).setTitle(accentMode?"رنگ Matrix و Code":"رنگ پشت صفحه").setView(l).setPositiveButton("اعمال",(d,w)->{if(accentMode)accentColor=Color.rgb(val(r),val(g),val(b));else bgColor=Color.rgb(val(r),val(g),val(b));updateButtons();save();}).setNegativeButton("لغو",null).show();
    }
    void updateButtons(){bg.setText("رنگ پشت صفحه  #"+String.format("%06X",0xFFFFFF&bgColor));accent.setText("رنگ Matrix / Code  #"+String.format("%06X",0xFFFFFF&accentColor));}
    void save(){String u=url.getText().toString().trim();if(u.isEmpty())u="https://example.com";if(!u.startsWith("http://")&&!u.startsWith("https://"))u="https://"+u;float z=.5f+(zoom.getProgress()/100f)*1.5f;Prefs.save(this,u,z,val(dim),bgColor,accentColor,mode.getSelectedItemPosition(),val(matrixSpeed),matrixFont.getSelectedItemPosition(),val(codeSpeed),codeFont.getSelectedItemPosition(),fullscreen.isChecked(),hideScroll.isChecked());}
    void load(){url.setText(Prefs.url(this));zoom.setProgress(Math.max(0,Math.min(100,(int)((Prefs.zoom(this)-.5f)/1.5f*100))));dim.setProgress(Prefs.dim(this));bgColor=Prefs.bg(this);accentColor=Prefs.accent(this);updateButtons();mode.setSelection(Prefs.mode(this));matrixSpeed.setProgress(Prefs.matrixSpeed(this)-1);matrixFont.setSelection(Prefs.matrixFont(this));codeSpeed.setProgress(Prefs.codeSpeed(this)-1);codeFont.setSelection(Prefs.codeFont(this));fullscreen.setChecked(Prefs.fullscreen(this));hideScroll.setChecked(Prefs.hideScroll(this));}
    void choose(){Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,LiveWallpaperService.class));try{startActivity(i);}catch(Exception e){startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}}
    void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u)));}catch(Exception ignored){}}
    void supportDialog(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(24,8,24,8);l.addView(tv("اگر برنامه برای شما مفید است، حمایت شما به ادامه توسعه کمک می‌کند.",15));
        Button c=new Button(this);c.setText("☕ CoffeeBede");l.addView(c);c.setOnClickListener(v->open("https://coffeebede.com/shahabsystem"));
        Button r=new Button(this);r.setText("Reymit");l.addView(r);r.setOnClickListener(v->open("https://reymit.ir/shahabsystem"));
        l.addView(tv("✉ hamedmohammadinikche@gmail.com\nGitHub: github.com/shahabsystem",14));
        new AlertDialog.Builder(this).setTitle("حمایت از توسعه‌دهنده").setView(l).setPositiveButton("بازگشت",null).show();
    }
}
