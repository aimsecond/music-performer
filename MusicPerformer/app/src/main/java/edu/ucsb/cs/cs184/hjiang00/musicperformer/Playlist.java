package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Playlist extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<String> myAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            mRecyclerView.setHasFixedSize(true);

            myAudio = new ArrayList<>();





            for (int i = 0; i < 10; i++){
                myAudio.add(i,"name" + String.valueOf(i));
            }


            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter = new MyAdapter(myAudio);
            mRecyclerView.setAdapter(mAdapter);



            //mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        Log.d("PlayList", "Saved sharedpreference");
        super.onDestroy();
    }
}

