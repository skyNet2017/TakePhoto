package org.devio.simple.compress;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hss01248.lubanturbo.TurboCompressor;

import org.devio.simple.BigPagerHolder;
import org.devio.simple.PhotoUtil;
import org.devio.simple.R;
import org.devio.simple.ResultActivity;
import org.devio.takephoto.app.TakePhotoFragmentActivity;
import org.devio.takephoto.model.TImage;
import org.devio.takephoto.model.TResult;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hss on 2018/12/16.
 */

public class CompressActivity extends TakePhotoFragmentActivity {

    @BindView(R.id.btn_selected)
    Button btnSelected;
    @BindView(R.id.tv_dir_info)
    TextView tvDirInfo;
    @BindView(R.id.btn_preview)
    Button btnPreview;
    @BindView(R.id.btn_start_compress)
    Button btnStartCompress;
    @BindView(R.id.ll_dirs)
    LinearLayout llDirs;

    ArrayList<TImage> images;
    @BindView(R.id.rb_compressall)
    RadioButton rbCompressall;
    File selectedDir;

    int quality;
    @BindView(R.id.tv_end)
    TextView tvEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);
        ButterKnife.bind(this);
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        images = result.getImages();
        tvDirInfo.setText(formatImages(images));
    }

    private String formatImages(ArrayList<TImage> images) {
        selectedDir = new File(images.get(0).getOriginalPath()).getParentFile();
        return selectedDir.getAbsolutePath() + ",total count:" + getFileCount(selectedDir) + ",selected count:" + images.size();
    }

    private int getFileCount(File dir) {
        return dir.listFiles().length;
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* this.quality = BigPagerHolder.quality;
        Log.e("dd", "quality:" + quality);*/
    }

    @OnClick({R.id.btn_selected, R.id.btn_preview, R.id.btn_start_compress})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_selected:
                getTakePhoto().onPickMultiple(65535);
                break;
            case R.id.btn_preview:
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("images", images);
                startActivity(intent);
                break;
            case R.id.btn_start_compress:
                if (rbCompressall.isChecked()) {
                    File[] files = selectedDir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            String name = pathname.getName();
                            return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")|| name.endsWith(".JPG");
                        }
                    });
                    compressAllFiles(new ArrayList<File>(Arrays.asList(files)));
                } else {
                    //只压缩那几张
                    compressAllFiles(getFiles(images));

                }
                break;
        }
    }

    private ArrayList<File> getFiles(ArrayList<TImage> images) {
        ArrayList<File> list = new ArrayList<File>();
        for (TImage image : images) {
            File file = new File(image.getOriginalPath());
            list.add(file);
        }
        return list;
    }

   volatile int progress;
    long startTime;

    private void compressAllFiles(final ArrayList<File> files) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(files.size());
        dialog.setProgress(0);
        //dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        startTime = System.currentTimeMillis();
        Flowable.fromIterable(files)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {

                        String name = file.getName();
                        File dir = new File(file.getParentFile(), file.getParentFile().getName() + "-compressed-quality-" + quality);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String outPath = new File(dir, name).getAbsolutePath();
                        long start = System.currentTimeMillis();
                        boolean success = TurboCompressor.compressOringinal(file.getAbsolutePath(), quality, outPath);
                        String cost = "compressed " + success + ",cost " + (System.currentTimeMillis() - start) + "ms,\n";
                        String filen = file.getName() + ", original:" + PhotoUtil.formatImagInfo(file.getAbsolutePath()) +
                                ",\ncompressedFile:" + PhotoUtil.formatImagInfo(outPath);
                        Log.w("dd", cost + filen);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress++;
                                dialog.setProgress(progress);
                            }
                        });

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        dialog.dismiss();
                        tvEnd.setText(getoutputDesc(files));
                    }
                });


    }

    private String getoutputDesc(ArrayList<File> files) {
        long originalSize = 0;
        for (File file : files) {
            originalSize += file.length();
        }
        File dir = new File(files.get(0).getParentFile(), files.get(0).getParentFile().getName() + "-compressed-quality-" + quality);
        File[] files1 = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".JPG");
            }
        });

        long sizeAfterCompressed = 0;
        for (File file : files1){
            sizeAfterCompressed += file.length();
        }


        return "compressed quality:"+quality+",cost time total:"+(System.currentTimeMillis() - startTime)/1000f+"s\n"+
                "original dir size:"+PhotoUtil.formatFileSize(originalSize)+"\n"+
                "sizeAfterCompressed:"+PhotoUtil.formatFileSize(sizeAfterCompressed)+"\n"+
                "save disk space:"+PhotoUtil.formatFileSize(originalSize - sizeAfterCompressed);
    }
}
