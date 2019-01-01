package com.sznq.finalcompress;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.ads.AdView;
import com.hss01248.analytics.ad.AdUtil;
import com.hss01248.analytics.ad.GoogleAdFullScreenActivity;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_selected)
    ButtonRectangle btnSelected;

    boolean firstIn = true;

    @Override
    protected void onResume() {
        super.onResume();
        if(firstIn){
            firstIn = false;
            return;
        }
        AdUtil.loadFullScreenAd(this);
        firstIn = true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AdView adView = findViewById(R.id.ad_banner);
        AdUtil.loadBannerAd(this,adView);
    }

    @OnClick(R.id.btn_selected)
    public void onViewClicked() {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }
}
