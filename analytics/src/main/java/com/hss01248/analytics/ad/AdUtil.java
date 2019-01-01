package com.hss01248.analytics.ad;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.hss01248.analytics.R;

/**
 * Created by hss on 2019/1/1.
 */

public class AdUtil {

    private static String interstitialAdId;
    private static Application context;
    private static String fullScreenDebugId = "ca-app-pub-3940256099942544/1033173712";
    private static String nativeAdDebugId = "ca-app-pub-3940256099942544/2247696110";
    private static String bannerDebugId = "ca-app-pub-3940256099942544/6300978111";
    private static boolean debug;
    private static Handler handler;


    private static String fullScreenId = "ca-app-pub-2335840373239478/5498002409";
    private static String homeBelowBtn = "ca-app-pub-2335840373239478/2231698907";

    public static Handler getHandler() {
        return handler;
    }

    public static void init(Application application, boolean isDebug, String appId) {
        MobileAds.initialize(application, appId);

        context = application;
        debug = isDebug;
        handler = new Handler();
    }

    public static void loadBannerAd(Activity activity,AdView adView){
      /* adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(debug? bannerDebugId : homeBelowBtn);*/
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    /**
     * 插页式全屏广告
     *
     * @param activity
     */
    public static void loadFullScreenAd(Activity activity) {
        final ProgressDialog dialog = new ProgressDialog(activity);
        final InterstitialAd mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(debug ? fullScreenDebugId : fullScreenId);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        dialog.show();
        mInterstitialAd.setImmersiveMode(true);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
                dialog.dismiss();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                dialog.dismiss();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }
        });

    }

    public static void loadNativeFullScreenAd(final Activity hostActivity) {


        final ProgressDialog dialog = new ProgressDialog(hostActivity);
        dialog.setCanceledOnTouchOutside(false);


        AdLoader adLoader = new AdLoader.Builder(context, getNativeAdId())
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd ad) {
                        dialog.dismiss();
                        // Show the ad.
                        // call this method anywhere to update splash view data
                        Log.i("dd", ad.getImages().get(0).getUri().toString());
                        GoogleAdFullScreenActivity.launch(hostActivity, ad);

                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.w("dd", "onAdFailedToLoad:" + errorCode);
                        dialog.dismiss();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
        dialog.show();
    }


    private static String getNativeAdId() {
        if (debug) {
            return nativeAdDebugId;
        }
        return interstitialAdId;
    }





}
