package com.hss01248.media.mymediastore;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import androidx.documentfile.provider.DocumentFile;

import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScanner {

    static long safStart;

    static AtomicInteger countGetSaf = new AtomicInteger(0);
    static final String TAG = "filescan";


    /**
     * 5条线程同时跑,cpu消耗整体为70%左右.
     * 1条线程,cpu22%  选用此方案
     * 2条线程 cpu 40%
     * 内存占用较小,约50M
     *
     * @param dir
     * @param observer
     */
     static void getAlbums(boolean hasDataInDb,final File dir, ExecutorService executorService, final ScanFolderCallback observer) {
        Log.v(TAG, "开始遍历当前文件夹,原子count计数:" + countGetSaf.incrementAndGet() + ", " + dir.getName());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                File[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    //   * 只能处理文件夹内部删除,而文件夹本身没有删除的情况.
                    //     * 如果文件夹本身被删除了呢?
                    DbUtil.delete(dir.getAbsolutePath(),BaseMediaInfo.TYPE_IMAGE,BaseMediaInfo.TYPE_VIDEO,BaseMediaInfo.TYPE_AUDIO);
                    int count0 = countGetSaf.decrementAndGet();
                    Log.v(TAG, "遍历当前一层文件夹完成,原子count计数:" + count0 + ", " + dir.getName());
                    if (count0 == 0) {
                        SafFileFinder.onComplete(observer,false,safStart);
                    }

                    return;
                }
                List<BaseMediaFolderInfo> folderInfos = new ArrayList<>();

                BaseMediaFolderInfo imageFolder = null;
                int imageCount = 0;
                long imagesFileSize = 0;

                BaseMediaFolderInfo videoFolder = null;
                int videoCount = 0;
                long videoFileSize = 0;
                long videoDuration = 0;

                BaseMediaFolderInfo audioFolder = null;
                int audioCount = 0;
                long audioFileSize = 0;
                long audioDuration = 0;

                List<BaseMediaInfo> images = null;
                List<BaseMediaInfo> videos = null;
                List<BaseMediaInfo> audios = null;
                int isHiden = 0;

                for (File file : files) {
                    if (file.isDirectory()) {
                        //todo 6500个文件夹. 最后将其归并显示
                        if ("MicroMsg".equals(file.getName())) {
                            continue;
                        }
                        //700多个
                        if ("MobileQQ".equals(file.getName())) {
                            continue;
                        }
                        //Log.d("监听","进入文件夹遍历:"+dir.getAbsolutePath());
                        //todo 单线程时为深度优先.  那么前后两次要反着来
                        getAlbums(hasDataInDb,file, executorService, observer);
                    } else {
                        String name = file.getName();
                        if(TextUtils.isEmpty(name)){
                            continue;
                        }
                        if(".nomedia".equals(name)){
                            isHiden = 1;
                            continue;
                        }
                        if(file.length() <=0){
                            continue;
                        }
                        int type = SafFileFinder.guessTypeByName(name);

                        if (type == BaseMediaInfo.TYPE_IMAGE) {
                            imageCount++;
                            imagesFileSize = imagesFileSize + file.length();

                            if (imageFolder == null) {
                                imageFolder = new BaseMediaFolderInfo();
                                imageFolder.name = dir.getName();
                                imageFolder.cover = file.getAbsolutePath();
                                imageFolder.mediaType = BaseMediaInfo.TYPE_IMAGE;
                                imageFolder.updatedTime = file.lastModified();
                                imageFolder.path = dir.getAbsolutePath();
                                Log.d("扫描", "添加有图文件夹:" + dir.getAbsolutePath());
                            }

                            //内部文件uri的保存:
                            if (images == null) {
                                images = new ArrayList<>(files.length / 2);
                            }
                            BaseMediaInfo image = new BaseMediaInfo();
                            image.dir = dir.getAbsolutePath();
                            image.path = file.getAbsolutePath();
                            image.updatedTime = file.lastModified();
                            image.name = file.getName();
                            image.fileSize = file.length();
                            image.mediaType = BaseMediaInfo.TYPE_IMAGE;
                            images.add(image);

                            //图片宽高:
                            int[] imageWidthHeight = SafUtil.getImageWidthHeight(file.getAbsolutePath());
                            if(imageWidthHeight != null && imageWidthHeight.length == 2){
                                image.maxSide = Math.max(imageWidthHeight[0],imageWidthHeight[1]);
                            }

                        } else if (type == BaseMediaInfo.TYPE_VIDEO) {
                            videoCount++;
                            videoFileSize = videoFileSize + file.length();
                            if (videoFolder == null) {
                                videoFolder = new BaseMediaFolderInfo();
                                videoFolder.name = dir.getName();
                                videoFolder.cover = file.getAbsolutePath();
                                videoFolder.updatedTime = file.lastModified();
                                videoFolder.mediaType = BaseMediaInfo.TYPE_VIDEO;
                                videoFolder.path = dir.getAbsolutePath();
                                Log.d("扫描", "添加有视频文件夹:" + dir.getAbsolutePath());
                            }


                            //内部文件uri的保存:
                            if (videos == null) {
                                videos = new ArrayList<>(files.length / 2);
                            }
                            BaseMediaInfo image = new BaseMediaInfo();
                            image.dir = dir.getAbsolutePath();
                            image.path = file.getAbsolutePath();
                            image.updatedTime = file.lastModified();
                            image.name = file.getName();
                            image.fileSize = file.length();
                            image.mediaType = BaseMediaInfo.TYPE_VIDEO;
                            videos.add(image);

                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            try {

                                //FileInputStream inputStream = new FileInputStream(new File(image.pathOrUri).getAbsolutePath());
                                retriever.setDataSource(image.path);
                                image.duration = SafUtil.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000;
                                videoDuration = videoDuration + image.duration;
                                int width = SafUtil.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); //宽
                                int height = SafUtil.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)); //高
                                image.maxSide = Math.max(width,height);
                            }catch (Throwable throwable){
                                Log.w("errorv",image.path);
                                throwable.printStackTrace();
                            }finally {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    retriever.close();
                                }
                            }
                        } else if (type == BaseMediaInfo.TYPE_AUDIO) {
                            audioCount++;
                            audioFileSize = audioFileSize + file.length();
                            if (audioFolder == null) {
                                audioFolder = new BaseMediaFolderInfo();
                                audioFolder.name = dir.getName();
                                audioFolder.cover = file.getAbsolutePath();
                                audioFolder.updatedTime = file.lastModified();
                                audioFolder.mediaType = BaseMediaInfo.TYPE_AUDIO;
                                audioFolder.path = dir.getAbsolutePath();
                                Log.d("扫描", "添加有音频文件夹:" + dir.getAbsolutePath());
                            }

                            //内部文件uri的保存:
                            if (audios == null) {
                                audios = new ArrayList<>(files.length / 2);
                            }
                            BaseMediaInfo image = new BaseMediaInfo();
                            image.dir = dir.getAbsolutePath();
                            image.path = file.getAbsolutePath();
                            image.updatedTime = file.lastModified();
                            image.name = file.getName();
                            image.fileSize = file.length();
                            image.mediaType = BaseMediaInfo.TYPE_AUDIO;
                            audios.add(image);


                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            try {
                                try {
                                    if(image.path.startsWith("content")){
                                        retriever.setDataSource(SafUtil.context,Uri.parse(image.path));
                                    }else {
                                        FileInputStream inputStream = new FileInputStream(new File(image.path).getAbsolutePath());
                                        retriever.setDataSource(inputStream.getFD());
                                    }

                                }catch (Throwable throwable){
                                    Log.w("error",image.path);
                                    throwable.printStackTrace();
                                }
                                audioDuration = audioDuration + SafUtil.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/1000;
                            }catch (Throwable throwable){
                                throwable.printStackTrace();
                            }finally {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    retriever.close();
                                }
                            }
                        }
                    }
                }
                if (imageFolder != null) {
                    imageFolder.generateTheId();
                    imageFolder.count = imageCount;
                    imageFolder.fileSize = imagesFileSize;
                    imageFolder.hidden = isHiden;
                    folderInfos.add(imageFolder);
                }else {
                    DbUtil.delete(dir.getAbsolutePath(),BaseMediaInfo.TYPE_IMAGE);
                }
                if (videoFolder != null) {
                    videoFolder.generateTheId();
                    videoFolder.count = videoCount;
                    videoFolder.hidden = isHiden;
                    videoFolder.fileSize = videoFileSize;
                    videoFolder.duration = videoDuration;
                    folderInfos.add(videoFolder);
                }else {
                    DbUtil.delete(dir.getAbsolutePath(),BaseMediaInfo.TYPE_VIDEO);
                }

                if (audioFolder != null) {
                    audioFolder.generateTheId();
                    audioFolder.count = audioCount;
                    audioFolder.hidden = isHiden;
                    audioFolder.fileSize = audioFileSize;
                    audioFolder.duration = audioDuration;
                    folderInfos.add(audioFolder);
                }else {
                    DbUtil.delete(dir.getAbsolutePath(),BaseMediaInfo.TYPE_AUDIO);
                }
                if (folderInfos.size() != 0) {
                    SafFileFinder.print(folderInfos,false);
                    if(!hasDataInDb){
                            if(DbUtil.showHidden){
                                observer.onScanEachFolder(folderInfos);
                            }else {
                                if(folderInfos.get(0).hidden == 0){
                                    observer.onScanEachFolder(folderInfos);
                                }
                            }

                    }
                }
                SafFileFinder.writeDB(DocumentFile.fromFile(dir), folderInfos, images, videos, audios);

                int count0 = countGetSaf.decrementAndGet();
                Log.v(TAG, "遍历当前一层文件夹完成,原子count计数:" + count0 + ", " + dir.getName());
                if (count0 == 0) {
                    SafFileFinder.onComplete(observer,false,safStart);
                }
            }
        });

    }
}
