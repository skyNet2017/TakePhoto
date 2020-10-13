package com.hss01248.lubanturbo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import it.sephiroth.android.library.exif2.ExifInterface;



/**
 * Created by hss on 2018/12/14.
 */

public class TurboCompressor {

    static {
        try {
            System.loadLibrary("luban");
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

    }

    public native static boolean nativeCompress(Bitmap bitmap,int quality,String outPath);

    /**
     *
     * @param srcPath
     * @param quality
     * @param outPath
     * @return 代表是否执行了压缩
     */
    public static boolean compressOringinal(String srcPath,int quality,String outPath){
        File file = new File(srcPath);
        if(!shouldCompress(file,true)){
            return false;
        }

        //todo 过大的图,resize到1600w像素,仿照谷歌.
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath);
        boolean success =  false;
        File outFile = new File(outPath);
        try {
            success =  nativeCompress(bitmap,quality,outPath);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            success = compressByAndroid(bitmap,quality,outPath);
        }

        if(outFile.exists() && outFile.length()> 50){
            //如果压缩后的图比压缩前还大,那么就不压缩,返回原图
            if(file.length() < outFile.length()){
                Log.w("tubor","file.length() < outFile.length()");
                //outFile.delete();
                return false;
            }
            success = true;
        }else {
            success = false;
        }

        //回写exif信息
        if(success){
            ExifInterface exif = new ExifInterface();
            try {
                exif.readExif( srcPath, ExifInterface.Options.OPTION_ALL );
                exif.writeExif( outPath );
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                return true;
            }
        }
        return false;
    }

     static boolean compressByAndroid(Bitmap bitmap, int quality, String outPath) {
        try {
            File file = new File(outPath);
             defaultCompressToFile(bitmap,file,false,quality);
             return file.exists() && file.length() > 50;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void defaultCompressToFile(Bitmap bitmap, File file, boolean focusAlpha, int quality) throws IOException{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(focusAlpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality, stream);
        bitmap.recycle();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();
    }

    static boolean shouldCompress(File pathname,boolean checkQuality) {

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
            if(suffix.equalsIgnoreCase("gif") || suffix.equalsIgnoreCase("webp")){
                return false;
            }
            return false;
        }
        if(!checkQuality){
            return true;
        }

        int quality = getQuality(pathname.getAbsolutePath());
        Log.i("quality","quality:"+quality +":"+pathname.getAbsolutePath());
        return  (quality ==  0) ||  (quality > getQuality());
    }

     static int getQuality() {
        return 80;
    }

    static int getQuality(String path) {
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
