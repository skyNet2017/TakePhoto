package org.devio.simple.holder;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.hss01248.lubanturbo.TurboCompressor;

import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;
import org.devio.takephoto.model.TImage;

import java.io.File;

/**
 * Created by hss on 2018/12/15.
 */

public class LubanHolder extends SinglePicHolder{
    public LubanHolder(Activity context) {
        super(context);
    }

    @Override
    protected CharSequence typeDesc() {
        return "luban";
    }

    boolean isNative;
    @Override
    protected void findViewsById(View rootView) {
        super.findViewsById(rootView);
        btnRxtra.setVisibility(View.VISIBLE);
        btnRxtra.setText("native/java");
        btnRxtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNative = !isNative;
                if(isNative){
                    btnRxtra.setText("now is native");
                }else {
                    btnRxtra.setText("now is java saver");
                }
                doCompress();
            }
        });
    }



    @Override
    protected void compress(String path, final CommonCallback<File> callback) {
        top.zibin.luban.Luban.with(rootView.getContext())
                .load(path)
                .saver(isNative ? TurboCompressor.getTurboCompressor() :null)
                .setTargetDir(rootView.getContext().getCacheDir().getAbsolutePath())
                .setCompressListener(new top.zibin.luban.OnCompressListener() {
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
                }).launch();
    }


}
