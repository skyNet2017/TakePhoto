package com.sznq.finalcompress.filemanager.search;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
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

public class MediaItemImgAdapter extends BaseQuickAdapter<BaseInfo, BaseViewHolder> {
    public MediaItemImgAdapter(int layoutResId) {
        super(layoutResId);
    }
    Drawable drawable = new ColorDrawable(Color.GRAY);
    @Override
    protected void convert(@NonNull BaseViewHolder helper, BaseInfo item) {

        helper.setText(R.id.tv_info, ImageInfoFormater.formatTime(item.getUpdatedTime())+"-"+ ImageInfoFormater.formatFileSize(item.getFileSize())+"\n"+item.getName());
        int type = FileTypeUtil.getTypeByFileName(item.getPath());
        boolean showImg = false;
        if(type == BaseMediaInfo.TYPE_IMAGE){
            showImg = true;
            showImg(helper,item);

        }else if(type == BaseMediaInfo.TYPE_VIDEO){
            if(item.getPath().startsWith("content") || item.getPath().startsWith("/storage/")){
                showImg = true;
                showImg(helper,item);
            }
        }
        if(!showImg){
            helper.setImageDrawable(R.id.iv_img,helper.itemView.getResources().getDrawable(R.drawable.image_placeholder));
        }


    }

    private void showImg(BaseViewHolder helper, BaseInfo item) {
        Glide.with(helper.itemView)
                .load(item.getPath())
                .thumbnail(0.2f)
                .placeholder(R.drawable.image_placeholder)
                .fitCenter()
                .into((ImageView) helper.getView(R.id.iv_img));
    }
}
