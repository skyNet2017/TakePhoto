package org.devio.simple.holder;

import android.app.Activity;
import android.support.annotation.Nullable;

import org.devio.simple.CommonCallback;
import org.devio.simple.SinglePicHolder;
import org.devio.takephoto.model.TImage;

import java.io.File;

/**
 * Created by hss on 2018/12/15.
 */

public class BasePicHolder extends SinglePicHolder {

    int position;
    public BasePicHolder(Activity context) {
        super(context);
    }

    @Override
    protected CharSequence typeDesc() {
        return "";
    }

    @Override
    protected void compress(String path, CommonCallback<File> callback) {
        switch (position){
            case 0:
                callback.onSuccess(new File(path));
                tvType.setText("original");
                break;
            case 1:
                //compressbyCpp(path,callback);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }
    }

    @Override
    public void assingDatasAndEvents(Activity activity, @Nullable TImage bean, int position) {
        this.position = position;
        super.assingDatasAndEvents(activity, bean, position);
    }
}
