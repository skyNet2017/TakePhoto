package com.darsh.multipleimageselect.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

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

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomImageSelectAdapter;
import com.darsh.multipleimageselect.compress.CompressResultCompareActivity;
import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.darsh.multipleimageselect.saf.SafUtil;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Darshan on 4/18/2015.
 */
public class ImageSelectActivity extends HelperActivity {
    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
    Toolbar toolbar;
    boolean isInSelectingMode;
    Menu menu;
    List<File> selected;
    private ArrayList<Image> images;
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
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ImageSelectActivity.this.menu = menu;
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_contextual_action_bar, menu);

            if (PhotoCompressHelper.isACompressedDr(new File(images.get(0).path))) {
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
                for (Image image : images) {
                    image.isSelected = isSelectAll;
                }
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
    private String albumDir;
    private Uri albumDir2;
    private boolean isAlbumFromSafApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        setView(findViewById(R.id.layout_image_select));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.image_preview);
        }

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);
        albumDir = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM_PATH);
        isAlbumFromFileApi = intent.getBooleanExtra(Constants.INTENT_EXTRA_ALBUM_IS_FILE_API,false);

        albumDir2 = (Uri) intent.getExtras().get(Constants.INTENT_EXTRA_ALBUM_SAF_DIR);
        if(albumDir2 != null){
            Log.d(SafUtil.TAG,"albumDir2:"+ URLDecoder.decode(albumDir2.toString()));
        }

        isAlbumFromSafApi = intent.getBooleanExtra(Constants.INTENT_EXTRA_ALBUM_IS_SAF_API,false);

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
                    //点击去预览
                    ArrayList<String> files = new ArrayList<>();
                    for (Image image : images) {
                        files.add(image.path);
                    }
                    CompressResultCompareActivity.lauchForPreview(ImageSelectActivity.this, files, position);
                }

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
        });
        initHandler();
        loadImages();
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
                    Iterator<Image> iterator = images.iterator();
                    while (iterator.hasNext()){
                        Image image = iterator.next();
                        if(new File(image.path).length() <=0){
                            iterator.remove();
                            image.isSelected = false;
                        }else {
                            image.isSelected = false;
                        }
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
        if (!images.get(position).isSelected && countSelected >= Constants.limit) {
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
        adapter.notifyDataSetChanged();
    }

    private void deselectAll() {
        for (int i = 0, l = images.size(); i < l; i++) {
            images.get(i).isSelected = false;
        }
        countSelected = 0;
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Image> getSelected() {
        ArrayList<Image> selectedImages = new ArrayList<>();
        selected = new ArrayList<>();
        for (int i = 0, l = images.size(); i < l; i++) {
            if (images.get(i).isSelected) {
                selectedImages.add(images.get(i));
                selected.add(new File(images.get(i).path));
            }
        }
        return selectedImages;
    }

    private void sendIntent() {
        /*Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES, getSelected());
        setResult(RESULT_OK, intent);
        finish();*/
        final ArrayList<Image> images = getSelected();
        if (images == null || images.isEmpty()) {
            Toast.makeText(this, R.string.c_mot_selected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PhotoCompressHelper.isACompressedDr(new File(images.get(0).path))) {
            List<File> files = new ArrayList<>();
            for (Image image : images) {
                files.add(new File(image.path));
            }
            PhotoCompressHelper.compressAllFiles(files, this, new Subscriber<String>() {
                @Override
                public void onSubscribe(Subscription s) {

                }

                @Override
                public void onNext(String s) {
                    ArrayList<String> paths = new ArrayList<>();
                    for (Image image : images) {
                        paths.add(image.path);
                    }
                    CompressResultCompareActivity.lauch(ImageSelectActivity.this, paths, countSelected == images.size());
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    if ("1".equalsIgnoreCase(t.getMessage())) {//全部都被压缩过了
                        menu.findItem(R.id.menu_item_add_image).setTitle(R.string.c_preview);
                        ArrayList<String> paths = new ArrayList<>();
                        for (Image image : images) {
                            paths.add(image.path);
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
        for (Image image : images) {
            paths.add(image.path);
        }
        CompressResultCompareActivity.lauch(this, paths, countSelected == images.size());


    }

    private void loadImages() {
        startThread(new ImageLoaderRunnable());
    }

    private void startThread(Runnable runnable) {
        stopThread();
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopThread() {
        if (thread == null || !thread.isAlive()) {
            return;
        }

        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            /*
            If the adapter is null, this is first time this activity's view is
            being shown, hence send FETCH_STARTED message to show progress bar
            while images are loaded from phone
             */
            if (adapter == null) {
                sendMessage(Constants.FETCH_STARTED);
            }

            File file;
            HashSet<Long> selectedImages = new HashSet<>();
            if (images != null) {
                Image image;
                for (int i = 0, l = images.size(); i < l; i++) {
                    image = images.get(i);
                    file = new File(image.path);
                    if (file.exists() && image.isSelected) {
                        selectedImages.add(image.id);
                    }
                }
            }

            if (images == null) {
                images = new ArrayList<>();
            }
            images.clear();



            if(isAlbumFromFileApi){
                File[] files = new File(albumDir).listFiles();
                if(files == null){
                    sendMessage(Constants.ERROR);
                    return;
                }

                ArrayList<Image> temp = new ArrayList<>(files.length);
                int tempCountSelected = 0;

                for (File file1 : files) {
                    String name = file1.getName();
                    Log.d(SafUtil.TAG,"image name:"+name);
                    if(name.endsWith(".jpg")|| name.endsWith(".png") || name.endsWith(".gif")
                            || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")){
                        temp.add(new Image(0, name, file1.getAbsolutePath(), false));
                    }
                }
                images.addAll(temp);
                sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);

            }else if(isAlbumFromSafApi){
                //DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(),albumDir2);
                //还是拿到根目录

                try {
                    long start = System.currentTimeMillis();
                    DocumentFile dir = AlbumSelectActivity.docs.get(albumDir).dirSaf;
                    DocumentFile[] files = dir.listFiles();
                    if(files == null){
                        sendMessage(Constants.ERROR);
                        return;
                    }

                    //ArrayList<Image> temp = new ArrayList<>(files.length);
                    int tempCountSelected = 0;

                    int count = 0;
                    for (DocumentFile file1 : files) {
                        String name = file1.getName();
                        //Log.d(SafUtil.TAG,"image name saf :"+name);
                        if(name.endsWith(".jpg")|| name.endsWith(".png") || name.endsWith(".gif")
                                || name.endsWith(".webp") || name.endsWith(".JPG") || name.endsWith(".jpeg")){
                            Image image = new Image(0, name, file1.getUri().toString(), false);
                            //temp.add();
                            count++;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    images.add(image);
                                }
                            });
                            if(count % 20 == 0){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);
                                    }
                                });
                            }
                            //new FileInputStream(getContentResolver().openFileDescriptor(file1.getUri(),"w"));
                            //getContentResolver().openFileDescriptor()
                        }
                    }
                    //images.addAll(temp);
                    Log.w(SafUtil.TAG,"cost:(s) "+(System.currentTimeMillis()-start)/1000);
                    //7-8s,太慢了
                    sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                    sendMessage(Constants.ERROR);
                    Toast.makeText(getApplicationContext(),throwable.getMessage(),Toast.LENGTH_LONG).show();
                }


            }else {
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{album}, MediaStore.Images.Media.DATE_ADDED);
                if (cursor == null) {
                    sendMessage(Constants.ERROR);
                    return;
                }

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */
                int tempCountSelected = 0;
                ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
                if (cursor.moveToLast()) {
                    do {
                        if (Thread.interrupted()) {
                            return;
                        }

                        long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                        String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                        String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                        boolean isSelected = selectedImages.contains(id);
                        if (isSelected) {
                            tempCountSelected++;
                        }

                        file = new File(path);
                        Log.i("path", path);
                        if (file.exists()) {
                            temp.add(new Image(id, name, path, isSelected));
                        }

                    } while (cursor.moveToPrevious());
                }
                cursor.close();

                images.addAll(temp);
                sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);
            }

        }
    }
}
