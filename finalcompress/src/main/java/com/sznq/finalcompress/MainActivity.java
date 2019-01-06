package com.sznq.finalcompress;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.ads.AdView;
import com.hss01248.analytics.ad.AdUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_selected)
    ButtonRectangle btnSelected;

    boolean firstIn = true;
    @BindView(R.id.btn_share)
    ButtonRectangle btnShare;
    @BindView(R.id.btn_star)
    ButtonRectangle btnStar;
    @BindView(R.id.ad_banner)
    AdView adBanner;

    @Override
    protected void onResume() {
        super.onResume();
        if (firstIn) {
            firstIn = false;
            return;
        }
        //AdUtil.loadFullScreenAd(this);
        firstIn = true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AdView adView = findViewById(R.id.ad_banner);
        AdUtil.loadBannerAd(this, adView);
    }



    @OnClick({R.id.btn_share, R.id.btn_star,R.id.btn_selected})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_selected:{
                Intent intent = new Intent(this, AlbumSelectActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE);}

                break;
            case R.id.btn_share:{
                Intent intent = ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setText("发现一个超赞的图片无损压缩app:终极图片压缩,下载地址:http://www.baidu.com")
                        .getIntent();
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
            }
                break;
            case R.id.btn_star:{
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
            }
                break;
        }
    }
}
