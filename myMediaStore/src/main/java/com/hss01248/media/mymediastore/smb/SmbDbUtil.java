package com.hss01248.media.mymediastore.smb;

import android.util.Log;

import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.db.BaseMediaFolderInfoDao;
import com.hss01248.media.mymediastore.db.StorageBeanDao;

import java.util.Arrays;
import java.util.List;

public class SmbDbUtil {


    public static List<StorageBean> getSmbHosts(){
        List<StorageBean> list =  DbUtil.getDaoSession().getStorageBeanDao().queryBuilder()
                .where(StorageBeanDao.Properties.Type.eq(StorageBean.TYPE_SMB))
                .list();
        Log.w("smb","smbs:"+ Arrays.toString(list.toArray()));
        return list;
    }

}
