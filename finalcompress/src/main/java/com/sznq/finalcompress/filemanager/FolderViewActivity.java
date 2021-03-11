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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.darsh.multipleimageselect.FileOpenUtil;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.FileTypeUtil;
import com.hss01248.media.mymediastore.SafFileFinder22;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.ScanFolderCallback;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.hss01248.media.mymediastore.bean.StorageBean;
import com.hss01248.media.mymediastore.fileapi.IDocumentFile;
import com.hss01248.media.mymediastore.fileapi.IFile;
import com.hss01248.media.mymediastore.fileapi.JavaFile;
import com.hss01248.media.mymediastore.http.HttpFile;
import com.hss01248.media.mymediastore.http.HttpResponseBean;
import com.hss01248.media.mymediastore.smb.FileApiForSmb;
import com.hss01248.media.mymediastore.usb.FileApiForUsb;
import com.hss01248.pagestate.PageStateManager;
import com.noober.menu.FloatMenu;
import com.sznq.finalcompress.R;
import com.sznq.finalcompress.databinding.ActivityFolderBinding;
import com.sznq.finalcompress.filemanager.adapter.FileItemAdapter;
import com.sznq.finalcompress.filemanager.adapter.FileItemImgAdapter;
import com.sznq.finalcompress.filemanager.folder.BaseFolderSort;
import com.sznq.finalcompress.filemanager.folder.sort.FileNameSortAes;
import com.sznq.finalcompress.filemanager.folder.sort.FileNameSortDes;
import com.sznq.finalcompress.filemanager.folder.sort.FileSizeSortAES;
import com.sznq.finalcompress.filemanager.folder.sort.FileSizeSortDes;
import com.sznq.finalcompress.filemanager.folder.sort.ModifyTimeSortAes;
import com.sznq.finalcompress.filemanager.folder.sort.ModifyTimeSortDes;
import com.sznq.finalcompress.filemanager.search.SearchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    BaseQuickAdapter adapter;
    ActivityFolderBinding binding;
    String ipOrPath, uName, pw;
    int type;
    int displayType;
    PageStateManager stateManager;
    private void parseIntent() {
        ipOrPath = getIntent().getStringExtra("ipOrPath");
        uName = getIntent().getStringExtra("uName");
        pw = getIntent().getStringExtra("pw");
        type = getIntent().getIntExtra("type",0);
        if(type == StorageBean.TYPE_HTTP_Everything){
            if(!ipOrPath.startsWith("http")){
                ipOrPath = "http://"+ipOrPath;
            }
        }
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
        initRecycleviewByType(0);


        binding.titlebar.getRightTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMenu();
            }
        });

        if(type != StorageBean.TYPE_EXTERNAL_STORAGE){
            stateManager = PageStateManager.initWhenUse(binding.recycler,null);
            stateManager.showContent();
        }

        binding.titlebar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.doSearch(FolderViewActivity.this,"");
            }
        });


    }

    private void initRecycleviewByType(int displayType) {
        this.displayType = displayType%2;
        if(displayType == 0){
            binding.recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            adapter = new FileItemAdapter(R.layout.item_file);
        }else if(displayType ==1){
            binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
            adapter = new FileItemImgAdapter(R.layout.item_file_img);
        }else {
            binding.recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
            adapter = new FileItemAdapter(R.layout.item_file);
        }
        binding.recycler.setAdapter(adapter);
        adapter.setNewData(files);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                IFile file =  files.get(position);
                if(file.isDirectory()){
                    listFiles(file);
                }else {
                    ToastUtils.showLong("打开文件:"+file.getPath());
                    openFile(file,files);

                }
            }
        });
    }

    private void openFile(IFile file, List<IFile> files) {
        String path = file.getPath();
        int type = FileTypeUtil.getTypeByFileName(path);
        List<String> paths = new ArrayList<>();
        int position = 0;
        int realP = 0;
        if(type == BaseMediaInfo.TYPE_IMAGE || type == BaseMediaInfo.TYPE_VIDEO){
            for (IFile iFile : files) {
                if(iFile.isDirectory()){
                    continue;
                }
                if(FileTypeUtil.getTypeByFileName(iFile.getPath()) == type){
                    if(iFile.equals(file)){
                        realP = position;
                    }
                    paths.add(iFile.getPath());
                    position++;
                }
            }
            FileOpenUtil.open(FolderViewActivity.this,file.getPath(),paths,realP);
        }else {
            FileOpenUtil.open(FolderViewActivity.this,file.getPath());
        }

    }

    private void initMenu() {
        final FloatMenu floatMenu = new FloatMenu(this, binding.titlebar.getRightTextView());
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[3];
        desc[0] = "排序"  ;
        desc[1] ="刷新当前文件夹和子文件夹";
        desc[2] ="切换表格和列表显示";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position == 0){
                    sortMenu();

                }else if(position == 1){
                    scanAll();


                }else if(position == 2){
                    changeGridOrList();
                }
            }
        });

        floatMenu.showAsDropDown(binding.titlebar.getRightTextView());
    }

    private void changeGridOrList() {
        initRecycleviewByType(displayType+1);

    }

    private void scanAll() {
        new SafFileFinder22<IFile>().getAlbums(folder, Executors.newFixedThreadPool(3), new ScanFolderCallback() {
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

    private void sortMenu() {
        final FloatMenu floatMenu = new FloatMenu(this, binding.titlebar.getRightTextView());
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[6];
        desc[0] ="按文件名 顺序";
        desc[1] ="按文件名  倒序";
        desc[2] ="按更新时间 新在前";
        desc[3] ="按更新时间顺序 旧在前";
        desc[4] = "文件大小从大到小";
        desc[5] ="文件大小从小到大";




        desc[fileSortType] =  desc[fileSortType] +"(now)";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                fileSortType = position;
                Collections.sort(files,comparators.get(position));
                adapter.setNewData(files);
            }
        });
        floatMenu.showAsDropDown(binding.titlebar.getRightTextView());
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
    static List<Comparator<IFile>> comparators;
    static {
        comparators = new ArrayList<>();
        comparators.add(new FileNameSortAes());
        comparators.add(new FileNameSortDes());
        comparators.add(new ModifyTimeSortAes());
        comparators.add(new ModifyTimeSortDes());
        comparators.add(new FileSizeSortAES());
        comparators.add(new FileSizeSortDes());
    }
    int fileSortType = 0;
    private void listFiles(IFile file) {
        Log.d("pp","path:"+file.getPath());
        folder = file;
        binding.tvPath.setText(file.getPath());
        if(stateManager != null){
            stateManager.showLoading();
        }

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
                }).map(new Function<IFile[], List<IFile>>() {
            @Override
            public List<IFile> apply(@NonNull IFile[] iFiles) throws Exception {
                List<IFile> files = new ArrayList<>();
                if(iFiles != null && iFiles.length> 0){
                    for (IFile iFile : iFiles) {
                        files.add(iFile);
                    }
                }
                //Collections.sort(files,comparators.get(fileSortType));
                return files;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<IFile>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<IFile> iFiles) {
                        files.clear();
                        files.addAll(iFiles);
                        adapter.setNewData(files);
                        if(stateManager != null){
                            stateManager.showContent();
                        }
                        //更新数据库
                        updateDB(folder,iFiles);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        files.clear();
                        adapter.setNewData(files);
                        if(stateManager != null){
                            stateManager.showError(e.getMessage());
                        }

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
