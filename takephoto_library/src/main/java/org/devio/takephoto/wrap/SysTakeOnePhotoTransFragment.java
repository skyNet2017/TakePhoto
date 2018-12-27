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
    boolean useSystemAlbum;
    boolean fromCamera;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public SysTakeOnePhotoTransFragment setListener(TakeOnePhotoListener listener){
        this.listener = listener;
        return this;
    }

    public void pickFromCamera(boolean fromCamera,boolean useSystemAlbum){
        this.fromCamera = fromCamera;
        this.useSystemAlbum = useSystemAlbum;
        if(fromCamera){
            getTakePhoto().onPickFromCapture(TUriParse.getTempUri(getContext()));
        }else {
            if(useSystemAlbum){
                getTakePhoto().onPickFromGallery();
            }else {
                getTakePhoto().onPickMultiple(1);
            }
        }

    }


    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        if(listener != null){
            if(fromCamera || useSystemAlbum){
                listener.onSuccess(result.getImage().getOriginalPath());
            }else {
                listener.onSuccess(result.getImages().get(0).getOriginalPath());
            }
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
