package com.hss01248.media.mymediastore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.smb.FileApiForSmb;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class SafFileFinder22<T extends IFile>{

    static final String SP_NAME = "DirPermission";

    private static void listAllAlbum(final ScanFolderCallback observer, boolean onlyDb) {
        listFromDb(observer,onlyDb);
    }

    public static List<String> folderToSkip = new ArrayList<>();
    static {
        folderToSkip.add("projects");
        folderToSkip.add("dev");
        folderToSkip.add("MicroMsg");
        folderToSkip.add("MobileQQ");
        //360驱动大师目录
        folderToSkip.add("360驱动大师目录");
        //Program Files
        folderToSkip.add("Program Files");
        //node_modules
        folderToSkip.add("node_modules");
        folderToSkip.add(".idea");
        folderToSkip.add("build");
        folderToSkip.add(".cxx");
        folderToSkip.add(".gradle");
        folderToSkip.add(".externalNativeBuild");
        folderToSkip.add(".m2");
        folderToSkip.add(".npm");
        //resources
        folderToSkip.add("resources");
        folderToSkip.add("res");
        folderToSkip.add("ProgramData");
        folderToSkip.add("Program Files");
        folderToSkip.add("Windows");
    }

    private static void listFromDb(ScanFolderCallback observer, boolean onlyDb) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<BaseMediaFolderInfo> infos = DbUtil.getAllFolders2();

                boolean hasDataInDb = false;
                if (infos != null && infos.size() > 0) {
                    observer.onFromDB(infos);
                    hasDataInDb = true;
                }

                if(onlyDb){
                    return;
                }

                scanByFile(hasDataInDb, executorService, observer);
                scanBySaf(hasDataInDb, executorService, observer);

            }
        });
    }

    private static void scanByFile(boolean hasDataInDb, ExecutorService executorService, ScanFolderCallback observer) {
        if(System.currentTimeMillis() - FileScanner.safStart < 60*1000*10){
            //10min内不刷新
            return;
        }
        FileScanner.safStart = System.currentTimeMillis();
        FileScanner.getAlbums(hasDataInDb,Environment.getExternalStorageDirectory(),executorService,observer);
    }

    volatile static boolean hasFinishedBefore;
    private static void scanBySaf(boolean hasDataInDb, ExecutorService executorService, ScanFolderCallback observer) {
        if (SafUtil.sdRoot == null) {
            Log.w(SafUtil.TAG, Thread.currentThread().getName() + "  SafUtil.sdRoot is null");
            observer.onComplete();
            return;
        }

        SharedPreferences sp = SafUtil.context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        if (hasDataInDb) {
            long latScanFinishedTime = sp.getLong("latScanFinishedTime", 0);
            if (latScanFinishedTime != 0 && (System.currentTimeMillis() - latScanFinishedTime < 12 * 60 * 60 * 1000)) {
                Log.w(SafUtil.TAG, "一天内只全量扫描一次");
                //遍历所有文件夹完成!!!!!!!!!!!!!!! 耗时(s):1039

                //每次重点扫描最大的几个文件夹
                return;
            }
            hasFinishedBefore = latScanFinishedTime !=0;
            sp.edit().putBoolean("isScaning", true).commit();
            //有数据,那么接下来就只用一个线程去跑
            //safStart = System.currentTimeMillis();
            IDocumentFile documentFile = new IDocumentFile(SafUtil.sdRoot);
            new SafFileFinder22<IDocumentFile>().getAlbums(documentFile, latScanFinishedTime !=0 ? executorService : Executors.newFixedThreadPool(8), observer);
        } else {
            sp.edit().putBoolean("isScaning", true).commit();
            //没有数据,就用5个线程去跑
            //safStart = System.currentTimeMillis();

            IDocumentFile documentFile = new IDocumentFile(SafUtil.sdRoot);
            new SafFileFinder22<IDocumentFile>().getAlbums(documentFile, Executors.newFixedThreadPool(8), observer);
        }
    }

    public static   void start(FileApiForSmb iFile,ScanFolderCallback observer){
        new SafFileFinder22<FileApiForSmb>().getAlbums(iFile, Executors.newFixedThreadPool(2), observer);
    }

     long safStart;

     AtomicInteger countGetSaf = new AtomicInteger(0);


    public void getAlbums(final T dir, ExecutorService executorService, final ScanFolderCallback observer) {
        getAlbums(dir,false,executorService,observer);
    }

    /**
     * 5条线程同时跑,cpu消耗整体为70%左右.
     * 1条线程,cpu22%  选用此方案
     * 2条线程 cpu 40%
     * 内存占用较小,约50M
     *
     * @param dir
     * @param observer
     */
      public void getAlbums(final T dir,boolean justScanCurrentDir, ExecutorService executorService, final ScanFolderCallback observer) {
          //todo 6500个文件夹. 最后将其归并显示

          if(folderToSkip.contains(dir.getName())){
              Log.e("smb","跳过文件夹:"+dir.getUri());
              return;
          }

        Log.w(SafUtil.TAG, "开始遍历当前文件夹,原子count计数:" + countGetSaf.incrementAndGet() + ", " + dir.getPath()+", name:"+dir.getName());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                IFile[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    DbUtil.delete(dir.getPath(),BaseMediaInfo.TYPE_IMAGE,BaseMediaInfo.TYPE_VIDEO,BaseMediaInfo.TYPE_AUDIO);
                    int count0 = countGetSaf.decrementAndGet();
                    Log.e(SafUtil.TAG, "当前文件夹为空,原子count计数:" + count0 + ", " + dir.getPath()+", name:"+dir.getName());
                    if (count0 == 0) {
                        onComplete(observer,true,safStart,dir);
                    }

                    return;
                }
                String p = dir.getPath();
                int diskType = StorageBean.TYPE_EXTERNAL_STORAGE;
                if(p.startsWith("content")){
                    diskType = StorageBean.TYPE_SAF;
                }else if(p.startsWith("http")){
                    diskType = StorageBean.TYPE_HTTP_Everything;
                }else if(p.startsWith("/storage/")){
                    diskType = StorageBean.TYPE_EXTERNAL_STORAGE;
                }

                Map<Integer,BaseMediaFolderInfo> folderMap = new HashMap<>();
                Map<Integer,List<BaseMediaInfo>> filesMap = new HashMap<>();
                int  isHiden = 0;

                for (IFile file : files) {
                    file.printInfo();
                    if (file.isDirectory()) {
                        if(justScanCurrentDir){
                            continue;
                        }
                       if( folderToSkip.contains(file.getName())){
                           Log.e("smb","跳过文件夹:"+file.getPath());
                            continue;
                        }

                        //Log.d("监听","进入文件夹遍历:"+dir.getAbsolutePath());
                        //todo 单线程时为深度优先.  那么前后两次要反着来
                        getAlbums((T) file, executorService, observer);
                    } else {
                        String name = file.getName();
                        if(TextUtils.isEmpty(name)){
                            Log.e("smb","file name is empty:"+file.getPath());
                            continue;
                        }
                        if(".nomedia".equals(name)){
                             isHiden = 1;
                            continue;
                        }
                        //RECYCLE.BIN
                        if(dir.getPath().contains("RECYCLE.BIN")){
                            isHiden = 1;
                        }
                        if(file.length() <=0){
                            Log.e("smb","file.length() <=0:"+file.getPath());
                            continue;
                        }

                        int type = guessTypeByName(name);
                        if(type == BaseMediaInfo.TYPE_UNKNOWN){
                            continue;
                        }

                        if(!folderMap.containsKey(type)){
                            BaseMediaFolderInfo folder = new BaseMediaFolderInfo();
                            folder.name = dir.getName();
                            folder.cover = file.getPath();
                            folder.mediaType = type;
                            folder.diskType = diskType;
                            folder.hidden = isHiden;
                            folder.updatedTime = file.lastModified();
                            folder.path = dir.getPath();
                            folder.generateTheId();
                            Log.w("扫描", "添加文件夹:" + dir.getPath());
                            folderMap.put(type,folder);
                        }
                        BaseMediaFolderInfo folderInfo =   folderMap.get(type);
                        folderInfo.count++;
                        folderInfo.fileSize += file.length();

                        if(!filesMap.containsKey(type)){
                            List<BaseMediaInfo> infos = new ArrayList<>();
                            filesMap.put(type,infos);
                        }
                        List<BaseMediaInfo> fileInfos = filesMap.get(type);

                        BaseMediaInfo image = new BaseMediaInfo();
                        image.dir = dir.getPath();
                        image.path = file.getPath();
                        image.hidden = isHiden;
                        image.updatedTime = file.lastModified();
                        image.name = file.getName();
                        image.diskType = diskType;
                        image.fileSize = file.length();
                        image.mediaType = type;
                        image.fillMediaInfo();
                        fileInfos.add(image);
                    }
                }
                //types:
                for (int i = 1; i < 9; i++) {
                    if(!filesMap.containsKey(i)){
                        DbUtil.delete(dir.getUri().toString(),i);
                    }
                }

                List<BaseMediaFolderInfo> folderInfos = new ArrayList<>();
                Iterator<Map.Entry<Integer, BaseMediaFolderInfo>> iterator1 = folderMap.entrySet().iterator();
                while (iterator1.hasNext()){
                    Map.Entry<Integer, BaseMediaFolderInfo> next = iterator1.next();
                    BaseMediaFolderInfo value = next.getValue();
                    value.hidden = isHiden;
                    value.generateTheId();
                    folderInfos.add(value);
                }
                if (folderInfos.size() != 0) {
                    print(folderInfos, true);
                    if(!hasFinishedBefore){
                        if(DbUtil.showHidden){
                            observer.onScanEachFolder(folderInfos);
                        }else {
                            if(folderInfos.get(0).hidden == 0){
                                observer.onScanEachFolder(folderInfos);
                            }
                        }
                    }
                }else {
                    Log.e("dd0","no media or doc exist in path:"+dir.getPath()+"/"+dir.getName());
                }
                writeDB(dir, folderInfos, filesMap);

                int count0 = countGetSaf.decrementAndGet();
                Log.w(SafUtil.TAG, "遍历当前一层文件夹完成,原子count计数:" + count0 + ", " + dir.getPath()+", name:"+dir.getName());
                if (count0 == 0) {
                    onComplete(observer,true,safStart,dir);
                }
            }
        });

    }



    static void writeDB(IFile dir, List<BaseMediaFolderInfo> folderInfos, Map<Integer,List<BaseMediaInfo>> filesMap) {
        long start = System.currentTimeMillis();
        //文件夹:
        if (folderInfos.size() > 0) {
            DbUtil.insertOrUpdate(folderInfos);
            //其实是同一个文件夹,同时有图片,音视频,怎么处理? 用type-path作为id:
           // DbUtil.getDaoSession().getBaseMediaFolderInfoDao().insertOrReplaceInTx(folderInfos);
            //DbUtil.getDaoSession().getBaseMediaFolderInfoDao().upda
            // 如何不更新里面的hidden值?
            // 已删除文件的处理:DbUtil.delete(dir.getUri().toString(),BaseMediaInfo.TYPE_AUDIO);
        }



         Iterator<Map.Entry<Integer, List<BaseMediaInfo>>> iterator = filesMap.entrySet().iterator();
         while (iterator.hasNext()){
             Map.Entry<Integer, List<BaseMediaInfo>> next = iterator.next();
             //DbUtil.getDaoSession().getBaseMediaInfoDao().insertOrReplaceInTx(next.getValue());
             DbUtil.insertOrUpdate2(next.getValue());

         }
        if (folderInfos.size() > 0) {
            Log.w(SafUtil.TAG, URLDecoder.decode(dir.getUri().toString()) + "  路径下更新数据库完成,耗时(ms):" + (System.currentTimeMillis() - start));
        }

        //todo 已经删除的文件,怎么删除数据库里的条目?


    }

     static void print(List<BaseMediaFolderInfo> folderInfos, boolean isSaf) {
        for (BaseMediaFolderInfo folderInfo : folderInfos) {
            Log.w(isSaf ? SafUtil.TAG : FileScanner.TAG, folderInfo.mediaType + "-type-count-" + folderInfo.count + "-文件夹---->:" + folderInfo.path);
        }
    }

      void onComplete(ScanFolderCallback observer,boolean isSaf,long safStart,T root) {
        List<BaseMediaFolderInfo> infos = DbUtil.getAllFolders2();
        observer.onScanFinished(infos);
        observer.onComplete();
        Log.w(isSaf ? SafUtil.TAG : FileScanner.TAG, URLDecoder.decode(root.getUri().toString())+"  遍历所有文件夹完成!!!!!!!!!!!!!!! 耗时(s):" + (System.currentTimeMillis() - safStart) / 1000);

        /*if(isSaf){
            SharedPreferences sp = SafUtil.context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putBoolean("isScaning", false).commit();
            editor.putLong("latScanFinishedTime", System.currentTimeMillis()).commit();
        }*/

    }


    /**
     * 图片: https://zh.wikipedia.org/wiki/%E5%9B%BE%E5%BD%A2%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F%E6%AF%94%E8%BE%83
     * 视频后缀
     * 最常见：.mpg .mpeg .avi .rm .rmvb .mov .wmv .asf .dat
     * 不常见的：.asx .wvx .mpe .mpa
     * 音频后缀
     * 常见的：.mp3 .wma .rm .wav .mid
     * .ape .flac
     * <p>
     * 常见 MIME 类型列表
     * https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     * <p>
     * 作者：耐住寂寞守住繁华_5b9a
     * 链接：https://www.jianshu.com/p/8962f2a5186e
     * 来源：简书
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     *
     * @param name
     * @return
     */
    public static int guessTypeByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return BaseMediaInfo.TYPE_UNKNOWN;
        }
        if(name.endsWith(".txt")){
            return BaseMediaInfo.TYPE_DOC_TXT;
        }
        if(name.endsWith(".pdf")){
            return BaseMediaInfo.TYPE_DOC_PDF;
        }
        String mime = getTypeForName(name);
        if (mime.startsWith("image/")) {
            return BaseMediaInfo.TYPE_IMAGE;
        } else if (mime.startsWith("video/")) {
            return BaseMediaInfo.TYPE_VIDEO;
        } else if (mime.startsWith("audio/")) {
            return BaseMediaInfo.TYPE_AUDIO;
        }else if (mime.contains("msword")) {
            return BaseMediaInfo.TYPE_DOC_WORD;
        }else if (mime.contains("excel")) {
            return BaseMediaInfo.TYPE_AUDIO;
        }else if (mime.contains("powerpoint")) {
            return BaseMediaInfo.TYPE_DOC_PPT;
        }
        /*if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif")
                || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")
                || name.endsWith(".svg")  || name.endsWith(".bmp")) {
            return BaseMediaInfo.TYPE_IMAGE;
        }else if(name.endsWith(".mp4") || name.endsWith(".MP4") || name.endsWith(".mkv") || name.endsWith(".avi")
                || name.endsWith(".mpeg") || name.endsWith(".wmv") || name.endsWith(".mpg") || name.endsWith(".rmvb")
                || name.endsWith(".mov") || name.endsWith(".flv")){
            return BaseMediaInfo.TYPE_VIDEO;
        }else if(name.endsWith(".m4a") ||name.endsWith(".mp3") || name.endsWith(".MP3") || name.endsWith(".aac") || name.endsWith(".wav")
                || name.endsWith(".wma") || name.endsWith(".mid") || name.endsWith(".ape") || name.endsWith(".flac")){
            return BaseMediaInfo.TYPE_AUDIO;
        }*/
        return BaseMediaInfo.TYPE_UNKNOWN;
    }

    public static boolean isVideo(String name) {
        return guessTypeByName(name) == BaseMediaInfo.TYPE_VIDEO;
    }

    public static boolean isImage(String name) {
        return guessTypeByName(name) == BaseMediaInfo.TYPE_IMAGE;
    }

    public static boolean isAudio(String name) {
        return guessTypeByName(name) == BaseMediaInfo.TYPE_AUDIO;
    }

    public static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                /*Log.v(SafUtil.TAG,"mimeType:"+mime +" ->>"+name);
                int last = mime.indexOf("/");
                if(last >0){
                    String type = mime.substring(0,last);
                    Log.v(SafUtil.TAG,"raw type:"+type +" ->>"+name);
                }*/

                return mime;
            }
        }

        return "application/octet-stream";
    }


}
