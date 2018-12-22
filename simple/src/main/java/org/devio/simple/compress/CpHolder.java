package org.devio.simple.compress;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.SuperPagerHolder;

import org.devio.simple.PhotoUtil;
import org.devio.simple.R;

import butterknife.BindView;

/**
 * Created by hss on 2018/12/16.
 */

public class CpHolder extends SuperPagerHolder<String, Activity> {

    @BindView(R.id.iv_original)
    SubsamplingScaleImageView ivOriginal;
    @BindView(R.id.tv_original)
    TextView tvOriginal;
    @BindView(R.id.rl_original)
    RelativeLayout rlOriginal;
    @BindView(R.id.iv_compressed)
    SubsamplingScaleImageView ivCompressed;
    @BindView(R.id.tv_compressed)
    TextView tvCompressed;
    @BindView(R.id.rl_compressed)
    RelativeLayout rlCompressed;
    @BindView(R.id.ll_compress)
    LinearLayout llCompress;

    public CpHolder(Activity context) {
        super(context);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.item_vp_compressed;
    }

    @Override
    protected void findViewsById(View rootView) {


    }


    @Override
    public void assingDatasAndEvents(Activity context, @Nullable String s, int i) {
        String originalPath = s;
        String compressedPath = PhotoUtil.getCompressedFilePath(s,true);
        PhotoUtil.setPathToPreview(ivOriginal,originalPath);
        tvOriginal.setText(PhotoUtil.formatImagInfo(originalPath));
        if(!TextUtils.isEmpty(compressedPath)){
            PhotoUtil.setPathToPreview(ivCompressed,originalPath);
            tvCompressed.setText(PhotoUtil.formatImagInfo(originalPath));
        }




    }
}
