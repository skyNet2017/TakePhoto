package org.devio.simple.holder;

import android.app.Activity;

import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;

import java.io.File;

/**
 * Created by hss on 2018/12/15.
 */

public class OriginalHolder extends SinglePicHolder {
    public OriginalHolder(Activity context) {
        super(context);
    }

    @Override
    protected CharSequence typeDesc() {
        return "original";
    }

    @Override
    protected void compress(String path, CommonCallback<File> callback) {
        callback.onSuccess(new File(path));
    }


}
