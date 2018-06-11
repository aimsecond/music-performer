package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        //file name need to be passed to here
        try {
            mp.setDataSource(path + filename);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
