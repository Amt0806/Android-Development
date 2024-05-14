package com.example.myaudioplayer;

import static com.example.myaudioplayer.ApplicationClass.ACTION_NEXT;
import static com.example.myaudioplayer.ApplicationClass.ACTION_PLAY;
import static com.example.myaudioplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.myaudioplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.myaudioplayer.PlayerActivity.listSongs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    setData setData;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";


    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPostion = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPostion != -1) {
            playMedia(myPostion);
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    playPauseBtn();
                    break;
                case "next":
                    nextBtn();
                    break;
                case "previous":
                    prevBtn();
                    break;
            }
        }
        return START_STICKY;

    }

    void prevBtn() {
        if (actionPlaying != null) {
            actionPlaying.playPrevSong();
        }
    }

    void nextBtn() {
        if (actionPlaying != null) {
            actionPlaying.playNextSong();
        }
    }

    void playPauseBtn() {
        if (actionPlaying != null) {
            actionPlaying.pausePlay();
        }
    }

    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        position = startPosition;
        if (mediaPlayer != null) {

            mediaPlayer.stop();
            // mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicFiles = listSongs;
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My audio");
    }

    void start() {
        mediaPlayer.start();
    }

    Boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop() {
        mediaPlayer.stop();
    }

    void pause() {
        mediaPlayer.pause();
    }

    void release() {
        mediaPlayer.release();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int position1) {
        position = position1;
        uri = Uri.parse(musicFiles.get(position1).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void onCompleted() {
        mediaPlayer.setOnCompletionListener(this::onCompletion);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (actionPlaying != null) {
            actionPlaying.playNextSong();
        }
        //NowPlayingFragmentBottom nb = new NowPlayingFragmentBottom();
        //nb.setData1();
        //MainActivity ma = new MainActivity();
        //ma.updateUi();
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

     void setCallBackData(setData setData){
        this.setData = setData;
    }
    void showNotification(int playPauseButton){
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,0);
        Intent preIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent
                .getBroadcast(this, 0 ,preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0 ,pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0 ,nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;

        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else {
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.images);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseButton)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"previous",prevPending)
                .addAction(playPauseButton,"pause",pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(1,notification);
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}