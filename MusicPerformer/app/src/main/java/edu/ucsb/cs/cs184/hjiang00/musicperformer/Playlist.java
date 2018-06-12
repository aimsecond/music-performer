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
    ListView listView;
    public static ArrayList<String> myAudio;
    private ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        myAudio = new ArrayList<>();


        listView = findViewById(R.id.mylist);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, myAudio);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                intent.putExtra("songposition", i);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                myAudio.remove(i);
                arrayAdapter.notifyDataSetChanged();
                return false;
            }
        });

    }

    @Override
    protected void onDestroy() {
        Log.d("PlayList", "Saved sharedpreference");
        super.onDestroy();
    }
}

