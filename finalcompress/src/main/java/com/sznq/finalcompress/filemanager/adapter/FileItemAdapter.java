package com.sznq.finalcompress.filemanager.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.sznq.finalcompress.R;

public class FileItemAdapter extends BaseQuickAdapter<IFile, BaseViewHolder> {
    public FileItemAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, IFile item) {
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_info, ImageInfoFormater.formatTime(item.lastModified())+"-"+ ImageInfoFormater.formatFileSize(item.length()));
        helper.setGone(R.id.iv_folder,item.isDirectory());


    }
}
