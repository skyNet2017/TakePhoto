package org.devio.simple;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.lubanturbo.TurboCompressor;

import org.apache.commons.io.FileUtils;
import org.devio.simple.compress.CompressResultCompareActivity;
import org.reactivestreams.Subscriber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;

/**
 * Created by hss on 2018/12/15.
 */

public class PhotoCompressHelper {


    public static final int DEFAULT_QUALITY = 70;

    public static int quality = DEFAULT_QUALITY;
    static final String NAME_COMPRESSED = "-compressed-quality-";

    public static String getCompressedDirSuffix(){
        return  NAME_COMPRESSED + quality;
    }

    public static boolean isACompressedDr(File file){
        File dir = file;
        if(file.isFile()){
            dir = file.getParentFile();
        }
        return dir.getName().contains(NAME_COMPRESSED);
    }

    /**
     * 不压缩png,因为会变黑,效果不好
     * @param pathname
     * @return
     */
    public static boolean shouldCompress(File pathname) {

        String name = pathname.getName();
        int idx = name.lastIndexOf(".");
        if(idx <0 || idx >= name.length()-1){
            return false;
        }
        String suffix = name.substring(idx+1);
        boolean isJpg = suffix.equalsIgnoreCase("jpg")
                || suffix.equalsIgnoreCase("jpeg");
        if(!isJpg){
            return false;
        }
        return true;
        /*int quality = PhotoCompressHelper.getQuality(pathname.getAbsolutePath());
        Log.i("quality","quality:"+quality +":"+pathname.getAbsolutePath());
        return  quality > PhotoCompressHelper.DEFAULT_QUALITY;*/
    }




    public static void compressAllFiles(final List<File> files, Activity activity, final Subscriber<String> subscriber){
        if(files == null || files.isEmpty()){
            subscriber.onError(new Throwable("files is empty"));
            return;
        }
        if(isACompressedDr(files.get(0))){
            subscriber.onError(new Throwable("files has already been compressed"));
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMax(files.size());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgress(0);
        dialog.setMessage("正在压缩中...");
        //dialog.setIndeterminate(false);
        final int[] progress = {0};
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
       final long startTime = System.currentTimeMillis();
        Flowable.fromIterable(files)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {

                        compressOneFile(file);



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
                       String str = getoutputDesc(files,startTime);
                       subscriber.onNext(str);
                    }
                });
    }

    public static void compressOneFile(File file) {
        String name = file.getName();
        File dir = new File(file.getParentFile(), file.getParentFile().getName() + getCompressedDirSuffix());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String outPath = new File(dir, name).getAbsolutePath();
        long start = System.currentTimeMillis();
        boolean success = TurboCompressor.compressOringinal(file.getAbsolutePath(), DEFAULT_QUALITY, outPath);
        String cost = "compressed " + success + ",cost " + (System.currentTimeMillis() - start) + "ms,\n";
        String filen = file.getName() + ", original:" + ImageInfoFormater.formatImagInfo(file.getAbsolutePath(),true) +
                ",\ncompressedFile:" + ImageInfoFormater.formatImagInfo(outPath,true);
        Log.w("dd", cost + filen);
    }

    private static String getoutputDesc(List<File> files,long startTime) {
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

        return "compressed quality:"+quality+",cost time total:"+(System.currentTimeMillis() - startTime)/1000f+"s\n"+
                "original dir size:"+ PhotoCompressHelper.formatFileSize(originalSize)+"\n"+
                "sizeAfterCompressed:"+ PhotoCompressHelper.formatFileSize(sizeAfterCompressed)+"\n"+
                "save disk space:"+ PhotoCompressHelper.formatFileSize(originalSize - sizeAfterCompressed);
    }

    public static void replaceAllFiles(List<File> files, final Activity activity){
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMax(files.size());
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgress(0);
        dialog.setMessage("正在替换中...");
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
                        Toast.makeText(activity.getApplicationContext(),"替换完成",Toast.LENGTH_LONG).show();
                    }
                });

    }

    public static void copyAndDelte(File file) {
        String path = PhotoCompressHelper.getCompressedFilePath(file.getAbsolutePath(),true);
        if(isACompressedDr(file)){
            path = getOriginalPath(file,false);
        }

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
