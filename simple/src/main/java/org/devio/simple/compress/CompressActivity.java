package org.devio.simple.compress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.gc.materialdesign.views.ButtonRectangle;

import org.devio.simple.R;
import org.devio.takephoto.app.TakePhotoFragmentActivity;
import org.devio.takephoto.model.TImage;
import org.devio.takephoto.model.TResult;
import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.devio.takephoto.wrap.TakePhotoUtil;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hss on 2018/12/16.
 */

public class CompressActivity extends TakePhotoFragmentActivity {

    @BindView(R.id.btn_selected)
    ButtonRectangle btnSelected;
    @BindView(R.id.tv_dir_info)
    TextView tvDirInfo;
    @BindView(R.id.btn_preview)
    ButtonRectangle btnPreview;
    @BindView(R.id.btn_start_compress)
    ButtonRectangle btnStartCompress;
    @BindView(R.id.ll_dirs)
    LinearLayout llDirs;
    @BindView(R.id.iv_preview)
    SubsamplingScaleImageView ivPreview;
    ArrayList<TImage> images;
    @BindView(R.id.rb_compressall)
    CheckBox rbCompressall;
    File selectedDir;
    ArrayList<File> files;

    int quality  = PhotoCompressHelper.DEFAULT_QUALITY;
    @BindView(R.id.tv_end)
    TextView tvEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if(intent != null){
            Log.w("onCreate",intent.getData()+"");

        }
    }

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.w("onNewIntent",intent.getData()+"");
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        images = result.getImages();
        tvDirInfo.setText(formatImages(images));
        if(!PhotoCompressHelper.isACompressedDr(selectedDir)){
            btnStartCompress.setVisibility(View.VISIBLE);
        }
        btnPreview.setVisibility(View.VISIBLE);
    }

    private String formatImages(ArrayList<TImage> images) {
        selectedDir = new File(images.get(0).getOriginalPath()).getParentFile();
        String str =  selectedDir.getAbsolutePath() + ",\ntotal count:" + getFileCount(selectedDir) + ",selected count:" + images.size();
        return str + "\n"+images.get(0).getOriginalPath()+"  等等....";
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

    @OnClick({R.id.btn_selected, R.id.btn_preview, R.id.btn_start_compress,R.id.btn_pick_img,R.id.btn_sendBroadcast})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_selected:
                getTakePhoto().onPickMultipleByMultiSelectLib(65535);
                break;
            case R.id.btn_start_compress:{
                if(selectedDir == null){
                    return;
                }
                if (rbCompressall.isChecked()) {
                    File[] files = selectedDir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return PhotoCompressHelper.shouldCompress(pathname);
                        }
                    });
                    compressAllFiles(new ArrayList<File>(Arrays.asList(files)));
                } else {
                    //只压缩那几张
                    compressAllFiles(getFiles(images));

                }
            }
                /*Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("images", images);
                startActivity(intent);*/

                break;
            case R.id.btn_preview:
                if(selectedDir == null){
                    return;
                }
                if (rbCompressall.isChecked()) {
                    File[] files = selectedDir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return PhotoCompressHelper.shouldCompress(pathname);
                            //return true;
                        }
                    });
                    this.files = new ArrayList<File>(Arrays.asList(files));
                } else {
                    this.files = getFiles(images);
                }
                CompressResultCompareActivity.files = this.files;
                startActivity(new Intent(this,CompressResultCompareActivity.class));
                break;

            case R.id.btn_pick_img:
                TakePhotoUtil.setUseSystemAlbum(false);
                TakePhotoUtil.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
                    @Override
                    public void onSuccess(String paths) {
                        Log.i("success",paths);
                        ivPreview.setImage(ImageSource.uri(paths));
                    }

                    @Override
                    public void onFail(String paths, String msg) {
                        Log.e("dd",msg);
                    }

                    @Override
                    public void onCancel() {
                        Log.w("dd","onCancel");
                    }
                });

                break;
            case R.id.btn_sendBroadcast:
                getWindow().getDecorView().setDrawingCacheEnabled(true);
                Bitmap bitmap = getWindow().getDecorView().getDrawingCache();
                PhotoCompressHelper.saveImageToGallery(this,bitmap);
                break;
        }
    }



    private ArrayList<File> getFiles(ArrayList<TImage> images) {
        ArrayList<File> list = new ArrayList<File>();
        for (TImage image : images) {
            File file = new File(image.getOriginalPath());
            //if(shouldCompress(file)){
                list.add(file);
           // }
        }
        return list;
    }



    private void compressAllFiles(final ArrayList<File> files) {
        PhotoCompressHelper.compressAllFiles(files, this, new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(String s) {
                btnStartCompress.setVisibility(View.GONE);
                tvEnd.setText(s);
            }

            @Override
            public void onError(Throwable t) {
                tvEnd.setText(t.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });


    }




}
