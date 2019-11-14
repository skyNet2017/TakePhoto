package com.darsh.multipleimageselect.compress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * time:2019/11/15
 * author:hss
 * desription:
 */
public class StorageUtils {

    private static String rootPath;
    public static Context context;
    private static boolean canExtWrite;

    public static boolean hasOutSdCard(Context context){
        StorageUtils.context = context.getApplicationContext();
        ArrayList<StorageUtils.Volume> list_volume = StorageUtils.getVolume(context);
        boolean hasOut = false;
        for (int i=0;i<list_volume.size();i++){
            Log.e(i+"","path:"+list_volume.get(i).getPath()+"----"+
                    "removable:"+list_volume.get(i).isRemovable()+"---"+
                    "state:"+list_volume.get(i).getState()+"---can write:"+new File(list_volume.get(i).getPath()).canWrite());
            if(list_volume.get(i).isRemovable()){
                rootPath = list_volume.get(i).getPath();
                canExtWrite = new File(list_volume.get(i).getPath()).canWrite();
                Log.d("hasOutSdCard","---can write:"+DocumentsUtils.checkWritableRootPath(StorageUtils.context,rootPath));
                hasOut = true;
                break;
            }
        }
        return hasOut;
    }


    public static void requestOutSdCradWritePermission(Activity activity){
        if(!hasOutSdCard(activity.getApplicationContext()) || canExtWrite){
            return;
        }
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StorageManager sm = activity.getSystemService(StorageManager.class);

            StorageVolume volume = sm.getStorageVolume(new File(rootPath));

            if (volume != null) {
                intent = volume.createAccessIntent(null);
            }
        }

        if (intent == null) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        activity.startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE);

    }

    public static void onActivityResultForOutSdcardPermission(Activity activity,int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case DocumentsUtils.OPEN_DOCUMENT_TREE_CODE:
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    DocumentsUtils.saveTreeUri(activity, rootPath, uri);
                    Log.d("onActivityResult","---can write:"+new File(rootPath).canWrite());
                    Log.d("onActivityResult2","---can write:"+DocumentsUtils.checkWritableRootPath(StorageUtils.context,rootPath));

                }
                break;
            default:
                break;
        }
    }












    /*
    获取全部存储设备信息封装对象
    context.getExternalFilesDirs(“external”)
     */
    public static ArrayList<Volume> getVolume(Context context) {
        ArrayList<Volume> list_storagevolume = new ArrayList<Volume>();

        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        try {
            Method method_volumeList = StorageManager.class.getMethod("getVolumeList");

            method_volumeList.setAccessible(true);

            Object[] volumeList = (Object[]) method_volumeList.invoke(storageManager);
            if (volumeList != null) {
                Volume volume;
                for (int i = 0; i < volumeList.length; i++) {
                    try {
                        volume = new Volume();
                        volume.setPath((String) volumeList[i].getClass().getMethod("getPath").invoke(volumeList[i]));
                        volume.setRemovable((boolean) volumeList[i].getClass().getMethod("isRemovable").invoke(volumeList[i]));
                        volume.setState((String) volumeList[i].getClass().getMethod("getState").invoke(volumeList[i]));
                        list_storagevolume.add(volume);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                }
            } else {
                Log.e("null", "null-------------------------------------");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return list_storagevolume;
    }



    /*
     存储设备信息封装类
     */
    public static class Volume {
        protected String path;
        protected boolean removable;
        protected String state;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isRemovable() {
            return removable;
        }

        public void setRemovable(boolean removable) {
            this.removable = removable;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}

