package org.devio.simple.holder;

import android.app.Activity;
import android.view.ViewGroup;

import com.light.body.CompressArgs;
import com.light.body.Light;

import org.devio.simple.BigPagerHolder;
import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;

import java.io.File;

/**
 * Created by hss on 2018/12/15.
 */

public class CppHolder extends SinglePicHolder {


    public CppHolder(Activity context, ViewGroup parent) {
        super(context, parent);
    }

    @Override
    protected CharSequence typeDesc() {
        return "libjpeg-turbo";
    }

    @Override
    protected void compress(String path, CommonCallback<File> callback) {
        CompressArgs args = new CompressArgs.Builder().ignoreSize(false).quality(BigPagerHolder.quality).build();
        String outPath = new File(activity.getCacheDir(),System.currentTimeMillis()+".jpg").getAbsolutePath();
        Light.getInstance().compress(path,args, outPath);
        callback.onSuccess(new File(outPath));
    }


}
