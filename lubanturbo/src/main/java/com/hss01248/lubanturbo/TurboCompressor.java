package com.hss01248.lubanturbo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import it.sephiroth.android.library.exif2.ExifInterface;
import top.zibin.luban.IBitmapToFile;

/**
 * Created by hss on 2018/12/14.
 */

public class TurboCompressor {

    static {
        System.loadLibrary("luban");
    }

    public native static boolean nativeCompress(Bitmap bitmap,int quality,String outPath);

    public static boolean compressOringinal(String srcPath,int quality,String outPath){
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath);
        boolean success =  nativeCompress(bitmap,quality,outPath);
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


    public static IBitmapToFile getTurboCompressor(){
        return new IBitmapToFile() {
            @Override
            public void compressToFile(Bitmap tagBitmap, File tagImg, boolean focusAlpha, int quality) throws IOException {
                Log.d("dd","TurboCompressor started");
                long start = System.currentTimeMillis();
                boolean isSuccess = nativeCompress(tagBitmap,quality,tagImg.getAbsolutePath());
                Log.d("dd","TurboCompressor ended,cost time:"+(System.currentTimeMillis() - start));
                if(!isSuccess){
                    throw new IOException("nativeCompress failed");
                }
            }
        };
    }
}
