package com.hss01248.view.viewholder;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * by hss
 * data:2020-03-30
 * desc:
 */
public class BaseRepository {



    public BaseRepository() {

    }

    private CompositeDisposable mCompositeSubscription;

    protected void addSubscribe(Disposable subscription) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeDisposable();
        }
        mCompositeSubscription.add(subscription);
    }

    public void unSubscribe() {
        if (mCompositeSubscription != null && mCompositeSubscription.size() != 0) {
            mCompositeSubscription.clear();
        }
    }
}
