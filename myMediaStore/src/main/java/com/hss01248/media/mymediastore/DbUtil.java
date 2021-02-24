package com.hss01248.media.mymediastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DbUtil {

    public static ISort folderSort = new SortByFolderName();
    public static ISort contentSort = new SortByFileName();

    public static boolean showHidden = false;
    public static int folderSortType = 0;
    public static int folderFilterType = 0;
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
        QueryBuilder<BaseMediaFolderInfo> builder = getDaoSession().getBaseMediaFolderInfoDao().queryBuilder();
        if (!showHidden) {
            builder.where(BaseMediaFolderInfoDao.Properties.Hidden.eq(0));
        }
        doFilter(builder);
        doSort(builder);
        List<BaseMediaFolderInfo> infos = builder.list();
        //移除掉不存在的文件夹:
        if(infos != null && infos.size() > 0){
            List<BaseMediaFolderInfo> toDelete = new ArrayList<>();
            Iterator<BaseMediaFolderInfo> iterator = infos.iterator();
            while (iterator.hasNext()){
                BaseMediaFolderInfo info = iterator.next();
                if(TextUtils.isEmpty(info.pathOrUri)){
                    toDelete.add(info);
                    iterator.remove();
                }else if(info.pathOrUri.startsWith("/storage/")){
                    if(!new File(info.pathOrUri).exists()){
                        toDelete.add(info);
                        iterator.remove();
                    }
                }else if(info.pathOrUri.startsWith("content:")){
                    try {
                        SafUtil.context.getContentResolver().openFileDescriptor(Uri.parse(info.pathOrUri),"r");
                    } catch (FileNotFoundException e) {
                        toDelete.add(info);
                        iterator.remove();
                        e.printStackTrace();
                    }
                }
            }
            if(toDelete.size() > 0){
                getDaoSession().getBaseMediaFolderInfoDao().deleteInTx(toDelete);
                Log.w(SafUtil.TAG, " getAllImageAndVideoFolders deleteInTx:" + toDelete.size());
            }
        }
        Log.w(SafUtil.TAG, " getAllImageAndVideoFolders 耗时(ms):" + (System.currentTimeMillis() - start) + ", size:" + infos.size());
        return infos;
    }

    /**
     *  desc[0] = "全部";
     *         desc[1] ="图片和视频";
     *         desc[2] ="只有图片";
     *         desc[3] ="只有视频";
     *         desc[4] ="只有音频";
     * @param builder
     */
    private static void doFilter(QueryBuilder<BaseMediaFolderInfo> builder) {
        if(folderFilterType == 0){
            builder.whereOr(BaseMediaFolderInfoDao.Properties.Type.eq(1),
                    BaseMediaFolderInfoDao.Properties.Type.eq(2)
                    , BaseMediaFolderInfoDao.Properties.Type.eq(3));
        }else if(folderFilterType == 1){
            builder.whereOr(BaseMediaFolderInfoDao.Properties.Type.eq(1),
                    BaseMediaFolderInfoDao.Properties.Type.eq(2));
        }else if(folderFilterType == 2){
            builder.where(BaseMediaFolderInfoDao.Properties.Type.eq(1));
        }else if(folderFilterType == 3){
            builder.where(BaseMediaFolderInfoDao.Properties.Type.eq(2));
        }else if(folderFilterType == 4){
            builder.where(BaseMediaFolderInfoDao.Properties.Type.eq(3));
        }

    }

    /**
     * 只能处理文件夹内部删除,而文件夹本身没有删除的情况.
     * 如果文件夹本身被删除了呢?
     * @param dirPathOrUri
     * @param types
     */
    public static void delete(String dirPathOrUri,int... types){
        if(types == null || types.length ==0){
            return;
        }
        String[] keys = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            keys[i] = types[i]+"-"+dirPathOrUri;
        }
        getDaoSession().getBaseMediaFolderInfoDao().deleteByKeyInTx(keys);
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
     *           desc[8] ="按时长 顺序";
     *         desc[9] ="按时长  倒序";
     * @param builder
     */
    private static void doSort(QueryBuilder<BaseMediaFolderInfo> builder) {
        if(folderSortType == 0){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.FileSize);
        }else if(folderSortType == 1){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.Count);
        }else if(folderSortType == 2){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.UpdatedTime);
        }else if(folderSortType == 3){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.UpdatedTime);
        }else if(folderSortType == 4){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.Name);
        }else if(folderSortType == 5){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.Name);
        }else if(folderSortType == 6){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.PathOrUri);
        }else if(folderSortType == 7){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.PathOrUri);
        }else if(folderSortType == 8){
            builder.orderAsc(BaseMediaFolderInfoDao.Properties.Duration);
        }else if(folderSortType == 9){
            builder.orderDesc(BaseMediaFolderInfoDao.Properties.Duration);
        }
    }

    public static List<BaseMediaInfo> getAllContentInFolders(String dir, int type) {
        long start = System.currentTimeMillis();

        QueryBuilder<BaseMediaInfo> builder =
         getDaoSession().getBaseMediaInfoDao().queryBuilder()
                .where(BaseMediaInfoDao.Properties.Type.eq(type), BaseMediaInfoDao.Properties.FolderPathOrUri.eq(dir));

        orderFiles(builder,type);
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

     desc[6] ="按画面尺寸 高分辨率在前";
     desc[7] ="按画面尺寸 低分辨率在前";
     if(type == BaseMediaInfo.TYPE_VIDEO || type == BaseMediaInfo.TYPE_AUDIO){
     desc[8] = "按时长 长在前";
     desc[9] ="按时长 短在前";
     }
     * @param builder
     * @param type
     */
    private static void orderFiles(QueryBuilder<BaseMediaInfo> builder, int type) {
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
        }else if(fileSortType == 6){
            if(type == BaseMediaInfo.TYPE_IMAGE){
                builder.orderDesc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Name);
            }else if(type == BaseMediaInfo.TYPE_VIDEO){
                builder.orderDesc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Duration);
            }else {
                builder.orderDesc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.FileSize);
            }

        }else if(fileSortType == 7){
            if(type == BaseMediaInfo.TYPE_IMAGE){
                builder.orderAsc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Name);
            }else if(type == BaseMediaInfo.TYPE_VIDEO){
                builder.orderAsc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Duration);
            }else {
                builder.orderAsc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.FileSize);
            }

        }else if(fileSortType == 8){
            builder.orderDesc(BaseMediaInfoDao.Properties.Duration);
        }else if(fileSortType == 9){
            builder.orderAsc(BaseMediaInfoDao.Properties.Duration);
        }
    }


}
