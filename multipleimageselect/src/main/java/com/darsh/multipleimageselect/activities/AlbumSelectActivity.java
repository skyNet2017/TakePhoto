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
import com.hss01248.media.mymediastore.DefaultScanFolderCallback;
import com.hss01248.media.mymediastore.SafFileFinder;
import com.hss01248.media.mymediastore.bean.BaseMediaFolderInfo;

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
    private List<BaseMediaFolderInfo> albums = new ArrayList<>();

    private TextView errorDisplay;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;

    private ActionBar actionBar;

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
                intent.putExtra(Constants.INTENT_EXTRA_TYPE, albums.get(position).type);

                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_PATH, albums.get(position).pathOrUri);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_IS_FILE_API, albums.get(position).pathOrUri.startsWith("/storage/"));
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_IS_SAF_API, albums.get(position).pathOrUri.startsWith("content"));


                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        });

        initData();




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

        SafFileFinder.listAllAlbum(callback);





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

        //原onstop里做的

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





    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }
}
