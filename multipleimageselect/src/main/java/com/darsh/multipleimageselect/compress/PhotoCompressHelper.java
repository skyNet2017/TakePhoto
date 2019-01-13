package com.darsh.multipleimageselect.compress;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.activities.ImageSelectActivity;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.lubanturbo.TurboCompressor;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Subscriber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by hss on 2018/12/15.
 */

public class PhotoCompressHelper {


    public static final int DEFAULT_QUALITY = 70;

    public static int getQuality() {
        return quality;
    }

    public static void setQuality(int quality) {
        PhotoCompressHelper.quality = quality;
    }

    public static int quality = DEFAULT_QUALITY;
    private static final String NAME_COMPRESSED = "-compressed";

    public static String getCompressedDirSuffix(){
        return  NAME_COMPRESSED;
    }

    public static boolean isACompressedDr(File file){
        File dir = file;
        if(file.isFile()){
            dir = file.getParentFile();
        }
        return dir.getName().contains(NAME_COMPRESSED);
    }

    public static Dialog showChooseQualityDialog(Activity activity, final Consumer<Integer> callback){
        SeekBar seekBar = null;
        View view = View.inflate(activity,R.layout.seek_quality,null);
        seekBar = view .findViewById(R.id.sbar_quality);
        final TextView textView = view.findViewById(R.id.tv_quality);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(progress+"/100");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar finalSeekBar = seekBar;
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle(R.string.c_change_quality)
                .setView(view)
                .setPositiveButton(R.string.c_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            callback.accept(finalSeekBar.getProgress());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(R.string.c_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();






        return dialog;

    }

    /**
     * 不压缩png,因为会变黑,效果不好
     * @param pathname
     * @return
     */
    public static boolean shouldCompress(File pathname,boolean checkQuality) {

        String name = pathname.getName();
        int idx = name.lastIndexOf(".");
        if(idx <0 || idx >= name.length()-1){
            return false;
        }
        String suffix = name.substring(idx+1);
        boolean isJpg = suffix.equalsIgnoreCase("jpg")
                || suffix.equalsIgnoreCase("jpeg");
        if(!isJpg){
            if(suffix.equalsIgnoreCase("png")){
                return true;
            }
            return false;
        }
        if(!checkQuality){
            return true;
        }

        int quality = ImageInfoFormater.getQuality(pathname.getAbsolutePath());
        Log.i("quality","quality:"+quality +":"+pathname.getAbsolutePath());
        return  quality > getQuality();
    }

    /**
     * 不压缩png,因为会变黑,效果不好
     * @param pathname
     * @return
     */
    public static boolean isImage(File pathname) {

        if(pathname.isDirectory()){
            return false;
        }
        String name = pathname.getName();
        int idx = name.lastIndexOf(".");
        if(idx <0 || idx >= name.length()-1){
            return false;
        }
        String suffix = name.substring(idx+1);
        boolean isJpg = suffix.equalsIgnoreCase("jpg")
                || suffix.equalsIgnoreCase("jpeg")
                || suffix.equalsIgnoreCase("png")
                || suffix.equalsIgnoreCase("gif")
                ||  suffix.equalsIgnoreCase("webp")
                ||  suffix.equalsIgnoreCase("raw");
        return  isJpg;
    }




    public static void compressAllFiles(final List<File> files, final Activity activity, final Subscriber<String> subscriber){
        if(files == null || files.isEmpty()){
            subscriber.onError(new Throwable("files is empty"));
            return;
        }
        /*if(isACompressedDr(files.get(0))){
            subscriber.onError(new Throwable("files has already been compressed"));
            return;
        }*/

        //提示用户有多少需要压缩,有多少不需要
        final List<File> files2 = new ArrayList<>(files);
        int total = files2.size();
        int compressedNum = 0;
        Iterator<File> iterator = files2.iterator();
        while (iterator.hasNext()){
            File file = iterator.next();
            if(!shouldCompress(file,true)){
                compressedNum++;
                iterator.remove();
            }
        }
        if(files2.isEmpty()){
            Toast.makeText(activity, R.string.all_has_been_compressed,Toast.LENGTH_SHORT).show();
            subscriber.onError(new Throwable("1"));
            return;
        }

        String str = activity.getString(R.string.c_total_selected)+total+"\n"
                + activity.getString(R.string.c_alreadycompressnum)+compressedNum+"\n"
                + activity.getString(R.string.c_left_count)+(total - compressedNum)+"\n"
               +activity.getString(R.string.c_doyouwant_to_compressleft);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.c_alert_title)
                .setMessage(str)
                .setPositiveButton(R.string.c_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        compressLeft(files2,files,activity,subscriber);
                    }
                }).setNegativeButton(R.string.c_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }

