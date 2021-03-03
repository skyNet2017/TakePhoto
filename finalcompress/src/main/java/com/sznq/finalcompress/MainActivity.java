package com.sznq.finalcompress;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;

import com.darsh.multipleimageselect.activities.ImageSelectActivity;
import com.darsh.multipleimageselect.compress.StorageUtils;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.saf.SafUtil;
import com.gc.materialdesign.views.ButtonRectangle;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.smb.SmbjUtil;
import com.hss01248.media.mymediastore.usb.UsbUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;


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


        com.hss01248.media.mymediastore.SafUtil.getRootDir(this, new com.hss01248.media.mymediastore.SafUtil.ISdRoot() {
            @Override
            public void onPermissionGet(DocumentFile dir) {
                Log.w(SafUtil.TAG,"getRootDir:"+dir.getUri());
            }

            @Override
            public void onPermissionDenied(int resultCode, String msg) {

            }
        });

       //AutoStartUtil.showDialog(MainActivity.this);
        //MyImageWatcher.init();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},78);
        }

        //smb();

        //usb();




    }

    private void usb() {
        UsbUtil.regist(this);
    }

    private void smb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    OutputStream out = null;
                    PrintStream ps = null;
                    try {
                        File localFile = new File("00iputto.txt");//远程服务器共享文件名称
                        String text = "来来来，我们来试一试";//要写入的文本内容
                        String host = "192.168.3.8";//远程服务器的地址
                        String username = "Administrator";//远程服务器的用户名
                        String password = "614511qc";//远程服务器的密码
                        String path = "/D/";//远程服务器共享文件夹名称
                        String remoteUrl = "smb://" + username + ":" + password + "@" + host + path + (path.endsWith("/") ? "" : "/");//带密码的url
                       // String remoteUrl = "smb://"+ host + path + (path.endsWith("/") ? "" : "/");//不需要输入用户名密码的url

                        Log.w("remoteUrl", remoteUrl);
                        SmbFile remoteFile = new SmbFile(remoteUrl + localFile.getPath());//创建远程对象
                        remoteFile.connect();//建立连接
                        Log.w("files", Arrays.toString(remoteFile.list()));
                        out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
                        ps = new PrintStream(out);
                        ps.println(text);
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "发生错误：" + e.getLocalizedMessage();
                        System.out.println(msg);
                    } finally {
                        try {
                            if (ps != null) {
                                ps.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }).start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StorageUtils.onActivityResultForOutSdcardPermission(this,requestCode,resultCode,data);

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


    public void smbj(View view) {

    }

    public void listAllImages(View view) {
        ImageSelectActivity.listAll(this, BaseMediaInfo.TYPE_IMAGE);
    }

    public void listAllVideo(View view) {
        ImageSelectActivity.listAll(this, BaseMediaInfo.TYPE_VIDEO);
    }
}
