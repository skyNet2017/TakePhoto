package org.devio.simple.holder;

import android.app.Activity;
import android.os.Environment;
import android.view.ViewGroup;

import com.hss01248.lubanturbo.TurboCompressor;

import org.devio.simple.BigPagerHolder;
import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;

import java.io.File;

/**
 * Created by hss on 2018/12/15.
 */

public class TuborOriginalHolder extends SinglePicHolder {


    public TuborOriginalHolder(Activity context, ViewGroup parent) {
        super(context, parent);
    }

    @Override
    protected CharSequence typeDesc() {
        return "turbo";
    }

    @Override
    protected void compress(String path, CommonCallback<File> callback) {
        File file = new File(path);
        String name = file.getName();
        File dir = new File(file.getParentFile(),file.getParentFile().getName()+"-compressed");
        if(!dir.exists()){
            dir.mkdirs();
        }
        String outPath = new File(dir,name).getAbsolutePath();
       boolean success =  TurboCompressor.compressOringinal(path, BigPagerHolder.quality,outPath);
       callback.onSuccess(new File(outPath));
    }
}
