package org.devio.simple;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.CommonViewHolder;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.adapter.SuperViewGroupSingleAdapter;

import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

import org.devio.simple.holder.AdvanceLubanHolder;
import org.devio.simple.holder.CppHolder;
import org.devio.simple.holder.LubanHolder;
import org.devio.simple.holder.OriginalHolder;
import org.devio.simple.holder.TuborOriginalHolder;
import org.devio.takephoto.model.TImage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangshuisheng on 2018/12/13.
 */

public class BigPagerHolder extends SuperPagerHolder<TImage,Activity> {

    TextView tvQuality;
    SeekBar seekBar;

    SuperViewGroupSingleAdapter<Activity> adapter;
    LinearLayout llContainer;
    Button btnCompress;
    TImage image;

    OriginalHolder originalHolder;
    CppHolder cppHolder;
    LubanHolder lubanHolder;
    AdvanceLubanHolder advanceLubanHolder;

    TuborOriginalHolder tuborOriginalHolder;

    public static int quality = 60;


    public BigPagerHolder(Activity context) {
        super(context);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.image_show;
    }

    @Override
    protected void findViewsById(View view) {


        seekBar = view.findViewById(R.id.seekbar);
        tvQuality = view.findViewById(R.id.tv_percent);
        btnCompress = view.findViewById(R.id.btn_compress);
        llContainer = view.findViewById(R.id.ll_pics);

        originalHolder =  new OriginalHolder(activity);
        cppHolder = new CppHolder(activity);
        lubanHolder = new LubanHolder(activity);
        advanceLubanHolder = new AdvanceLubanHolder(activity);
        tuborOriginalHolder = new TuborOriginalHolder(activity);

        llContainer.addView(originalHolder.rootView);
        llContainer.addView(tuborOriginalHolder.rootView);
        llContainer.addView(cppHolder.rootView);
        llContainer.addView(lubanHolder.rootView);

        llContainer.addView(advanceLubanHolder.rootView);

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originalHolder.doCompress();
                tuborOriginalHolder.doCompress();
                cppHolder.doCompress();
                lubanHolder.doCompress();
                advanceLubanHolder.doCompress();
            }
        });

    }

    @Override
    public void assingDatasAndEvents(Activity activity, @Nullable TImage tImage, int i) {
        this.image = tImage;

        originalHolder.assingDatasAndEvents(activity,image,0);
        tuborOriginalHolder.assingDatasAndEvents(activity,image,1);
        cppHolder.assingDatasAndEvents(activity,image,2);
        lubanHolder.assingDatasAndEvents(activity,image,3);
        advanceLubanHolder.assingDatasAndEvents(activity,image,4);


        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //compressByQuality(seekBar.getProgress());
                quality = seekBar.getProgress();
                tvQuality.setText("质量:"+quality+"%");
                compressByQuality();

            }
        });


    }

    private void compressByQuality() {
        tuborOriginalHolder.doCompress();
        cppHolder.doCompress();
    }


}
