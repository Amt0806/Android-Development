package com.example.myaudioplayer;

import static android.content.ContentValues.TAG;
import static android.content.Context.SEARCH_SERVICE;
import static com.example.myaudioplayer.ApplicationClass.ACTION_NEXT;
import static com.example.myaudioplayer.ApplicationClass.ACTION_PLAY;
import static com.example.myaudioplayer.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context,MusicService.class);
        if (actionName != null){
            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName", "playPause");
                    Log.e(TAG, "Action play" );
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName", "next");
                    context.startService(serviceIntent);
                    Log.e(TAG, "Action next" );
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName", "previous");
                    context.startService(serviceIntent);
                    Log.e(TAG, "Action pre" );
                    break;
            }
        }
    }
}
