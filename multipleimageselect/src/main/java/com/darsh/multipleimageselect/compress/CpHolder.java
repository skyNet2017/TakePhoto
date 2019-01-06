package com.darsh.multipleimageselect.compress;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.darsh.multipleimageselect.R;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.imginfo.ImageInfoFormater;



import java.io.File;


/**
 * Created by hss on 2018/12/16.
 */

public class CpHolder extends SuperPagerHolder<String, Activity> {


    SubsamplingScaleImageView ivOriginal;

    TextView tvOriginal;

    RelativeLayout rlOriginal;

    SubsamplingScaleImageView ivCompressed;

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
        return R.layout.item_vp_compressed;
    }

    @Override
    protected void findViewsById(View rootView) {

        llCompress = (LinearLayout) rootView.findViewById(R.id.ll_compress);
        rlOriginal = (RelativeLayout) rootView.findViewById(R.id.rl_original);
        ivOriginal = (SubsamplingScaleImageView) rootView.findViewById(R.id.iv_original);
        tvOriginal = (TextView) rootView.findViewById(R.id.tv_original);
        rlCompressed = (RelativeLayout) rootView.findViewById(R.id.rl_compressed);
        ivCompressed = (SubsamplingScaleImageView) rootView.findViewById(R.id.iv_compressed);
        tvCompressed = (TextView) rootView.findViewById(R.id.tv_compressed);

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
       File file = new File(s);
       if(file.exists()){
           rlOriginal.setVisibility(View.VISIBLE);
           PhotoCompressHelper.setPathToPreview(ivOriginal,originalPath);
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
               PhotoCompressHelper.setPathToPreview(ivCompressed,compressedPath);
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
