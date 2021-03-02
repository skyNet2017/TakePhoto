package com.hss01248.media.mymediastore.smb;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.StorageBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SmbjUtil {

    static Map<String,Map<String,DiskShare>> map = new HashMap<>();

    public static DiskShare getShare(String host,String rootDir) {
        if(!map.containsKey(host)){
            return null;
        }
        if(!map.get(host).containsKey(rootDir)){
            return null;
        }
        return map.get(host).get(rootDir);
    }


    public static String username;
    public static String password;
    public static String myIp ;

    public static void testConnect(){
        List<StorageBean> beans = SmbDbUtil.getSmbHosts();
        if(beans == null || beans.isEmpty()){
            return;
        }
        for (StorageBean bean : beans) {
            checkIfAvaiable(bean);
        }
    }

    private static void checkIfAvaiable(StorageBean bean) {
        if(TextUtils.isEmpty(myIp)){
            myIp = IPUtils.getIpAddress(SafUtil.context);
        }
        String netGate0 = bean.smbHost.substring(0,bean.smbHost.lastIndexOf("."));
        String netGate1 = myIp.substring(0,myIp.lastIndexOf("."));
        if(!netGate0.equals(netGate1)){
            Log.w("smb","不在同一个局域网");
            return;
        }

        SmbConfig config = SmbConfig.builder().withTimeout(120, TimeUnit.SECONDS)
                .withTimeout(120, TimeUnit.SECONDS) // 超时设置读，写和Transact超时（默认为60秒）
                .withSoTimeout(180, TimeUnit.SECONDS) // Socket超时（默认为0秒）
                .build();


        String ip = bean.smbHost;
        String username = bean.smbUName;
        String password = bean.smbPw;
        // 如果不设置超时时间	SMBClient client = new SMBClient();
        SMBClient client = new SMBClient(config);

        try {
            Connection connection = client.connect(ip);    // 如:123.123.123.123
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), ip);
            Session session = connection.authenticate(ac);

            //todo 扫描整个目录

            String dirStr = bean.smbRootDirs;
            if(!TextUtils.isEmpty(dirStr)){
                if(!map.containsKey(bean.smbHost)){
                    map.put(bean.smbHost,new HashMap<>());
                }
                if(dirStr.contains("-")){
                    String[] dirs = dirStr.split("-");
                    for (String dir : dirs) {
                        if(!TextUtils.isEmpty(dir)){
                            try {
                                DiskShare  share = (DiskShare) session.connectShare(dir);
                                map.get(bean.smbHost).put(dir, share);
                            }catch (Throwable throwable){
                                //该dir共享目录没有挂载
                                throwable.printStackTrace();
                                //DbUtil.getDaoSession().getBaseMediaFolderInfoDao().
                            }
                        }
                    }

                }else {
                    String dir = dirStr;
                    if(!TextUtils.isEmpty(dir)){
                        try {
                            DiskShare  share = (DiskShare) session.connectShare(dir);
                            map.get(bean.smbHost).put(dir, share);
                        }catch (Throwable throwable){
                            //该dir共享目录没有挂载
                            throwable.printStackTrace();
                        }
                    }
                }
            }


            /*after digging a bit deeper i found a solution, but in my opinion there is an inconsistant behavior.
by calling share.getSecurityInfo("",...) for Root folder i'm getting an Error.
If i open the directory and call getSecurityInfo it works fine. The difference between those two calls is open(,...) and openDirectory(,...)*/
            // 连接共享文件夹

        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }


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
            SmbToHttp.startServer();

            checkRootDirs(session,ip);

            /*after digging a bit deeper i found a solution, but in my opinion there is an inconsistant behavior.
by calling share.getSecurityInfo("",...) for Root folder i'm getting an Error.
If i open the directory and call getSecurityInfo it works fine. The difference between those two calls is open(,...) and openDirectory(,...)*/
            // 连接共享文件夹

           // share.openDirectory("..",)
// https://github.com/hierynomus/smbj/issues/344   连接根目录的解决方案  https://github.com/rapid7/smbj-rpc

            //外链: 完整的 smb://name:password@host/rootDir/path
            //内链: rootDir拿到对应的share,然后直接通过parentDir/filename就可以拿到文件对象

            //content://com.android.externalstorage.documents/tree/0123-4567:/documents/tree/0123-4567:path

            //整体数据库设计遵循uri结构:
            /**
             * schema
             * name
             * pw
             * host
             * rootDir
             * path
             */




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

    private static void checkRootDirs(Session session, String ip) {
        String[] rootDirs = new String[]{"D","F","G"};
        for (String rootDir : rootDirs) {
            try {
                DiskShare  share = (DiskShare) session.connectShare(rootDir);
                if(!map.containsKey(ip)){
                    map.put(ip,new HashMap<>());
                }
                map.get(ip).put(rootDir,share);
                Log.w("smb", "request path:"+share.toString());
                scanRootDir(ip,rootDir,share);
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }
        }


    }

    private static void scanRootDir(String ip, String rootDir, DiskShare share) {
        List<FileIdBothDirectoryInformation> list = share.list("");
        Log.w("smb", ip + "/" + rootDir + "->files in dir: num " + list.size() + "," + Arrays.toString(list.toArray()));

        for (FileIdBothDirectoryInformation f : list) {
            FileApiForSmb api = new FileApiForSmb(f);
            api.setShare(share, "");
            api.setContext(ip, rootDir);
            api.printInfo();
            SafFileFinder22.start(api, new ScanFolderCallback() {
                @Override
                public void onComplete() {

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
        }
    }

}
