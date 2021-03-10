package com.sznq.finalcompress.filemanager.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hss01248.media.mymediastore.TfCardBean;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.sznq.finalcompress.R;

public class StorageAdapter extends BaseQuickAdapter<StorageBean, BaseViewHolder> {
    public StorageAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, StorageBean item) {
        helper.setText(R.id.tv_info,item.getIp());
    }
}
