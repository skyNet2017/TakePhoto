package com.darsh.multipleimageselect.helpers;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Locale;

public class LoggingListener<R> implements RequestListener<R> {

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<R> target, boolean isFirstResource) {
        android.util.Log.e("GLIDE", String.format(Locale.ROOT,
                "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);
        return false;
    }

    @Override
    public boolean onResourceReady(R resource, Object model, Target<R> target, DataSource dataSource, boolean isFirstResource) {
        android.util.Log.d("GLIDE", String.format(Locale.ROOT,
                "onResourceReady(%s, %s, %s, %s)", resource, model, target,  isFirstResource));
        return false;
    }
}
