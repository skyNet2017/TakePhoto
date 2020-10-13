package com.hss01248.lubanturbo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import it.sephiroth.android.library.exif2.ExifInterface;
import top.zibin.luban.DefaultBitmapToFile;
import top.zibin.luban.IBitmapToFile;

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

    public static boolean compressOringinal(String srcPath,int quality,String outPath){
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath);
        boolean success =  false;
        try {
            success =  nativeCompress(bitmap,quality,outPath);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            success = compressByAndroid(bitmap,quality,outPath);
        }

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
        return success;
    }

    private static boolean compressByAndroid(Bitmap bitmap, int quality, String outPath) {
        try {
            File file = new File(outPath);
             new DefaultBitmapToFile().compressToFile(bitmap,file,false,quality);
             return file.exists() && file.length() > 50;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static IBitmapToFile getTurboCompressor(){
        return new IBitmapToFile() {
            @Override
            public void compressToFile(Bitmap tagBitmap, File tagImg, boolean focusAlpha, int quality) throws IOException {
                Log.d("dd","TurboCompressor started");
                long start = System.currentTimeMillis();
                //boolean isSuccess = nativeCompress(tagBitmap,quality,tagImg.getAbsolutePath());

                boolean success =  false;
                try {
                    success =  nativeCompress(tagBitmap,quality,tagImg.getAbsolutePath());
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                    success = compressByAndroid(tagBitmap,quality,tagImg.getAbsolutePath());
                }

                Log.d("dd","TurboCompressor ended,cost time:"+(System.currentTimeMillis() - start));
                if(!success){
                    throw new IOException("nativeCompress failed");
                }
            }
        };
    }
}
