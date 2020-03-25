package com.ashish.dalaapmusicplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    String TAG=PlayerActivity.class.getSimpleName();

   // Button play,pause,next,previous;
    TextView leftTime,rightTime,songLabel;
    ImageButton btnLeft,btnRight;
    RelativeLayout relativeLayout;
    ImageView play_play,next_play,previous_play,btnShuffle,repeat_play,album_play;
    SeekBar seekBar_play;
    MainActivity mainActivity;
    MediaPlayerService player;
    int totalDuration,currentPosition;

    StorageUtil storage;
    private ArrayList<Audio> audioList;
    public int audioIndex = -1;
    public Audio activeAudio; //an object on the currently playing audio
    //Handle incoming phone calls
    BlastVisualizer mVisualizer;
    Bitmap albumArt;
    boolean serviceBound = false;
//    private boolean isShuffle = false;
//    private boolean isRepeat = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        storage = new StorageUtil(getApplicationContext());
        audioList = storage.loadAudio();

        mVisualizer = findViewById(R.id.blast);
        relativeLayout=findViewById(R.id.visulizer);
        play_play=findViewById(R.id.play_play);
        next_play=findViewById(R.id.next_play);
        previous_play=findViewById(R.id.prev_play);
        btnShuffle=findViewById(R.id.shuffle_play);
        repeat_play=findViewById(R.id.repeat_play);
        seekBar_play=findViewById(R.id.seekBar_play);
        album_play=findViewById(R.id.album_play);
        leftTime= findViewById(R.id.leftTime);
        rightTime= findViewById(R.id.rightTime);

        songLabel=findViewById(R.id.XsongtextView);





        play_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAudio();
            }
        });

        next_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               nextBtn();
            }
        });

        previous_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preBtn();
            }
        });

        repeat_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.isRepeat)
                {
                    //repeat OFF
                    player.isRepeat=false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();

                    //player.DisableLoop();
                    repeat_play.setColorFilter(repeat_play.getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


                }else
                {
                    //repeat ON
                    player.isRepeat=true;
                    player.isShuffle = false;
                   // player.EnableLoop();

                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    repeat_play.setColorFilter(repeat_play.getContext().getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    btnShuffle.setColorFilter(btnShuffle.getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

                }
            }
        });


        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(player.isShuffle){
                    player.isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setColorFilter(btnShuffle.getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

                }else{
                    // make repeat to true
                    player.isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    player.isRepeat = false;
                    btnShuffle.setColorFilter(btnShuffle.getContext().getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
                    repeat_play.setColorFilter(repeat_play.getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

                }
            }
        });


        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            public void run() {
                Log.d(TAG,"Player_HAndler");
                int index = -1;
                try{
                    index=player.audioIndex;
                   // Log.d(TAG,"index"+index);

                }catch (NullPointerException e){}

                if(index>=0)
                {
                   // Log.d(TAG,"IndexG");
                    totalDuration=player.mediaPlayer.getDuration();
                    seekBar_play.setMax(player.mediaPlayer.getDuration());
                    String time = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                            TimeUnit.MILLISECONDS.toSeconds(totalDuration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration)));
                    rightTime.setText(time);
                    ShowAlbum();
                    seekBar_play.setProgress(player.mediaPlayer.getCurrentPosition());


                    if (player.CheckStatus()){

                        play_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);

