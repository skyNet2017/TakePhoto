package org.devio.simple.compress;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.hss01248.adapter.SuperPagerAdapter;
import com.hss01248.adapter.SuperPagerHolder;

import org.apache.commons.io.FileUtils;
import org.devio.simple.PhotoCompressHelper;
import org.devio.simple.R;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hss on 2018/12/16.
 */

public class CompressResultCompareActivity extends AppCompatActivity {

    @BindView(R.id.vp_compress)
    ViewPager vpCompress;
    SuperPagerAdapter adapter;
    public static ArrayList<File> files;
    @BindView(R.id.sbar)
    SeekBar sbar;
    @BindView(R.id.tv_progress)
    TextView tvProgress;
    int size;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.fb_override_single)
    FloatingActionButton fbOverrideSingle;
    @BindView(R.id.fb_override_all)
    FloatingActionButton fbOverrideAll;
    @BindView(R.id.menu)
    FloatingActionMenu menu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_compress_compare);
        ButterKnife.bind(this);
        adapter = new SuperPagerAdapter(this) {
            @Override
            protected SuperPagerHolder generateNewHolder(Context context, ViewGroup viewGroup, int i) {
                return new CpHolder(CompressResultCompareActivity.this,viewGroup);
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
        sbar.setProgress(1);
        tvProgress.setText("1/" + size);

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


    @OnClick({R.id.fb_override_single, R.id.fb_override_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fb_override_single:{
                File file = files.get(position);
                if(PhotoCompressHelper.isACompressedDr(file)){
                    //替换
                    PhotoCompressHelper.copyAndDelte(file);
                    Toast.makeText(getApplicationContext(),"替换完成",Toast.LENGTH_LONG).show();
                    //finish();
                }else {
                    //压缩
                    PhotoCompressHelper.compressOneFile(file);
                    Toast.makeText(getApplicationContext(),"压缩完成",Toast.LENGTH_LONG).show();
                }

            }

                break;
            case R.id.fb_override_all:
                File file = files.get(position);
                if(PhotoCompressHelper.isACompressedDr(file)){
                    //替换
                    PhotoCompressHelper.replaceAllFiles(files,this);
                    Toast.makeText(getApplicationContext(),"替换完成",Toast.LENGTH_LONG).show();
                    finish();
                }else {
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
                            Toast.makeText(CompressResultCompareActivity.this,"压缩完成",Toast.LENGTH_LONG).show();
                        }
                    });

                }


                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
