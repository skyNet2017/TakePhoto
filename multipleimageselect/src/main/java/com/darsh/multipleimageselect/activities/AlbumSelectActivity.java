package com.darsh.multipleimageselect.activities;

import android.content.Context;
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
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.darsh.multipleimageselect.compress.PhotoCompressHelper;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Album;
import com.darsh.multipleimageselect.saf.TfAlbumFinder;

import java.io.File;
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
    private ArrayList<Album> albums = new ArrayList<>();

    private TextView errorDisplay;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;

    private ActionBar actionBar;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private final String[] projection = new String[]{
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);
        setView(findViewById(R.id.layout_album_select));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.album_view);
        }

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

                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_PATH, albums.get(position).dir);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_IS_FILE_API, albums.get(position).fromFileApi);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_IS_SAF_API, albums.get(position).fromSAFApi);


                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        });

        initHandler();
        initData();




    }

    private void initData() {
        progressBar.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);
        adapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
        gridView.setAdapter(adapter);
        orientationBasedUI(getResources().getConfiguration().orientation);
        //loadByMediaStore();
        loadBySaf();
    }

    private void loadByMediaStore() {

    }



    private void loadBySaf() {
        TfAlbumFinder.listSafAlbum(new Observer<Album>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Album album) {
                //Log.d("监听", "添加有图文件夹 完成 :" + album.dir);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                        if(albums.contains(album)){
                            Album albumOld =  albums.get(albums.indexOf(album));
                            albumOld.count = album.count;
                            albumOld.fileSize = album.fileSize;
                        }else {
                            albums.add(album);
                        }
                        Collections.sort(albums, new Comparator<Album>() {
                            @Override
                            public int compare(Album o1, Album o2) {
                                return (o2.fileSize > o1.fileSize) ? 1 : -1;
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();

            }

            @Override
            public void onComplete() {

            }
        }, new Observer<List<Album>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Album> albums0) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                        albums.addAll(albums0);
                        Collections.sort(albums, new Comparator<Album>() {
                            @Override
                            public int compare(Album o1, Album o2) {
                                return (o2.fileSize > o1.fileSize) ? 1 : -1;
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                loadByFileApi();
            }
        });

    }

    private void loadByFileApi() {
        AlbumFinder.listAllAlbum(new Observer<Album>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Album album) {
                Log.d("监听","添加有图文件夹 完成 :"+album.dir);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter == null) {
                            adapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
                            gridView.setAdapter(adapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        }
                                        /*if(albums.contains(album)){
                                            albums.remove(albums.indexOf(album));
                                        }*/
                        albums.add(album);
                        //再次排序:按图片个数排:
                        //按文件大小排序:
                                        /*Collections.sort(albums, new Comparator<Album>() {
                                            @Override
                                            public int compare(Album o1, Album o2) {
                                                return (int) (o2.count - o1.count);
                                            }
                                        });*/
                        Collections.sort(albums, new Comparator<Album>() {
                            @Override
                            public int compare(Album o1, Album o2) {
                                return (o2.fileSize > o1.fileSize) ? 1: -1;
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                });

                //calFileSize(albums,adapter);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.PERMISSION_GRANTED: {
                        loadAlbums();
                        break;
                    }

                    case Constants.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constants.FETCH_COMPLETED: {

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
        /*observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);

        checkPermission();*/
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void calFileSize(final ArrayList<Album> albums, final CustomAlbumSelectAdapter adapter) {
        Observable.fromIterable(albums)
                .doOnNext(new Consumer<Album>() {
                    @Override
                    public void accept(Album album) throws Exception {
                        if(album.fileSize > 0){
                            return;
                        }
                        File dir = new File(album.dir);
                        File[] files = dir.listFiles();
                        int count = 0;
                        long fileSize = 0;
                        for (File file1 : files){
                            if(PhotoCompressHelper.isImage(file1)){
                                count++;
                                fileSize = fileSize + file1.length();
                            }
                        }
                        album.count = count;
                        album.fileSize = fileSize;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Album>() {
                    @Override
                    public void onNext(Album album) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onComplete() {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //原onstop里做的
        try {
            stopThread();

            getContentResolver().unregisterContentObserver(observer);
            observer = null;

            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                //handler = null;
            }
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        }



        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
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

    private void loadAlbums() {
        //startThread(new AlbumLoaderRunnable());
        sendMessage(Constants.FETCH_COMPLETED);
    }

    private class AlbumLoaderRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (adapter == null) {
                sendMessage(Constants.FETCH_STARTED);
            }

            Cursor cursor = getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            null, null, MediaStore.Images.Media.DATE_ADDED);
            if (cursor == null) {
                sendMessage(Constants.ERROR);
                return;
            }

            ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
            HashSet<Long> albumSet = new HashSet<>();
            File file;
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    long albumId = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String album = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String image = cursor.getString(cursor.getColumnIndex(projection[2]));
                    //int size = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));

                    if (!albumSet.contains(albumId)) {
                        /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                        file = new File(image);
                        if (file.exists()) {
                            Album album1 = new Album(album,image);
                            temp.add(album1);
                            albumSet.add(albumId);

                            File dir = file.getParentFile();
                            File[] files = dir.listFiles();
                            /*int count = 0;
                            long fileSize = 0;
                            for (File file1 : files){
                                if(PhotoCompressHelper.isImage(file1)){
                                    count++;
                                    fileSize = fileSize + file1.length();
                                }
                            }*/
                            album1.dir = dir.getAbsolutePath();
                            album1.count = files == null ? 0 : files.length;
                           // album1.fileSize = fileSize;
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (albums == null) {
                albums = new ArrayList<>();
            }
            albums.clear();
            albums.addAll(temp);
            //按文件大小排序:
            Collections.sort(albums, new Comparator<Album>() {
                @Override
                public int compare(Album o1, Album o2) {
                    return (int) (o2.count - o1.count);
                }
            });

            sendMessage(Constants.FETCH_COMPLETED);
        }
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
        if (handler == null) {
            return;
        }

        Message message = handler.obtainMessage();
        message.what = what;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        Message message = handler.obtainMessage();
        message.what = Constants.PERMISSION_GRANTED;
        message.sendToTarget();
    }

    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }
}
