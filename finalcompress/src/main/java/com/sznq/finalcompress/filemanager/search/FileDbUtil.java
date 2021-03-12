package com.sznq.finalcompress.filemanager.search;

import android.text.TextUtils;
import android.util.Log;

import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.db.BaseMediaInfoDao;
import com.hss01248.media.mymediastore.db.BaseMediaInfoDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class FileDbUtil {

    public static int pagesize = 2000;

    static List<BaseMediaInfo> searc(String word, int diskType, int mediaType, int sortType,int hiddenType, int[] pageInfo, int sizeType) {
        long start = System.currentTimeMillis();
        //
        QueryBuilder<BaseMediaInfo> builder = DbUtil.getDaoSession().getBaseMediaInfoDao().queryBuilder();
        doFilter(builder,word,diskType,mediaType,hiddenType,sizeType);
        doSort(builder,sortType,mediaType);
        List<BaseMediaInfo> infos = pager(builder,pageInfo);

        Log.w("DirDbUtil", " searchDir 耗时(ms):" + (System.currentTimeMillis() - start) + ", size:" + infos.size());
        return infos;
    }




    private static void doFilter(QueryBuilder<BaseMediaInfo> builder, String word, int diskType, int mediaType, int hiddenType, int sizeType) {
        if(!TextUtils.isEmpty(word)){
            word = "%"+word+"%";
            builder.where(BaseMediaInfoDao.Properties.Name.like(word));
        }
        Log.w("filter","word:"+word);
        filterMediaType(builder,mediaType);
        filterDiskType(builder,diskType);
        filterHiddenType(builder,hiddenType);
        filterSizetype(builder,sizeType);

    }

    /**
     *   desc[0] = ">50kB";
     *         desc[1] ="全部";
     *         desc[2] =">1KB";
     *         desc[3] =">500KB";
     *         desc[4] =">1MB";
     *         desc[5] =">10MB";
     *         desc[6] =">100MB";
     *         desc[7] =">1GB";
     *         desc[8] ="50kB-10M";
     *         desc[9] ="50kB-100M";
     *         desc[10] ="50kB-1GB";
     * @param builder
     * @param sizeType
     */
    private static void filterSizetype(QueryBuilder<BaseMediaInfo> builder, int sizeType) {
        switch (sizeType){
            case 0:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(50*1024));
                break;
            case 2:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(1024));
                break;
            case 3:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(500*1024));
                break;
            case 4:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(1024*1024));
                break;
            case 5:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(10*1024*1024));
                break;
            case 6:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(100*1024*1024));
                break;
            case 7:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(1024*1024*1024));
                break;
            case 8:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(50*1024))
                .where(BaseMediaInfoDao.Properties.FileSize.lt(10*1024*1024));
                break;
            case 9:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(50*1024))
                        .where(BaseMediaInfoDao.Properties.FileSize.lt(100*1024*1024));
                break;
            case 10:
                builder.where(BaseMediaInfoDao.Properties.FileSize.gt(50*1024))
                        .where(BaseMediaInfoDao.Properties.FileSize.lt(1024*1024*1024));
                break;
        }
    }

    /**
     *  desc[0] = "全部";
     *         desc[1] ="仅搜索公开的内容";
     *         desc[2] ="仅搜索隐藏的内容";
     * @param builder
     * @param hiddenType
     */
    private static void filterHiddenType(QueryBuilder<BaseMediaInfo> builder, int hiddenType) {
        switch (hiddenType){
            case 1:
                builder.where(BaseMediaInfoDao.Properties.Hidden.eq(0));
                break;
            case 2:
                builder.where(BaseMediaInfoDao.Properties.Hidden.eq(1));
                break;

        }
    }

    /**
     *  desc[0] = "全部";
     *         desc[1] ="仅手机存储卡";
     *         desc[2] ="手机存储卡和http服务器";
     *         desc[3] ="仅http服务器";
     *         desc[4] ="具体某台http服务器//todo";
     */
    private static void filterDiskType(QueryBuilder<BaseMediaInfo> builder, int diskType) {
        switch (diskType){
            case 1:
                builder.where(BaseMediaInfoDao.Properties.DiskType.eq(StorageBean.TYPE_EXTERNAL_STORAGE));
                break;
            case 2:
                builder.whereOr(BaseMediaInfoDao.Properties.DiskType.eq(StorageBean.TYPE_EXTERNAL_STORAGE),
                        BaseMediaInfoDao.Properties.DiskType.eq(StorageBean.TYPE_HTTP_Everything));
                break;
            case 3:
                builder.where(BaseMediaInfoDao.Properties.DiskType.eq(StorageBean.TYPE_HTTP_Everything));
                break;
            case 4:
                // builder.where(BaseMediaInfoDao.Properties.DiskType.eq(StorageBean.TYPE_EXTERNAL_STORAGE));
                break;
        }
    }

    /**
     desc[0] ="图片和视频";
     desc[1] ="只有图片";
     desc[2] ="只有视频";
     desc[3] ="只有音频";
     desc[4] ="全部文档";
     desc[5] ="pdf";
     desc[6] ="doc";
     desc[7] ="ppt";
     desc[8] ="excel";
     desc[9] ="txt";
     desc[10] ="全部office";
     * @param builder
     * @param mediaType
     */
    private static void filterMediaType(QueryBuilder<BaseMediaInfo> builder, int mediaType) {
        switch (mediaType){
            case 0: builder.whereOr(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_IMAGE),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_VIDEO));
                break;
            case 1: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_IMAGE));
                break;
            case 2: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_VIDEO));
                break;
            case 3: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_AUDIO));
                break;
            case 4: builder.whereOr(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_EXCEL),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_PDF),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_PPT),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_TXT),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_WORD));
                break;
            case 5: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_PDF));
                break;
            case 6: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_WORD));
                break;
            case 7: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_PPT));
                break;
            case 8: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_EXCEL));
                break;
            case 9: builder.where(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_TXT));
                break;
            case 10: builder.whereOr(BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_EXCEL),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_PPT),
                    BaseMediaInfoDao.Properties.MediaType.eq(BaseMediaInfo.TYPE_DOC_WORD));
                break;

        }

    }

    private static void doSort(QueryBuilder<BaseMediaInfo> builder, int fileSortType, int mediaType) {
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
            if(mediaType == BaseMediaInfo.TYPE_IMAGE){
                builder.orderDesc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Name);
            }else if(mediaType == BaseMediaInfo.TYPE_VIDEO){
                builder.orderDesc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Duration);
            }else {
                builder.orderDesc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.FileSize);
            }

        }else if(fileSortType == 7){
            if(mediaType == BaseMediaInfo.TYPE_IMAGE){
                builder.orderAsc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Name);
            }else if(mediaType == BaseMediaInfo.TYPE_VIDEO){
                builder.orderAsc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.Duration);
            }else {
                builder.orderAsc(BaseMediaInfoDao.Properties.MaxSide,BaseMediaInfoDao.Properties.FileSize);
            }

        }else if(fileSortType == 8){
            builder.orderAsc(BaseMediaInfoDao.Properties.Path);
        }else if(fileSortType == 9){
            builder.orderDesc(BaseMediaInfoDao.Properties.Path);
        }else if(fileSortType == 10){
            builder.orderDesc(BaseMediaInfoDao.Properties.Duration);
        }else if(fileSortType == 11){
            builder.orderAsc(BaseMediaInfoDao.Properties.Duration);
        }else if(fileSortType == 12){
            builder.orderDesc(BaseMediaInfoDao.Properties.PraiseCount);
        }
    }

    private static List<BaseMediaInfo> pager(QueryBuilder<BaseMediaInfo> builder, int[] pageIndex) {
        if(pageIndex[0] ==0){
            long count =   builder.count();
            if(count> pagesize){
                //需要使用分页,每页两千
                List<BaseMediaInfo> infos =    builder.limit(pagesize)
                        .offset(pageIndex[0]*pagesize).list();
                pageIndex[1] = (int) Math.ceil(count*1.0f/pagesize);//总条数
                return infos;
            }else {
                List<BaseMediaInfo> infos  = builder .list();
                return infos;
            }
        }else {
            long count =   builder.count();
            List<BaseMediaInfo> infos =    builder.limit(pagesize)
                    .offset(pageIndex[0]*pagesize).list();
            pageIndex[1] = (int) Math.ceil(count*1.0f/pagesize);//总条数
            return infos;
        }
    }
}
