package com.hss01248.analytics.ad;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.hss01248.analytics.R;

/**
 * Created by hss on 2019/1/1.
 */

public class GoogleAdFullScreenActivity extends AppCompatActivity {


    static UnifiedNativeAd ad;
    UnifiedNativeAdView adView;
    private Activity activity;

    public static void launch(Activity activity, UnifiedNativeAd ad) {
        GoogleAdFullScreenActivity.ad = ad;
        activity.startActivity(new Intent(activity, GoogleAdFullScreenActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gad);

        showad(ad);


    }

    private void showad(UnifiedNativeAd ad) {
        Log.i("dd", ad.getImages().get(0).getUri().toString());
                       /* SplashView.updateSplashData(activity, unifiedNativeAd.getImages().get(0).getUri().toString(),
                                unifiedNativeAd.getBody());*/

        adView = findViewById(R.id.adview);


        TextView headlineView = adView.findViewById(R.id.tv_ads);
        headlineView.setText(ad.getHeadline());
        adView.setHeadlineView(headlineView);

        MediaView mediaView = (MediaView) adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Call the UnifiedNativeAdView's setNativeAd method to register the
        // NativeAdObject.
        adView.setNativeAd(ad);

        //倒计时与跳过:
        final TextView tvSkip = adView.findViewById(R.id.tv_skip);
        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adView.destroy();
                activity.finish();
            }
        });
        int i = 5;
        showCount(i, activity, tvSkip);
    }

    private void showCount(final int i, final Activity googleAdFullScreenActivity, final TextView tvSkip) {
        AdUtil.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int i1 = i - 1;
                if (i >= 0) {
                    if (isFinishing()) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (isDestroyed()) {
                            return;
                        }
                    }
                    tvSkip.setText("跳过\n" + i + "s");
                    showCount(i1, googleAdFullScreenActivity, tvSkip);
                } else {
                    googleAdFullScreenActivity.finish();
                }

            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adView.destroy();
        AdUtil.getHandler().removeCallbacksAndMessages(null);
    }
}
