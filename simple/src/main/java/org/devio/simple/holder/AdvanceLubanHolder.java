package org.devio.simple.holder;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;

import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;

import java.io.File;

import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

/**
 * Created by hss on 2018/12/15.
 */

public class AdvanceLubanHolder extends SinglePicHolder {


    public AdvanceLubanHolder(Activity context, ViewGroup parent) {
        super(context, parent);
    }

    @Override
    protected CharSequence typeDesc() {
        return "advanceLuban";
    }

    @Override
    protected void compress(String path, final CommonCallback<File> callback) {
        Luban.compress(rootView.getContext(), new File(path))
                .putGear(Luban.FIRST_GEAR)     // use CUSTOM GEAR compression mode
                .launch(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                       callback.onSuccess(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }
                });
    }


}
