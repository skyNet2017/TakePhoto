package com.darsh.multipleimageselect.compress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darsh.multipleimageselect.R;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.hss01248.adapter.SuperPagerAdapter;
import com.hss01248.adapter.SuperPagerHolder;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



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

    ArrayList<String> paths ;
    boolean isPreview;
    boolean isAllSelected;

    public static void lauch(Activity activity, ArrayList<String> paths,boolean isAllSelected){
        Intent intent = new Intent(activity,CompressResultCompareActivity.class);
        intent.putExtra("paths",paths);
        intent.putExtra("isAllSelected",isAllSelected);
        activity.startActivityForResult(intent,980);
    }

    public static void lauchForPreview(Activity activity, ArrayList<String> paths,int position){
        Intent intent = new Intent(activity,CompressResultCompareActivity.class);
        intent.putExtra("paths",paths);
        intent.putExtra("position",position);
        intent.putExtra("isPreview",true);
        intent.putExtra("isAllSelected",true);
        activity.startActivityForResult(intent,980);
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
            super.onBackPressed();
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
        paths = getIntent().getStringArrayListExtra("paths");
        position = getIntent().getIntExtra("position",0);
        isPreview = getIntent().getBooleanExtra("isPreview",false);
        isAllSelected = getIntent().getBooleanExtra("isAllSelected",false);
        if(isPreview){
            menu.setVisibility(View.GONE);
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
                    return new CpHolder(CompressResultCompareActivity.this,viewGroup).setPreview(isPreview);


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
        adapter.refresh(getFilePaths(files));
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
                overrideSingle();
            }
        });

        fbOverrideAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overrideAllFile();
            }
        });
    }

    private void overrideAllFile() {
        File file = files.get(position);
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
        System.gc();
    }
}
