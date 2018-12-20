package org.devio.takephoto.wrap;

import android.os.Bundle;
import org.devio.takephoto.app.TakePhotoFragment;
import org.devio.takephoto.model.TResult;
import org.devio.takephoto.uitl.TUriParse;

/**
 * Created by huangshuisheng on 2018/12/20.
 */

public class SysTakeOnePhotoTransFragment extends TakePhotoFragment {


    private TakeOnePhotoListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public SysTakeOnePhotoTransFragment setListener(TakeOnePhotoListener listener){
        this.listener = listener;
        return this;
    }

    public void pickFromCamera(boolean fromCamera){
        if(fromCamera){
            getTakePhoto().onPickFromCapture(TUriParse.getTempUri(getContext()));
        }else {
            getTakePhoto().onPickFromGallery();
        }

    }


    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        if(listener != null){
            listener.onSuccess(result.getImage().getOriginalPath());
        }
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
        if(listener != null){
            listener.onFail("",msg);
        }
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
        if(listener != null){
            listener.onCancel();
        }
    }
}
