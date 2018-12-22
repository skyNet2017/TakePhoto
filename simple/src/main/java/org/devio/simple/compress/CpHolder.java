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
import com.hss01248.imginfo.ImageInfoFormater;

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
    LinearLayout rlOriginal;
    @BindView(R.id.iv_compressed)
    SubsamplingScaleImageView ivCompressed;
    @BindView(R.id.tv_compressed)
    TextView tvCompressed;
    @BindView(R.id.rl_compressed)
    LinearLayout rlCompressed;
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
        tvOriginal.setText(ImageInfoFormater.formatImagInfo(originalPath,true));
        if(!TextUtils.isEmpty(compressedPath)){
            PhotoUtil.setPathToPreview(ivCompressed,compressedPath);
            tvCompressed.setText(ImageInfoFormater.formatImagInfo(originalPath,true));
        }




    }
}
