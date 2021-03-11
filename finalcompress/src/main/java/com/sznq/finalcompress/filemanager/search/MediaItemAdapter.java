package com.sznq.finalcompress.filemanager.search;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.sznq.finalcompress.R;

public class MediaItemAdapter extends BaseQuickAdapter<BaseMediaInfo, BaseViewHolder> {
    public MediaItemAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, BaseMediaInfo item) {
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_info, ImageInfoFormater.formatTime(item.updatedTime)+"-"+ ImageInfoFormater.formatFileSize(item.fileSize));
        helper.setGone(R.id.iv_folder,true);


    }
}
