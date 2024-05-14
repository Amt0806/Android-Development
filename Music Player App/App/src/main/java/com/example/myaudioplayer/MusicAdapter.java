package com.example.myaudioplayer;

import static androidx.core.app.ActivityCompat.startIntentSenderForResult;


import static com.example.myaudioplayer.SongsFragment.musicAdapter;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private static final int REQUEST_PERM_DELETE = 200;
    private Context mContext;
    static ArrayList<MusicFiles> mFiles;
    private ActivityResultLauncher<IntentSenderRequest> SongDeleteIntentLauncher;


    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles){
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int pos = position;

        holder.file_name.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null){
            Glide.with(mContext).asBitmap().
                    load(image).
                    into(holder.album_art);
        }
        else {
            Glide.with(mContext).asBitmap().
                    load(R.drawable.images).
                    into(holder.album_art);
        }

        if (pos == PlayerActivity.position){
            holder.file_name.setTextColor(Color.parseColor("#FF0000"));
        }
        else{
            holder.file_name.setTextColor(Color.parseColor("#000000"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,PlayerActivity.class);
                intent.putExtra("position", pos);
                mContext.startActivity(intent);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popmenu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()){
                        case R.id.delete:
                            Toast.makeText(mContext,"Delete clicked!",Toast.LENGTH_SHORT).show();
                            delete(pos, String.valueOf(holder.file_name));

                            break;
                    }
                    return true;
                });

            }
        });



    }



    public void delete(int pos, String file_name) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,Long.parseLong(mFiles.get(pos).getId()));
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(contentUri);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            PendingIntent pi = MediaStore.createDeleteRequest(mContext.getContentResolver(), uris);


            IntentSender intentSender = pi.getIntentSender();
            //startIntentSenderForResult(SongsFragment.class,intentSender,REQUEST_PERM_DELETE,null,0,0,0,0);
            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
            SongDeleteIntentLauncher.launch(intentSenderRequest);
            mFiles.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, mFiles.size());
            Toast.makeText(mContext, "File Deleted", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(mContext, "Can't be deleted", Toast.LENGTH_SHORT).show();
        }

        /*
        File file = new File(mFiles.get(pos).getPath());
        if (file.exists()) {
            boolean deleted = file.delete();

            if (deleted) {
                mContext.getContentResolver().delete(contentUri, null, null);
                mFiles.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, mFiles.size());
                Toast.makeText(mContext, "File Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Can't be deleted", Toast.LENGTH_SHORT).show();
            }
        }
         */
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView file_name;
        ImageView album_art, menuMore;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.moreMenu);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mFiles = new ArrayList<>();
        mFiles.clear();
        mFiles.addAll(musicFilesArrayList);


        notifyDataSetChanged();
    }



}
