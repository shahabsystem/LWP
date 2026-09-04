package com.shahabsystem.livewebwallpaper;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText url; SeekBar zoom, dim; Spinner mode; Button bg, apply;
    int bgColor;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setPadding(28,28,28,28);
        TextView title = new TextView(this); title.setText("Live Web Wallpaper"); title.setTextSize(25); box.addView(title);
        TextView hint = new TextView(this); hint.setText("صفحه وب، ماتریکس یا تایپ کد را به پس‌زمینه زنده تبدیل کنید."); box.addView(hint);

        url = new EditText(this); url.setHint("https://example.com"); url.setSingleLine(); url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI); box.addView(url);
        mode = new Spinner(this); mode.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"صفحه وب","ماتریکس متحرک","تایپ کد"})); box.addView(mode);

        addLabel(box,"بزرگ‌نمایی / کوچک‌نمایی");
        zoom = new SeekBar(this); zoom.setMax(150); zoom.setProgress(50); box.addView(zoom);
        addLabel(box,"کم‌رنگ کردن محتوا");
        dim = new SeekBar(this); dim.setMax(90); dim.setProgress(0); box.addView(dim);

        bgColor = Prefs.bg(this);
        bg = new Button(this); bg.setText("رنگ پس‌زمینه: #"+Integer.toHexString(bgColor).substring(2)); box.addView(bg);
        bg.setOnClickListener(v -> {
            // انتخاب سریع چند رنگ بدون وابستگی به کتابخانه خارجی
            String[] names={"مشکی","سرمه‌ای","سبز تیره","سفید","خاکستری"};
            int[] colors={Color.BLACK,Color.rgb(5,10,35),Color.rgb(0,35,18),Color.WHITE,Color.DKGRAY};
            new AlertDialog.Builder(this).setTitle("رنگ پس‌زمینه").setItems(names,(d,w)->{
                bgColor=colors[w]; bg.setText("رنگ پس‌زمینه: #"+Integer.toHexString(bgColor).substring(2)); save();
            }).show();
        });
        apply = new Button(this); apply.setText("اعمال و انتخاب Live Wallpaper"); box.addView(apply);
        apply.setOnClickListener(v -> {
            save();
            Intent i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, LiveWallpaperService.class));
            try { startActivity(i); } catch(Exception e) { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
        });
        setContentView(box);
    }

    void addLabel(LinearLayout b,String s){ TextView t=new TextView(this); t.setText(s); t.setPadding(0,18,0,2); b.addView(t); }
    void save(){
        float z=0.5f+(zoom.getProgress()/100f)*1.5f;
        int d=dim.getProgress();
        String u=url.getText().toString().trim();
        if(u.isEmpty())u="https://example.com";
        if(!u.startsWith("http://")&&!u.startsWith("https://"))u="https://"+u;
        Prefs.save(this,u,z,d,bgColor,mode.getSelectedItemPosition());
    }
    @Override protected void onResume(){super.onResume();
        if(url!=null){url.setText(Prefs.url(this)); zoom.setProgress((int)((Prefs.zoom(this)-.5f)/1.5f*100)); dim.setProgress(Prefs.dim(this)); mode.setSelection(Prefs.mode(this));}
    }
}
