package com.sznq.finalcompress.filemanager.search;

import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.db.BaseMediaInfoDao;

import org.greenrobot.greendao.database.Database;

import java.util.List;

public class SearchDbUtil {

    public static List<? extends BaseInfo> searchItem(String word, int diskType, int mediaType, int sortType,
                                                      boolean isSearchDir, int hiddenType, int[] pageInfo, int sizeType) {

        if(isSearchDir){
            return DirDbUtil.searchDir(word,diskType,mediaType,sortType,hiddenType,pageInfo,sizeType);
        }
        return FileDbUtil.searc(word,diskType,mediaType,sortType,hiddenType,pageInfo,sizeType);
    }

    public static void hidePath(String path, BaseInfo baseInfo){
        String dir = path;
        if(baseInfo instanceof BaseMediaInfo){
            dir = path.substring(0,path.lastIndexOf("/"));
        }else if(baseInfo instanceof BaseMediaFolderInfo){
            BaseMediaFolderInfo folderInfo = (BaseMediaFolderInfo) baseInfo;
            folderInfo.hidden = 1;
            DbUtil.getDaoSession().getBaseMediaFolderInfoDao().update(folderInfo);
        }
        Database database = DbUtil.getDaoSession().getDatabase();
        database.execSQL("update BASE_MEDIA_INFO set HIDDEN =1 where DIR = ?" ,new String[]{dir});
        //Database database = daoSession.getDatabase();
        //        database.execSQL("update " + tableName + " set " + columnName + " = ?",
        //
        //        // GreenDAO的数据库和SQLite一样，都没有boolean类型，需要转换
        //        new Integer[]{Boolean.valueOf(isChecked + "") ? 1 : 0});
        //————————————————
        //的可写数据库并不能实时获取到，必须等待其他操作释放了线程锁才能执行操作……

       // 也就是说，GreenDAO的execSQL是异步的
        //版权声明：本文为CSDN博主「Eternity岚」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
        //原文链接：https://blog.csdn.net/u014653815/article/details/84635090
    }


}
