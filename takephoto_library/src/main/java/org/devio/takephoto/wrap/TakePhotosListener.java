package org.devio.takephoto.wrap;

import java.util.List;

/**
 * Created by huangshuisheng on 2018/12/20.
 */

public interface TakePhotosListener {

    void onSuccess(List<String> paths);

    void onFail(List<String> paths, String msg);

    void onCancel();
}
