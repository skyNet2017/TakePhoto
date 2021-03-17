package com.sznq.finalcompress.filemanager.search;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.bean.BaseInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.kongzue.dialog.interfaces.OnInputDialogButtonClickListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.InputDialog;
import com.sznq.finalcompress.MainActivity;

import org.greenrobot.greendao.database.Database;

public class HiddenUtil {

    public static boolean isShowHidden() {
        return showHidden;
    }

    public static void setShowHidden(boolean showHidden) {
        HiddenUtil.showHidden = showHidden;
    }

    static boolean showHidden = false;

    public static void checkPw(AppCompatActivity context,Runnable success){
        if(showHidden){
            success.run();
            return;
        }
        String str = SPUtils.getInstance().getString("hiddenpw");
        if(TextUtils.isEmpty(str)){
            //先设置密码:
            InputDialog.show(context, "设置密码", "设置查看隐藏文件的密码", "确定", "取消")
                    .setOnOkButtonClickListener(new OnInputDialogButtonClickListener() {
                        @Override
                        public boolean onClick(BaseDialog baseDialog, View v, String inputStr) {
                            //inputStr 即当前输入的文本
                            if(TextUtils.isEmpty(inputStr)){
                                ToastUtils.showLong("密码不能为空");
                                return true;
                            }
                            SPUtils.getInstance().put("hiddenpw",EncryptUtils.encryptMD5ToString(inputStr));
                            ToastUtils.showLong("密码设置成功.开始使用隐藏功能吧.");
                            showHidden = true;
                            success.run();
                            baseDialog.doDismiss();
                            return false;
                        }
                    });
        }else {
            InputDialog.show(context, "提示", "请输入密码", "确定", "取消")
                    .setOnOkButtonClickListener(new OnInputDialogButtonClickListener() {
                        @Override
                        public boolean onClick(BaseDialog baseDialog, View v, String inputStr) {
                            //inputStr 即当前输入的文本
                            if(TextUtils.isEmpty(inputStr)){
                                ToastUtils.showLong("密码不能为空");
                                return true;
                            }
                            if(EncryptUtils.encryptMD5ToString(inputStr).equals(str)){
                                showHidden = true;
                                success.run();
                                baseDialog.doDismiss();
                                return false;
                            }
                            ToastUtils.showLong("密码错误");
                            return false;
                        }
                    });
        }

    }


    public static void switchHidePath(String path, BaseInfo baseInfo){
        boolean hidden = baseInfo.getHidden() == 1;
        if(hidden){
            //unhide
            String dir = path;
            if(baseInfo instanceof BaseMediaInfo){
                dir = path.substring(0,path.lastIndexOf("/"));
            }else if(baseInfo instanceof BaseMediaFolderInfo){
                BaseMediaFolderInfo folderInfo = (BaseMediaFolderInfo) baseInfo;
                folderInfo.hidden = 0;
                DbUtil.getDaoSession().getBaseMediaFolderInfoDao().update(folderInfo);
            }
            Database database = DbUtil.getDaoSession().getDatabase();
            database.execSQL("update BASE_MEDIA_INFO set HIDDEN =0 where DIR = ?" ,new String[]{dir});
        }else {
            //hide
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
        }

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
