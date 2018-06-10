package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        VideoView myvideo = findViewById(R.id.videoView);
        Uri video; //The address of stored video

        myvideo.setMediaController(new MediaController(this));
        //myvideo.setVideoURI(video);
        myvideo.start();


    }
}
