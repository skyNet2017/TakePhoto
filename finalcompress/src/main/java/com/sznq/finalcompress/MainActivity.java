package com.sznq.finalcompress;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.gc.materialdesign.views.ButtonRectangle;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_selected)
    ButtonRectangle btnSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_selected)
    public void onViewClicked() {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }
}
