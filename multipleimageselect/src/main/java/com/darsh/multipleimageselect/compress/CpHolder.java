package com.darsh.multipleimageselect.compress;

import android.app.Activity;

import androidx.annotation.Nullable;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.saf.SafUtil;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * Created by hss on 2018/12/16.
 */

public class CpHolder extends SuperPagerHolder<String, Activity> {


    //SubsamplingScaleImageView ivOriginal;

    TextView tvOriginal;

    RelativeLayout rlOriginal;

   // SubsamplingScaleImageView ivCompressed;
    LargeImageView ivOriginalSaf;
    LargeImageView ivCompressedSaf;

    TextView tvCompressed;

    RelativeLayout rlCompressed;

    LinearLayout llCompress;

    public CpHolder setPreview(boolean preview) {
        isPreview = preview;
        return this;
    }

    boolean isPreview;

    public CpHolder(Activity context, ViewGroup parent) {
        super(context, parent);
    }


    @Override
    protected int setLayoutRes() {
        int oritation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if(oritation == Surface.ROTATION_90 || oritation == Surface.ROTATION_270){
            return R.layout.item_vp_compressed_landscape;
        }
        return R.layout.item_vp_compressed;
    }

    @Override
    protected void findViewsById(View rootView) {

        llCompress = (LinearLayout) rootView.findViewById(R.id.ll_compress);
        rlOriginal = (RelativeLayout) rootView.findViewById(R.id.rl_original);
        //ivOriginal = (SubsamplingScaleImageView) rootView.findViewById(R.id.iv_original);
        tvOriginal = (TextView) rootView.findViewById(R.id.tv_original);
        rlCompressed = (RelativeLayout) rootView.findViewById(R.id.rl_compressed);
       // ivCompressed = (SubsamplingScaleImageView) rootView.findViewById(R.id.iv_compressed);
        tvCompressed = (TextView) rootView.findViewById(R.id.tv_compressed);
        ivOriginalSaf = rootView.findViewById(R.id.iv_original_saf);
        ivCompressedSaf = rootView.findViewById(R.id.iv_compressed_saf);

    }

    public void switchDec(boolean show){
        if(show){
            tvCompressed.setVisibility(View.VISIBLE);
            tvOriginal.setVisibility(View.VISIBLE);
        }else {
            tvCompressed.setVisibility(View.GONE);
            tvOriginal.setVisibility(View.GONE);
        }
    }




    @Override
    public void assingDatasAndEvents(Activity context, @Nullable String s, int i) {
        String originalPath = s;
        Log.d(SafUtil.TAG,"uri: "+s);
       // ivCompressed.setVisibility(View.GONE);
       // ivOriginal.setVisibility(View.GONE);
        ivOriginalSaf.setVisibility(View.VISIBLE);
        ivCompressedSaf.setVisibility(View.VISIBLE);
        if(s.startsWith("content://")){

            ivOriginalSaf.setVisibility(View.VISIBLE);
            ivCompressedSaf.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(s);
            if(!isPreview){
                rlCompressed.setVisibility(View.VISIBLE);
                tvOriginal.setText(context.getResources().getString(R.string.c_origianl)+":"+ImageInfoFormater.formatImagInfo(originalPath,true));
            }else {
                rlCompressed.setVisibility(View.GONE);
                tvOriginal.setText(ImageInfoFormater.formatImagInfo(originalPath,true));
            }

            //tvOriginal.setText(s);
            try {
                ivOriginalSaf.setImage(new InputStreamBitmapDecoderFactory(
                        new FileInputStream(context.getContentResolver().openFileDescriptor(uri,"r").getFileDescriptor())));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }







       File file = new File(s);
       if(file.exists()){
           rlOriginal.setVisibility(View.VISIBLE);
           PhotoCompressHelper.setPathToPreview(ivOriginalSaf,originalPath);
           if(isPreview){
               tvOriginal.setText(ImageInfoFormater.formatImagInfo(originalPath,true));
           }else {
               tvOriginal.setText(context.getResources().getString(R.string.c_origianl)+":"+ImageInfoFormater.formatImagInfo(originalPath,true));
           }

       }else {
           rlOriginal.setVisibility(View.GONE);
       }


       if(!isPreview){
           String compressedPath = PhotoCompressHelper.getCompressedFilePath(s,true);
           if(!TextUtils.isEmpty(compressedPath)){
               PhotoCompressHelper.setPathToPreview(ivCompressedSaf,compressedPath);
               tvCompressed.setText(context.getResources().getString(R.string.c_compressed)+":"+ImageInfoFormater.formatImagInfo(compressedPath,true));
               rlCompressed.setVisibility(View.VISIBLE);
           }else {
               rlCompressed.setVisibility(View.GONE);
           }
       }else {
           rlCompressed.setVisibility(View.GONE);
       }
    }
}
