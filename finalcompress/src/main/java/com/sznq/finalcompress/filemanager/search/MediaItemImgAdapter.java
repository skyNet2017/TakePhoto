package com.sznq.finalcompress.filemanager.search;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.sznq.finalcompress.R;

import java.util.Collections;

public class MediaItemImgAdapter extends BaseQuickAdapter<IFile, BaseViewHolder> {
    public MediaItemImgAdapter(int layoutResId) {
        super(layoutResId);
    }

    Drawable drawable = new ColorDrawable(Color.GRAY);

    @Override
    protected void convert(@NonNull BaseViewHolder helper, IFile item) {

        if (FilterViewHolder.disPlayMode == 0) {
            //去掉adjustviewbonds
            ImageView imageView = helper.getView(R.id.iv_img);
            imageView.setAdjustViewBounds(false);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        helper.setGone(R.id.tv_info, true);
        if(FilterViewHolder.disPlayMode == 2 || FileTypeUtil.getTypeByFileName(item.getName()) != BaseMediaInfo.TYPE_IMAGE){
            helper.setText(R.id.tv_info, ImageInfoFormater.formatTime(item.lastModified())+"-"+ ImageInfoFormater.formatFileSize(item.length())+"\n"+item.getName());
        }else {
            helper.setText(R.id.tv_info, ImageInfoFormater.formatTime(item.lastModified())+"-"+ ImageInfoFormater.formatFileSize(item.length()));
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
            RequestBuilder<Drawable> builder = Glide.with(helper.itemView)
                    .load(item.getPath())
                    .thumbnail(0.2f)
                    .placeholder(R.drawable.image_placeholder);
            if (FilterViewHolder.disPlayMode == 2) {
                builder.centerCrop().listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (model != null && model.equals(item.getPath())) {
                            return false;
                        }
                        return true;

                    }
                }).into((ImageView) helper.getView(R.id.iv_img));
            } else {
                builder.listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (model != null && model.equals(item.getPath())) {
                            return false;
                        }
                        return true;

                    }
                }).into((ImageView) helper.getView(R.id.iv_img));
            }

        }

    }
}
