package com.darsh.multipleimageselect;

import android.net.Uri;

import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.http.HttpHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PathToStream {

    public static InputStream getInput(String path) throws IOException {
        if(path.startsWith("/storage/")){
            return new FileInputStream(new File(path));
        }
        if(path.startsWith("http")){
            return HttpHelper.getInputStream(path,null);
        }
        if(path.startsWith("content://")){
            return SafUtil.context.getContentResolver().openInputStream(Uri.parse(path));
        }

        return SafUtil.context.getContentResolver().openInputStream(Uri.parse(path));
    }
}