//                        TranslateAnimation anim = new TranslateAnimation(-800f, 300f, 0f, 0f);  // might need to review the docs
//                        anim.setDuration(1500); // set how long you want the animation
//
//                        relativeLayout.setAnimation(anim);
                        relativeLayout.setVisibility(View.VISIBLE);
                    }else {
                        play_play.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);
                        relativeLayout.setVisibility(View.GONE);

                    }

                    try{

                        int audioSessionId = player.audioSessionId;

                        if (audioSessionId != -1 && audioSessionId != AudioManager.ERROR){
                            mVisualizer.setAudioSessionId(audioSessionId);
                        }
                    }catch (IllegalStateException e){}



                }



                handler.postDelayed(this, 1000);  //for interval...
            }
        };
        handler.postDelayed(runnable, 1000);


        seekBar_play.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekBar_play.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);


        seekBar_play.setOnSeekBarChangeListener(new
                                              SeekBar.OnSeekBarChangeListener() {
                                                  @Override
                                                  public void onProgressChanged(SeekBar seekBar, int i,
                                                                                boolean b) {
                                                      currentPosition=player.mediaPlayer.getCurrentPosition();
                                                      String time = String.format("%02d:%02d",
                                                              TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                                              TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                                                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)));
                                                      leftTime.setText(time);
                                                  }
                                                  @Override
                                                  public void onStartTrackingTouch(SeekBar seekBar) {


                                                  }
                                                  @Override
                                                  public void onStopTrackingTouch(SeekBar seekBar) {
                                                      player.mediaPlayer.seekTo(seekBar.getProgress());


                                                  }
                                              });












        album_play.setOnTouchListener(new OnSwipeTouchListener(PlayerActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(PlayerActivity.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                Toast.makeText(PlayerActivity.this,"Next",Toast.LENGTH_SHORT).show();
                nextBtn();
            }
            public void onSwipeLeft() {
                Toast.makeText(PlayerActivity.this,"Previous",Toast.LENGTH_SHORT).show();
                preBtn();
            }
            public void onSwipeBottom() {
                Toast.makeText(PlayerActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });


    }
    void preBtn(){

        play_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);
        player.skipToPrevious();
        player.StartNotify();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar_play.setMin(0);
        }
        ShowAlbum();
    }
    void nextBtn()
    {

        play_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);
        player.skipToNext();
        player.StartNotify();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar_play.setMin(0);
        }
        ShowAlbum();
    }


    void startAudio(){

        try {
            if (player.CheckStatus()){
                player.pauseMedia();
                play_play.setBackgroundResource(R.drawable.ic_play_arrow_white_24dp);
                player.StopNotify();

            }else {
                player.resumeMedia();
                play_play.setBackgroundResource(R.drawable.ic_pause_white_24dp);
                player.StartNotify();
            }
        }catch (NullPointerException e)
        {

        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @SuppressLint("ResourceType")
    public void ShowAlbum()
    {
        //Log.d(TAG,"Player_showAlb");

        MediaMetadataRetriever meta = new MediaMetadataRetriever();
        // StorageUtil storage = new StorageUtil(getApplicationContext());
        audioIndex = player.audioIndex;
       // activeAudio =player.activeAudio;

        audioIndex = storage.loadAudioIndex();
        if (audioIndex != -1) {
            //index is in a valid range
            activeAudio = audioList.get(audioIndex);
        }
        try {
           // Log.d(TAG,"player_Image");
            meta.setDataSource(activeAudio.getData());
            byte[] imgdata = meta.getEmbeddedPicture();
            albumArt = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);
            // imageView.setImageBitmap(albumArt);
            album_play.setImageBitmap(albumArt);
            songLabel.setText(activeAudio.getTitle());
            songLabel.setSelected(true);

//       songNameText.setText(activeAudio.getTitle());
//       songNameText.setSelected(true);
        } catch (Exception e) {
            albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.cover_art);
            //imageView.setImageResource(R.drawable.unsplash);
           // album_play.setImageBitmap(albumArt);
        }



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVisualizer != null)
            mVisualizer.release();

        if (serviceBound) {
            unbindService(serviceConnection);
            // service is active
           // player.stopSelf();
            //  player.StopNotify();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }
        @Override
        protected Void doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here



            MediaMetadataRetriever meta = new MediaMetadataRetriever();
            // StorageUtil storage = new StorageUtil(getApplicationContext());
            audioIndex = player.audioIndex;
            // activeAudio =player.activeAudio;

            audioIndex = storage.loadAudioIndex();
            if (audioIndex != -1) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            }
            try {
                Log.d(TAG,"player_Image");
                meta.setDataSource(activeAudio.getData());
                byte[] imgdata = meta.getEmbeddedPicture();
                albumArt = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);
                // imageView.setImageBitmap(albumArt);


            } catch (Exception e) {
                albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.cover_art);
                // album_play.setImageBitmap(albumArt);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            album_play.setImageBitmap(albumArt);
            songLabel.setText(activeAudio.getTitle());
            songLabel.setSelected(true);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //this method will be running on UI thread


        }

    }
}


