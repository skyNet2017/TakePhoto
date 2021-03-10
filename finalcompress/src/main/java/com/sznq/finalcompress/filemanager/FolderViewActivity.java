package com.sznq.finalcompress.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.hss01248.media.mymediastore.http.HttpFile;
import com.hss01248.media.mymediastore.http.HttpResponseBean;
import com.hss01248.media.mymediastore.smb.FileApiForSmb;
import com.hss01248.media.mymediastore.usb.FileApiForUsb;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.databinding.ActivityFolderBinding;
import com.sznq.finalcompress.filemanager.adapter.FileItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class FolderViewActivity extends AppCompatActivity {

    public static void goTo(Activity activity,String ipOrPath,int type,String uName,String pw){
        Intent intent = new Intent(activity,FolderViewActivity.class);
        intent.putExtra("ipOrPath",ipOrPath);
        intent.putExtra("type",type);
        intent.putExtra("uName",uName);
        intent.putExtra("pw",pw);
        activity.startActivity(intent);
    }

    FileItemAdapter adapter;
    ActivityFolderBinding binding;
    String ipOrPath, uName, pw;
    int type;
    private void parseIntent() {
        ipOrPath = getIntent().getStringExtra("ipOrPath");
        uName = getIntent().getStringExtra("uName");
        pw = getIntent().getStringExtra("pw");
        type = getIntent().getIntExtra("type",0);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        binding = ActivityFolderBinding.inflate(getLayoutInflater(), (ViewGroup) getWindow().getDecorView(),false);
        setContentView(binding.getRoot());
        initView();
        IFile file = getFile();
        listFiles(file);

    }

    private void initView() {
        binding.recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapter = new FileItemAdapter(R.layout.item_file);
        binding.recycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
               IFile file =  files.get(position);
               if(file.isDirectory()){
                   listFiles(file);
               }else {
                   ToastUtils.showLong("打开文件:"+file.getPath());
               }
            }
        });

        initMenu();

    }

    private void initMenu() {

    }

    @Override
    public void onBackPressed() {
        if(ipOrPath.equals(folder.getPath())){
            super.onBackPressed();
            return;
        }
        if(folder != null){
            IFile parent = folder.getParentFile();
            if(parent != null){
                listFiles(folder.getParentFile());
                return;
            }
        }
        super.onBackPressed();
    }

    IFile folder;
    List<IFile> files = new ArrayList<>();
    private void listFiles(IFile file) {
        Log.d("pp","path:"+file.getPath());
        folder = file;
        binding.tvPath.setText(file.getPath());
        Observable.just(file)
                .subscribeOn(Schedulers.io())
                .map(new Function<IFile, IFile[]>() {
                    @Override
                    public IFile[] apply(@NonNull IFile iFile) throws Exception {
                        IFile[] files =  iFile.listFiles();
                        if(files == null){
                            return new IFile[0];
                        }
                        return files;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<IFile[]>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull IFile[] iFiles) {
                        files.clear();
                        if(iFiles != null && iFiles.length> 0){
                            for (IFile iFile : iFiles) {
                                files.add(iFile);
                            }
                        }
                        //Collections.sort(files,);
                        adapter.setNewData(files);

                        //更新数据库
                        updateDB(folder,new ArrayList(files));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        files.clear();
                        adapter.setNewData(files);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateDB(IFile folder,  List<IFile> files) {
        new SafFileFinder22<IFile>().getAlbums(folder, true, Executors.newFixedThreadPool(2),
                new ScanFolderCallback() {
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

    private IFile getFile() {
        if(type == StorageBean.TYPE_EXTERNAL_STORAGE){
            return new JavaFile(Environment.getExternalStorageDirectory());
        }
        if(type == StorageBean.TYPE_SAF){
            return new IDocumentFile(SafUtil.sdRoot);
        }
        if(type == StorageBean.TYPE_HTTP_Everything){
            HttpResponseBean bean = new HttpResponseBean();
            bean.url = ipOrPath;
            bean.isDir = true;
            return new HttpFile(bean);
        }
        return null;
    }


}
