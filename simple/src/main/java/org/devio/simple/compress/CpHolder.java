package org.devio.simple.compress;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hss01248.adapter.SuperPagerHolder;

import org.devio.simple.PhotoUtil;
import org.devio.simple.R;
import org.devio.simple.SinglePicHolder;
import org.devio.simple.holder.OriginalHolder;

/**
 * Created by hss on 2018/12/16.
 */

public class CpHolder extends SuperPagerHolder<String,Activity> {
    LinearLayout layout;
    OriginalHolder originalHolder;
    OriginalHolder compressedHolder;
    public CpHolder(Activity context) {
        super(context);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.item_vp_compressed;
    }

    @Override
    protected void findViewsById(View rootView) {
        layout = rootView.findViewById(R.id.ll_compress);
        layout.setOrientation(LinearLayout.VERTICAL);
        originalHolder = new OriginalHolder(activity);
        compressedHolder = new OriginalHolder(activity);
        layout.addView(originalHolder.rootView);
        layout.addView(compressedHolder.rootView);
    }


    @Override
    public void assingDatasAndEvents(Activity context, @Nullable String s, int i) {
        originalHolder.assingDatasAndEvents(context,s);
        String path = PhotoUtil.getCompressedFilePath(s,true);
        if(!TextUtils.isEmpty(path)){
            compressedHolder.assingDatasAndEvents(context, path);
        }

    }
}
