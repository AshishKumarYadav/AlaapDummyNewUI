package com.ashish.dalaapmusicplayer.Playlist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ashish.dalaapmusicplayer.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayListActivity extends AppCompatActivity {

    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);



    }
}
