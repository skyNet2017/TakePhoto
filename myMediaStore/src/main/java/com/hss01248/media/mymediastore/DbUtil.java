package com.hss01248.media.mymediastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.db.BaseMediaFolderInfoDao;
import com.hss01248.media.mymediastore.db.BaseMediaInfoDao;
import com.hss01248.media.mymediastore.db.DaoMaster;
import com.hss01248.media.mymediastore.db.DaoSession;
import com.hss01248.media.mymediastore.sort.SortByFileName;
import com.hss01248.media.mymediastore.sort.SortByFolderName;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class DbUtil {

    public static ISort folderSort = new SortByFolderName();
    public static ISort contentSort = new SortByFileName();

    public static boolean showHidden = false;
    public static int folderSortType = 0;
    public static int fileSortType = 0;

    static void init(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "mymedia.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private volatile static DaoSession daoSession;

    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            synchronized (DbUtil.class) {
                if (daoSession == null) {
                    init(SafUtil.context);
                }
            }
        }
        return daoSession;
    }

    public static List<BaseMediaFolderInfo> getAllFolders() {
        return getAllFolders(BaseMediaInfo.TYPE_IMAGE);
    }

    public static List<BaseMediaFolderInfo> getAllFolders(int type) {
        long start = System.currentTimeMillis();
        List<BaseMediaFolderInfo> infos = getDaoSession().getBaseMediaFolderInfoDao().queryBuilder()
                .where(BaseMediaFolderInfoDao.Properties.Type.eq(type))
                .orderDesc(BaseMediaFolderInfoDao.Properties.FileSize)
                .list();
        Log.w(SafUtil.TAG, " getAllFolders 耗时(ms):" + (System.currentTimeMillis() - start) + ", size:" + infos.size());
        //耗时(ms):103
        return infos;
    }

    public static List<BaseMediaFolderInfo> getAllImageAndVideoFolders() {
        long start = System.currentTimeMillis();
        List<BaseMediaFolderInfo> infos = getDaoSession().getBaseMediaFolderInfoDao().queryBuilder()
                .whereOr(BaseMediaFolderInfoDao.Properties.Type.eq(1), BaseMediaFolderInfoDao.Properties.Type.eq(2))
                .orderDesc(BaseMediaFolderInfoDao.Properties.FileSize)
                .list();
        Log.w(SafUtil.TAG, " getAllImageAndVideoFolders 耗时(ms):" + (System.currentTimeMillis() - start) + ", size:" + infos.size());
        return infos;
    }

    public static List<BaseMediaFolderInfo> getAllFolders2() {
        long start = System.currentTimeMillis();
        //
        QueryBuilder<BaseMediaFolderInfo> builder = getDaoSession().getBaseMediaFolderInfoDao().queryBuilder()
                .whereOr(BaseMediaFolderInfoDao.Properties.Type.eq(1), BaseMediaFolderInfoDao.Properties.Type.eq(2)
                        , BaseMediaFolderInfoDao.Properties.Type.eq(2));
        if (!showHidden) {
            builder.where(BaseMediaFolderInfoDao.Properties.Hidden.eq(0));
        }
        doSort(builder);
        List<BaseMediaFolderInfo> infos = builder.list();
        Log.w(SafUtil.TAG, " getAllImageAndVideoFolders 耗时(ms):" + (System.currentTimeMillis() - start) + ", size:" + infos.size());
        return infos;
    }

    /**
     *  desc[0] = "按文件从大到小";
     *         desc[1] ="按文件个数从大到小";
     *         desc[2] ="按更新时间 新在前";
     *         desc[3] ="按更新时间顺序 旧在前";
     *         desc[4] ="按文件夹名 顺序";
     *         desc[5] ="按文件夹名  倒序";
     *         desc[6] ="按路径 顺序";
     *         desc[7] ="按路径  倒序";
     * @param builder
     */
    private static void doSort(QueryBuilder<BaseMediaFolderInfo> builder) {
        if(folderSortType == 0){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.FileSize);
        }else if(folderSortType == 1){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.Count);
        }else if(folderSortType == 2){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.UpdatedTime)
            .where(BaseMediaFolderInfoDao.Properties.Count.gt(2),BaseMediaFolderInfoDao.Properties.FileSize.gt(300*1024));
        }else if(folderSortType == 3){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.UpdatedTime)
                    .where(BaseMediaFolderInfoDao.Properties.Count.gt(2),BaseMediaFolderInfoDao.Properties.FileSize.gt(300*1024));
        }else if(folderSortType == 4){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.Name)
                    .where(BaseMediaFolderInfoDao.Properties.Count.gt(2),BaseMediaFolderInfoDao.Properties.FileSize.gt(300*1024));
        }else if(folderSortType == 5){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.Name)
                    .where(BaseMediaFolderInfoDao.Properties.Count.gt(2),BaseMediaFolderInfoDao.Properties.FileSize.gt(300*1024));
        }else if(folderSortType == 6){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.PathOrUri)
                    .where(BaseMediaFolderInfoDao.Properties.Count.gt(2),BaseMediaFolderInfoDao.Properties.FileSize.gt(300*1024));
        }else if(folderSortType == 7){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.PathOrUri)
                    .where(BaseMediaFolderInfoDao.Properties.Count.gt(2),BaseMediaFolderInfoDao.Properties.FileSize.gt(300*1024));
        }
    }

    public static List<BaseMediaInfo> getAllContentInFolders(String dir, int type) {
        long start = System.currentTimeMillis();

        QueryBuilder<BaseMediaInfo> builder =
         getDaoSession().getBaseMediaInfoDao().queryBuilder()
                .where(BaseMediaInfoDao.Properties.Type.eq(type), BaseMediaInfoDao.Properties.FolderPathOrUri.eq(dir));

        orderFiles(builder);
               // .orderDesc(BaseMediaInfoDao.Properties.UpdatedTime);
        List<BaseMediaInfo> infos  = builder .list();
        Log.w(SafUtil.TAG, " getAllContentInFolders 耗时(ms):" + (System.currentTimeMillis() - start) + ", size:" + infos.size() + ", dir:" + dir);
        return infos;
    }

    /**
     desc[0] ="按更新时间 新在前";
     desc[1] ="按更新时间顺序 旧在前";
     desc[2] = "文件大小从大到小";
     desc[3] ="文件大小从小到大";
     desc[4] ="按文件名 顺序";
     desc[5] ="按文件名  倒序";
     * @param builder
     */
    private static void orderFiles(QueryBuilder<BaseMediaInfo> builder) {
        if(fileSortType == 2){
            builder.orderDesc(BaseMediaInfoDao.Properties.FileSize);
        }else if(fileSortType == 3){
            builder.orderAsc(BaseMediaInfoDao.Properties.FileSize);
        }else if(fileSortType == 0){
            builder.orderDesc(BaseMediaInfoDao.Properties.UpdatedTime);
        }else if(fileSortType == 1){
            builder.orderAsc(BaseMediaInfoDao.Properties.UpdatedTime);
        }else if(fileSortType == 4){
            builder.orderAsc(BaseMediaInfoDao.Properties.Name);
        }else if(fileSortType == 5){
            builder.orderDesc(BaseMediaInfoDao.Properties.Name);
        }
    }


}
