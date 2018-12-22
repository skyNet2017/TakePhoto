package org.devio.simple;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hss01248.adapter.SuperPagerHolder;
import com.hss01248.adapter.SuperViewGroupSingleAdapter;
import org.devio.simple.holder.*;
import org.devio.takephoto.model.TImage;

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
    String path;

    OriginalHolder originalHolder;
    CppHolder cppHolder;
    LubanHolder lubanHolder;
    AdvanceLubanHolder advanceLubanHolder;

    TuborOriginalHolder tuborOriginalHolder;

    public static int quality = 70;

    public BigPagerHolder(Activity context, ViewGroup parent) {
        super(context, parent);
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

        originalHolder =  new OriginalHolder(activity,llContainer);
        cppHolder = new CppHolder(activity,llContainer);
        lubanHolder = new LubanHolder(activity,llContainer);
        advanceLubanHolder = new AdvanceLubanHolder(activity,llContainer);
        tuborOriginalHolder = new TuborOriginalHolder(activity,llContainer);

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
        this.path = image.getOriginalPath();

        originalHolder.assingDatasAndEvents(activity,path,0);
        tuborOriginalHolder.assingDatasAndEvents(activity,path,1);
        cppHolder.assingDatasAndEvents(activity,path,2);
        lubanHolder.assingDatasAndEvents(activity,path,3);
        advanceLubanHolder.assingDatasAndEvents(activity,path,4);


        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvQuality.setText("质量:"+progress+"%");
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
