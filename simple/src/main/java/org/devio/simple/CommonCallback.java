package org.devio.simple;

/**
 * Created by hss on 2018/12/15.
 */

public interface CommonCallback<T> {

    void onSuccess(T t);

    void onError(Throwable e);
}
