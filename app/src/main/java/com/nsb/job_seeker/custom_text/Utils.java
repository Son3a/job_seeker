package com.nsb.job_seeker.custom_text;

import android.content.Context;
import android.graphics.Typeface;

public class Utils {
    private static Typeface aztekTypeface;
    private static Typeface bauhausTypeface;

    public static Typeface getAztekTypeface(Context context) {
        if(aztekTypeface == null){
            aztekTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/aztek.ttf");
        }
        return aztekTypeface;
    }

    public static Typeface getBauhausTypeface(Context context) {
        if(bauhausTypeface == null){
            bauhausTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/bauhaus.ttf");
        }
        return bauhausTypeface;
    }
}
