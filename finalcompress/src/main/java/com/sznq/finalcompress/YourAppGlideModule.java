package com.sznq.finalcompress;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.hss01248.media.mymediastore.http.HttpHelper;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

@GlideModule
public class YourAppGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NotNull Context context, GlideBuilder builder) {
        long diskCacheSizeBytes = 1024L*1024*1024*2;//2G
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(HttpHelper.getClient()));
    }
}