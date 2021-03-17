package com.sznq.finalcompress.filemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.darsh.multipleimageselect.saf.SafUtil;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.TfCardBean;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.db.StorageBeanDao;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.hss01248.media.mymediastore.http.EverythingSearchParser;
import com.noober.menu.FloatMenu;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.filemanager.adapter.StorageAdapter;
import com.sznq.finalcompress.filemanager.storages.AddHttpDialogViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class StorageListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    StorageAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storageslist);
        initView();
        checkPermissions();
    }

    int totalCount = 0;
    private void initView() {
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        adapter = new StorageAdapter(R.layout.item_storage);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Log.d("xx","position:"+position);
                if(position == totalCount -1){
                    ToastUtils.showShort("点击添加:"+position);
                    addHost(position);
                }else {
                    ToastUtils.showShort("点击进入:"+position);
                    StorageBean bean = list1.get(position);
                    FolderViewActivity.goTo(StorageListActivity.this,bean.ip,bean.type,bean.uname,bean.pw);
                }
            }
        });

        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View v, int position) {
                final FloatMenu floatMenu = new FloatMenu(v.getContext(), v);
                //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
                StorageBean bean = list1.get(position);
                ScanFolderCallback folderCallback = new ScanFolderCallback() {
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
                };

                String[] desc = new String[1];
                desc[0] = "扫描此磁盘所有内容";
                floatMenu.items(desc);
                floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        if(bean.type == StorageBean.TYPE_HTTP_Everything){
                            EverythingSearchParser.searchDocType(bean.ip);
                            EverythingSearchParser.searchMediaType(bean.ip);
                        }else if(bean.type == StorageBean.TYPE_EXTERNAL_STORAGE){
                            JavaFile file = new JavaFile(Environment.getExternalStorageDirectory());
                            new SafFileFinder22<JavaFile>().getAlbums(file, Executors.newFixedThreadPool(3),folderCallback);
                        }else if(bean.type == StorageBean.TYPE_SAF){
                            IDocumentFile file = new IDocumentFile(com.hss01248.media.mymediastore.SafUtil.sdRoot);
                            new SafFileFinder22<IDocumentFile>().getAlbums(file, Executors.newFixedThreadPool(3),folderCallback);

                        }
                    }
                });
                floatMenu.showAsDropDown(v);
                return false;
            }
        });
    }

    private void addHost(int position) {
        AddHttpDialogViewHolder viewHolder = new AddHttpDialogViewHolder(null,this,null,false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("")
                .setView(viewHolder.rootView).create();
        dialog.show();
        viewHolder.initDataAndEvent(dialog);
    }

    private void checkPermissions() {

        com.hss01248.media.mymediastore.SafUtil.getRootDir(this, new com.hss01248.media.mymediastore.SafUtil.ISdRoot() {
            @Override
            public void onPermissionGet(DocumentFile dir) {
                Log.w(SafUtil.TAG,"getRootDir:"+dir.getUri());
            }

            @Override
            public void onPermissionDenied(int resultCode, String msg) {

            }
        });

        showStorages();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},78);
            }else {

            }
        }


    }
    List<StorageBean> list1 = new ArrayList<>();
    private void showStorages() {

        list1.clear();
        List<TfCardBean> storages =  new ArrayList<>(com.hss01248.media.mymediastore.SafUtil.storages);

        List<StorageBean> list = DbUtil.getDaoSession().getStorageBeanDao().queryBuilder()
                .where(StorageBeanDao.Properties.Type.eq(StorageBean.TYPE_HTTP_Everything)).list();


        int count = 0;
        for (TfCardBean storage : storages) {
            StorageBean bean = new StorageBean();
           bean.setIp(storage.getPath());
           if(count ==0){
               bean.setType(StorageBean.TYPE_EXTERNAL_STORAGE);
           }else {
               bean.setType(StorageBean.TYPE_SAF);
           }

            list1.add(bean);
            count++;
        }
        list1.addAll(list);
        StorageBean bean = new StorageBean();
        bean.setIp("点击添加everything的http存储器");
        list1.add(bean);
        totalCount = list1.size();
        adapter.replaceData(list1);
    }

    public void addBean(StorageBean bean){
        list1.add(bean);
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
