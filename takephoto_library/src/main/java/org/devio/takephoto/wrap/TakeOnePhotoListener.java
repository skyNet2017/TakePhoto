package org.devio.takephoto.wrap;

/**
 * Created by huangshuisheng on 2018/12/20.
 */

public interface TakeOnePhotoListener {

    void onSuccess(String path);

    void onFail(String path, String msg);

    void onCancel();
}
