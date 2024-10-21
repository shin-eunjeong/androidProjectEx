package com.example.jungexweb.ui.home;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.jungexweb.R;

//implement 하면 자동으로 생성되나 onBind는 쓰지 않는다.
//해당클래스는 androidManifest.xml에 신고해야한다.
public class MusicService extends Service {
    MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /* ctrl + o  생성하며 서비스 진행시 초기화 / 시작/ 종료를 구성하게 된다. */
    @Override
    public void onCreate() {
        super.onCreate();
        /** 음악등을 res>raw폴더에 넣는다.
        new Resource Directory >> resource type을 raw로 변경 ok 함 */
        mediaPlayer = MediaPlayer.create(this, R.raw.steinway1);
        mediaPlayer.setLooping(true);  // 반복재생여부
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
