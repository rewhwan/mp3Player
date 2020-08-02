package com.example.rhmp3project;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyMusicPlayerService extends Service {

    //미디어 플레이어 선언
    private MediaPlayer mediaPlayer = new MediaPlayer();
    public static FragmentPlaystatus fragmentPlaystatus = new FragmentPlaystatus();

    //미디어 플레이어에서 필요한 arraylist와 변수를 전역으로 선언
    private ArrayList<MusicData> arrayList;
    private int selectedItemNum;    //현재 재생중인 곡의 객체를 알려주는 변수
    private int seekNow;            //플레이 중이던 곡이 몇초를 재생중인지 알려주는 변수
    private String sdPath;          //SD카드의 절대경로를 문자열로 저장

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service","서비스 onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","서비스 onStartCommand()");

        //미디어 플레이어가 끝까지 재생되면 다음곡을 자동으로 재생시켜주도록 이벤트 선언
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayerNextSong();
            }
        });

        //서비스가 실행됬을때 실행중이던 미디어 플레이어에서 정보를 받습니다.
        selectedItemNum = intent.getIntExtra("selectedItemNum",0);  //재생중이던 곡의 배열 순서를 저장
        seekNow = intent.getIntExtra("seekNow",0);                  //재생중이던 시간을 저장
        sdPath = intent.getStringExtra("sdPath");                                //SD카드 절대경로를 저장
        arrayList = intent.getParcelableArrayListExtra("arraylist");             //곡정보가 담긴 arraylist 저장

        //현재 재생중이던 파일정보를 알려줌
        MusicData selectedItem = arrayList.get(selectedItemNum);

        //원래 재생중이던 파일을 서비스에 세팅을 해줍니다.
        mediaPlayerFileSetting(sdPath+selectedItem.getMp3FileName());
        //원래 재생하던 곡을 다시 재생시켜줍니다.
        mediaPlayerSeek(seekNow);
        mediaPlayerStart();
        mediaPlayerThread();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        //종료하기 이전에 파일에 현재 재생중인곡의 정보와 시간을 전달
        SharedPreferences sharedPref = getSharedPreferences("service",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("selectedItemNum",selectedItemNum);
        editor.putInt("seekNow",mediaPlayerGetCurrentPosition());
        editor.commit();

        super.onDestroy();
        mediaPlayer.stop();
        Log.d("Service","서비스 onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service","서비스 onBind()");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //미디어 플레이어에 파일을 지정해주는 함수
    private boolean mediaPlayerFileSetting(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MyMusicPlayerService",e.getMessage());
            return false;
        }
        return true;
    }

    //미디어 플레이어를 시작시키는 함수
    private void mediaPlayerStart() { mediaPlayer.start(); }

    //미디어 플레이어를 정지시키는 함수
    private void mediaPlayerStop() { mediaPlayer.stop(); }

    //미디어 플레이어를 일시정지 시키는 함수
    private void mediaPlayerPause() {
        mediaPlayer.pause();
    }

    //미디어 플레이어를 리셋시키는 함수
    private void mediaPlayerReset() { mediaPlayer.reset(); }

    //미디어 플레이어를 이동 시키는 함수
    private void mediaPlayerSeek(int i) {
        mediaPlayer.seekTo(i);
    }

    //미디어 플레이어 현재 재생하는 부분을 리턴해주는 함수
    public int mediaPlayerGetCurrentPosition() {
        int currentTime = mediaPlayer.getCurrentPosition();
        return currentTime;
    }

    //미디어 플레이어에 등록된 파일의 재생시간을 리턴해주는 함수
    public int mediaPlayerGetDuration() {
        int maxTime = mediaPlayer.getDuration();
        return maxTime;
    }

    //미디어 플레이어가 재생중인지 알려주는 함수
    public boolean mediaPlayerIsPlaying() {
        if(mediaPlayer.isPlaying()) return true;
        return false;
    }

    //미디어 플레이어 다음곡을 재생시켜주는 함수 마지막곡이었다면 서비스를 종료합니다
    private void mediaPlayerNextSong() {
        //마지막곡인지 확인해주는 함수
        if(selectedItemNum +1 >= arrayList.size()){
            Toast.makeText(this, "마지막곡입니다.", Toast.LENGTH_SHORT).show();
            SystemClock.sleep(1000);
            //서비스를 종료시켜줍니다.
            onDestroy();
            return;
        }
        else selectedItemNum++;

        //다음곡의 데이터를 객체화하여 재생시켜줍니다.
        MusicData selectedItem = arrayList.get(selectedItemNum);

        mediaPlayerStop();
        mediaPlayerReset();
        mediaPlayerFileSetting(sdPath+selectedItem.getMp3FileName());
        mediaPlayerStart();
    }

    //미디어 플레이어의 지금 현재 상태를 sharedPreference로 저장해주는 Thread
    private void mediaPlayerThread() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (mediaPlayerIsPlaying()) {
                    //종료하기 이전에 파일에 현재 재생중인곡의 정보와 시간을 전달
                    SharedPreferences sharedPref = getSharedPreferences("service",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("selectedItemNum",selectedItemNum);
                    editor.putInt("seekNow",mediaPlayerGetCurrentPosition());
                    editor.commit();
                    SystemClock.sleep(100);
                }
            }
        };
        thread.start();
    }
}
