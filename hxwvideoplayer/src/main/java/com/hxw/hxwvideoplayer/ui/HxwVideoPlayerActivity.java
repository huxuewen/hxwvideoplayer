package com.hxw.hxwvideoplayer.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hxw.hxwvideoplayer.common.constans.HxwVideoPlayerConstant;
import com.hxw.hxwvideoplayer.R;
import com.hxw.hxwvideoplayer.common.cast.CastManager;
import com.hxw.hxwvideoplayer.common.cast.CastScreenArrayAdapter;
import com.hxw.hxwvideoplayer.common.cast.DeviceDisplay;
import com.hxw.hxwvideoplayer.common.mediakernel.JZMediaExo;
import com.hxw.hxwvideoplayer.databinding.ActivityHxwVideoPlayerBinding;

import cn.jzvd.Jzvd;

/**
 * @author xuewen hu
 * @date 2021/6/23 22:08
 */
public class HxwVideoPlayerActivity extends AppCompatActivity {

    private SimpleJzvd simpleJzvd;

    private String url;

    private String title;

    private String videoFormat;

    private ActivityHxwVideoPlayerBinding binding;

    private ArrayAdapter<DeviceDisplay> listAdapter;

    private CastManager castManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHxwVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        url = intent.getStringExtra(HxwVideoPlayerConstant.VIDEO_URL);
        title = intent.getStringExtra(HxwVideoPlayerConstant.VIDEO_TITLE);
        videoFormat = intent.getStringExtra(HxwVideoPlayerConstant.VIDEO_FORMAT);
        simpleJzvd = binding.jzVideo;
        setupJzvdStd();
        setupCastScreenList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        castManager.destroy();
        simpleJzvd.destroy();
    }

    public void setupJzvdStd() {
        // 设置播放内核：阿里云播放器内核(不支持x86、x86_64)
        // 有四种播放内核：JZMediaAliyun,JZMediaIjk.class,JZMediaSystem.class,JZMediaExo.class
        simpleJzvd.setUp(url, title, videoFormat, Jzvd.SCREEN_FULLSCREEN, JZMediaExo.class);
        simpleJzvd.backButton.setOnClickListener(v -> this.finish());
        simpleJzvd.play();
    }

    public void setupCastScreenList() {
        binding.castScreenList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle("投屏设备");
            DeviceDisplay deviceDisplay = (DeviceDisplay) parent.getItemAtPosition(position);
            dialog.setMessage(
                    "设备名称：" +
                            deviceDisplay.getDeviceName() + "\n" +
                            "设备类型：" +
                            deviceDisplay.getDeviceType() + "\n" +
                            "设备命名空间：" +
                            deviceDisplay.getNamespace() + "\n" +
                            "视频地址：" +
                            url);
            dialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    "cancel",
                    (dialog1, which) -> {
                    }
            );
            dialog.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    "OK",
                    (dialog2, which) -> {
                        DeviceDisplay item = listAdapter.getItem(position);
                        castManager.playAfterPush(item.getDevice(), title, url);
                    }
            );
            dialog.show();
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(12);
        });
        listAdapter = new CastScreenArrayAdapter<>(this, R.layout.cast_screen_list_item);
        castManager = new CastManager(this, listAdapter);
        binding.castScreenList.setAdapter(listAdapter);
    }
}