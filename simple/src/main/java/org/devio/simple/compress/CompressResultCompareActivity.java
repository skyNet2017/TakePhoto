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
import org.devio.simple.PhotoUtil;
import org.devio.simple.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
                return new CpHolder(CompressResultCompareActivity.this);
            }

            @Override
            public List getListData() {
                return null;
            }
        };
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
            case R.id.fb_override_single:
                copyAndDelte(files.get(position));
                Toast.makeText(this,"替换完成",Toast.LENGTH_LONG).show();
                break;
            case R.id.fb_override_all:
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMax(files.size());
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgress(0);
                dialog.setMessage("正在替换中...");
                //dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.show();
                for (int i = 0;i<files.size();i++) {
                    File file = files.get(i);
                    copyAndDelte(file);
                    dialog.setProgress(i+1);
                }
                dialog.dismiss();
                Toast.makeText(this,"替换完成",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void copyAndDelte(File file) {
        String path = PhotoUtil.getCompressedFilePath(file.getAbsolutePath(),true);
        if(TextUtils.isEmpty(path)){
            Log.w("dd","file not exist:"+path);
            return;
        }
        try {
            File file1 = new File(path);
            FileUtils.copyFile(file1,file);
            file1.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
