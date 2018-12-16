package org.devio.simple.compress;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hss01248.adapter.SuperPagerAdapter;
import com.hss01248.adapter.SuperPagerHolder;

import org.devio.simple.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        size = files.size();
        sbar.setMax(size);
        tvProgress.setText("0/"+size);

        sbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvProgress.setText(i+"/"+size);
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
                sbar.setProgress(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        adapter.refresh(getFilePaths(files));
    }

    private List getFilePaths(ArrayList<File> files) {
        ArrayList<String> paths = new ArrayList<>();
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }
}
