package com.example.rhmp3project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //DrawerLayout 및 프레그먼트 변수 선언
    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    private FragmentPlaystatus fragmentPlaystatus = MyMusicPlayerService.fragmentPlaystatus;

    //파일 관련 변수
    private String sdcardPath;  //SD카드 절대경로를 저장

    //SD카드 리스트뷰 변수
    private ListView listSDCard;
    private TextView tvSDCard;

    //Like 리스트뷰 변수
    private ListView listPlaylist;
    private TextView tvPlaylist;

    //Like 리스트의 LinearLayout 변수
    private LinearLayout likeLayout;

    //미디어 플레이어 관련 변수
//    public MyMusicPlayerService myMusicPlayerService = new MyMusicPlayerService();
    private Mp3Player mp3Player = Mp3Player.getInstance();
    public ArrayList<MusicData> mp3MusicDataArrayList = new ArrayList<>();
    public ArrayList<MusicData> likeArrayList = new ArrayList<>();
    private ProgressBar prevProgressBar;
    public static int selectedItemNum = 0;

    private Intent intent;

    private MusicAdapter likeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //서비스가 실행중인지 체크이후 실행중이면 서비스를 종료합니다.
        if(isServiceRunningCheck()) {
            Log.d("activity","startservice 서비스 종료");
            intent = new Intent(getApplicationContext(),MyMusicPlayerService.class);
            stopService(intent);
        }else {
            Log.d("activity","startservice 서비스가 실행중이지 않음");
            Toast.makeText(this, "서비스 XXXXX", Toast.LENGTH_SHORT).show();
        }

        //UI객체 생성
        findViewByIdfunction();

        //외부저장장치 권한을 요청
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE},MODE_PRIVATE);

        //Fragment를 frameLayout에 붙여줌
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragmentPlaystatus).commit();

        //SDcard 절대경로를 저장한다.
        sdcardPath = Environment.getExternalStorageDirectory().getPath()+"/";
        //sd카드에 있는 모든 파일을 가져와서 mp3파일만 골라내는 작업 진행
        loadMP3FileSdcard();

        //어댑터 생성
        MusicAdapter sdCardAdapter = new MusicAdapter(getApplicationContext());
        sdCardAdapter.setArrayList(mp3MusicDataArrayList);
        likeAdapter = new MusicAdapter(getApplicationContext());
        likeAdapter.setArrayList(likeArrayList);

        //SD카드 UI내용을 세팅해주는 코드
        listSDCard.setAdapter(sdCardAdapter);
        tvSDCard.setText("SDcard ("+mp3MusicDataArrayList.size()+")");

        //플레이 리스트 UI내용을 세팅해주는 코드
        listPlaylist.setAdapter(likeAdapter);
        tvPlaylist.setText("Like ("+likeArrayList.size()+")");

        //하드웨어 가속을 하도록 설정
        listSDCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        listPlaylist.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        //listView가 클릭되면 발생하는 이벤트문
        listSDCard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            MusicData selectedItem = mp3MusicDataArrayList.get(i);
            selectedItemNum = i;

            //재생화면의 UI에 곡정보를 세팅해줍니다.
            fragmentPlaystatus.setUI(selectedItem);
            mp3Player.mediaPlayerReset();

            //새로운 곡을 플레이 하기전에 파일에 문제가 있는지 체크해줍니다.
            boolean fileError = mp3Player.mediaPlayerFileSetting(sdcardPath+selectedItem.getMp3FileName());
            if(!fileError) {
                Toast.makeText(getApplicationContext(), "미디어 플레이어가 파일을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            mp3Player.mediaPlayerStart();
            fragmentPlaystatus.musicStart();
            drawerLayout.closeDrawer(Gravity.LEFT);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //sharedPreference에서 정보를 가져와줍니다.
        SharedPreferences sf = getSharedPreferences("service",MODE_PRIVATE);
        int tmpSelectedItemNum = sf.getInt("selectedItemNum",0);
        int tmpSeekNow = sf.getInt("seekNow",0);

        if(tmpSeekNow != 0 || tmpSelectedItemNum != 0) {
            MusicData selectedItem = mp3MusicDataArrayList.get(tmpSelectedItemNum);
            selectedItemNum = tmpSelectedItemNum;

            //재생화면의 UI에 곡정보를 세팅해줍니다.
            fragmentPlaystatus.setUI(selectedItem);
            mp3Player.mediaPlayerReset();

            //새로운 곡을 플레이 하기전에 파일에 문제가 있는지 체크해줍니다.
            boolean fileError = mp3Player.mediaPlayerFileSetting(sdcardPath+selectedItem.getMp3FileName());
            if(!fileError) {
                Toast.makeText(getApplicationContext(), "미디어 플레이어가 파일을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            mp3Player.mediaPlayerSeek(tmpSeekNow);
            mp3Player.mediaPlayerStart();
            fragmentPlaystatus.musicStart();
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    //SD카드에서 파일을 불러와줍니다.
    private void loadMP3FileSdcard() {
        File[] listFiles = new File(sdcardPath).listFiles();
        for(File file : listFiles) {
            String fileName = file.getName();
            if(fileName.length() >= 4) {
                String extendName = fileName.substring(fileName.length()-3);
                if(extendName.equals("mp3")) {
                    //메타데이터 객체생성
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(sdcardPath+fileName);
                    byte [] data = mmr.getEmbeddedPicture();
                    String  songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String  artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String  lyric = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER);

                    if(data != null)
                    {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        MusicData musicData = new MusicData(bitmap,fileName,artist,songName,lyric,false);
                        mp3MusicDataArrayList.add(musicData);
                    }
                }
            }
        }
    }

    //UI객체를 불러와줍니다
    private void findViewByIdfunction() {
        drawerLayout = findViewById(R.id.drawerLayout);
        frameLayout = findViewById(R.id.frameLayout);

        listSDCard = findViewById(R.id.listSDCard);
        tvSDCard = findViewById(R.id.tvSDCard);

        listPlaylist = findViewById(R.id.listPlaylist);
        tvPlaylist = findViewById(R.id.tvplaylist);

        likeLayout = findViewById(R.id.likeLayout);

    }

    //좋아요 표시를 바꾸어 주고 UI를 다시 불러와줍니다.
    public void likeInvalidate() {
        tvPlaylist.setText("Like ("+likeArrayList.size()+")");
        likeAdapter.notifyDataSetChanged();
    }

    //서비스가 실행하는 중인지 알려주는 함수
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.rhmp3project.MyMusicPlayerService".equals(service.service.getClassName())){
                return true;
            }
        }
        return  false;
    }

    //어플리케이션이 백그라운드로 실행되려할때 서비스를 실행시켜줍니다.
    @Override
    protected void onStop() {
        super.onStop();
        intent = new Intent(getApplicationContext(),MyMusicPlayerService.class);

        //서비스에서 필요한
        intent.putParcelableArrayListExtra("arraylist",mp3MusicDataArrayList);
        intent.putExtra("selectedItemNum",selectedItemNum);
        intent.putExtra("seekNow",mp3Player.mediaPlayerGetCurrentPosition());
        intent.putExtra("sdPath",sdcardPath);
        mp3Player.mediaPlayerStop();
        startService(intent);
        Log.d("activity","startservice 작동");
    }

    //어플이 재시작되면 서비스를 다시 종료시켜줍니다.
    @Override
    protected void onRestart() {
        super.onRestart();
        //서비스가 실행중인지 체크이후 실행중이면 서비스를 종료합니다.
        if(isServiceRunningCheck()) {
            Log.d("activity","startservice 서비스 종료");
            intent = new Intent(getApplicationContext(),MyMusicPlayerService.class);
            stopService(intent);

        //서비스가 실행중이지 않으면 로그만 남긴다.
        }else {
            Log.d("activity","startservice 서비스가 실행중이지 않음");
            Toast.makeText(this, "서비스 XXXXX", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(getApplicationContext(), "activity를 새로 생성하지 않았어", Toast.LENGTH_SHORT).show();
    }
}