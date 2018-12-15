package org.devio.simple.holder;

import android.app.Activity;

import com.hss01248.lubanturbo.TurboCompressor;

import org.devio.simple.BigPagerHolder;
import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;

import java.io.File;

/**
 * Created by hss on 2018/12/15.
 */

public class TuborOriginalHolder extends SinglePicHolder {
    public TuborOriginalHolder(Activity context) {
        super(context);
    }

    @Override
    protected CharSequence typeDesc() {
        return "turbo";
    }

    @Override
    protected void compress(String path, CommonCallback<File> callback) {
        String outPath = new File(rootView.getContext().getCacheDir(),System.currentTimeMillis()+".jpg").getAbsolutePath();
       boolean success =  TurboCompressor.compressOringinal(path, BigPagerHolder.quality,outPath);
       callback.onSuccess(new File(outPath));
    }
}
