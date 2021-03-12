package com.sznq.finalcompress.filemanager.search;

import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;

import java.util.List;

public class SearchDbUtil {

    public static List<? extends BaseInfo> searchItem(String word, int diskType, int mediaType, int sortType,
                                                      boolean isSearchDir, int hiddenType, int[] pageInfo, int sizeType) {

        if(isSearchDir){
            return DirDbUtil.searchDir(word,diskType,mediaType,sortType,hiddenType,pageInfo,sizeType);
        }
        return FileDbUtil.searc(word,diskType,mediaType,sortType,hiddenType,pageInfo,sizeType);
    }


}
