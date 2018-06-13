package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.ucsb.cs.cs184.hjiang00.musicperformer.Playlist.PREFS_NAME;

public class PerformActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String TAG = "MusicPerformer";
//--------------------Button----------------
    private Button mLeftButton;
    private Button mRightButton;
    private Button mCenterButton;
    private Button mRecordButton;
    private Button mDeleteButton;

//------------------Button Mode----------------
    private enum ButtonMode{
        CALIBRATEBACK_INVISIBLE, CALIBRATEHAND_INVISIBLE, GENERATEBIN_INVISIBLE, PERFORM_ADD_TRAIN, PERFORM_RECORD, ADD_GESTRUE
    }
    private ButtonMode bm = ButtonMode.CALIBRATEBACK_INVISIBLE;

//------------------Piano Implementation-------------------
    static private int lastPredict = 0;

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

    public void playC(){mSoundPool.play(csound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("1");}
    public void playD(){mSoundPool.play(dsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("2");}
    public void playE(){mSoundPool.play(esound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("3");}
    public void playF(){mSoundPool.play(fsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("4");}
    public void playG(){mSoundPool.play(gsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("5");}
    public void playA(){mSoundPool.play(asound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("6");}
    public void playB(){mSoundPool.play(bsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("7");}
    public void playCC(){mSoundPool.play(ccsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);Record("8");}

//---------------- Recording Utilities ----------
    private Boolean RECORD_MODE = false;
    private long startTime = 0;
    private ArrayList<String> performData = new ArrayList<>();
    File root = Environment.getExternalStorageDirectory();
    private File SongStoreDir = null;
    private String lastRecordedFile = null;
    private Map<String, String> mySongMap = new HashMap<>();

//--------------------Performance improvement---------
    private int[] lastReturnedLabel;
    private int checkStable = 0;
    private static int currentSound = 0;
    //Max Number of frames collected
    private static final int MAX_FRAME_COLLECT= 10;

    //Number of frames used for SVM
    private static final int FRAME_BUFFER_NUM = 1;
    private float[][] values = new float[FRAME_BUFFER_NUM][];
    private int[][] indices = new int[FRAME_BUFFER_NUM][];

    private Handler mHandler = new Handler();
    private static final String DATASET_NAME = "/data_collect.txt";


    private String storeFolderName = null;
    private File storeFolder = null;
    private FileWriter fw = null;


    private MyCameraView mOpenCvCameraView;
    private static final int SAMPLE_NUM = 7;

    private ArrayList<ArrayList<Double>> averChans = new ArrayList<ArrayList<Double>>();

//-------------Parameters for binary image construction-------------
    private Point[][] samplePoints = null;
    private double[][] colorCorrect = null;
    private double[][] backgroundCorrect = null;
    private int collectPointLen;
    //Boundary correction data
    private double[][] handLower = new double[SAMPLE_NUM][3];
    private double[][] handUpper = new double[SAMPLE_NUM][3];
    private double[][] backgroundLower = new double[SAMPLE_NUM][3];
    private double[][] backGroundUpper = new double[SAMPLE_NUM][3];

    //Boundary correction
    private Scalar lowerBound = new Scalar(0, 0, 0);
    private Scalar upperBound = new Scalar(0, 0, 0);

    //Binary Image matrix
    private Mat sampleColorMat = null;
    private List<Mat> sampleColorMats = null;

    private Mat[] sampleMats = null ;

    private Mat rgbaMat = null;

    private Mat rgbMat = null;
    private Mat bgrMat = null;


    private Mat interMat = null;

    private Mat binMat = null;
    private Mat binTmpMat = null;
    private Mat binTmpMat2 = null;
    private Mat binTmpMat0 = null;
    private Mat binTmpMat3 = null;

    private Mat tmpMat = null;
    private Mat backMat = null;
    private Mat difMat = null;
    private Mat binDifMat = null;

    private Scalar mColorsRGB[] = null;

    //Info of hand
    private GestureRecognition gr = null;

    private int imgNum;
    private int gesFrameCount;
    private int curLabel = 0;
    private int selectedLabel = -2;
    private int curMaxLabel = 0;
    private final int MAX_GESTURES = 8;


    private ArrayList<String> feaStrs = new ArrayList<String>();
    private boolean isPictureSaved = false;
    public final Object sync = new Object();


    private Boolean recordButtonClicked() {
        Toast.makeText(getApplicationContext(), "Recording!",
                Toast.LENGTH_LONG).show();
        if(!isExternalStorageWritable()){
            Toast.makeText(getApplicationContext(), "External Not Writable!",
                    Toast.LENGTH_LONG).show();
            return false;
        }else if (SongStoreDir == null) {

            SongStoreDir= new File(root.getAbsolutePath() + "/MySongs");
            if (!SongStoreDir.exists()) {
                if (!SongStoreDir.mkdir()){
                    Toast.makeText(getApplicationContext(), "Failed to create directory "+ SongStoreDir, Toast.LENGTH_SHORT).show();
                    SongStoreDir = null;
                    return false;
                }
            }
        }
        startTime = System.currentTimeMillis();
        return true;
    }
    private void stopButtonClicked() {
        startTime = 0;
        String filename = new Date().toString();
        File file = new File(SongStoreDir,filename);
        try{
            FileWriter fw = new FileWriter(file,true);
            for (int i = 0; i < performData.size(); i++) {
                fw.write(performData.get(i));
            }
            fw.close();
        }catch(Exception e){Log.e("Error","write error");}
        performData.clear();
        lastRecordedFile = filename;
        Toast.makeText(getApplicationContext(), "Recording Stored!"+lastRecordedFile,
                Toast.LENGTH_LONG).show();
        mySongMap.put("Song" + lastRecordedFile, lastRecordedFile);
        saveMap(mySongMap);
    }
    public void Record(String str){
        if(RECORD_MODE) {
            long currentTime = System.currentTimeMillis();
            long time = currentTime - startTime;
            startTime = currentTime;
            String data = Long.toString(time)+","+ str+"\n";
            performData.add(data);
        }
    }
    //-----------------------------------------------

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("Android Tutorial", "OpenCV loaded successfully");



                    try {
                        System.loadLibrary("MusicPerformer");
                        Log.d(TAG, "MusicPerformer loaded successfully");
                    } catch (UnsatisfiedLinkError ule) {
                        Log.e(TAG, "Could not load native MusicPerformer library");
                    }
                    try {
                        System.loadLibrary("signal");
                        Log.d(TAG, "signal loaded successfully");
                    } catch (UnsatisfiedLinkError ule) {
                        Log.e(TAG, "Could not load native signal library");
                    }

                    mOpenCvCameraView.enableView();

                    mRightButton = findViewById(R.id.right_button);
                    mLeftButton = findViewById(R.id.left_button);
                    mLeftButton.setText(R.string.CALIBRATEBACK);
                    mCenterButton = findViewById(R.id.center_button);
                    mRecordButton = findViewById(R.id.record_stop);
                    mDeleteButton = findViewById(R.id.delete_button);

                    Toast.makeText(getApplicationContext(),"Please use square boxes cover a unicolor background and hit calibrate", Toast.LENGTH_SHORT).show();

                    mLeftButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(bm == ButtonMode.CALIBRATEBACK_INVISIBLE){
                                rgbaMat.copyTo(backMat);
                                bm = ButtonMode.CALIBRATEHAND_INVISIBLE;
                                mLeftButton.setText(R.string.CALIBRATEHAND);
                                Toast.makeText(getApplicationContext(),"Please use square boxes cover your hand and hit calibrate", Toast.LENGTH_SHORT).show();
                            }else if (bm == ButtonMode.CALIBRATEHAND_INVISIBLE) {
                                bm = ButtonMode.GENERATEBIN_INVISIBLE;
                                mLeftButton.setText(R.string.FINALSTEP);
                                Toast.makeText(getApplicationContext(),"HOLD ON! Please check your hand is clear and continue.", Toast.LENGTH_SHORT).show();
                            }else if (bm == ButtonMode.GENERATEBIN_INVISIBLE){
                                bm = ButtonMode.PERFORM_ADD_TRAIN;
                                mLeftButton.setText(R.string.PERFORM);
                                mDeleteButton.setVisibility(View.VISIBLE);
                                mCenterButton.setVisibility(View.VISIBLE);
                                mRightButton.setVisibility(View.VISIBLE);
                                createDataFolder();
                            }else if (bm == ButtonMode.PERFORM_ADD_TRAIN && isModelExist()){
                                bm = ButtonMode.PERFORM_RECORD;
                                mRecordButton.setVisibility(View.VISIBLE);
                                mDeleteButton.setVisibility(View.GONE);
                                mCenterButton.setVisibility(View.GONE);
                                mRightButton.setVisibility(View.GONE);
                            }else if (bm == ButtonMode.PERFORM_RECORD){
                                bm = ButtonMode.PERFORM_ADD_TRAIN;
                                mRecordButton.setVisibility(View.GONE);
                                mCenterButton.setVisibility(View.VISIBLE);
                                mRightButton.setVisibility(View.VISIBLE);
                                mDeleteButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } break;
                    default: {
                        super.onManagerConnected(status);

                }break;
            }
        }
    };
//-------------------------------Continue-----------------------

    // svm native
    private native int trainClassifierNative(String trainingFile, int kernelType,
                                             int cost, float gamma, int isProb, String modelFile);
    private native int doClassificationNative(float values[][], int indices[][],
                                              int isProb, String modelFile, int labels[], double probs[]);

    //SVM training outputs a model file
    private void train() {
        // Svm training
        int kernelType = 2;
        int cost = 4;
        int isProb = 0;
        float gamma = 0.001f;
        String trainingFileLoc = storeFolderName+DATASET_NAME;
        String modelFileLoc = storeFolderName+"/model";
        Log.i("Store Path", modelFileLoc);

        if (trainClassifierNative(trainingFileLoc, kernelType, cost, gamma, isProb,
                modelFileLoc) == -1) {
            Log.d(TAG, "training err");
            finish();
        }
        Toast.makeText(this, "Training is done", Toast.LENGTH_SHORT).show();
    }

    public void train(View view) {
        train();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_perform);

        mySongMap = loadMap();
        final Button recordButton = findViewById(R.id.record_stop);
        recordButton.setTag(1);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int status =(Integer) v.getTag();
                if(status == 1) {
                    if(recordButtonClicked()){
                        RECORD_MODE = true;
                        recordButton.setText("STOP");
                        v.setTag(0); //stop recording
                    }
                } else {
                    RECORD_MODE = false;
                    recordButton.setText("RECORD");
                    v.setTag(1); //start over
                    stopButtonClicked();
                }
            }
        });


        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        csound = mSoundPool.load(getApplicationContext(),R.raw.c,1);
        dsound = mSoundPool.load(getApplicationContext(),R.raw.d,1);
        esound = mSoundPool.load(getApplicationContext(),R.raw.e,1);
        fsound = mSoundPool.load(getApplicationContext(),R.raw.f,1);
        gsound = mSoundPool.load(getApplicationContext(),R.raw.g,1);
        asound = mSoundPool.load(getApplicationContext(),R.raw.a,1);
        bsound = mSoundPool.load(getApplicationContext(),R.raw.b,1);
        ccsound = mSoundPool.load(getApplicationContext(),R.raw.c2,1);

        mOpenCvCameraView = (MyCameraView) findViewById(R.id.myCameraView);
        mOpenCvCameraView.setMaxFrameSize(200, 200);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        samplePoints = new Point[SAMPLE_NUM][2];
        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 2; j++)
            {
                samplePoints[i][j] = new Point();
            }
        }

        colorCorrect = new double[SAMPLE_NUM][3];
        backgroundCorrect = new double[SAMPLE_NUM][3];

        for (int i = 0; i < 3; i++)
            averChans.add(new ArrayList<Double>());

        //HLS
        //inithandLowerUpper(7, 7, 80, 80, 80, 80);

        //RGB
        //inithandLowerUpper(30, 30, 30, 30, 30, 30);

        //HSV
        //inithandLowerUpper(15, 15, 50, 50, 50, 50);
        //initbackgroundLowerUpper(5, 5, 80, 80, 100, 100);

        //Ycrcb
        //	inithandLowerUpper(40, 40, 10, 10, 10, 10);

        //Lab
        inithandLowerUpper(50, 50, 10, 10, 10, 10);
        initbackgroundLowerUpper(50, 50, 3, 3, 3, 3);

        SharedPreferences numbers = getSharedPreferences("Numbers", 0);
        imgNum = numbers.getInt("imgNum", 0);


    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isModelExist(){
        File modelfile = new File(storeFolderName + "/model");
        if (modelfile.exists()) {
            return true;
        }
        Toast.makeText(getApplicationContext(), "Please add guesture to your model first ", Toast.LENGTH_SHORT).show();
        return false;
    }

    //All the trained gestures jpg files and SVM training model, train_data.txt
    //are stored in ExternalStorageDirectory/MyDataSet
    //If MyDataSet doesn't exist, then it will be created in this function
    public void createDataFolder() {
        if (!isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(), "External storage is not writable!", Toast.LENGTH_SHORT).show();
        } else if (storeFolder == null) {
            storeFolderName = Environment.getExternalStorageDirectory() + "/MyDataSet";
            storeFolder = new File(storeFolderName);
            boolean success;
            if (!storeFolder.exists()) {
                success = storeFolder.mkdir();
                if (!success) {
                    Toast.makeText(getApplicationContext(), "Failed to create directory "+ storeFolderName, Toast.LENGTH_SHORT).show();
                    storeFolder = null;
                    storeFolderName = null;
                }
            }
        }

        if (storeFolder != null) {
            labelInitialize();
        }


    }
    public void labelInitialize() {
        File file[] = storeFolder.listFiles();

        int maxLabel = 0;
        for (int i=0; i < file.length; i++)
        {

            String fullName = file[i].getName();
            final int dotId = fullName.lastIndexOf('.');
            if (dotId > 0) {
                String name = fullName.substring(0, dotId);
                String extName = fullName.substring(dotId+1);
                if (extName.equals("jpg")) {
                    int curName = Integer.valueOf(name);
                    if (curName > maxLabel)
                        maxLabel = curName;
                }
            }
        }

        curLabel = maxLabel;
        curMaxLabel = curLabel;
    }

    //Initialize lower and upper bound for hand and background
    void inithandLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3,
                         double cu3)
    {
        handLower[0][0] = cl1;
        handUpper[0][0] = cu1;
        handLower[0][1] = cl2;
        handUpper[0][1] = cu2;
        handLower[0][2] = cl3;
        handUpper[0][2] = cu3;
    }

    void initbackgroundLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3,
                             double cu3)
    {
        backgroundLower[0][0] = cl1;
        backGroundUpper[0][0] = cu1;
        backgroundLower[0][1] = cl2;
        backGroundUpper[0][1] = cu2;
        backgroundLower[0][2] = cl3;
        backGroundUpper[0][2] = cu3;
    }

    public void releaseCV(Mat img) {
        if (img != null)
            img.release();
    }

    public void releaseOpenCV() {
        releaseCV(sampleColorMat);
        sampleColorMat = null;

        if (sampleColorMats!=null) {
            for (int i = 0; i < sampleColorMats.size(); i++)
            {
                releaseCV(sampleColorMats.get(i));
            }
        }
        sampleColorMats = null;

        if (sampleMats != null) {
            for (int i = 0; i < sampleMats.length; i++)
            {
                releaseCV(sampleMats[i]);
            }
        }
        sampleMats = null;

        releaseCV(rgbMat);
        rgbMat = null;

        releaseCV(bgrMat);
        bgrMat = null;

        releaseCV(interMat);
        interMat = null;

        releaseCV(binMat);
        binMat = null;

        releaseCV(binTmpMat0);
        binTmpMat0 = null;

        releaseCV(binTmpMat3);
        binTmpMat3 = null;

        releaseCV(binTmpMat2);
        binTmpMat2 = null;

        releaseCV(tmpMat);
        tmpMat = null;

        releaseCV(backMat);
        backMat = null;

        releaseCV(difMat);
        difMat = null;

        releaseCV(binDifMat);
        binDifMat = null;

    }



    @Override
    public void onCameraViewStarted(int width, int height) {
        // TODO Auto-generated method stub
        Log.i(TAG, "On cameraview started!");

        if (sampleColorMat == null)
            sampleColorMat = new Mat();


        if (sampleColorMats == null)
            sampleColorMats = new ArrayList<Mat>();

        if (sampleMats == null) {
            sampleMats = new Mat[SAMPLE_NUM];
            for (int i = 0; i < SAMPLE_NUM; i++)
                sampleMats[i] = new Mat();
        }

        if (rgbMat == null)
            rgbMat = new Mat();

        if (bgrMat == null)
            bgrMat = new Mat();

        if (interMat == null)
            interMat = new Mat();

        if (binMat == null)
            binMat = new Mat();

        if (binTmpMat == null)
            binTmpMat = new Mat();

        if (binTmpMat2 == null)
            binTmpMat2 = new Mat();

        if (binTmpMat0 == null)
            binTmpMat0 = new Mat();

        if (binTmpMat3 == null)
            binTmpMat3 = new Mat();

        if (tmpMat == null)
            tmpMat = new Mat();

        if (backMat==null)
            backMat = new Mat();

        if (difMat == null)
            difMat = new Mat();

        if (binDifMat == null)
            binDifMat = new Mat();


        if (gr == null)
            gr = new GestureRecognition();

        mColorsRGB = new Scalar[] { new Scalar(255, 0, 0, 255), new Scalar(0, 255, 0, 255), new Scalar(0, 0, 255, 255) };

    }

    @Override
    public void onCameraViewStopped() {
        // TODO Auto-generated method stub
        Log.i(TAG, "On cameraview stopped!");
    }


    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        rgbaMat = inputFrame.rgba();

        //flip input frame matrix
        Core.flip(rgbaMat, rgbaMat, 1);


        Imgproc.GaussianBlur(rgbaMat, rgbaMat, new Size(5,5), 5, 5);

        Imgproc.cvtColor(rgbaMat, rgbMat, Imgproc.COLOR_RGBA2RGB);

        //Convert original RGB colorspace
        Imgproc.cvtColor(rgbaMat, interMat, Imgproc.COLOR_RGB2Lab);


          if (bm == ButtonMode.CALIBRATEHAND_INVISIBLE) {
            sampleHand(rgbaMat);

          } else if (bm == ButtonMode.GENERATEBIN_INVISIBLE) {
            constructBinImg(interMat, binMat);

            return binMat;

          } else if ((bm == ButtonMode.PERFORM_ADD_TRAIN)||(bm == ButtonMode.ADD_GESTRUE)
                  || (bm == ButtonMode.PERFORM_RECORD) ){

            constructBinImg(interMat, binMat);
            drawContour();

			String entry = gr.handExtraction(rgbaMat, curLabel);

              if (bm == ButtonMode.ADD_GESTRUE) {
				gesFrameCount++;
				Core.putText(rgbaMat, Integer.toString(gesFrameCount), new Point(10,
				10), Core.FONT_HERSHEY_SIMPLEX, 0.6, Scalar.all(0));

				feaStrs.add(entry);

				if (gesFrameCount == MAX_FRAME_COLLECT ) {
					 Runnable runnableShowBeforeAdd = new Runnable() {
				            @Override
				            public void run() {
				                {
				                	showDialogBeforeAdd("Add?", "Add this new gesture labeled as "
											+ curLabel + "?");
				                }
				            }
				        };

					mHandler.post(runnableShowBeforeAdd);

					try {
						synchronized(sync) {
							sync.wait();
						}
					} catch (Exception e) {}
                    bm = ButtonMode.PERFORM_ADD_TRAIN;
				}
              } else if ((bm == ButtonMode.PERFORM_RECORD)) {
                Double[] doubleValue = gr.features.toArray(new Double[gr.features.size()]);
                values[0] = new float[doubleValue.length];
                indices[0] = new int[doubleValue.length];

                for (int i = 0; i < doubleValue.length; i++)
                {
                    values[0][i] = (float)(doubleValue[i]*1.0f);
                    indices[0][i] = i+1;
                }

                int isProb = 0;

                String modelFile = storeFolderName + "/model";
                int[] returnedLabel = {0};
                double[] returnedProb = {0.0};

//                boolean isHand = gr.detectIsHand(rgbaMat);

                //Predicted labels are stored in returnedLabel
                //Since currently prediction is made for each frame, only returnedLabel[0] is useful.
                int r = doClassificationNative(values, indices, isProb, modelFile, returnedLabel, returnedProb);
                Log.i("returnedLabel"," "+returnedLabel[0]);
                Log.i("lastPredict"," "+lastPredict);

                if (r == 0) {
                    if (lastPredict == returnedLabel[0]) {
                        checkStable++;
                    }else{
                        checkStable = 0;
                    }
                    lastPredict = returnedLabel[0];
                        Core.putText(rgbaMat, Integer.toString(returnedLabel[0]), new Point(15,
                                15), Core.FONT_HERSHEY_SIMPLEX, 0.6, mColorsRGB[0]);
                        if (returnedLabel[0] != 0 && checkStable >= 2 && currentSound != lastPredict) {
                                // Play music here
                            switch (returnedLabel[0]) {
                                case 1:
                                        playC();break;
                                case 2:
                                        playD();break;
                                case 3:
                                        playE();break;
                                case 4:
                                        playF();break;
                                case 5:
                                        playG();break;
                                case 6:
                                        playA();break;
                                case 7:
                                        playB();break;
                                case 8:
                                        break;
                            }
                            checkStable = 0;
                            currentSound = returnedLabel[0];
                        }

                }
            }
          } else if (bm == ButtonMode.CALIBRATEBACK_INVISIBLE) { //First mode which presamples background colors
            sampleBackground(rgbaMat);
        }

		if (isPictureSaved) {
			savePicture();
			isPictureSaved = false;
		}
        return rgbaMat;
    }

    //Collect hand sample data
    void sampleHand(Mat img)
    {
        int cols = img.cols();
        int rows = img.rows();
        Log.e(TAG,Integer.toString(cols));
        Log.e(TAG,Integer.toString(rows));
        collectPointLen = rows/20;
        Scalar color = mColorsRGB[2];

//Still can be modified to improve the result of constructing binary image
//------------------Sample Points location------
        samplePoints[0][0].x = cols*3/22;
        samplePoints[0][0].y = rows/2;
        samplePoints[1][0].x = cols*6/25;
        samplePoints[1][0].y = rows*7/20;
        samplePoints[2][0].x = cols*3/8;
        samplePoints[2][0].y = rows/3;
        samplePoints[3][0].x = cols*4/7;
        samplePoints[3][0].y = rows/3;
        samplePoints[4][0].x = cols*4/7;
        samplePoints[4][0].y = rows/2;
        samplePoints[5][0].x = cols*3/7;
        samplePoints[5][0].y = rows/2;
        samplePoints[6][0].x = cols/3;
        samplePoints[6][0].y = rows*9/16;

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            samplePoints[i][1].x = samplePoints[i][0].x+collectPointLen;
            samplePoints[i][1].y = samplePoints[i][0].y+collectPointLen;
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            Core.rectangle(img,  samplePoints[i][0], samplePoints[i][1], color, 1);
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                colorCorrect[i][j] = (interMat.get((int)(samplePoints[i][0].y+collectPointLen/2), (int)(samplePoints[i][0].x+collectPointLen/2)))[j];
            }
        }

    }

    //Collect Background data
    void sampleBackground(Mat img)
    {
        int cols = img.cols();
        int rows = img.rows();
        collectPointLen = rows/20;
        Scalar color = mColorsRGB[2];

//Still can be modified to improve the result of constructing binary image
//------------------Sample Points location------
        samplePoints[0][0].x = cols/6;
        samplePoints[0][0].y = rows/3;
        samplePoints[1][0].x = cols/6;
        samplePoints[1][0].y = rows*2/3;
        samplePoints[2][0].x = cols/2;
        samplePoints[2][0].y = rows/6;
        samplePoints[3][0].x = cols/2;
        samplePoints[3][0].y = rows/2;
        samplePoints[4][0].x = cols/2;
        samplePoints[4][0].y = rows*5/6;
        samplePoints[5][0].x = cols*5/6;
        samplePoints[5][0].y = rows/3;
        samplePoints[6][0].x = cols*5/6;
        samplePoints[6][0].y = rows*2/3;

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            samplePoints[i][1].x = samplePoints[i][0].x+collectPointLen;
            samplePoints[i][1].y = samplePoints[i][0].y+collectPointLen;
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            Core.rectangle(img,  samplePoints[i][0], samplePoints[i][1], color, 1);
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                backgroundCorrect[i][j] = (interMat.get((int)(samplePoints[i][0].y+collectPointLen/2), (int)(samplePoints[i][0].x+collectPointLen/2)))[j];
            }
        }

    }

    void boundaryCorrect()
    {
        for (int i = 1; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                handLower[i][j] = handLower[0][j];
                handUpper[i][j] = handUpper[0][j];

                backgroundLower[i][j] = backgroundLower[0][j];
                backGroundUpper[i][j] = backGroundUpper[0][j];
            }
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                if (colorCorrect[i][j] - handLower[i][j] < 0)
                    handLower[i][j] = colorCorrect[i][j];

                if (colorCorrect[i][j] + handUpper[i][j] > 255)
                    handUpper[i][j] = 255 - colorCorrect[i][j];

                if (backgroundCorrect[i][j] - backgroundLower[i][j] < 0)
                    backgroundLower[i][j] = backgroundCorrect[i][j];

                if (backgroundCorrect[i][j] + backGroundUpper[i][j] > 255)
                    backGroundUpper[i][j] = 255 - backgroundCorrect[i][j];
            }
        }
    }

    //Construct Binary Image for hand
    void constructHand(Mat imgIn, Mat imgOut)
    {
        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            lowerBound.set(new double[]{colorCorrect[i][0]-handLower[i][0], colorCorrect[i][1]-handLower[i][1],
                    colorCorrect[i][2]-handLower[i][2]});
            upperBound.set(new double[]{colorCorrect[i][0]+handUpper[i][0], colorCorrect[i][1]+handUpper[i][1],
                    colorCorrect[i][2]+handUpper[i][2]});
            Core.inRange(imgIn, lowerBound, upperBound, sampleMats[i]);
        }

        imgOut.release();
        sampleMats[0].copyTo(imgOut);
        for (int i = 1; i < SAMPLE_NUM; i++)
        {
            Core.add(imgOut, sampleMats[i], imgOut);
        }
        Imgproc.medianBlur(imgOut, imgOut, 3);
    }

    //Construct Binary Image for Background
    void constructBackground(Mat imgIn, Mat imgOut)
    {
        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            lowerBound.set(new double[]{backgroundCorrect[i][0]-backgroundLower[i][0], backgroundCorrect[i][1]-backgroundLower[i][1],
                    backgroundCorrect[i][2]-backgroundLower[i][2]});
            upperBound.set(new double[]{backgroundCorrect[i][0]+backGroundUpper[i][0], backgroundCorrect[i][1]+backGroundUpper[i][1],
                    backgroundCorrect[i][2]+backGroundUpper[i][2]});
            Core.inRange(imgIn, lowerBound, upperBound, sampleMats[i]);
        }

        imgOut.release();
        sampleMats[0].copyTo(imgOut);

        for (int i = 1; i < SAMPLE_NUM; i++)
        {
            Core.add(imgOut, sampleMats[i], imgOut);
        }

        Core.bitwise_not(imgOut, imgOut);
        Imgproc.medianBlur(imgOut, imgOut, 7);
    }

    //Construct binary image containing as a whole
    void constructBinImg(Mat imgIn, Mat imgOut)
    {
        int colNum = imgIn.cols();
        int rowNum = imgIn.rows();
        int boxExtension = 0;

        boundaryCorrect();
        constructHand(imgIn, binTmpMat);
        constructBackground(imgIn, binTmpMat2);

        Core.bitwise_and(binTmpMat, binTmpMat2, binTmpMat);
        binTmpMat.copyTo(tmpMat);
        binTmpMat.copyTo(imgOut);

        Rect roiRect = makeBoundingBox(tmpMat);

        if (roiRect!=null) {
            roiRect.x = Math.max(0, roiRect.x - boxExtension);
            roiRect.y = Math.max(0, roiRect.y - boxExtension);
            roiRect.width = Math.min(roiRect.width+boxExtension, colNum);
            roiRect.height = Math.min(roiRect.height+boxExtension, rowNum);

            Mat roi1 = new Mat(binTmpMat, roiRect);
            Mat roi3 = new Mat(imgOut, roiRect);
            imgOut.setTo(Scalar.all(0));

            roi1.copyTo(roi3);

            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
            Imgproc.dilate(roi3, roi3, element, new Point(-1, -1), 2);
            Imgproc.erode(roi3, roi3, element, new Point(-1, -1), 2);
        }
    }

    void drawContour()
    {
        gr.contours.clear();
        Imgproc.findContours(binMat, gr.contours, gr.hie, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        //Find biggest contour and return the index of the contour, which is gr.cMaxId
        gr.findBiggestContour();
        if (gr.cMaxId > -1) {

            gr.approxContour.fromList(gr.contours.get(gr.cMaxId).toList());
            Imgproc.approxPolyDP(gr.approxContour, gr.approxContour, 2, true);
            gr.contours.get(gr.cMaxId).fromList(gr.approxContour.toList());

            //gr.contours.get(gr.cMaxId) represents the contour of the hand
            Imgproc.drawContours(rgbaMat, gr.contours, gr.cMaxId, mColorsRGB[0], 1);

            //Palm center is stored in gr.inCircle, radius of the inscribed circle is stored in gr.inCircleRadius
            gr.findInscribedCircle(rgbaMat);
            gr.boundingRect = Imgproc.boundingRect(gr.contours.get(gr.cMaxId));
            Imgproc.convexHull(gr.contours.get(gr.cMaxId), gr.hullI, false);
            gr.hullP.clear();
            for (int i = 0; i < gr.contours.size(); i++)
                gr.hullP.add(new MatOfPoint());

            int[] cId = gr.hullI.toArray();
            List<Point> lp = new ArrayList<Point>();
            Point[] contourPts = gr.contours.get(gr.cMaxId).toArray();

            for (int i = 0; i < cId.length; i++)
            {
                lp.add(contourPts[cId[i]]);
            }

            //gr.hullP.get(gr.cMaxId) returns the locations of the points in the convex hull of the hand
            gr.hullP.get(gr.cMaxId).fromList(lp);
            lp.clear();
            gr.fingerTips.clear();
            gr.defectPoints.clear();
            gr.defectPointsOrdered.clear();
            gr.fingerTipsOrdered.clear();
            gr.defectIdAfter.clear();

            if ((contourPts.length >= 5)
                    && gr.detectIsHand(rgbaMat) && (cId.length >=5)){
                Imgproc.convexityDefects(gr.contours.get(gr.cMaxId), gr.hullI, gr.defects);
                List<Integer> dList = gr.defects.toList();

                for (int i = 0; i < dList.size(); i++)
                {
                    int id = i % 4;
                    Point curPoint;

                    if (id == 2) { //Defect point
                        double depth = (double)dList.get(i+1)/256.0;
                        curPoint = contourPts[dList.get(i)];

                        Point curPoint0 = contourPts[dList.get(i-2)];
                        Point curPoint1 = contourPts[dList.get(i-1)];
                        Point vec0 = new Point(curPoint0.x - curPoint.x, curPoint0.y - curPoint.y);
                        Point vec1 = new Point(curPoint1.x - curPoint.x, curPoint1.y - curPoint.y);
                        double dot = vec0.x*vec1.x + vec0.y*vec1.y;
                        double lenth0 = Math.sqrt(vec0.x*vec0.x + vec0.y*vec0.y);
                        double lenth1 = Math.sqrt(vec1.x*vec1.x + vec1.y*vec1.y);
                        double cosTheta = dot/(lenth0*lenth1);

                        if ((depth > gr.inCircleRadius*0.7)&&(cosTheta>=-0.7)
                                && (!isClosedToBoundary(curPoint0, rgbaMat))
                                &&(!isClosedToBoundary(curPoint1, rgbaMat))
                                ){

                            gr.defectIdAfter.add((i));
                            Point finVec0 = new Point(curPoint0.x-gr.inCircle.x,
                                    curPoint0.y-gr.inCircle.y);
                            double finAngle0 = Math.atan2(finVec0.y, finVec0.x);
                            Point finVec1 = new Point(curPoint1.x-gr.inCircle.x,
                                    curPoint1.y - gr.inCircle.y);
                            double finAngle1 = Math.atan2(finVec1.y, finVec1.x);

                            if (gr.fingerTipsOrdered.size() == 0) {
                                gr.fingerTipsOrdered.put(finAngle0, curPoint0);
                                gr.fingerTipsOrdered.put(finAngle1, curPoint1);

                            } else {
                                gr.fingerTipsOrdered.put(finAngle0, curPoint0);
                                gr.fingerTipsOrdered.put(finAngle1, curPoint1);
                            }
                        }
                    }
                }
            }
        }

        if (gr.detectIsHand(rgbaMat)) {
            //gr.boundingRect represents four coordinates of the bounding box.
            Core.rectangle(rgbaMat, gr.boundingRect.tl(), gr.boundingRect.br(), mColorsRGB[1], 2);
            Imgproc.drawContours(rgbaMat, gr.hullP, gr.cMaxId, mColorsRGB[2]);
        }
    }

    boolean isClosedToBoundary(Point pt, Mat img)
    {
        int margin = 5;
        if ((pt.x > margin) && (pt.y > margin) && (pt.x < img.cols()-margin) && (pt.y < img.rows()-margin)) {
            return false;
        }
        return true;
    }

    Rect makeBoundingBox(Mat img)
    {
        gr.contours.clear();
        Imgproc.findContours(img, gr.contours, gr.hie, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        gr.findBiggestContour();

        if (gr.cMaxId > -1) {
            gr.boundingRect = Imgproc.boundingRect(gr.contours.get(gr.cMaxId));
        }

        if (gr.detectIsHand(rgbaMat)) {

            return gr.boundingRect;
        } else
            return null;
    }

    @Override
    public void onPause(){
        Log.i(TAG, "Paused!");
        super.onPause();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        Log.i(TAG, "Resumed!");
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "Destroyed!");
        releaseOpenCV();
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        SharedPreferences numbers = getSharedPreferences("Numbers", 0);
        SharedPreferences.Editor editor = numbers.edit();
        editor.putInt("imgNum", imgNum);
        editor.commit();
    }


    	public void showDialogBeforeAdd(String title,String message){
		Log.i("Show Dialog", "Entered");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                  this);
             // set title
             alertDialogBuilder.setTitle(title);
             // set dialog message
             alertDialogBuilder
                  .setMessage(message)
                  .setCancelable(false)
                  .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog,int id) {
                            doAddNewGesture();
                            synchronized(sync) {
                            	sync.notify();
                            }
                            dialog.cancel();
                       }
                   })
                  .setNegativeButton("No",new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                    	   synchronized(sync) {
                               sync.notify();
                               }
                            dialog.cancel();

                       }
                  });
                  // create alert dialog
                  AlertDialog alertDialog = alertDialogBuilder.create();
                  // show it
                  alertDialog.show();
   }


 	public void addGesture(View view) {
        if(curLabel >= MAX_GESTURES){
            Toast.makeText(getApplicationContext(), "You can't add more than 8 gestures!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bm == ButtonMode.PERFORM_ADD_TRAIN) {
            if (storeFolder != null) {
                File myFile = new File(storeFolderName + DATASET_NAME);

                if (myFile.exists()) {

                } else {
                    try {
                    myFile.createNewFile();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to create dataset at "
                                + myFile, Toast.LENGTH_SHORT).show();
                    }
                }

                 try {
                     fw = new FileWriter(myFile, true);
                     feaStrs.clear();

                     if (selectedLabel == -2)
                         curLabel = curMaxLabel + 1;
                     else {
                         curLabel++;
                         selectedLabel = -2;
                     }
                     gesFrameCount = 0;
                     bm = ButtonMode.ADD_GESTRUE;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.i(TAG, "File not found.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
 		} else {
 		}
 	}

 	public void doAddNewGesture() {
 		try {
            for (int i = 0; i < feaStrs.size(); i++) {
                fw.write(feaStrs.get(i));
            }
            fw.close();
 		} catch (Exception e) {

 		}

 		savePicture();
 		if (curLabel > curMaxLabel) {
 			curMaxLabel = curLabel;
 		}
 	}

 	boolean savePicture()
	{
		Mat img;

        if (((bm == ButtonMode.CALIBRATEBACK_INVISIBLE) || (bm == ButtonMode.CALIBRATEHAND_INVISIBLE)
                || (bm == ButtonMode.PERFORM_ADD_TRAIN)) || (bm == ButtonMode.ADD_GESTRUE) ||
                (bm == ButtonMode.PERFORM_RECORD)) {
        Imgproc.cvtColor(rgbaMat, bgrMat, Imgproc.COLOR_RGBA2BGR, 3);
        img = bgrMat;
        } else if (bm == ButtonMode.GENERATEBIN_INVISIBLE) {
        img = binMat;
		} else
			img = null;

		if (img != null) {
			 if (!isExternalStorageWritable()) {
				  return false;
			 }

			 File path;
			 String filename;
            if (bm != ButtonMode.ADD_GESTRUE) {
				 path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				 filename = "image_" + imgNum + ".jpg";
			 } else {
				 path = storeFolder;
				 filename = curLabel + ".jpg";
			 }

			 imgNum++;
			 File file = new File(path, filename);

			  Boolean bool = false;
			  filename = file.toString();

			  bool = Highgui.imwrite(filename, img);

			  if (bool == true) {
				//  Toast.makeText(getApplicationContext(), "Saved as " + filename, Toast.LENGTH_SHORT).show();
				  Log.d(TAG, "Succeed save image to" + filename);
			  } else
			    Log.d(TAG, "Fail to save Image");

			  return bool;
		}
		return false;
	}

    //Call when finish recording a new song
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
    //Call when start to record a new song
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
    public void deleteDir(View view) {
        File dir = new File(Environment.getExternalStorageDirectory()+"/MyDataSet");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
        Toast.makeText(getApplicationContext(), "All gestures have been deleted ", Toast.LENGTH_SHORT).show();
        curLabel = 0;
        curMaxLabel = 0;
    }

}
