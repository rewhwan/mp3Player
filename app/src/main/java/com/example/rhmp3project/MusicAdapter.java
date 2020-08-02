package com.example.rhmp3project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<MusicData> arrayList = new ArrayList<>();

    //생성자
    public MusicAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        //리스트뷰에 들어갈 music 레이아웃 화면 객체 생성
        if(view == null) {
            view = inflater.inflate(R.layout.list_layout,null);
        }

        //레이아웃에 속해있는 위젯을 찾는다.
        ImageView ivPicture = view.findViewById(R.id.ivAlbum);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvSinger = view.findViewById(R.id.tvSinger);

        //해당된 데이터를 가져온다
        MusicData musicData = arrayList.get(position);

        //해당되는 값을 저장한다.
        ivPicture.setImageBitmap(musicData.getBitmap());
//        ivPicture.setImageResource(R.drawable.ic_launcher_foreground);
        tvTitle.setText(musicData.getSongName());
        tvSinger.setText(musicData.getArtist());

        //하드웨어 가속을 사용하도록 설정
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        return view;
    }

    //Getter
    public ArrayList<MusicData> getArrayList() {
        return arrayList;
    }

    //Setter
    public void setArrayList(ArrayList<MusicData> arrayList) {
        this.arrayList = arrayList;
    }

}
