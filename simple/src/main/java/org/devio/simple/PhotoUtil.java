package org.devio.simple;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;

/**
 * Created by hss on 2018/12/15.
 */

public class PhotoUtil {


    public static final int DEFAULT_QUALITY = 70;

    public static String formatImagInfo(String path){
        String size = formatFileSize(new File(path).length());
        int [] wh = getImageWidthHeight(path);
        return "path:"+path+",\nw:"+wh[0]+",h:"+wh[1]+",filesize:"+size;
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

    public static String formatExifs(String path) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif( path, ExifInterface.Options.OPTION_ALL );

            List<ExifTag> all_tags = exif.getAllTags();
           StringBuilder builder = new StringBuilder(320);
           builder.append("path:")
                   .append(path)
                   .append("\n")
                   .append(exif.getImageSize()[0])
                   .append("x")
                   .append(exif.getImageSize()[1])
                   .append(",")
                   .append("filesize:")
                   .append(formatFileSize(new File(path).length()))
                   .append("\n")
                   .append("quality:")
                   .append(exif.getQualityGuess())
                   .append("\n");
          /* if(all_tags != null && !all_tags.isEmpty()){
               for (ExifTag tag : all_tags) {
                   if(tag.hasValue() ){
                       builder.append(tag.getValueAsString())
                               .append("\n");
                   }

               }
           }*/

           return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return formatImagInfo(path);
    }

    public static int getQuality(String path){
        if(TextUtils.isEmpty(path)){
            return 0;
        }
        File file = new File(path);
        if(!file.exists()){
            return 0;
        }
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif( path, ExifInterface.Options.OPTION_ALL );
            return exif.getQualityGuess();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getCompressedFilePath(String path,boolean needFileExistFirst){
        File file = new File(path);
        String name = file.getName();
        File dir = new File(file.getParentFile(), file.getParentFile().getName() + "-compressed-quality-" + DEFAULT_QUALITY);
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
