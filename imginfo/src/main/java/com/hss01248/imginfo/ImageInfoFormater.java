package com.hss01248.imginfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.List;

import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;

/**
 * Created by hss on 2018/12/15.
 */

public class ImageInfoFormater {

   public static Context context;

    public static void init(Context context) {
        ImageInfoFormater.context = context;
    }


    public static String formatImagInfo(String path, boolean showFullPath) {
        if(path.startsWith("content://")){
            Uri uri = Uri.parse(path);
            try {
                FileInputStream inputStream = new FileInputStream(context.getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor());
               int len =  inputStream.available();
                inputStream.close();
                String size = formatFileSize(len);
                int[] wh = getImageWidthHeight(path);
                //return path+"\n"+size;
                if(!showFullPath){
                    return size+" "+wh[0]+"x"+wh[1];
                }

                String path2 = URLDecoder.decode(path);
                if(path2.contains(":")){
                    path2 = path2.substring(path2.lastIndexOf(":")+1);
                }
                return path2+"\n"+size+" "+wh[0]+"x"+wh[1];
            } catch (IOException e) {
                e.printStackTrace();
            }

            return path+"\n"+"";
        }
        File file = new File(path);
        String size = formatFileSize(file.length());
        int[] wh = getImageWidthHeight(path);
        int quality = getQuality(path);
        String needCompress = quality > 85 ? context.getString(R.string.c_not_compressed) : context.getString(R.string.t_compressed);
        String str = wh[0] + "x" + wh[1] + ", " + size + context.getString(R.string.c_quality_info) + quality + needCompress;
        if (showFullPath) {
            return str + "\n" + path;

        } else {
            return str;
        }
    }


    public static String formatFileSize(long size) {
        try {
            DecimalFormat dff = new DecimalFormat(".0");

            if (size >= 1024 * 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024 * 1024);
                dff = new DecimalFormat(".00");
                String value = dff.format(doubleValue);
                return value + "G";
            } else if (size >= 1024 * 1024) {
                double doubleValue = ((double) size) / (1024 * 1024);
                String value = dff.format(doubleValue);
                return value + "M";
            } else if (size > 1024) {
                double doubleValue = ((double) size) / 1024;
                String value = dff.format(doubleValue);
                return value + "K";
            } else {
                return size + "B";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(size);
    }

    public static int[] getImageWidthHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        if(path.startsWith("content://")){
            try {
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(
                        context.getContentResolver().openFileDescriptor(Uri.parse(path), "r").getFileDescriptor(),
                        null, options);
                // 此时返回的bitmap为null
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth, options.outHeight};
    }

    public static String formatExifs(String path) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(path, ExifInterface.Options.OPTION_ALL);

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
        return formatImagInfo(path, true);
    }

    public static int getQuality(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(path, ExifInterface.Options.OPTION_ALL);
            return exif.getQualityGuess();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


}
