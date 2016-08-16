package com.anarchy.blurdemo;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableFloat;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.widget.SeekBar;

import com.anarchy.blurdemo.blur.BlurUtils;

/**
 * Version 2.1.1
 * <p/>
 * Date: 16/8/16 10:30
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 * Copyright © 2014-2016 Shanghai Xiaotu Network Technology Co., Ltd.
 */
public class BlurInfo {
    public int maxBlurRadius = 50;
    public ObservableInt blurRadius = new ObservableInt();
    public ObservableFloat scaleRatio = new ObservableFloat();
    public ObservableField<Bitmap> bitmap = new ObservableField<>();
    public ObservableLong duration = new ObservableLong();
    public Bitmap sourceBitmap;
    public float maxScaleRatio = 1f;
    public int selectedPosition;
    private Context context;
    public SeekBar.OnSeekBarChangeListener onBlurChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            blurRadius.set(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public SeekBar.OnSeekBarChangeListener onScaleChange = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            scaleRatio.set(progress/1000f);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public BlurInfo(Context context){
        this.context = context;
        blurRadius.set(20);
        scaleRatio.set(0.125f);
    }

    public void setSourceBitmap(@DrawableRes int drawableRes){
        sourceBitmap = BlurUtils.getBitmapFromDrawable(ContextCompat.getDrawable(context,drawableRes));
        bitmap.set(sourceBitmap);
    }


    public void setSourceBitmap(Bitmap b){
        sourceBitmap = b;
        bitmap.set(sourceBitmap);
    }

    public void doBlur(){
        switch (selectedPosition){
            case 0:
                updateBitmap(BlurUtils.CONFIG.FAST_BLUR);
                break;
            case 1:
                updateBitmap(BlurUtils.CONFIG.FAST_BLUR_JNI);
                break;
            case 2:
                updateBitmap(BlurUtils.CONFIG.BOX_BLUR);
                break;
            case 3:
                updateBitmap(BlurUtils.CONFIG.GAUSSIA_FAST_BLUR);
                break;
            case 4:
                updateBitmap(BlurUtils.CONFIG.STACK_BLUR);
                break;
            case 5:
                updateBitmap(BlurUtils.CONFIG.RENDER_SCRIPT);
                break;
            case 6:
                updateBitmap(BlurUtils.CONFIG.RENDER_SCRIPT_3X3);
                break;
            case 7:
                updateBitmap(BlurUtils.CONFIG.RENDER_SCRIPT_5X5);
                break;
            case 8:
                updateBitmap(BlurUtils.CONFIG.RENDER_SCRIPT_GAUSSIA_5X5);
                break;
        }
    }


    private void updateBitmap(BlurUtils.CONFIG config){
        bitmap.set(BlurUtils.doBlur(context,this,sourceBitmap,scaleRatio.get(),blurRadius.get(),config));
    }
}
