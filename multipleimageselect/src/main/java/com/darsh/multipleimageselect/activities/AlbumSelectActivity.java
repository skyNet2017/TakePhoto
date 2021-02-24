package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Album;
import com.darsh.multipleimageselect.saf.TfAlbumFinder;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.DefaultScanFolderCallback;
import com.hss01248.media.mymediastore.SafFileFinder;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.noober.menu.FloatMenu;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Darshan on 4/14/2015.
 */
public class AlbumSelectActivity extends HelperActivity {
    private static final String COLUMN_COUNT = "count";
    private List<BaseMediaFolderInfo> albums = new ArrayList<>();

    private TextView errorDisplay;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;


    CommonTitleBar titleBar;

    private final String[] projection = new String[]{
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);
        //https://github.com/wuhenzhizao/android-titlebar
        titleBar = findViewById(R.id.titlebar);
        titleBar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        Constants.limit = intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);

        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_album_select);
        gridView = (GridView) findViewById(R.id.grid_view_album_select);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
                intent.putExtra(Constants.INTENT_EXTRA_TYPE, albums.get(position).type);

                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_PATH, albums.get(position).pathOrUri);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_IS_FILE_API, albums.get(position).pathOrUri.startsWith("/storage/"));
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_IS_SAF_API, albums.get(position).pathOrUri.startsWith("content"));


                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showLongPressMenu(position,view);
                return true;
            }
        });

        initData();

        initMenu();



    }

    private void showLongPressMenu(int position, View view) {
        BaseMediaFolderInfo folderInfo = albums.get(position);
        final FloatMenu floatMenu = new FloatMenu(this, view);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[2];
        desc[0] = folderInfo.hidden == 0 ? "隐藏此文件夹" : "取消此文件夹的隐藏"  ;
        desc[1] ="删除此文件夹";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
               if(position == 0){
                   hideOrUnHide(folderInfo,position);
               }else if(position == 1){
                   delete(folderInfo,position);
               }
            }
        });
        floatMenu.showAsDropDown(view);
    }

    private void delete(BaseMediaFolderInfo folderInfo, int position) {
        //删除的确认弹窗:
        new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("真的要删除这个文件夹里的"+CustomAlbumSelectAdapter.typeDes(folderInfo.type)+"吗?")
                .setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        albums.remove(folderInfo);
                        DbUtil.getDaoSession().getBaseMediaFolderInfoDao().delete(folderInfo);
                        adapter.notifyDataSetChanged();

                        //删除那一类的文件:
                        deleteDir(folderInfo);
                    }
                }).setNegativeButton("不删了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();

    }

    private void deleteDir(BaseMediaFolderInfo folderInfo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = folderInfo.pathOrUri;
                if(path.startsWith("content")){
                    DocumentFile dir = SafUtil.findFile(SafUtil.sdRoot,path);
                    if(dir == null){
                        return;
                    }
                    DocumentFile[] files = dir.listFiles();
                    if(files ==null || files.length==0){
                        return;
                    }
                    for (DocumentFile file : files) {
                        int type = SafFileFinder.guessTypeByName(file.getName());
                        if(type == folderInfo.type){
                            file.delete();
                        }
                    }
                    showFinishToast(folderInfo);
                }else {
                    File dir = new File(path);
                    File[] files = dir.listFiles();
                    if(files ==null || files.length==0){
                        return;
                    }
                    for (File file : files) {
                        int type = SafFileFinder.guessTypeByName(file.getName());
                        if(type == folderInfo.type){
                            file.delete();
                        }
                    }
                    showFinishToast(folderInfo);
                }
            }
        }).start();


    }

    private void showFinishToast(BaseMediaFolderInfo folderInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = "文件夹内("+CustomAlbumSelectAdapter.typeDes(folderInfo.type)+")删除完成\n"+folderInfo.pathOrUri;
                Toast.makeText(AlbumSelectActivity.this.getApplicationContext(),text,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void hideOrUnHide(BaseMediaFolderInfo folderInfo,int position) {
        if(folderInfo.hidden == 0){
            folderInfo.hidden = 1;
            if(!DbUtil.showHidden){
                albums.remove(position);
                adapter.notifyDataSetChanged();
            }
        }else {
            folderInfo.hidden = 0;
        }
        //会影响同路径下其他类型文件的显示和隐藏
        DbUtil.getDaoSession().getBaseMediaFolderInfoDao().update(folderInfo);
        dealNoMediaFile(folderInfo,folderInfo.hidden ==0);
    }

    private void dealNoMediaFile(BaseMediaFolderInfo folderInfo,boolean showFolder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = folderInfo.pathOrUri;
                if(path.startsWith("content")){
                    DocumentFile dir = SafUtil.findFile(SafUtil.sdRoot,path);
                    if(dir == null){
                        return;
                    }
                    DocumentFile[] files = dir.listFiles();
                    if(files ==null || files.length==0){
                        return;
                    }
                    DocumentFile file = dir.findFile(".nomedia");
                    if(showFolder){
                        if(file != null && file.exists()){
                            file.delete();
                        }
                    }else {
                        dir.createFile("text/plain",".nomedia");
                    }


                }else {
                    File dir = new File(path);
                    File[] files = dir.listFiles();
                    if(files ==null || files.length==0){
                        return;
                    }
                    File file = new File(dir,".nomedia");
                    if(showFolder){
                        file.delete();
                    }else {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();
    }

    private void initMenu() {
        final FloatMenu floatMenu = new FloatMenu(this, titleBar.getRightTextView());
        String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        floatMenu.items(hide, "过滤","排序");
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position ==0){
                    DbUtil.showHidden = !DbUtil.showHidden;
                    refresh();
                }else if(position ==2){
                    showSortMenu(v);
                }else if(position == 1){
                    showFilterMenu(v);
                }
            }
        });
        titleBar.getRightTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
                floatMenu.items(hide, "过滤","排序");
                floatMenu.show();
            }
        });
    }

    private void showFilterMenu(View view) {
        final FloatMenu floatMenu = new FloatMenu(this, view);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[5];
        desc[0] = "全部";
        desc[1] ="图片和视频";
        desc[2] ="只有图片";
        desc[3] ="只有视频";
        desc[4] ="只有音频";

        desc[DbUtil.folderFilterType] =  desc[DbUtil.folderFilterType] +"(now)";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                DbUtil.folderFilterType = position;
                refresh();
            }
        });
        floatMenu.showAsDropDown(titleBar.getRightTextView());
    }

    private void showSortMenu(View view) {
        final FloatMenu floatMenu = new FloatMenu(this, view);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[10];
        desc[0] = "按文件夹容量从大到小";
        desc[1] ="按文件个数从大到小";
        desc[2] ="按更新时间 新在前";
        desc[3] ="按更新时间顺序 旧在前";
        desc[4] ="按文件夹名 顺序";
        desc[5] ="按文件夹名  倒序";
        desc[6] ="按路径 顺序";
        desc[7] ="按路径  倒序";
        desc[8] ="按时长 顺序";
        desc[9] ="按时长  倒序";

        desc[DbUtil.folderSortType] =  desc[DbUtil.folderSortType] +"(now)";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                DbUtil.folderSortType = position;
                refresh();
            }
        });
        floatMenu.showAsDropDown(titleBar.getRightTextView());
        //floatMenu.show();
    }

    private void refresh() {
        SafFileFinder.listAllAlbum(callback,true);
    }

    private void initData() {
        progressBar.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);

         callback = new DefaultScanFolderCallback() {
            @Override
            protected void notifyDataSetChanged() {
                adapter.notifyDataSetChanged();
            }
        };

         albums = callback.getInfos();
        adapter = new CustomAlbumSelectAdapter(getApplicationContext(), callback.getInfos());
        gridView.setAdapter(adapter);
        orientationBasedUI(getResources().getConfiguration().orientation);

        SafFileFinder.listAllAlbum(callback,false);





    }

    DefaultScanFolderCallback callback;









    @Override
    protected void onStart() {
        super.onStart();


    }



    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        //albums = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        if (adapter != null) {
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 2 : metrics.widthPixels / 4;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }

            default: {
                return false;
            }
        }
    }





    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }
}
