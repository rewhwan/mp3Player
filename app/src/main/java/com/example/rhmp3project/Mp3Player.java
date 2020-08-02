package com.example.rhmp3project;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class Mp3Player {

    //미디어 플레이어 선언
    private static Mp3Player instance;
    private MediaPlayer mediaPlayer;
    private FragmentPlaystatus fragmentPlaystatus;

    //생성자
    private Mp3Player() {
        //미디어 플레이어와 UI가 있는 Fragment를 객체로 선언
        this.mediaPlayer = new MediaPlayer();
        this.fragmentPlaystatus = MyMusicPlayerService.fragmentPlaystatus;
    }

    //싱글톤 디자인
    public static Mp3Player getInstance() {
        if(instance == null) {
            instance = new Mp3Player();
        }
        return instance;
    }

    //미디어 플레이어에 새로운 파일을 지정해주는 함수
    public boolean mediaPlayerFileSetting(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MyMusicPlayerService",e.getMessage());
            return false;
        }
        return true;
    }

    //미디어 플레이어를 시작 시키는 함수
    public void mediaPlayerStart() { mediaPlayer.start(); }

    //미디어 플레이어를 일시정지 시키는 함수
    public void mediaPlayerPause() {
        mediaPlayer.pause();
    }

    //미디어 플레이어를 정지 시키는 함수
    public void mediaPlayerStop() {
        mediaPlayer.stop();
    }

    //미디어 플레이어를 리셋시키는 함수
    public void mediaPlayerReset() { mediaPlayer.reset(); }

    //미디어 플레이어를 이동 시키는 함수
    public void mediaPlayerSeek(int i) {
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

    //getter
    public MediaPlayer getMediaPlayer() { return mediaPlayer; }
}
