package com.sznq.finalcompress;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.gc.materialdesign.views.ButtonRectangle;

import java.util.ArrayList;
import java.util.List;

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
   /* @BindView(R.id.ad_banner)
    AdView adBanner;*/

    static List<String> platforms;
    static {
        platforms = new ArrayList<>();
        platforms.add("com.tencent.mm");
        platforms.add("com.tencent.mobileqq");
        platforms.add("com.sina.weibo");
        platforms.add("com.facebook.orca");
        platforms.add("jp.naver.line.android");
        platforms.add("com.twitter.android");
        platforms.add("com.facebook.katana");
        platforms.add("com.whatsapp");
        platforms.add("com.instagram.android");

    }


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
       /* AdView adView = findViewById(R.id.ad_banner);
        AdUtil.loadBannerAd(this, adView);*/
    }


    @OnClick({R.id.btn_share, R.id.btn_star, R.id.btn_selected})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_selected: {
                Intent intent = new Intent(this, AlbumSelectActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE);
            }

            break;
            case R.id.btn_share: {
               /* Intent intent = ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setText("发现一个超赞的图片无损压缩app:终极图片压缩,下载地址:http://www.baidu.com")
                        .getIntent();*/
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                limitPlatForm(intent);


            }
            break;
            case R.id.btn_star: {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
            break;
        }
    }

    private void limitPlatForm(Intent intent) {


        String text = getString(R.string.c_share_text)+":https://play.google.com/store/apps/details?id="+getPackageName();
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos.isEmpty()) {
            return;
        }
        List<Intent> targetIntents = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            ActivityInfo ainfo = info.activityInfo;
            if(platforms.contains(ainfo.packageName)){
                addShareIntent(targetIntents, ainfo,text);
            }
        }
        if ( targetIntents.size() == 0) {
            return;
        }
        Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), getString(R.string.c_choose_share_platform));
        if (chooserIntent == null) {
            return;
        }
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[]{}));
        try {
            startActivity(chooserIntent);
        }catch (Exception e){
            e.printStackTrace();
        }

                /*作者：隋胖胖LoveFat
                链接：https://www.jianshu.com/p/ca33b726645a
        來源：简书
        简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。*/
    }


    private void addShareIntent(List<Intent> list, ActivityInfo ainfo,String text) {
        Intent target = new Intent(Intent.ACTION_SEND);
        target.setType("text/plain");
        target.putExtra(Intent.EXTRA_TEXT, text);
        target.setPackage(ainfo.packageName);
        target.setClassName(ainfo.packageName, ainfo.name);
        list.add(target);
    }


}
