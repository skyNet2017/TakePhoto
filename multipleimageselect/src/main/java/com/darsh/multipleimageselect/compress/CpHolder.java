package com.darsh.multipleimageselect.compress;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.cache.DiskCache;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.EmptySignature;
import com.bumptech.glide.util.LruCache;
import com.bumptech.glide.util.Util;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.activities.ImageSelectActivity;
import com.darsh.multipleimageselect.helpers.LoggingListener;
import com.darsh.multipleimageselect.saf.SafUtil;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.Gson;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.smb.FileApiForSmb;
import com.hss01248.media.mymediastore.smb.SmbToHttp;
import com.hss01248.media.mymediastore.smb.SmbjUtil;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
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
    ImageView ivGlide;
    TextView tvProgress;

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

        tvProgress = rootView.findViewById(R.id.tv_progress);

        ivGlide = rootView.findViewById(R.id.iv_glide);

    }

    public void switchDec(boolean show){
        if(show){
            tvCompressed.setVisibility(View.GONE);
            tvOriginal.setVisibility(View.GONE);
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


        //showImage(originalPath,context);

       /* if(getShare() != null){
            showImage2(s,context,i);
        }else {
            showImage(originalPath,context);
        }*/

        //showImage(originalPath,context);
        showImage2(s,context,i);
        showInfo(originalPath);




    }

    private void showImage2(String s, Activity context,int idx) {

        if(s.contains(".gif")){
            gif.setVisibility(View.VISIBLE);
            ivOriginalSaf.setVisibility(View.GONE);
            //gif.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if(s.startsWith("/storage/")){
                gif.setImageURI(Uri.fromFile(new File(s)));
            }else {
                if( s.startsWith("http")){
                    String http = s;
                    getFile(http, context, new Observer<File>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull File resource) {

                            try {
                                gif.setImageURI(Uri.fromFile(resource));
                                tvOriginal.setText(URLDecoder.decode(s)+"\n"+ ImageInfoFormater.formatFileSize(resource.length()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                tvOriginal.setText(e.getMessage());
                            }
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            getByGlide(http,s,context);
                        }

                        @Override
                        public void onComplete() {

                        }
                    });

                }else {
                    gif.setImageURI(Uri.parse(s));
                }

            }

            //下载,然后播放

        }else {
            gif.setVisibility(View.GONE);
            ivOriginalSaf.setVisibility(View.VISIBLE);
            ivCompressedSaf.setVisibility(View.VISIBLE);
            //Uri uri = Uri.parse(s);
            if(!isPreview){
                rlCompressed.setVisibility(View.VISIBLE);
                //tvOriginal.setText(context.getResources().getString(R.string.c_origianl)+":"+ImageInfoFormater.formatImagInfo(originalPath,true));
            }else {
                rlCompressed.setVisibility(View.GONE);
                // tvOriginal.setText(ImageInfoFormater.formatImagInfo(originalPath,true));
            }


            if(s.startsWith("smb") || s.startsWith("http")){
                String http = SmbToHttp.getHttpUrlFromSmb(s);
                 getFile(http, context, new Observer<File>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull File resource) {

                        try {
                            ivOriginalSaf.setImage(new InputStreamBitmapDecoderFactory(new FileInputStream(resource)));
                            tvOriginal.setText(URLDecoder.decode(s)+"\n"+ ImageInfoFormater.formatFileSize(resource.length()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            tvOriginal.setText(e.getMessage());
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        getByGlide(http,s,context);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

            }else {
                Observable.just(s)
                        .subscribeOn(Schedulers.io())
                        .map(new Function<String, InputStreamBitmapDecoderFactory>() {
                            @Override
                            public InputStreamBitmapDecoderFactory apply(String uri) throws Exception {
                                if(uri.startsWith("content")){
                                    return new InputStreamBitmapDecoderFactory(
                                            context.getContentResolver().openInputStream(Uri.parse(s)));
                                }else if(uri.startsWith("/storage/")){
                                    return new InputStreamBitmapDecoderFactory(new FileInputStream(new File(s)));
                                }
                                return new InputStreamBitmapDecoderFactory(new FileInputStream(new File(s)));
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
            }

            //tvOriginal.setText(s);


        }
    }
    Handler handler = new Handler(Looper.getMainLooper());

    private void getByGlide(String http, String s, Activity context) {

        ProgressManager.getInstance().addResponseListener(http, new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tvProgress.setText(progressInfo.getPercent()+"% , speed: "+(progressInfo.getSpeed()/1024/8)+"KB/s");
                        }catch (Throwable throwable){
                            throwable.printStackTrace();
                        }

                    }
                });

            }

            @Override
            public void onError(long id, Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvProgress.setText(e.getMessage());
                    }
                });

            }
        });
        Glide.with(context)
                .load(http)
                .priority(Priority.IMMEDIATE)

                .listener(new RequestListener< Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("GLIDE", String.format(Locale.ROOT,
                                "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.w("GLIDE", String.format(Locale.ROOT,
                                "onResourceReady(%s, %s, %s, %s)", resource, model, target, isFirstResource));
                        Glide.with(context)
                                .load(http)
                                // .priority(Priority.HIGH)
                                .downloadOnly(new SimpleTarget<File>() {
                                    @Override
                                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                        try {
                                            Log.w("glide","onResourceReady->"+resource.getAbsolutePath());
                                            if(s.contains(".gif")){
                                                gif.setImageURI(Uri.fromFile(resource));
                                            }else {
                                                ivOriginalSaf.setImage(new InputStreamBitmapDecoderFactory(new FileInputStream(resource)));
                                            }

                                            tvOriginal.setText(URLDecoder.decode(s)+"\n"+ ImageInfoFormater.formatFileSize(resource.length()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            tvOriginal.setText(e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        super.onLoadFailed(errorDrawable);

                                    }
                                });
                        return false;
                    }
                })
                .into(ivGlide);
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
               // rlOriginal.setVisibility(View.GONE);
                //FileApiForSmb smb =
            }
        }
    }

    public void getFile(String url, Context context, Observer<File> callable){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = getFileFromCache(url,context);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(file != null){
                            callable.onNext(file);
                        }else {
                            callable.onError(new Throwable("file not exist"));
                        }
                    }
                });

            }
        }).start();
    }

    public File getFileFromCache(String url,Context context){
        try {
            //不能在主线程
            File file  =  Glide.with(context).downloadOnly().load(url).apply(new RequestOptions().onlyRetrieveFromCache(true)).submit().get();
            if(file.exists()){
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isCached(String url,Context context) {



        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(originalKey);
        try {
            DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null && value.getFile(0).exists() && value.getFile(0).length() > 30) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private static class OriginalKey implements Key {

        private final String id;
        private final Key signature;

        public OriginalKey(String id, Key signature) {
            this.id = id;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            OriginalKey that = (OriginalKey) o;

            if (!id.equals(that.id)) {
                return false;
            }
            if (!signature.equals(that.signature)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            try {
                messageDigest.update(id.getBytes(STRING_CHARSET_NAME));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            signature.updateDiskCacheKey(messageDigest);
        }
    }

    private static class SafeKeyGenerator {
        private final LruCache<Key, String> loadIdToSafeHash = new LruCache<Key, String>(1000);

        public String getSafeKey(Key key) {
            String safeKey;
            synchronized (loadIdToSafeHash) {
                safeKey = loadIdToSafeHash.get(key);
            }
            if (safeKey == null) {
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    key.updateDiskCacheKey(messageDigest);
                    safeKey = Util.sha256BytesToHex(messageDigest.digest());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                synchronized (loadIdToSafeHash) {
                    loadIdToSafeHash.put(key, safeKey);
                }
            }
            return safeKey;
        }
    }

    private void showInfo(String originalPath) {
        Observable.just(originalPath).subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        if(s.startsWith("http") || s.startsWith("smb")){
                            return s;
                        }else {
                            return ImageInfoFormater.formatImagInfo(s,true);
                        }

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
                            tvOriginal.setText(URLDecoder.decode(i));
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
