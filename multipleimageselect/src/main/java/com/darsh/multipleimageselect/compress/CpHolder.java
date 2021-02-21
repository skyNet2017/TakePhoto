package com.darsh.multipleimageselect.compress;

import android.app.Activity;

import androidx.annotation.Nullable;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.saf.SafUtil;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.Gson;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifImageView;


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
    GifImageView gif;

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
        gif = rootView.findViewById(R.id.gif_original);

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
        Log.w(SafUtil.TAG,"uri: "+s);
       // ivCompressed.setVisibility(View.GONE);
       // ivOriginal.setVisibility(View.GONE);
        ivOriginalSaf.setVisibility(View.VISIBLE);
        ivCompressedSaf.setVisibility(View.VISIBLE);
        if(!isPreview){
            /*String compressedPath = PhotoCompressHelper.getCompressedFilePath(s,true);
            if(!TextUtils.isEmpty(compressedPath)){
                PhotoCompressHelper.setPathToPreview(ivCompressedSaf,compressedPath);
                tvCompressed.setText(context.getResources().getString(R.string.c_compressed)+":"+ImageInfoFormater.formatImagInfo(compressedPath,true));
                rlCompressed.setVisibility(View.VISIBLE);
            }else {
                rlCompressed.setVisibility(View.GONE);
            }*/
        }else {
            rlCompressed.setVisibility(View.GONE);
        }
        rlCompressed.setVisibility(View.GONE);
        showImage(originalPath,context);
        showInfo(originalPath);




    }

    private void showImage(String s, Context context) {
        if(s.startsWith("content://")){
            if(s.contains(".gif")){
                gif.setVisibility(View.VISIBLE);
                ivOriginalSaf.setVisibility(View.GONE);
                //gif.setScaleType(ImageView.ScaleType.FIT_CENTER);
                gif.setImageURI(Uri.parse(s));
            }else {
                gif.setVisibility(View.GONE);
                ivOriginalSaf.setVisibility(View.VISIBLE);
                ivCompressedSaf.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse(s);
                if(!isPreview){
                    rlCompressed.setVisibility(View.VISIBLE);
                    //tvOriginal.setText(context.getResources().getString(R.string.c_origianl)+":"+ImageInfoFormater.formatImagInfo(originalPath,true));
                }else {
                    rlCompressed.setVisibility(View.GONE);
                    // tvOriginal.setText(ImageInfoFormater.formatImagInfo(originalPath,true));
                }



                //tvOriginal.setText(s);
                Observable.just(uri)
                        .subscribeOn(Schedulers.io())
                        .map(new Function<Uri, InputStreamBitmapDecoderFactory>() {
                            @Override
                            public InputStreamBitmapDecoderFactory apply(Uri uri) throws Exception {
                                // return new InputStreamBitmapDecoderFactory(
                                //        new FileInputStream(context.getContentResolver().openFileDescriptor(uri,"r").getFileDescriptor()));
                                return new InputStreamBitmapDecoderFactory(
                                        context.getContentResolver().openInputStream(uri));
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<InputStreamBitmapDecoderFactory>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(InputStreamBitmapDecoderFactory inputStreamBitmapDecoderFactory) {
                                ivOriginalSaf.setImage(inputStreamBitmapDecoderFactory);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                try {
                    //ivOriginalSaf.setImage());
                   /* ivOriginalSaf.setImage(new InputStreamBitmapDecoderFactory(
                            context.getContentResolver().openInputStream(uri)));*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }else {
            File file = new File(s);
            if(file.exists()){

                if(s.contains(".gif")){
                    gif.setVisibility(View.VISIBLE);
                    ivOriginalSaf.setVisibility(View.GONE);
                    //gif.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    gif.setImageURI(Uri.fromFile(file));
                }else {
                    gif.setVisibility(View.GONE);
                    rlOriginal.setVisibility(View.VISIBLE);
                    PhotoCompressHelper.setPathToPreview(ivOriginalSaf,s);
                }


            }else {
                rlOriginal.setVisibility(View.GONE);
            }
        }
    }

    private void showInfo(String originalPath) {
        Observable.just(originalPath).subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        return ImageInfoFormater.formatImagInfo(s,true);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String i) {
                        if(!isPreview){
                            tvOriginal.setText(rootView.getContext().getResources().getString(R.string.c_origianl)+":"+i);
                        }else {
                            tvOriginal.setText(i);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
