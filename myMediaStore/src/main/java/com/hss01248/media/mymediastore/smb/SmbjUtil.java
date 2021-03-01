package com.hss01248.media.mymediastore.smb;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.SecurityDescriptor;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.fileapi.IFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SmbjUtil {

    private static final String SHARE_SRC_DIR = "D";
    static Map<String,DiskShare> map = new HashMap<>();

    public static DiskShare getShare() {
        return share;
    }

    public static DiskShare share;

    static boolean hasFinished;

    public static String username;
    public static String password;

    public static void connect(){
        // 设置超时时间(可选)
        SmbConfig config = SmbConfig.builder().withTimeout(120, TimeUnit.SECONDS)
                .withTimeout(120, TimeUnit.SECONDS) // 超时设置读，写和Transact超时（默认为60秒）
                .withSoTimeout(180, TimeUnit.SECONDS) // Socket超时（默认为0秒）
                .build();


        String ip = "192.168.3.8";
         username = "Administrator";
         password = "614511qc";
        // 如果不设置超时时间	SMBClient client = new SMBClient();
        SMBClient client = new SMBClient(config);

        try {
            Connection connection = client.connect(ip);	// 如:123.123.123.123
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), ip);
            Session session = connection.authenticate(ac);


            /*after digging a bit deeper i found a solution, but in my opinion there is an inconsistant behavior.
by calling share.getSecurityInfo("",...) for Root folder i'm getting an Error.
If i open the directory and call getSecurityInfo it works fine. The difference between those two calls is open(,...) and openDirectory(,...)*/
            // 连接共享文件夹
             share = (DiskShare) session.connectShare(SHARE_SRC_DIR);
            map.put(ip+"/"+SHARE_SRC_DIR,share);
            Log.w("smb", "request path:"+share.toString());
            SmbToHttp.startServer();
           // share.openDirectory("..",)
// https://github.com/hierynomus/smbj/issues/344   连接根目录的解决方案  https://github.com/rapid7/smbj-rpc

            /*if(SafUtil.context.getSharedPreferences("smb", Context.MODE_PRIVATE).getBoolean("hassmbfinished"+SHARE_SRC_DIR,false)){
                Log.w("smb", SHARE_SRC_DIR+"已经扫描过,app进程挂掉前不再扫描");
                return;
            }*/
            /*List<FileIdBothDirectoryInformation> list = share.list("");
            Log.w("smb", "files in smb: num "+list.size()+","+Arrays.toString(list.toArray()));

            String folder = SHARE_SRC_DIR ;
            String dstRoot = new java.io.File(Environment.getExternalStorageDirectory(),"smbdownloa").getAbsolutePath();	// 如: D:/smd2/

            for (FileIdBothDirectoryInformation f : list) {
                //, "*.mp4"
                String filePath = folder + f.getFileName();
                String dstPath = dstRoot + f.getFileName();
                FileApiForSmb api = new FileApiForSmb(f);
                api.setShare(share,"");
                api.setContext(ip,SHARE_SRC_DIR);
                api.printInfo();
                SafFileFinder22.start(api, new ScanFolderCallback() {
                    @Override
                    public void onComplete() {
                        *//*SafUtil.context.getSharedPreferences("smb", Context.MODE_PRIVATE)
                                .edit().putBoolean("hassmbfinished"+SHARE_SRC_DIR,true).apply();*//*
                    }

                    @Override
                    public void onFromDB(List<BaseMediaFolderInfo> folderInfos) {

                    }

                    @Override
                    public void onScanEachFolder(List<BaseMediaFolderInfo> folderInfos) {

                    }

                    @Override
                    public void onScanFinished(List<BaseMediaFolderInfo> folderInfos) {

                    }
                });


            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                //client.close();
            }
        }
    }

}
