package com.sznq.finalcompress.filemanager.search;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.sznq.finalcompress.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MediaItemImgAdapter extends BaseQuickAdapter<IFile, BaseViewHolder> {
    public MediaItemImgAdapter(int layoutResId) {
        super(layoutResId);
    }

    Drawable drawable = new ColorDrawable(Color.GRAY);
    static int width = ScreenUtils.getScreenWidth()/3;

    @Override
    protected void convert(@NonNull BaseViewHolder helper, IFile item) {
        ImageView imageView = helper.getView(R.id.iv_img);
        if (FilterViewHolder.disPlayMode == 0) {
            //去掉adjustviewbonds
            imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            if(layoutParams != null){
                layoutParams.height = width;
                layoutParams.width = width;
            }else {
                layoutParams = new ViewGroup.LayoutParams(width,width);
            }
            imageView.setLayoutParams(layoutParams);
        }else {
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        helper.setGone(R.id.tv_info, true);
        if(FilterViewHolder.disPlayMode == 2 || FileTypeUtil.getTypeByFileName(item.getName()) != BaseMediaInfo.TYPE_IMAGE){
            helper.setText(R.id.tv_info,
                    ImageInfoFormater.formatFileSize(item.length())+"\n"+ImageInfoFormater.formatTime(item.lastModified())+" "+item.getName());
        }else {
            helper.setText(R.id.tv_info,
                    ImageInfoFormater.formatFileSize(item.length())+"\n"+ ImageInfoFormater.formatTime(item.lastModified()));//+"\n"+item.getName()
        }


        checkIfShowImg(helper, item);


    }

    public static void checkIfShowImg(BaseViewHolder helper, IFile item) {
        int type = FileTypeUtil.getTypeByFileName(item.getPath());
        boolean showImg = false;
        if (type == BaseMediaInfo.TYPE_IMAGE) {
            showImg = true;
            showImg(helper, item);

        } else if (type == BaseMediaInfo.TYPE_VIDEO) {
            if (item.getPath().startsWith("content") || item.getPath().startsWith("/storage/")) {
                showImg = true;
                showImg(helper, item);
            }
        }
        if (!showImg) {
            if (item instanceof BaseMediaInfo || item instanceof BaseMediaFolderInfo) {
                helper.setImageDrawable(R.id.iv_img, helper.itemView.getResources().getDrawable(R.drawable.image_placeholder));
            } else {
                helper.setGone(R.id.iv_img, item.isDirectory());
            }

        }
    }

    private static void showImg(BaseViewHolder helper, IFile item) {
        if (item instanceof BaseMediaFolderInfo) {
            if (!TextUtils.isEmpty(((BaseMediaFolderInfo) item).getCover())) {
                Glide.with(helper.itemView)
                        .load(((BaseMediaFolderInfo) item).getCover())
                        .thumbnail(0.2f)
                        .placeholder(R.drawable.image_placeholder)
                        //.fitCenter()
                        .into((ImageView) helper.getView(R.id.iv_img));
            } else {
                helper.setImageDrawable(R.id.iv_img, helper.itemView.getResources().getDrawable(R.drawable.image_placeholder));
            }

        } else {

            String path = item.getPath();

            path = path.replace(":9265/",":8080/img?path=");
            RequestBuilder<Drawable> builder = Glide.with(helper.itemView)
                    .load(path)
                    //.thumbnail(0.2f)
                    .placeholder(R.drawable.image_placeholder);
            String finalPath = path;
            if (FilterViewHolder.disPlayMode == 0) {
                //.override(width,width).centerCrop()

                builder.listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        helper.itemView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if(event.getX()<30 && event.getY()<30){
                                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                                        showInfo(e,model,v.getContext());
                                    }

                                }
                                return false;
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;

                    }
                }).into((ImageView) helper.getView(R.id.iv_img));
            } else {
                String finalPath1 = path;
                builder.listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        helper.itemView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if(event.getX()<30 && event.getY()<30){
                                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                                        showInfo(e,model,v.getContext());
                                    }
                                }
                                return false;
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                      /*  if (model != null && model.equals(finalPath1)) {
                            return false;
                        }
                        return true;*/
                        return false;

                    }
                }).into((ImageView) helper.getView(R.id.iv_img));
            }
            if(path.startsWith("/storage/")){
                if(!new File(path).exists()){
                    BaseMediaInfo mediaInfo = BaseMediaInfo.fromJavaFile(new File(path));
                    FileDbUtil.delete(mediaInfo);
                    return;
                }
            }
        }

    }

    private static void showInfo(GlideException e, Object model, Context context) {
        StringBuilder msg = new StringBuilder();
        msg.append(model)
                .append("\n\n");
        if(e != null){
            msg.append(e.getClass().getSimpleName())
                    .append(": ")
                    .append(e.getMessage());
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("info")
                .setMessage(msg.toString())
                .setPositiveButton("ok",null).create();
        dialog.show();
    }
}
