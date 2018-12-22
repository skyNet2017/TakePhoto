package org.devio.simple;

import android.app.Application;
import android.view.View;

import com.hss01248.adapter.IBindView;
import com.hss01248.adapter.SuperHolderInitor;

import butterknife.ButterKnife;

/**
 * Created by hss on 2018/12/22.
 */

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SuperHolderInitor.init(new IBindView() {
            @Override
            public void bind(Object o, View view) {
                ButterKnife.bind(o,view);
            }
        });
    }
}
