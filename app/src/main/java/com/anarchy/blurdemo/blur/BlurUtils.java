package com.anarchy.blurdemo.blur;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.ScriptIntrinsicConvolve3x3;
import android.support.v8.renderscript.ScriptIntrinsicConvolve5x5;

import com.anarchy.blurdemo.BlurInfo;

import java.util.List;


/**
 * Version 2.1.1
 * <p>
 * Date: 16/8/16 11:25
 * Author: zhendong.wu@shoufuyou.com
 * <p>
 * Copyright © 2014-2016 Shanghai Xiaotu Network Technology Co., Ltd.
 */
public class BlurUtils {
    public static final int SCALED_DEFAULT = 0;
    public static final int SCALED_WITH_FILTER = 1;
    public static int scaledType;
    public enum CONFIG{
        RENDER_SCRIPT,FAST_BLUR,BOX_BLUR,FAST_BLUR_JNI,GAUSSIA_FAST_BLUR,
        STACK_BLUR,RENDER_SCRIPT_3X3,RENDER_SCRIPT_5X5,RENDER_SCRIPT_GAUSSIA_5X5
    }

    public static Bitmap doBlur(Context context, @NonNull BlurInfo info, Bitmap src, float scaled, int radius, CONFIG config){
        int startSize = getMemorySize(context);
        long start = System.currentTimeMillis();
        Bitmap result = null;
        switch (config){
            case FAST_BLUR:
                result = FastBlur.fastBlur(getScaledBitmap(src,scaled),radius);
                break;
            case RENDER_SCRIPT:
                result = doRenderScriptBlur(context,src,scaled,radius);
                break;
            case FAST_BLUR_JNI:
                Bitmap s = getScaledBitmap(src,scaled);
                result = s.copy(Bitmap.Config.ARGB_8888,true);
                FastBlur.fastBlurJni(s,result,radius);
                break;
            case BOX_BLUR:
                result = BoxBlur.blur(radius,getScaledBitmap(src,scaled));
                break;
            case STACK_BLUR:
                result = StackBlur.blur(radius,getScaledBitmap(src,scaled));
                break;
            case GAUSSIA_FAST_BLUR:
                result = GaussianFastBlur.blur(radius,getScaledBitmap(src,scaled));
                break;
            case RENDER_SCRIPT_3X3:
                result = doRenderScriptBox3x3Blur(context,getScaledBitmap(src,scaled),radius);
                break;
            case RENDER_SCRIPT_5X5:
                result = doRenderScriptBox5x5Blur(context,getScaledBitmap(src,scaled),radius);
                break;
            case RENDER_SCRIPT_GAUSSIA_5X5:
                result = doRenderScriptGaussian5x5Blur(context,getScaledBitmap(src,scaled),radius);
                break;
        }
        long end = System.currentTimeMillis();
        info.duration.set(end-start);
        int endSize = getMemorySize(context);
        info.sizeChanged.set(endSize-startSize);
        return result;
    }



    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;
    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static Bitmap doRenderScriptBlur(Context context, Bitmap bitmap,float scale,int radius){
        Bitmap result =  getScaledBitmap(bitmap,scale);
        RenderScript renderScript = RenderScript.create(context);
        Allocation allocation = Allocation.createFromBitmap(renderScript,result);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,allocation.getElement());
        blur.setInput(allocation);
        blur.setRadius(radius);
        blur.forEach(allocation);
        allocation.copyTo(result);
        return result;
    }


    private static Bitmap doRenderScriptBox3x3Blur(Context context,Bitmap bitmap,int radius){
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicConvolve3x3 script = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        script.setCoefficients(BlurCore.BOX_3x3);
        for (int i = 0; i < radius; i++) {
            script.setInput(input);
            script.forEach(output);
            input = output;
        }
        output.copyTo(bitmap);
        return bitmap;
    }



    private static Bitmap doRenderScriptBox5x5Blur(Context context,Bitmap bitmap,int radius){
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicConvolve5x5 script = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs));
        script.setCoefficients(BlurCore.BOX_5x5);
        for (int i = 0; i < radius; i++) {
            script.setInput(input);
            script.forEach(output);
            input = output;
        }
        output.copyTo(bitmap);
        return bitmap;
    }


    private static Bitmap doRenderScriptGaussian5x5Blur(Context context,Bitmap bitmap,int radius){
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicConvolve5x5 script = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs));
        script.setCoefficients(BlurCore.GAUSSIAN_5x5);
        for (int i = 0; i < radius; i++) {
            script.setInput(input);
            script.forEach(output);
            input = output;
        }
        output.copyTo(bitmap);
        return bitmap;
    }



    private static Bitmap getScaledBitmap(Bitmap src,float scaled){
        switch (scaledType){
            case SCALED_WITH_FILTER:
                return getScaledBitmapWithFilter(src,scaled);
            default:
                return getScaledBitmapDefault(src,scaled);
        }
    }
    
    private static Bitmap getScaledBitmapDefault(Bitmap src, float scaled){
        Bitmap result = Bitmap.createBitmap((int)(src.getWidth()*scaled),(int)(src.getHeight()*scaled), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.scale(scaled,scaled);
        canvas.drawBitmap(src,0,0,null);
        return result;
    }


    private static Bitmap getScaledBitmapWithFilter(Bitmap src, float scaled){
        return Bitmap.createScaledBitmap(src,(int)(src.getWidth()*scaled),(int)(src.getHeight()*scaled),true);
    }

    private static int getMemorySize(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int uid = 0;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            uid = info.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        List<ActivityManager.RunningAppProcessInfo> infos = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo info:infos){
            if(info.uid == uid){
                return activityManager.getProcessMemoryInfo(new int[]{info.pid})[0].dalvikPrivateDirty;
            }
        }
        return 0;
    }
}
