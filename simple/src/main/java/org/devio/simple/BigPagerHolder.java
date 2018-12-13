package org.devio.simple;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.SuperPagerHolder;
import com.light.body.Light;
import org.devio.takephoto.model.TImage;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by huangshuisheng on 2018/12/13.
 */

public class BigPagerHolder extends SuperPagerHolder<TImage,Activity> {

    SubsamplingScaleImageView imageView1;
    SubsamplingScaleImageView imageView2;
    SubsamplingScaleImageView imageView3;
    public BigPagerHolder(Activity context) {
        super(context);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.image_show;
    }

    @Override
    protected void findViewsById(View view) {
         imageView1 = (SubsamplingScaleImageView) view.findViewById(R.id.imgShow1);
         imageView2 = (SubsamplingScaleImageView) view.findViewById(R.id.imgShow2);
        imageView3 = (SubsamplingScaleImageView) view.findViewById(R.id.imgShow3);
    }

    @Override
    public void assingDatasAndEvents(Activity activity, @Nullable TImage tImage, int i) {
        if(!TextUtils.isEmpty(tImage.getOriginalPath())){
            imageView1.setImage(ImageSource.uri(tImage.getOriginalPath()));
            Log.d("dd","original path:"+tImage.getOriginalPath()+",size:"+formatFileSize(new File(tImage.getOriginalPath()).length()));

            //压缩:
            //CompressArgs args = new CompressArgs.Builder().quality(70).ignoreSize(false).build();
            String outPath = new File(activity.getCacheDir(),System.currentTimeMillis()+".jpg").getAbsolutePath();
            Light.getInstance().compress(tImage.getOriginalPath(), outPath);
            imageView2.setImage(ImageSource.uri(outPath));

            Log.d("dd","compressed by c++,path:"+outPath+",size:"+formatFileSize(new File(outPath).length()));

        }
        if(!TextUtils.isEmpty(tImage.getCompressPath())){
            imageView3.setImage(ImageSource.uri(tImage.getCompressPath()));
            Log.d("dd","compressed by luban path:"+tImage.getCompressPath()+",size:"+formatFileSize(new File(tImage.getCompressPath()).length()));
        }



    }

    public static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".00");
            if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "MB";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "KB";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }
}
