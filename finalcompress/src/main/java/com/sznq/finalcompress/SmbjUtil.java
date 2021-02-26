package com.sznq.finalcompress;

import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SmbjUtil {

    private static final String SHARE_SRC_DIR = "/";


    public static void connect(){
        // 设置超时时间(可选)
        SmbConfig config = SmbConfig.builder().withTimeout(120, TimeUnit.SECONDS)
                .withTimeout(120, TimeUnit.SECONDS) // 超时设置读，写和Transact超时（默认为60秒）
                .withSoTimeout(180, TimeUnit.SECONDS) // Socket超时（默认为0秒）
                .build();


        String ip = "192.168.3.8";
        String username = "Administrator";
        String password = "614511qc";
        // 如果不设置超时时间	SMBClient client = new SMBClient();
        SMBClient client = new SMBClient(config);

        try {
            Connection connection = client.connect(ip);	// 如:123.123.123.123
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), ip);
            Session session = connection.authenticate(ac);

            // 连接共享文件夹
            DiskShare share = (DiskShare) session.connectShare("D");
            List<FileIdBothDirectoryInformation> list = share.list("");
            Log.w("files", "files in smb: num "+list.size()+","+Arrays.toString(list.toArray()));

            String folder = SHARE_SRC_DIR ;
            String dstRoot = "要保存的本地文件夹路径";	// 如: D:/smd2/

            /*for (FileIdBothDirectoryInformation f : share.list(SHARE_DST_DIR, "*.mp4")) {
                String filePath = folder + f.getFileName();
                String dstPath = dstRoot + f.getFileName();

                FileOutputStream fos = new FileOutputStream(dstPath);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                if (share.fileExists(filePath)) {
                    System.out.println("正在下载文件:" + f.getFileName());

                    File smbFileRead = share.openFile(filePath, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
                    InputStream in = smbFileRead.getInputStream();
                    byte[] buffer = new byte[4096];
                    int len = 0;
                    while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, len);
                    }

                    bos.flush();
                    bos.close();

                    System.out.println("文件下载成功");
                    System.out.println("==========================");
                } else {
                    System.out.println("文件不存在");
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

}
