package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class PlayActivity extends AppCompatActivity {
    private String filename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        MediaPlayer mp = new MediaPlayer();
        File path = android.os.Environment.getExternalStorageDirectory();
        Intent intent = getIntent();
//        String songPos = intent.getStringExtra("songposition");
        Log.d("path location", path.toString());
        //file name need to be passed to here
        try {
            mp.setDataSource(path + "/MySongs/Mon Jun 11 14:24:50 GMT+08:00 2018");
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("PlayTest", "Unable to play song");
        }


    }
}
