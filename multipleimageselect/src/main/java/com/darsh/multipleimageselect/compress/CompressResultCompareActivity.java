package com.darsh.multipleimageselect.compress;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.darsh.multipleimageselect.PathToStream;
import com.darsh.multipleimageselect.R;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.hss01248.adapter.SuperPagerAdapter;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.MediaInfoUtil;
import com.hss01248.media.metadata.MetaDataUtil;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by hss on 2018/12/16.
 */

public class CompressResultCompareActivity extends AppCompatActivity {


    ViewPager vpCompress;
    SuperPagerAdapter adapter;
    public static ArrayList<File> files;

    SeekBar sbar;

    TextView tvProgress;
    int size;

    ImageView ivBack;

    FloatingActionButton fbOverrideSingle;

    FloatingActionButton fbOverrideAll;

    FloatingActionMenu menu;

    static List<String> paths ;
    boolean isPreview;
    boolean isAllSelected;
    RelativeLayout rlRoot;
    boolean isDescShow;
    FloatingActionButton fbChangeQuality;

    public static void lauch(Activity activity, ArrayList<String> paths,boolean isAllSelected){
        try {
            Intent intent = new Intent(activity,CompressResultCompareActivity.class);
            if(paths.size() > 1000){
                CompressResultCompareActivity.paths = paths;
            }else {
                intent.putExtra("paths",paths);
            }

            intent.putExtra("isAllSelected",isAllSelected);
            activity.startActivityForResult(intent,980);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }

    public static void lauchForPreview(Context activity, List<String> paths,int position){
        try {
            Intent intent = new Intent(activity,CompressResultCompareActivity.class);
            if(paths.size() > 1000){
                CompressResultCompareActivity.paths = paths;
            }else {
                intent.putExtra("paths",(ArrayList<String>)paths);
            }
            intent.putExtra("position",position);
            intent.putExtra("isPreview",true);
            intent.putExtra("isAllSelected",true);
            activity.startActivity(intent);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if(isAllSelected){
            //将position传回去:
            Intent intent = new Intent();
            intent.putExtra("position",position);
            setResult(RESULT_CANCELED,intent);
            finish();
        }else {
            finish();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_compress_compare);
        initView();
        ArrayList<String> paths2 = getIntent().getStringArrayListExtra("paths");
        if(paths2 != null){
            paths = paths2;
        }
        position = getIntent().getIntExtra("position",0);
        isPreview = getIntent().getBooleanExtra("isPreview",false);
        isAllSelected = getIntent().getBooleanExtra("isAllSelected",false);
        if(isPreview){
           // menu.setVisibility(View.GONE);
        }


        if(paths != null && !paths.isEmpty()){
            files = new ArrayList<>();
            for (String path : paths){
                files.add(new File(path));
            }
        }
        adapter = new SuperPagerAdapter(this) {
            @Override
            protected SuperPagerHolder generateNewHolder(Context context, ViewGroup viewGroup, int i) {
                CpHolder holder =  new CpHolder(CompressResultCompareActivity.this,viewGroup).setPreview(isPreview);
                return holder;


            }

            @Override
            public List getListData() {
                return null;
            }
        };
        //adapter.setOnlyOneTypeItem(false);
        vpCompress.setAdapter(adapter);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //处理点击进来的情况:



        size = files.size();
        sbar.setMax(size);
        sbar.setProgress(position+1);
        tvProgress.setText(position+1+"/" + size);

        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvProgress.setText(i + "/" + size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                vpCompress.setCurrentItem(seekBar.getProgress());
            }
        });

        vpCompress.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CompressResultCompareActivity.this.position = position;
                sbar.setProgress(position + 1);
                File file = files.get(position);
                if(PhotoCompressHelper.isACompressedDr(file)){
                    //fbOverrideSingle.setLabelText("替换此");
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        adapter.refresh(paths);
        vpCompress.setCurrentItem(position);

    }

    private void initView() {
        vpCompress = (ViewPager) findViewById(R.id.vp_compress);
        sbar = (SeekBar) findViewById(R.id.sbar);
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        menu = (FloatingActionMenu) findViewById(R.id.menu);
        fbOverrideSingle = (FloatingActionButton) findViewById(R.id.fb_override_single);
        fbOverrideAll = (FloatingActionButton) findViewById(R.id.fb_override_all);
        fbOverrideSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(false);
                overrideSingle();
            }
        });

        fbOverrideAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.close(false);
                overrideAllFile();
            }
        });
        rlRoot = findViewById(R.id.rl_root);
        fbChangeQuality = findViewById(R.id.fb_change_quality);
        fbChangeQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoCompressHelper.showChooseQualityDialog(CompressResultCompareActivity.this, new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        menu.close(false);
                        changeQuality(integer);
                    }
                });
            }
        });
        //if(isPreview){
            fbOverrideSingle.setVisibility(View.GONE);
            fbOverrideAll.setVisibility(View.GONE);
            fbChangeQuality.setLabelText("查看exif/metadata");
            fbChangeQuality.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showExif(CompressResultCompareActivity.this,paths.get(position));
                }
            });
       // }


        /*rlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDescUI();
                return ;
            }
        });
        isDescShow = isPreview;
        switchDescUI();*/
    }

    public static void showExif(Activity activity,String path) {
        int type = FileTypeUtil.getTypeByFileName(path);

        Observable.just(path)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String path) throws Exception {
                        String str = "";
                        if(type== BaseMediaInfo.TYPE_IMAGE){
                            Map<String, String> exif = ExifUtil.readExif(PathToStream.getInput(path));
                            str = exif.toString().replace(",","\n");
                        }else if(type == BaseMediaInfo.TYPE_VIDEO){
                            if(!path.startsWith("http")){
                                Map<String, String> allInfo = MetaDataUtil.getAllInfo(path);
                                str = allInfo.toString().replace(",","\n");
                            }
                        }
                        return str;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                       new  AlertDialog.Builder(activity)
                               .setTitle("detail")
                               .setMessage(s)
                               .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {

                                   }
                               }).show();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        new  AlertDialog.Builder(activity)
                                .setTitle("detail")
                                .setMessage(e.getMessage())
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void changeQuality(int quality) {
        File file = files.get(position);
        PhotoCompressHelper.setQuality(quality);
        PhotoCompressHelper.compressOneFile(file,false);
        int count = vpCompress.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = vpCompress.getChildAt(i);
            Object tag = child.getTag();
            if(tag instanceof SuperPagerHolder){
                SuperPagerHolder holder = (SuperPagerHolder) tag;
                holder.assingDatasAndEvents(this,files.get(position).getAbsolutePath(),position);
            }
        }
    }

    private void switchDescUI() {
        isDescShow = !isDescShow;
        if(isDescShow){
            sbar.setVisibility(View.VISIBLE);
            tvProgress.setVisibility(View.VISIBLE);
        }else {
            sbar.setVisibility(View.GONE);
            tvProgress.setVisibility(View.GONE);
        }
        int count =  vpCompress.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = vpCompress.getChildAt(i);
            Object tag = view.getTag();
            if (tag instanceof CpHolder) {
                CpHolder holder = (CpHolder) tag;
                holder.switchDec(isDescShow);
            }
        }
    }

    private void overrideAllFile() {
        PhotoCompressHelper.replaceAllFiles(files, this);
        Toast.makeText(getApplicationContext(), R.string.c_override_finished, Toast.LENGTH_LONG).show();
        /*if (PhotoCompressHelper.isACompressedDr(file)) {
            //替换
            PhotoCompressHelper.replaceAllFiles(files, this);
            Toast.makeText(getApplicationContext(), "替换完成", Toast.LENGTH_LONG).show();
            finish();
        } else {
            //压缩
            PhotoCompressHelper.compressAllFiles(files, this, new Subscriber<String>() {
                @Override
                public void onSubscribe(Subscription s) {

                }

                @Override
                public void onNext(String s) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {
                    Toast.makeText(CompressResultCompareActivity.this, "压缩完成", Toast.LENGTH_LONG).show();
                }
            });
        }*/
    }

    private void overrideSingle() {
        File file = files.get(position);
        PhotoCompressHelper.copyAndDelte(file);
        Toast.makeText(getApplicationContext(), R.string.c_override_finished,Toast.LENGTH_LONG).show();
       /* if(PhotoCompressHelper.isACompressedDr(file)){
            //替换
            PhotoCompressHelper.copyAndDelte(file);
            Toast.makeText(getApplicationContext(),"替换完成",Toast.LENGTH_LONG).show();
            //finish();
        }else {
            //压缩
            PhotoCompressHelper.compressOneFile(file);
            Toast.makeText(getApplicationContext(),"压缩完成",Toast.LENGTH_LONG).show();
        }*/
    }

    int position = 1;

    private List<String> getFilePaths(ArrayList<File> files) {
        ArrayList<String> paths = new ArrayList<>();
        for (File file : files) {
            if(file.exists()){
                paths.add(file.getAbsolutePath());
            }

        }
        return paths;
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhotoCompressHelper.setQuality(PhotoCompressHelper.DEFAULT_QUALITY);
        System.gc();
    }
}
