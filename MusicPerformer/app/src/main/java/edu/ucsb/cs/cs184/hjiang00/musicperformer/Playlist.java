package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Environment;
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
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Playlist extends AppCompatActivity {
    ListView listView;
    private ArrayList<String> myAudioNameList;
    private ArrayAdapter arrayAdapter;
    public static final String PREFS_NAME = "myPrefFile";
    private Map<String, String> mySongMap = new HashMap<>();

    //------------------Piano Implementation-------------------
    private ArrayList<Integer> ReadData = new ArrayList<>();
    private SoundPool mSoundPool;
    private int csound;
    private int dsound;
    private int esound;
    private int fsound;
    private int gsound;
    private int asound;
    private int bsound;
    private int ccsound;

    private float LEFT_VOL = 1.0f;
    private float RIGHT_VOL = 1.0f;
    private int PRIORITY = 1;
    private int LOOP = 0;
    private float RATE = 1.0f;

    private File SongStoreDir = null;
    private String fileName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        SongStoreDir = new File(Environment.getExternalStorageDirectory() + "/MySongs");

//        mySongMap.put("song1", "fileName1");
//        mySongMap.put("song2", "fileName2");
//        mySongMap.put("song3", "fileName3");
//
//        saveMap(mySongMap);


        mySongMap = loadMap();
        if (mySongMap != null) {
            myAudioNameList = new ArrayList<>(mySongMap.keySet());
        }else {
            myAudioNameList = new ArrayList<>();
        }

        listView = findViewById(R.id.mylist);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, myAudioNameList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listView.getItemAtPosition(i);
                fileName = mySongMap.get(songName);
                Log.d("PlayTest", "Play muisc file" + fileName);
//                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
//                intent.putExtra("songFile", fileName);
//                startActivity(intent);
                playbackButtonClicked();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = (String) listView.getItemAtPosition(i);
                //delete sharedPreference as well as actual file
                String deleteFileName = mySongMap.get(songName);
                File file = new File(SongStoreDir, deleteFileName);
                file.delete();
                mySongMap.remove(songName);
                myAudioNameList.remove(songName);
                saveMap(mySongMap);
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
    //



    //Save Hashmap to sharedpreference file
    private void saveMap(Map<String,String> inputMap){
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove("My_map").commit();
            editor.putString("My_map", jsonString);
            editor.commit();
        }
    }
    //Load Hashmap to sharedpreference file
    private Map<String,String> loadMap(){
        Map<String,String> outputMap = new HashMap<String,String>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString("My_map", (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);
                    outputMap.put(key, value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }

    private void playbackButtonClicked() {
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        csound = mSoundPool.load(getApplicationContext(),R.raw.c,1);
        dsound = mSoundPool.load(getApplicationContext(),R.raw.d,1);
        esound = mSoundPool.load(getApplicationContext(),R.raw.e,1);
        fsound = mSoundPool.load(getApplicationContext(),R.raw.f,1);
        gsound = mSoundPool.load(getApplicationContext(),R.raw.g,1);
        asound = mSoundPool.load(getApplicationContext(),R.raw.a,1);
        bsound = mSoundPool.load(getApplicationContext(),R.raw.b,1);
        ccsound = mSoundPool.load(getApplicationContext(),R.raw.c2,1);

        int keys = 0;
        File file = new File(SongStoreDir,fileName);
//        Toast.makeText(getApplicationContext(), "playing "+fileName,
//                Toast.LENGTH_LONG).show();
        try {
            String line;
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null){
                String[] key_interval = line.split("\\s*,\\s*");
                ReadData.add(Integer.parseInt(key_interval[0]));
                ReadData.add(Integer.parseInt(key_interval[1]));
                keys++;
            }
            bufferedReader.close();
        }catch(FileNotFoundException e) {
            Log.e("error", "Unable to open file ");
        }
        catch(IOException e) {
            Log.e("error", "Error reading file ");
        }
        try {
            for (int i = 0; i < keys; i++) {
                Thread.sleep(ReadData.get(2 * i));
                int key = ReadData.get(2 * i+1);

                switch (key) {
                    case 1:
                        mSoundPool.play(csound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 2:
                        mSoundPool.play(dsound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 3:
                        mSoundPool.play(esound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 4:
                        mSoundPool.play(fsound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 5:
                        mSoundPool.play(gsound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 6:
                        mSoundPool.play(asound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 7:
                        mSoundPool.play(bsound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                    case 8:
                        mSoundPool.play(ccsound, LEFT_VOL, RIGHT_VOL, PRIORITY, LOOP, RATE);
                        break;
                }
            }
        }catch(IndexOutOfBoundsException e){

            Log.e("Error","Index out of bound");
        }catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        ReadData.clear();
        try {
            Thread.sleep(1500);
        }catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        mSoundPool.release();
    }

}

