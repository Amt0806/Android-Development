package com.example.myaudioplayer;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static com.example.myaudioplayer.MainActivity.ARTIST_TO_FRAG;
import static com.example.myaudioplayer.MainActivity.PATH_TO_FRAG;
import static com.example.myaudioplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.example.myaudioplayer.MainActivity.SONG_TO_FRAG;
import static com.example.myaudioplayer.MainActivity.musicFiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection, setData {


    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View view;
    RelativeLayout card_bottom_player;
    MusicService musicService;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";



    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom,container,false);
        artist = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer);
        card_bottom_player = view.findViewById(R.id.card_bottom_player);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService != null){
                    musicService.nextBtn();
                    if (getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE,null);
                        String artistName = preferences.getString(ARTIST_NAME,null);
                        String song_name = preferences.getString(SONG_NAME,null);
                        if (path != null){
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistName;
                            SONG_TO_FRAG = song_name;
                        }
                        else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_TO_FRAG = null;
                        }
                        if (SHOW_MINI_PLAYER){
                            if (PATH_TO_FRAG != null){
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if (art != null) {
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }
                                else{
                                    Glide.with(getContext()).load(R.drawable.images).into(albumArt);
                                }
                                songName.setText(SONG_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);
                                if (musicService.isPlaying()){
                                    playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                                }
                                else
                                {
                                    playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                                }
                            }
                        }
                    }
                }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"playPause", Toast.LENGTH_SHORT).show();
                if (musicService != null){
                    musicService.playPauseBtn();
                    if (musicService.isPlaying()){
                        playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                    else
                    {
                        playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    }


                }
                else{

                }


            }
        });
        card_bottom_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomClicked(view);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getContext(), MusicService.class);

        if (SHOW_MINI_PLAYER){
            if (PATH_TO_FRAG != null){
                /*
                FragmentTransaction fragmentTransaction = null;
                if (getFragmentManager() != null) {
                    fragmentTransaction = getFragmentManager().beginTransaction();
                }
                fragmentTransaction.show(this);
                fragmentTransaction.commit();

                 */
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null) {
                    Glide.with(getContext()).load(art).into(albumArt);
                }
                else{
                    Glide.with(getContext()).load(R.drawable.images).into(albumArt);
                }
                songName.setText(SONG_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);

                if (getContext() != null){
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
        else {
            if (getContext() != null){
                getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
            }
            /*
            FragmentTransaction fragmentTransaction = null;
            if (getFragmentManager() != null) {
                fragmentTransaction = getFragmentManager().beginTransaction();
            }
            fragmentTransaction.hide(this);
            fragmentTransaction.commit();

             */

        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null){
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
        if (musicService != null){
            if (musicService.mediaPlayer != null) {
                if (musicService.isPlaying()) {
                    playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                } else {
                    playPauseBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
            }
        }


    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }

    public void onBottomClicked(View view) {
        Intent intent = new Intent(getContext(),PlayerActivity.class);
        intent.putExtra("bottom",1);
        startActivity(intent);
    }

    public void setData1() {
        byte[] art = getAlbumArt(PATH_TO_FRAG);
        if (art != null) {
            Glide.with(getContext()).load(art).into(albumArt);
        }
        else{
            Glide.with(getContext()).load(R.drawable.images).into(albumArt);
        }
        songName.setText(SONG_TO_FRAG);
        artist.setText(ARTIST_TO_FRAG);
    }
}