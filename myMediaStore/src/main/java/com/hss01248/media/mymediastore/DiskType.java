package com.hss01248.media.mymediastore;

import androidx.documentfile.provider.DocumentFile;

import com.hierynomus.smbj.share.DiskShare;

import java.util.HashMap;
import java.util.Map;

public class DiskType {

   public static Map<String, DiskShare> smbMap = new HashMap<>();
    static Map<String, DocumentFile> documentFileMap = new HashMap<>();


}
