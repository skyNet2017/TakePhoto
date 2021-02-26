package com.hss01248.media.localvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.solver.GoalRow;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import moe.codeest.enviews.ENPlayView;

public class LocalVideoPlayer extends StandardGSYVideoPlayer {
    public LocalVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public LocalVideoPlayer(Context context) {
        super(context);
    }

    public LocalVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    SeekBar seekBar;
    TextView tvPre;
    TextView tvNext;
    LinearLayout llSingle;
    LinearLayout bottomLl;


    public void setActivity(PictureVideoPlayByGSYActivity activity) {
        this.activity = activity;
    }

    PictureVideoPlayByGSYActivity activity;

    @Override
    protected void init(Context context) {
        super.init(context);
        seekBar = findViewById(R.id.progress);
        tvPre = findViewById(R.id.tv_play_pre);
        tvNext = findViewById(R.id.tv_play_next);
        llSingle = findViewById(R.id.bottom_single);
        bottomLl = findViewById(R.id.layout_bottom);
        preOrNext();
        if(context instanceof PictureVideoPlayByGSYActivity){
            activity = (PictureVideoPlayByGSYActivity) context;
        }
    }

    public void setVideoList(boolean videoList) {
        isVideoList = videoList;
        if(isVideoList){
            llSingle.setVisibility(VISIBLE);
            preOrNext();
        }else {
            llSingle.setVisibility(VISIBLE);
        }
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer player =  super.startWindowFullscreen(context, actionBar, statusBar);
        return player;
    }


    private void preOrNext() {
        tvPre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               activity.onPlayPre();
            }
        });

        tvNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onPlayNext();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(v.getId()==R.id.tv_play_next || v.getId()==R.id.tv_play_pre){
                return true;
            }
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(v.getId()==R.id.tv_play_next || v.getId()==R.id.tv_play_pre){
                v.performClick();
                return true;
            }
        }


        return super.onTouch(v, event);
    }

    boolean isVideoList;

    @Override
    protected void changeUiToPreparingShow() {
        //super.changeUiToPreparingShow();
    }

    @Override
    public void startPlayLogic() {
        super.startPlayLogic();
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb");
            mVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, LocalVideoPlayer.this);
        }
        prepareVideo();
        changeUiToPlayingShow();

        //startWindowFullscreen(activity,false,false);
    }


    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_local;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
