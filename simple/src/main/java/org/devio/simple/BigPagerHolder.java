package org.devio.simple;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.adapter.SuperPagerHolder;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;
import org.devio.takephoto.model.TImage;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by huangshuisheng on 2018/12/13.
 */

public class BigPagerHolder extends SuperPagerHolder<TImage,Activity> {

    TextView tvQuality;
    SeekBar seekBar;

    SubsamplingScaleImageView imageView1;
    SubsamplingScaleImageView imageView2;
    SubsamplingScaleImageView imageView3;

    TextView tvSizeOriginal;
    TextView tvSizecpp;
    TextView tvSizeLuabn;

    TImage tImage;


    public BigPagerHolder(Activity context) {
        super(context);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.image_show;
    }

    @Override
    protected void findViewsById(View view) {
         imageView1 = (SubsamplingScaleImageView) view.findViewById(R.id.imgShow1);
         imageView2 = (SubsamplingScaleImageView) view.findViewById(R.id.imgShow2);
        imageView3 = (SubsamplingScaleImageView) view.findViewById(R.id.imgShow3);

        seekBar = view.findViewById(R.id.seekbar);
        tvQuality = view.findViewById(R.id.tv_percent);

        tvSizeOriginal = view.findViewById(R.id.tv_size_original);
        tvSizecpp = view.findViewById(R.id.tv_size_cpp);
        tvSizeLuabn = view.findViewById(R.id.tv_size_luban);
    }

    @Override
    public void assingDatasAndEvents(Activity activity, @Nullable TImage tImage, int i) {
        this.tImage = tImage;
        if(!TextUtils.isEmpty(tImage.getOriginalPath())){
            imageView1.setImage(ImageSource.uri(tImage.getOriginalPath()));
            String info = formatImagInfo(tImage.getOriginalPath());
            Log.d("dd","original :"+info);
            tvSizeOriginal.setText(info);



        }
        if(!TextUtils.isEmpty(tImage.getCompressPath())){
            imageView3.setImage(ImageSource.uri(tImage.getCompressPath()));
            String info = formatImagInfo(tImage.getCompressPath());
            Log.d("dd","original :"+info);
            tvSizeLuabn.setText(info);
            Log.d("dd","compressed by luban :"+info);
        }


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
                compressByQuality(seekBar.getProgress());
            }
        });


    }

    private void compressByQuality(int progress) {

        tvQuality.setText(progress+"%");
        //c++ 压缩:
        //CompressArgs args = new CompressArgs.Builder().compressFileSize(300*1024).build();
       /* String outPath = new File(activity.getCacheDir(),System.currentTimeMillis()+".jpg").getAbsolutePath();
        Light.getInstance().compress(tImage.getOriginalPath(), outPath);
        imageView2.setImage(ImageSource.uri(outPath));
        String info = formatImagInfo(outPath);
        tvSizecpp.setText(info);

        Log.d("dd","compressed by c++,:"+info);*/
        top.zibin.luban.Luban.with(rootView.getContext())
                .load(tImage.getOriginalPath())
                .setTargetDir(rootView.getContext().getCacheDir().getAbsolutePath())
                .setCompressListener(new top.zibin.luban.OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        imageView2.setImage(ImageSource.uri(file.getAbsolutePath()));
                        String info = formatImagInfo(file.getAbsolutePath());
                        tvSizecpp.setText(info);

                        Log.d("dd","compressed by luban,:"+info);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();


        //advanceluban:
        int [] wh = getImageWidthHeight(tImage.getOriginalPath());

        Luban.compress(rootView.getContext(), new File(tImage.getOriginalPath()))
                .setMaxSize(400)                // limit the final image size（unit：Kb）
                .setMaxHeight(2000)             // limit image height
                .setMaxWidth(2000)// limit image width
                .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
        .launch(new OnCompressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(File file) {
                imageView3.setImage(ImageSource.uri(file.getAbsolutePath()));
                String info = formatImagInfo(file.getAbsolutePath());
                tvSizeLuabn.setText(info);

                Log.d("dd","compressed by advance luban,:"+info);
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }


    public static String formatImagInfo(String path){
        String size = formatFileSize(new File(path).length());
        int [] wh = getImageWidthHeight(path);
        return "path:"+path+",\nw:"+wh[0]+",h:"+wh[1]+",\nfilesize:"+size;
    }


    public static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".00");
            if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "MB";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "KB";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }

    public static int[] getImageWidthHeight(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth,options.outHeight};
    }
}
