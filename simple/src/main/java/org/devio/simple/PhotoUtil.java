package org.devio.simple;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;

/**
 * Created by hss on 2018/12/15.
 */

public class PhotoUtil {

    public static String formatImagInfo(String path){
        String size = formatFileSize(new File(path).length());
        int [] wh = getImageWidthHeight(path);
        return "path:"+path+",\nw:"+wh[0]+",h:"+wh[1]+",filesize:"+size;
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
}