    private static void compressLeft(final List<File> filesToCompress, final List<File> files, final Activity activity, final Subscriber<String> subscriber) {
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMax(filesToCompress.size());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgress(0);
        dialog.setMessage(activity.getString(R.string.compressing));
        //dialog.setIndeterminate(false);
        final int[] progress = {0};
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        final long startTime = System.currentTimeMillis();
        Flowable.fromIterable(filesToCompress)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        compressOneFile(file,false);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        progress[0]++;
                        dialog.setProgress(progress[0]);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        subscriber.onError(throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        dialog.dismiss();
                        String str = getoutputDesc(filesToCompress,startTime,activity);
                        showDesc(str,activity,subscriber,filesToCompress);
                        //subscriber.onNext(str);
                    }
                });
    }

    private static void showDesc(final String str, final Activity activity, final Subscriber<String> subscriber, final List<File> filesToCompress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
       Dialog dialog =  builder.setTitle(R.string.c_alert_title)
                .setMessage(str)
                .setPositiveButton(R.string.c_preview, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        subscriber.onNext(str);
                    }
                }).setNegativeButton(R.string.c_cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                       //subscriber.onNext(str);
                       //删除生成的压缩文件
                       deleteAllFiles(filesToCompress,false);


                   }
               }).setNeutralButton(R.string.t_override_all, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                       //subscriber.onNext(str);
                       //替换生成的压缩文件
                       replaceAllFiles(filesToCompress,activity);

                   }
               })
               .show();
       dialog.setCancelable(false);
       dialog.setCanceledOnTouchOutside(false);
    }

    private static void deleteAllFiles(List<File> filesToCompress, boolean isOriginal) {
        Observable.fromIterable(filesToCompress)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        try {
                            File file1 = new File(PhotoCompressHelper.getCompressedFilePath(file.getAbsolutePath(),true));
                            file1.delete();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).subscribe();
    }



    public static void compressOneFile(File file,boolean override) {
        String name = file.getName();
        File dir = new File(file.getParentFile(), file.getParentFile().getName() + getCompressedDirSuffix());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File outFile = new File(dir, name);
        String outPath = outFile.getAbsolutePath();
        long start = System.currentTimeMillis();
        boolean success = TurboCompressor.compressOringinal(file.getAbsolutePath(), quality, outPath);
        String cost = "compressed " + success + ",cost " + (System.currentTimeMillis() - start) + "ms,\n";
        String filen = file.getName() + ", original:" + ImageInfoFormater.formatImagInfo(file.getAbsolutePath(),true) +
                ",\ncompressedFile:" + ImageInfoFormater.formatImagInfo(outPath,true);
        Log.w("dd", cost + filen);
        if(override){
            try {
                FileUtils.copyFile(outFile,file);
                outFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getoutputDesc(List<File> files,long startTime,Activity activity) {
        if(files.isEmpty()){
            return "";
        }
        long originalSize = 0;
        long sizeAfterCompressed = 0;
        for (File file : files) {
            originalSize += file.length();
            File file1 = new File(PhotoCompressHelper.getCompressedFilePath(file.getAbsolutePath(),false));
            if(file1.exists()){
                sizeAfterCompressed += file1.length();
            }
        }

        return activity.getString(R.string.c_compressquality)+quality+activity.getString(R.string.c_costtime)+(System.currentTimeMillis() - startTime)/1000f+"s\n"+
                activity.getString(R.string.c_origin_disk_size)+ PhotoCompressHelper.formatFileSize(originalSize)+"\n"+
                activity.getString(R.string.c_sieze_after)+ PhotoCompressHelper.formatFileSize(sizeAfterCompressed)+"\n"+
                activity.getString(R.string.c_save_disk_space)+ PhotoCompressHelper.formatFileSize(originalSize - sizeAfterCompressed);
    }

    public static void replaceAllFiles(List<File> files, final Activity activity){
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMax(files.size());
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgress(0);
        dialog.setMessage(activity.getString(R.string.c_replacing));
        //dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        final int[] i = {0};
        Observable.fromIterable(files)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        try {
                            copyAndDelte(file);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        i[0]++;
                        dialog.setProgress(i[0]);
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
                        if(activity instanceof ImageSelectActivity){
                            ImageSelectActivity activity1 = (ImageSelectActivity) activity;
                            activity1.refresh();
                        }
                    }
                });

    }

    public static void deleteAllFiles(List<File> files, final Activity activity){
        if(files.size() < 20){
            for (File file : files) {
                file.delete();
            }
            if(activity instanceof ImageSelectActivity){
                ImageSelectActivity activity1 = (ImageSelectActivity) activity;
                activity1.refresh();
            }
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMax(files.size());
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgress(0);
        dialog.setMessage(activity.getString(R.string.c_replacing));
        //dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        final int[] i = {0};
        Observable.fromIterable(files)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        try {
                            file.delete();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        i[0]++;
                        dialog.setProgress(i[0]);
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
                        if(activity instanceof ImageSelectActivity){
                            ImageSelectActivity activity1 = (ImageSelectActivity) activity;
                            activity1.refresh();
                        }
                    }
                });

    }

    public static void copyAndDelte(File file) {
        String path = PhotoCompressHelper.getCompressedFilePath(file.getAbsolutePath(),true);
        /*if(isACompressedDr(file)){
            path = getOriginalPath(file,false);
        }*/


        if(TextUtils.isEmpty(path)){
            Log.w("dd","file not exist:"+path);
            return;
        }

        try {
            File file1 = new File(path);
            if(shouldCompress(file,true)){
                FileUtils.copyFile(file1,file);
            }else {

            }
            file1.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void setPathToPreview(SubsamplingScaleImageView imageView,String filePath){
        if(TextUtils.isEmpty(filePath)){
            return;
        }
        Uri uri = null;
        File file = new File(filePath);
        if(file.exists()){
            uri = Uri.fromFile(file);
        }else {
            uri = Uri.parse(filePath);
        }
        imageView.setImage(ImageSource.uri(uri));

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





    public static String getCompressedFilePath(String path,boolean needFileExistFirst){
        File file = new File(path);
        String name = file.getName();
        File dir = new File(file.getParentFile(), file.getParentFile().getName() + getCompressedDirSuffix());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File fileCompressed = new File(dir,name);
        if(needFileExistFirst && !fileCompressed.exists()){
            return "";
        }
        String outPath = fileCompressed.getAbsolutePath();
        return outPath;
    }

    private static String getOriginalPath(File file,boolean needFileExistFirst) {
        if(file.isDirectory()){
            return "";
        }
        String name = file.getName();
        File dir = new File(file.getParentFile(),file.getParentFile().getName().substring(0,
                file.getParentFile().getName().indexOf(getCompressedDirSuffix())));
        File originlFile = new File(dir,name);
        if(needFileExistFirst && !originlFile.exists()){
            return "";
        }
        return originlFile.getAbsolutePath();
    }

    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath())));
        //intent.setComponent(new ComponentName(context.getPackageName(), "org.devio.simple.compress.NewPhotoAddedReceiver"));
        context.sendBroadcast(intent);
    }

}
