package com.example.rhmp3project;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;

import static com.example.rhmp3project.R.*;

public class FragmentPlaystatus extends Fragment implements View.OnClickListener {

    private MainActivity mainActivity;

    //UI객체 전역변수선언
    private ImageButton btnPlayPause;
    private ImageButton btnBack;
    private ImageButton btnNext;
    private ImageButton btnLike;
    private ImageView ivplaystatus1;
    private ImageView ivplaystatus2;
    private TextView tvTitle;
    private TextView tvSinger;
    private SeekBar seekBar;
    private TextView tvSeekCurrent;
    private TextView tvSeekMax;

    //미디어 플레이어 관련 변수
    private Mp3Player mp3Player = Mp3Player.getInstance();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //만들어 놓은 xml 파일을 inflate 시켜줍니다.
        final ViewGroup viewGroup = (ViewGroup) inflater.inflate(layout.fragment_playstatus,container,false);

        findViewByIdfunction(viewGroup);

        //재생&일시정지&이전곡&다음곡 버튼 클릭이벤트
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnLike.setOnClickListener(this);

        //시크바가 변경이 되면 미디어 파일의 구간도 같이 변경시켜줍니다.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) mp3Player.mediaPlayerSeek(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //미디어 플레이어가 재생이 완료된 이후에 다음곡을 플레이 해주도록함
        mp3Player.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                onClick(viewGroup.findViewById(id.btnNext));
            }
        });

        return viewGroup;
    }

    //UI객체 전역변수에 UI객체를 연결하여 주는 함수
    private void findViewByIdfunction(ViewGroup viewGroup) {

        ivplaystatus1 = viewGroup.findViewById(id.ivplaystatus1);
        ivplaystatus2 = viewGroup.findViewById(id.ivplaystatus2);

        tvTitle = viewGroup.findViewById(id.tvTitle);
        tvSinger = viewGroup.findViewById(id.tvSinger);

        seekBar = viewGroup.findViewById(id.seekBar);
        tvSeekCurrent = viewGroup.findViewById(id.tvSeekCurrent);
        tvSeekMax = viewGroup.findViewById(id.tvSeekMax);

        btnPlayPause = viewGroup.findViewById(R.id.btnPlayPause);
        btnBack = viewGroup.findViewById(id.btnBack);
        btnNext = viewGroup.findViewById(id.btnNext);
        btnLike = viewGroup.findViewById(id.btnLike);
    }

    //재생중인 곡의 메타데이터 정보를 가져와서 화면에 세팅시켜 줍니다.
    public void setUI(MusicData selectedItem) {

        tvTitle.setText(selectedItem.getSongName());
        tvSinger.setText(selectedItem.getArtist());
        ivplaystatus1.setScaleType(ImageView.ScaleType.FIT_XY);
        ivplaystatus1.setImageBitmap(selectedItem.getBitmap());
        ivplaystatus2.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivplaystatus2.setImageBitmap(selectedItem.getBitmap());

        btnPlayPause.setActivated(true);
        btnLike.setActivated(selectedItem.getLike());
    }

    //음악이 재생중일때 시크바와 재생시간 UI를 계속 업데이트 해주는 쓰레드 함수
    public void musicStart() {
        Thread thread = new Thread(){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while (mp3Player.mediaPlayerIsPlaying()) {
                    final int maxTime = mp3Player.mediaPlayerGetDuration();
                    final int currentTime = mp3Player.mediaPlayerGetCurrentPosition();

                    seekBar.setMax(maxTime);
                    seekBar.setProgress(currentTime);

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvSeekCurrent.setText(simpleDateFormat.format(currentTime));
                            tvSeekMax.setText(simpleDateFormat.format(maxTime));
                        }
                    });
                    SystemClock.sleep(100);
                }
            }
        };
        thread.start();
    }

    //onClick 함수 모든 버튼의 클릭이벤트를 한곳에서 처리시켜 줍니다.
    @Override
    public void onClick(View view) {
        MusicData selectedItem = null;

        switch (view.getId()) {

            //재생버튼 클릭시 발생 이벤트
            case R.id.btnPlayPause:
                if (btnPlayPause.isActivated()) {
                    //일시정지 하는 코드
                    btnPlayPause.setActivated(false);
                    mp3Player.mediaPlayerPause();
                } else {
                    //재생 코드
                    btnPlayPause.setActivated(true);
                    mp3Player.mediaPlayerStart();
                    musicStart();
                }
                break;

            //다음곡 버튼 클릭시 발생 이벤트
            case R.id.btnNext:
                //마지막곡을 플레이 하는 중인지 체크해주는 if문
                if(MainActivity.selectedItemNum+1 == mainActivity.mp3MusicDataArrayList.size()) {
                    Toast.makeText(mainActivity, "마지막곡 입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.selectedItemNum += 1;

                //현재 플레이 중인 곡의 메타 데이터를 가져와 주는 함수
                selectedItem = mainActivity.mp3MusicDataArrayList.get(MainActivity.selectedItemNum);
                mp3Player.mediaPlayerReset();
                mp3Player.mediaPlayerFileSetting(Environment.getExternalStorageDirectory().getPath()+"/"+selectedItem.getMp3FileName());
                mp3Player.mediaPlayerStart();

                //UI와 시크바를 조절해줍니다.
                setUI(selectedItem);
                musicStart();
                break;

                //이전곡 버튼 클릭시 발생 이벤트
            case R.id.btnBack:
                if(MainActivity.selectedItemNum == 0) {
                    Toast.makeText(mainActivity, "첫번째곡 입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.selectedItemNum -= 1;

                //현재 플레이 중인 곡의 메타 데이터를 가져와 주는 함수
                selectedItem = mainActivity.mp3MusicDataArrayList.get(MainActivity.selectedItemNum);
                mp3Player.mediaPlayerReset();
                mp3Player.mediaPlayerFileSetting(Environment.getExternalStorageDirectory().getPath()+"/"+selectedItem.getMp3FileName());
                mp3Player.mediaPlayerStart();

                //UI와 시크바를 조절해줍니다.
                setUI(selectedItem);
                musicStart();
                break;

                //좋아요 버튼 클릭시 발생 이벤트
            case id.btnLike:
                selectedItem = mainActivity.mp3MusicDataArrayList.get(MainActivity.selectedItemNum);
                if(btnLike.isActivated()){
                    //좋아요를 비활성화
                    selectedItem.setLike(false);
                    btnLike.setActivated(false);
                }
                else {
                    //좋아요를 활성화
                    selectedItem.setLike(true);
                    btnLike.setActivated(true);
                    mainActivity.likeArrayList.add(selectedItem);
                    mainActivity.likeInvalidate();
                }
                break;
        }
    }
}
