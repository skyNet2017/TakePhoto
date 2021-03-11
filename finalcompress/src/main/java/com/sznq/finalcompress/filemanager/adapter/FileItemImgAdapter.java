package com.sznq.finalcompress.filemanager.adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.sznq.finalcompress.R;

import java.util.Collections;

public class FileItemImgAdapter extends BaseQuickAdapter<IFile, BaseViewHolder> {
    public FileItemImgAdapter(int layoutResId) {
        super(layoutResId);
    }
    ColorDrawable drawable = new ColorDrawable(Color.GRAY);
    @Override
    protected void convert(@NonNull BaseViewHolder helper, IFile item) {

        helper.setText(R.id.tv_info, ImageInfoFormater.formatTime(item.lastModified())+"-"+ ImageInfoFormater.formatFileSize(item.length())+"\n"+item.getName());
        int type = FileTypeUtil.getTypeByFileName(item.getName());
        boolean showImg = false;
        if(type == BaseMediaInfo.TYPE_IMAGE){
            showImg = true;
            showImg(helper,item);

        }else if(type == BaseMediaInfo.TYPE_VIDEO){
            if(item instanceof JavaFile  || item instanceof IDocumentFile){
                showImg = true;
                showImg(helper,item);
            }
        }
        if(!showImg){
            helper.setImageDrawable(R.id.iv_img,drawable);
        }


    }

    private void showImg(BaseViewHolder helper, IFile item) {
        Glide.with(helper.itemView)
                .load(item.getPath())
                .thumbnail(0.2f)
                .into((ImageView) helper.getView(R.id.iv_img));
    }
}