package com.example.rhmp3project;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MusicData implements Parcelable {

    private String mp3FileName;
    private String songName;
    private String artist;
    private String lyric;
    private Bitmap bitmap;
    private Boolean like;

    protected MusicData(Parcel in) {
        mp3FileName = in.readString();
        songName = in.readString();
    }

    public MusicData(Bitmap bitmap, String mp3FileName, String artist, String songName, String lyric, Boolean like) {
        this.bitmap = bitmap;
        this.mp3FileName = mp3FileName;
        this.artist = artist;
        this.songName = songName;
        this.lyric = lyric;
        this.like = false;
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getMp3FileName() {
        return mp3FileName;
    }

    public void setMp3FileName(String mp3FileName) {
        this.mp3FileName = mp3FileName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public Boolean getLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mp3FileName);
        parcel.writeString(songName);
    }
}
