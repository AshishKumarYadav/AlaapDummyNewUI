package com.ashish.dalaapmusicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class RecyclerView_Adapter extends RecyclerView.Adapter<ViewHolder> {
    List<Audio> list = Collections.emptyList();
    Context context;

    public RecyclerView_Adapter(List<Audio> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.setText(list.get(position).getTitle());
        Bitmap albumArt;
        MediaMetadataRetriever meta = new MediaMetadataRetriever();
        try {
            Log.d(TAG,"Image");
            meta.setDataSource(list.get(position).getData());
            byte[] imgdata = meta.getEmbeddedPicture();
            albumArt = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);

           holder.coverImage.setImageBitmap(albumArt);
        } catch (Exception e) {
          //  albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.cover_art);
            holder.coverImage.setImageResource(R.drawable.unsplash);
        }
      //  holder.coverImage.setImageBitmap();
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView coverImage;
   // ImageView play_pause;
    ViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        coverImage= itemView.findViewById(R.id.cover_image);

    }



}