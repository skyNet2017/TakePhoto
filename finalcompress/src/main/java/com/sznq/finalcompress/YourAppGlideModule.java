package com.sznq.finalcompress;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
@GlideModule
public class YourAppGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        long diskCacheSizeBytes = 1024*1024*1024*2;//2G
        //builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
}