package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darsh.multipleimageselect.MySelectFileProvider;
import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.darsh.multipleimageselect.adapters.CustomImageSelectAdapter;
import com.darsh.multipleimageselect.compress.CompressResultCompareActivity;
import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Album;
import com.darsh.multipleimageselect.models.Image;
import com.darsh.multipleimageselect.saf.TfAlbumFinder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hss01248.imginfo.ImageInfoFormater;
import com.hss01248.media.localvideoplayer.VideoPlayUtil;
import com.hss01248.media.mymediastore.DbUtil;
import com.hss01248.media.mymediastore.SafFileFinder;
import com.hss01248.media.mymediastore.SafUtil;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;
import com.hss01248.media.mymediastore.bean.BaseMediaInfo;
import com.noober.menu.FloatMenu;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.apache.commons.io.FileUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Darshan on 4/18/2015.
 */
public class ImageSelectActivity extends HelperActivity {
    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
    Toolbar toolbar;
    boolean isInSelectingMode;
    Menu menu;
    List<File> selected;
    public static List<BaseMediaInfo> images = new ArrayList<>();
    private String album;
    private TextView errorDisplay;
    private ProgressBar progressBar;
    private GridView gridView;
    private CustomImageSelectAdapter adapter;
    private ActionBar actionBar;
    private ActionMode actionMode;
    private int countSelected;
    private ContentObserver observer;
    private Handler handler;
    private Thread thread;
    private boolean isSelectAll;
    boolean isAlbumFromFileApi;
    int type;
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ImageSelectActivity.this.menu = menu;
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_contextual_action_bar, menu);

            if (PhotoCompressHelper.isACompressedDr(new File(images.get(0).pathOrUri))) {
                menu.findItem(R.id.menu_item_add_image).setTitle(R.string.c_preview);
            }
            actionMode = mode;
            countSelected = 0;

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.menu_item_add_image) {//压缩图片
                sendIntent();
                return true;
            }
            if (i == R.id.menu_item_select_all) {//全选与取消全选
                isSelectAll = !isSelectAll;
               /* for (Image image : images) {
                    image.isSelected = isSelectAll;
                }*/
                if (actionMode != null) {
                    countSelected = isSelectAll ? images.size() : 0;
                    actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                }
                adapter.notifyDataSetChanged();
                return true;
            }

            if (i == R.id.menu_item_delete) {//删除选中项
                confirmDelete();
                return true;
            }
            if (i == R.id.menu_item_preview) {//预览选中项
                getSelected();
                if(countSelected <= 0){
                    Toast.makeText(ImageSelectActivity.this, R.string.c_mot_selected, Toast.LENGTH_SHORT).show();
                    return true;
                }
                ArrayList<String> paths = new ArrayList<>();
                for (File image : selected) {
                    paths.add(image.getAbsolutePath());
                }
                CompressResultCompareActivity.lauchForPreview(ImageSelectActivity.this, paths, 0);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (countSelected > 0) {
                deselectAll();
            }
            countSelected = 0;
            if(selected != null){
                selected.clear();
            }
            isInSelectingMode = false;
            toolbar.setTitle(R.string.image_preview);
            actionMode = null;
        }
    };
    CommonTitleBar titleBar;
    private String albumDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

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
        album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);
        albumDir = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM_PATH);
        isAlbumFromFileApi = intent.getBooleanExtra(Constants.INTENT_EXTRA_ALBUM_IS_FILE_API,false);
        type = intent.getIntExtra(Constants.INTENT_EXTRA_TYPE,0);

        String name = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);
        if(!TextUtils.isEmpty(name)){
            titleBar.getLeftTextView().setText(name);
        }
        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_image_select);
        gridView = (GridView) findViewById(R.id.grid_view_image_select);
        gridView.setFastScrollEnabled(true);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isInSelectingMode) {

                    toggleSelection(position);
                    if (actionMode != null) {
                        actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                    }


                } else {
                    ArrayList<String> files = new ArrayList<>();
                    for (BaseMediaInfo image : images) {
                        files.add(image.pathOrUri);
                    }
                    if(type == 1){
                        //点击去预览
                        CompressResultCompareActivity.lauchForPreview(ImageSelectActivity.this, files, position);
                    }else {
                        //viewVideo(images.get(position));
                        VideoPlayUtil.startPreviewInList(ImageSelectActivity.this,files,position);
                    }

                }

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showLongPressMenu(position,view);
                return true;
            }
        });

        /*gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isInSelectingMode = true;
                if (actionMode == null) {
                    actionMode = ImageSelectActivity.this.startActionMode(callback);
                }
                //toolbar.setTitle(R.string.image_view);
                toggleSelection(position);
                actionMode.setTitle(countSelected + " " + getString(R.string.selected));

                if (countSelected == 0) {
                    //actionMode.finish();

                }


                return true;
            }
        });*/
        initHandler();

        adapter = new CustomImageSelectAdapter(getApplicationContext(), images);
        gridView.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.VISIBLE);
        orientationBasedUI(getResources().getConfiguration().orientation);

        initMenu();
        loadImages();
    }

    private void showLongPressMenu(int position, View view) {
        BaseMediaInfo folderInfo = images.get(position);
        final FloatMenu floatMenu = new FloatMenu(this, view);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[2];
        desc[0] = "开启图片选择模式"  ;
        desc[1] ="删除此文件";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position == 0){
                    switchToSelectMode();
                }else if(position == 1){
                    delete(folderInfo,position);
                }
            }
        });
        floatMenu.showAsDropDown(view);
    }

    private void switchToSelectMode() {

    }

    private void delete(BaseMediaInfo folderInfo, int position) {
        //删除的确认弹窗:
        new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("真的要删除这个"+ CustomAlbumSelectAdapter.typeDes(folderInfo.type)+"文件吗?")
                .setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除文件:
                        deleteFile2(folderInfo);
                        DbUtil.getDaoSession().getBaseMediaInfoDao().delete(folderInfo);
                        images.remove(folderInfo);
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("不删了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();

    }

    private void deleteFile2(BaseMediaInfo folderInfo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(folderInfo.pathOrUri.startsWith("content")){
                    DocumentFile file = com.hss01248.media.mymediastore.SafUtil.findFile(SafUtil.sdRoot,folderInfo.pathOrUri);
                    if(file.exists()){
                        file.delete();
                    }
                }else {
                    File file = new File(folderInfo.pathOrUri);
                    file.delete();

                }
            }
        }).start();


    }

    private void initMenu() {
        final FloatMenu floatMenu = new FloatMenu(this, titleBar.getRightTextView());
        String hide = "待定....";
        floatMenu.items(hide, "排序");
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(position ==0){

                }else if(position ==1){
                    showSortMenu(v);
                }
            }
        });
        titleBar.getRightTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hide = "待定....";
                floatMenu.items(hide, "排序");
                floatMenu.show();
            }
        });
    }

    private void showSortMenu(View view) {
        final FloatMenu floatMenu = new FloatMenu(this, view);
        //String hide = DbUtil.showHidden ? "隐藏文件夹":"显示隐藏的文件夹";
        String[] desc = new String[10];
        desc[0] ="按更新时间 新在前";
        desc[1] ="按更新时间顺序 旧在前";
        desc[2] = "文件大小从大到小";
        desc[3] ="文件大小从小到大";
        desc[4] ="按文件名 顺序";
        desc[5] ="按文件名  倒序";

        desc[6] ="按画面尺寸 高分辨率在前";
        desc[7] ="按画面尺寸 低分辨率在前";
        if(type == BaseMediaInfo.TYPE_VIDEO || type == BaseMediaInfo.TYPE_AUDIO){
            desc[8] = "按时长 长在前";
            desc[9] ="按时长 短在前";
        }


        desc[DbUtil.fileSortType] =  desc[DbUtil.fileSortType] +"(now)";

        floatMenu.items(desc);
        floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                DbUtil.fileSortType = position;
                loadImages();
            }
        });
        floatMenu.showAsDropDown(titleBar.getRightTextView());
    }



    private void viewVideo(BaseMediaInfo baseMediaInfo) {
        try {
            if (baseMediaInfo.pathOrUri.startsWith("/storage/")) {
                Uri uri = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //判断版本是否在7.0以上
                    uri =
                            MySelectFileProvider.getUriForFile(getApplicationContext(),
                                    getPackageName() + ".selectfileprovider",
                                    new File(baseMediaInfo.pathOrUri));
                    //添加这一句表示对目标应用临时授权该Uri所代表的文件
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    uri = Uri.fromFile(new File(baseMediaInfo.pathOrUri));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } else {
                Uri uri = Uri.parse(baseMediaInfo.pathOrUri);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }


        }catch (Throwable throwable){
            throwable.printStackTrace();
            Toast.makeText(this,throwable.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void refresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("select","do refresh:"+images.size());
                //还要更新数据:
                /*ArrayList<Image> images1 = new ArrayList<>();
                images1.addAll(images);
                images.clear();
                images.addAll(images1);
                if(images.size() > 0){
                    images.get(0).name = "-"+images.get(0).name;
                }*/
                //deselectAll();
                if(images.size()>0){
                    Iterator<BaseMediaInfo> iterator = images.iterator();
                    while (iterator.hasNext()){
                        BaseMediaInfo image = iterator.next();
                        /*if(new File(image.path).length() <=0){
                            iterator.remove();
                            image.isSelected = false;
                        }else {
                            image.isSelected = false;
                        }*/
                    }
                }
                Log.d("select","do refresh2:"+images.size());
                adapter.notifyDataSetChanged();
                actionMode.finish();

                    /*isInSelectingMode = false;
                    if (selected != null) {
                        selected.clear();
                    }
                    countSelected = 0;
                    if(actionMode != null){
                        actionMode.setTitle(R.string.image_preview);

                    }
                actionMode.finish();
                    toolbar.setTitle(R.string.image_preview);*/

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (isInSelectingMode) {
            isInSelectingMode = false;
            //取消所有选择
            deselectAll();
            toolbar.setTitle(R.string.image_preview);
            return;
        }
        super.onBackPressed();
    }

   void initHandler(){
       handler = new Handler() {
           @Override
           public void handleMessage(Message msg) {
               switch (msg.what) {
                   case Constants.PERMISSION_GRANTED: {
                       //loadImages();
                       break;
                   }

                   case Constants.FETCH_STARTED: {
                       progressBar.setVisibility(View.VISIBLE);
                       gridView.setVisibility(View.INVISIBLE);
                       break;
                   }

                   case Constants.FETCH_COMPLETED: {
                        /*
                        If adapter is null, this implies that the loaded images will be shown
                        for the first time, hence send FETCH_COMPLETED message.
                        However, if adapter has been initialised, this thread was run either
                        due to the activity being restarted or content being changed.
                         */
                       if (adapter == null) {
                           adapter = new CustomImageSelectAdapter(getApplicationContext(), images);
                           gridView.setAdapter(adapter);

                           progressBar.setVisibility(View.INVISIBLE);
                           gridView.setVisibility(View.VISIBLE);
                           orientationBasedUI(getResources().getConfiguration().orientation);



                       } else {
                           adapter.notifyDataSetChanged();
                            /*
                            Some selected images may have been deleted
                            hence update action mode title
                             */
                           if (actionMode != null) {
                               countSelected = msg.arg1;
                               actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                           }
                       }
                       break;
                   }

                   case Constants.ERROR: {
                       progressBar.setVisibility(View.INVISIBLE);
                       errorDisplay.setVisibility(View.VISIBLE);
                       break;
                   }

                   default: {
                       super.handleMessage(msg);
                   }
               }
           }
       };
       observer = new ContentObserver(handler) {
           @Override
           public void onChange(boolean selfChange) {
               // loadImages();
           }
       };
       getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
      // checkPermission();
   }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
        //images = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 980 && resultCode == RESULT_CANCELED && data != null) {
            int positon = data.getIntExtra("position", -1);
            if (positon >= 0) {
                //gridView.smoothScrollToPosition(positon);
            }
        }
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
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 3 : metrics.widthPixels / 5;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5);
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

    private void confirmDelete() {
        getSelected();
        if (countSelected <= 0) {
            Toast.makeText(this, R.string.c_mot_selected, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.c_alert_title)
                .setMessage(R.string.c_is_to_delete)
                .setPositiveButton(R.string.c_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PhotoCompressHelper.deleteAllFiles(selected, ImageSelectActivity.this);
                    }
                }).setNegativeButton(R.string.c_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    /*** 通过反射，设置menu显示icon** @param view* @param menu* @return*/
    /*@Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }*/

    private void toggleSelection(int position) {
       /* if (!images.get(position).isSelected && countSelected >= Constants.limit) {
            Toast.makeText(
                    getApplicationContext(),
                    String.format(getString(R.string.limit_exceeded), Constants.limit),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        images.get(position).isSelected = !images.get(position).isSelected;
        if (images.get(position).isSelected) {
            countSelected++;
        } else {
            countSelected--;
        }
        adapter.notifyDataSetChanged();*/
    }

    private void deselectAll() {
       /* for (int i = 0, l = images.size(); i < l; i++) {
            images.get(i).isSelected = false;
        }
        countSelected = 0;
        adapter.notifyDataSetChanged();*/
    }

    private ArrayList<BaseMediaInfo> getSelected() {
        ArrayList<BaseMediaInfo> selectedImages = new ArrayList<>();
       /* selected = new ArrayList<>();
        for (int i = 0, l = images.size(); i < l; i++) {
            if (images.get(i).isSelected) {
                selectedImages.add(images.get(i));
                selected.add(new File(images.get(i).path));
            }
        }*/
        return selectedImages;
    }

    private void sendIntent() {
        /*Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES, getSelected());
        setResult(RESULT_OK, intent);
        finish();*/
        final ArrayList<BaseMediaInfo> images = getSelected();
        if (images == null || images.isEmpty()) {
            Toast.makeText(this, R.string.c_mot_selected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PhotoCompressHelper.isACompressedDr(new File(images.get(0).pathOrUri))) {
            List<File> files = new ArrayList<>();
            for (BaseMediaInfo image : images) {
                files.add(new File(image.pathOrUri));
            }
            PhotoCompressHelper.compressAllFiles(files, this, new Subscriber<String>() {
                @Override
                public void onSubscribe(Subscription s) {

                }

                @Override
                public void onNext(String s) {
                    ArrayList<String> paths = new ArrayList<>();
                    for (BaseMediaInfo image : images) {
                        paths.add(image.pathOrUri);
                    }
                    CompressResultCompareActivity.lauch(ImageSelectActivity.this, paths, countSelected == images.size());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    if ("1".equalsIgnoreCase(t.getMessage())) {//全部都被压缩过了
                        menu.findItem(R.id.menu_item_add_image).setTitle(R.string.c_preview);
                        ArrayList<String> paths = new ArrayList<>();
                        for (BaseMediaInfo image : images) {
                            paths.add(image.pathOrUri);
                        }
                        CompressResultCompareActivity.lauch(ImageSelectActivity.this, paths, countSelected == images.size());
                    }
                }

                @Override
                public void onComplete() {

                }
            });
            return;
        }


        ArrayList<String> paths = new ArrayList<>();
        for (BaseMediaInfo image : images) {
            paths.add(image.pathOrUri);
        }
        CompressResultCompareActivity.lauch(this, paths, countSelected == images.size());


    }

    private void loadImages() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                images.clear();
                images.addAll(DbUtil.getAllContentInFolders(albumDir,type))    ;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        }).start();

    }



    private void sendMessage(int what) {
        sendMessage(what, 0);
    }

    private void sendMessage(int what, int arg1) {
        if (handler == null) {
            return;
        }

        Message message = handler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        sendMessage(Constants.PERMISSION_GRANTED);
    }

    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }









}
