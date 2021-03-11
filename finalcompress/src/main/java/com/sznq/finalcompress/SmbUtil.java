package com.sznq.finalcompress;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;


/**
 * 【Android 冷知识】利用SMB协议远程查看电脑文件或者其他存储设备
 * https://www.jianshu.com/p/6b322df0f68b
 * <p>
 * 【Android 冷知识】SMB协议转Http，实现视频在线播放
 * https://www.jianshu.com/p/e576c8df04bc
 */
public class SmbUtil {

    public static void init(Context context) {
        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        System.setProperty("jcifs.smb.client.soTimeout", "1000000");
        System.setProperty("jcifs.smb.client.responseTimeout", "30000");
    }

    public static void connect() {
        try {
            String ip = "192.168.3.8";
            String username = "Administrator";
            String password = "";

            UniAddress mDomain = UniAddress.getByName(ip);
            NtlmPasswordAuthentication mAuthentication = new NtlmPasswordAuthentication(ip, username, password);
            SmbSession.logon(mDomain, mAuthentication);


            // 获取跟目录然后获取下面各个盘符
            String rootPath = "smb://" + ip + "/";
            SmbFile mRootFolder;
// 匿名登录即无需登录
            /*if (mSpu.isAnonymous()) {
                mRootFolder = new SmbFile(rootPath);
            } else {*/
                mRootFolder = new SmbFile(rootPath, mAuthentication);
           // }

            try {
                SmbFile[] files;
                files = mRootFolder.listFiles();
                Log.w("files", Arrays.toString(files));
                for (SmbFile smbfile : files) {
                    //mAdapterList.add(smbfile);
                }
            } catch (SmbException e) {
                e.printStackTrace();
                /**
                 * jcifs.smb.SmbException: Failed to connect: 0.0.0.0<00>/192.168.1.1
                 * jcifs.util.transport.TransportException
                 * 解决方案
                 * jsifs仅支持SMB1,而不支持SMB2/SMB3。而win10已经不默认开启smb1服务。
                 * ————————————————
                 * 版权声明：本文为CSDN博主「不为自己找借口」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
                 * 原文链接：https://blog.csdn.net/m15738518751/article/details/103462174
                 *
                 * 或者使用implementation group: 'com.hierynomus', name: 'smbj', version: '0.10.0'
                 */
                // ...
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
