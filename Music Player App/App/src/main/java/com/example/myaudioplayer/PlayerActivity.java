package com.example.myaudioplayer;

import static com.example.myaudioplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.myaudioplayer.ApplicationClass.ACTION_NEXT;
import static com.example.myaudioplayer.ApplicationClass.ACTION_PLAY;
import static com.example.myaudioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.myaudioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.myaudioplayer.MainActivity.RepeatBoolean;
import static com.example.myaudioplayer.MainActivity.ShuffleBoolean;
import static com.example.myaudioplayer.MainActivity.musicFiles;
import static com.example.myaudioplayer.MusicAdapter.mFiles;
import static com.example.myaudioplayer.NowPlayingFragmentBottom.MUSIC_FILE;
import static com.example.myaudioplayer.NowPlayingFragmentBottom.MUSIC_LAST_PLAYED;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextButton, PrevButton, BackButton, Shuffle, Repeat;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;
    public static int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    Uri uri;
    //public static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setFulScreen();
        //changeColor();
        setContentView(R.layout.activity_player);

        getSupportActionBar().hide();

        initViews();
        getIntentMethod();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (musicService != null && b){
                    musicService.seekTo(i * 1000);
                    if (!(musicService.isPlaying())){
                        musicService.start();
                        playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });
        PrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrevSong();
            }
        });
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausePlay();
            }
        });

        Shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShuffleBoolean)
                {
                    ShuffleBoolean = false;
                    Shuffle.setImageResource(R.drawable.shuffle_off);
                }
                else {
                    ShuffleBoolean = true;
                    Shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24);
                }
            }
        });
        Repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RepeatBoolean){
                    RepeatBoolean = false;
                    Repeat.setImageResource(R.drawable.repeat_off);
                }
                else {
                    RepeatBoolean = true;
                    Repeat.setImageResource(R.drawable.ic_baseline_repeat_24);
                }
            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null){
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private String formattedTime(int mCurrentPosition) {

        String totalOut;
        String totalNew;
        String second = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + second;
        totalNew = minutes + ":0" + second;
        if (second.length() == 1){
            return totalNew;

        }
        else {
            return totalOut;
        }
    }

    private void getIntentMethod() {
        int bottom = 0;
        bottom = getIntent().getIntExtra("bottom",0);
        if (bottom == 0) {
        position = getIntent().getIntExtra("position",-1);
        String sender = getIntent().getStringExtra("sender");

            if (sender != null && sender.equals("AlbumDetails")) {
                listSongs = albumFiles;
            } else {
                listSongs = mFiles;
            }
            if (listSongs != null) {
                playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
                uri = Uri.parse(listSongs.get(position).getPath());
            }

            Intent intent = new Intent(this, MusicService.class);

            intent.putExtra("servicePosition", position);
            startService(intent);
        }


    }

    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.duration_played);
        duration_total = findViewById(R.id.duration_total);
        cover_art = findViewById(R.id.cover_art);
        nextButton = findViewById(R.id.next);
        PrevButton = findViewById(R.id.prev);
        BackButton = findViewById(R.id.back_button);
        Shuffle = findViewById(R.id.shuffle);
        Repeat = findViewById(R.id.repeat);
        playPauseButton = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekbar);
    }

    private void metadata(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (uri != null) {
            retriever.setDataSource(uri.toString());
        }
        else{
            SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
            String path = preferences.getString(MUSIC_FILE,null);
            retriever.setDataSource(path);
        }
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null){

            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null){
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());

                    }
                    else {
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0x00000000});
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else {
            ImageAnimationNo(this,cover_art);
            ImageView gradient = findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);



        }
    }


    public void playNextSong(){
        if (musicService != null){

            musicService.stop();
           // musicService.release();

        }
        if (!(listSongs.size() == 1)) {
        if (ShuffleBoolean && !RepeatBoolean){
            position = getRandom(listSongs.size() - 1);
        }
        else if (!ShuffleBoolean && !RepeatBoolean){

                position = (position + 1) % (listSongs.size() - 1);
            }
        }

        playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
        uri = Uri.parse(listSongs.get(position).getPath());

        musicService.createMediaPlayer(position);

        seekBar.setMax(musicService.getDuration() / 1000);
        metadata(uri);

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_baseline_pause_24);
        musicService.start();


    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public void playPrevSong(){
        if (ShuffleBoolean && !RepeatBoolean){
            position = getRandom(listSongs.size() - 1);
        }
        else if (!ShuffleBoolean && !RepeatBoolean){
            if (position == 0) {
                position = (listSongs.size()-1);
            }
            else {
                position = position-1;
            }
        }

        playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
        uri = Uri.parse(listSongs.get(position).getPath());
        if (musicService != null){

            musicService.stop();

        }
        musicService.createMediaPlayer(position);
        musicService.start();
        seekBar.setMax(musicService.getDuration() / 1000);
        metadata(uri);

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_baseline_pause_24);


    }

    public void pausePlay(){
        if (musicService.isPlaying()){
            musicService.pause();
            playPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
        }
        else {
            musicService.start();
            playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
        }
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);

    }

    public void ImageAnimationNo(Context context, ImageView imageView){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(R.drawable.images).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);

    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metadata(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_baseline_pause_24);
        if (musicService != null){
            if (musicService.mediaPlayer != null){
                if (musicService.isPlaying()){
                    playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
                }
                else {
                    playPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
            }
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;

    }



}