package org.devio.simple;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.CommonViewHolder;

import org.devio.takephoto.model.TImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;

/**
 * Created by hss on 2018/12/15.
 */

public abstract class SinglePicHolder extends CommonViewHolder<String,Activity> {


    protected SubsamplingScaleImageView imageView;
    protected TextView tvInfo;
    protected String path;
    protected TextView tvType;
    protected Button btnRxtra;

    public SinglePicHolder(Activity context, ViewGroup parent) {
        super(context, parent);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.image;
    }

    @Override
    protected void findViewsById(View rootView) {
        super.findViewsById(rootView);
        imageView = rootView.findViewById(R.id.imgShow1);
        tvInfo = rootView.findViewById(R.id.tv_size_original);
        tvType = rootView.findViewById(R.id.tv_type);
        btnRxtra = rootView.findViewById(R.id.btn_extra);
    }

    @Override
    public void assingDatasAndEvents(Activity activity, @Nullable String path) {
        this.path = path;
        displayImageAndInfo(path,0);
        tvType.setText(typeDesc());
    }

    protected abstract CharSequence typeDesc();

    public void doCompress(){
        final long startTime = System.currentTimeMillis();
        compress(path,new CommonCallback<File>() {
            @Override
            public void onSuccess(File file) {
                long duration = System.currentTimeMillis() - startTime;
                displayImageAndInfo(file.getAbsolutePath(),duration);
            }

            @Override
            public void onError(Throwable e) {
                long duration = System.currentTimeMillis() - startTime;
                tvInfo.setText(e.getMessage()+",cost :"+duration+"ms");
            }
        });
    }

    protected abstract void compress(String path,CommonCallback<File> callback);

    protected void displayImageAndInfo(String path,long duration) {
        imageView.setImage(ImageSource.uri(path));
        tvInfo.setText(PhotoUtil.formatExifs(path)+"\n cost:"+duration+"ms");
    }
}
