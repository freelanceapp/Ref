package com.apps.ref.share;


import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.apps.ref.language.Language;


public class App extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Language.onAttach(newBase,"ar"));
    }



    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.setDefaultFont(this, "DEFAULT", "fonts/ar_font.ttf");
        TypefaceUtil.setDefaultFont(this, "MONOSPACE", "fonts/ar_font.ttf");
        TypefaceUtil.setDefaultFont(this, "SERIF", "fonts/ar_font.ttf");
        TypefaceUtil.setDefaultFont(this, "SANS_SERIF", "fonts/ar_font.ttf");

    }
}

