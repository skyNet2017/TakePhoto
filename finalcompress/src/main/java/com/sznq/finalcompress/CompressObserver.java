package com.sznq.finalcompress;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by hss on 2019/1/13.
 */

public class CompressObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public CompressObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
    }
}
