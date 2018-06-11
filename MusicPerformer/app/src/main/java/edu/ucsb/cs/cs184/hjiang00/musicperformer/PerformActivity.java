package edu.ucsb.cs.cs184.hjiang00.musicperformer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
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
import java.util.HashMap;
import java.util.List;

public class PerformActivity extends AppCompatActivity implements CvCameraViewListener2 {

    //Just for debugging
    private static final String TAG = "MusicPerformer";

    //Color Space used for hand segmentation
    private static final int COLOR_SPACE = Imgproc.COLOR_RGB2Lab;

    //Number of frames collected for each gesture in the training set
    private static final int GES_FRAME_MAX= 10;

    public final Object sync = new Object();

    //Number of frames used for prediction
    private static final int FRAME_BUFFER_NUM = 1;

    private boolean isPictureSaved = false;

    private float[][] values = new float[FRAME_BUFFER_NUM][];
    private int[][] indices = new int[FRAME_BUFFER_NUM][];

    private Handler mHandler = new Handler();
    private static final String DATASET_NAME = "/train_data.txt";

    private String storeFolderName = null;
    private File storeFolder = null;
    private FileWriter fw = null;


    private MyCameraView mOpenCvCameraView;
    private static final int SAMPLE_NUM = 7;


    private Point[][] samplePoints = null;
    private double[][] avgColor = null;
    private double[][] avgBackColor = null;

    private double[] channelsPixel = new double[4];
    private ArrayList<ArrayList<Double>> averChans = new ArrayList<ArrayList<Double>>();

    private double[][] cLower = new double[SAMPLE_NUM][3];
    private double[][] cUpper = new double[SAMPLE_NUM][3];
    private double[][] cBackLower = new double[SAMPLE_NUM][3];
    private double[][] cBackUpper = new double[SAMPLE_NUM][3];

    private Scalar lowerBound = new Scalar(0, 0, 0);
    private Scalar upperBound = new Scalar(0, 0, 0);
    private int squareLen;

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

    //Stores all the information about the hand
    private GestureRecognition gr = null;

    private int imgNum;
    private int gesFrameCount;
    private int curLabel = 0;
    private int selectedLabel = -2;
    private int curMaxLabel = 0;

    //Stores string representation of features to be written to train_data.txt
    private ArrayList<String> feaStrs = new ArrayList<String>();



    private Button mLeftButton;
    private Button mRightButton;
    private Button mCenterButton;
    private Button mRecordButton;
//------------------Revised for Button Mode----------------
    private enum ButtonMode{
        CALIBRATEBACK_INVISIBLE, CALIBRATEHAND_INVISIBLE, GENERATEBIN_INVISIBLE, PERFORM_ADD_TRAIN, PERFORM_RECORD, ADD_GESTRUE
}
    private ButtonMode bm = ButtonMode.CALIBRATEBACK_INVISIBLE;

//------------------Piano Implementation-------------------
    static private int lastPredict = 0;

    private static SoundPool mSoundPool;
    private int csound;
    private int dsound;
    private int esound;
    private int fsound;
    private int gsound;
    private int asound;
    private int bsound;
    private int ccsound;
    private int cssound;
    private int csssound;
    private int dssound;
    private int gssound;
    private int assound;
    private int fssound;

    private float LEFT_VOL = 1.0f;
    private float RIGHT_VOL = 1.0f;
    private int PRIORITY = 1;
    private int LOOP = 0;
    private float RATE = 1.0f;

    public void playC(){mSoundPool.play(csound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);}

    public void playD(){
        mSoundPool.play(dsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playE(){
        mSoundPool.play(esound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playF(){
        mSoundPool.play(fsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playG(){
        mSoundPool.play(gsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playA(){
        mSoundPool.play(asound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playB(){
        mSoundPool.play(bsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playCC(){
        mSoundPool.play(ccsound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playCS(){
        mSoundPool.play(cssound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playDS(){
        mSoundPool.play(dssound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playFS(){
        mSoundPool.play(fssound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playGS(){
        mSoundPool.play(gssound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playAS(){
        mSoundPool.play(assound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }

    public void playCSS(){
        mSoundPool.play(csssound,LEFT_VOL,RIGHT_VOL,PRIORITY,LOOP,RATE);
    }
    //----------------------------------

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
                        Log.e(TAG, "Hey, could not load native MusicPerformer library");
                    }
                    try {
                        System.loadLibrary("signal");
                        Log.d(TAG, "signal loaded successfully");
                    } catch (UnsatisfiedLinkError ule) {
                        Log.e(TAG, "Hey, could not load native signal library");
                    }

                    mOpenCvCameraView.enableView();

                    mRightButton = findViewById(R.id.right_button);
                    mRightButton.setVisibility(View.GONE);
                    mLeftButton = findViewById(R.id.left_button);
                    mLeftButton.setText("CALIBRATEBACK");
                    mCenterButton = findViewById(R.id.center_button);
                    mCenterButton.setVisibility(View.GONE);
                    mRecordButton = findViewById(R.id.record_button);
                    mRecordButton.setVisibility(View.GONE);
                    mLeftButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(bm == ButtonMode.CALIBRATEBACK_INVISIBLE){
                                rgbaMat.copyTo(backMat);
                                bm = ButtonMode.CALIBRATEHAND_INVISIBLE;
                                mLeftButton.setText("CALIBRATEHAND");
                            }else if (bm == ButtonMode.CALIBRATEHAND_INVISIBLE) {
                                bm = ButtonMode.GENERATEBIN_INVISIBLE;
                                mLeftButton.setText("FINALSTEP");
                            }else if (bm == ButtonMode.GENERATEBIN_INVISIBLE){
                                bm = ButtonMode.PERFORM_ADD_TRAIN;
                                mLeftButton.setText("PERFORM");
                                mCenterButton.setVisibility(View.VISIBLE);
                                mRightButton.setVisibility(View.VISIBLE);
                                preTrain();
                            }else if (bm == ButtonMode.PERFORM_ADD_TRAIN){
                                bm = ButtonMode.PERFORM_RECORD;
                                mRecordButton.setVisibility(View.VISIBLE);
                                mCenterButton.setVisibility(View.GONE);
                                mRightButton.setVisibility(View.GONE);
                            }else if (bm == ButtonMode.PERFORM_RECORD){
                                bm = ButtonMode.PERFORM_ADD_TRAIN;
                                mRecordButton.setVisibility(View.GONE);
                                mCenterButton.setVisibility(View.VISIBLE);
                                mRightButton.setVisibility(View.VISIBLE);
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

    //SVM training which outputs a file named as "model" in MyDataSet
    private void train() {
        // Svm training
        int kernelType = 2; // Radial basis function
        int cost = 4; // Cost
        int isProb = 0;
        float gamma = 0.001f; // Gamma
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

    public void initLabel() {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_perform);

//---------------------Piano Initialization--------------------

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        csound = mSoundPool.load(getApplicationContext(),R.raw.c,1);
        dsound = mSoundPool.load(getApplicationContext(),R.raw.d,1);
        esound = mSoundPool.load(getApplicationContext(),R.raw.e,1);
        fsound = mSoundPool.load(getApplicationContext(),R.raw.f,1);
        gsound = mSoundPool.load(getApplicationContext(),R.raw.g,1);
        asound = mSoundPool.load(getApplicationContext(),R.raw.a,1);
        bsound = mSoundPool.load(getApplicationContext(),R.raw.b,1);
        cssound = mSoundPool.load(getApplicationContext(),R.raw.c_hash,1);
        csssound = mSoundPool.load(getApplicationContext(),R.raw.c_hash,1);
        ccsound = mSoundPool.load(getApplicationContext(),R.raw.c2,1);
        assound = mSoundPool.load(getApplicationContext(),R.raw.a_hash,1);
        fssound = mSoundPool.load(getApplicationContext(),R.raw.f_hash,1);
        gssound = mSoundPool.load(getApplicationContext(),R.raw.g_hash,1);
        dssound = mSoundPool.load(getApplicationContext(),R.raw.d_hash,1);

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

        avgColor = new double[SAMPLE_NUM][3];
        avgBackColor = new double[SAMPLE_NUM][3];

        for (int i = 0; i < 3; i++)
            averChans.add(new ArrayList<Double>());

        //HLS
        //initCLowerUpper(7, 7, 80, 80, 80, 80);

        //RGB
        //initCLowerUpper(30, 30, 30, 30, 30, 30);

        //HSV
        //initCLowerUpper(15, 15, 50, 50, 50, 50);
        //initCBackLowerUpper(5, 5, 80, 80, 100, 100);

        //Ycrcb
        //	initCLowerUpper(40, 40, 10, 10, 10, 10);

        //Lab
        initCLowerUpper(50, 50, 10, 10, 10, 10);
        initCBackLowerUpper(50, 50, 3, 3, 3, 3);

        SharedPreferences numbers = getSharedPreferences("Numbers", 0);
        imgNum = numbers.getInt("imgNum", 0);

        Log.i(TAG, "Created!");

    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



    //All the trained gestures jpg files and SVM training model, train_data.txt
    //are stored in ExternalStorageDirectory/MyDataSet
    //If MyDataSet doesn't exist, then it will be created in this function
    public void preTrain() {
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
            initLabel();
        }


    }

    //Called when user clicks "Train" button
    public void train(View view) {
        train();
    }

    //Just initialize boundaries of the first sample
    void initCLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3,
                         double cu3)
    {
        cLower[0][0] = cl1;
        cUpper[0][0] = cu1;
        cLower[0][1] = cl2;
        cUpper[0][1] = cu2;
        cLower[0][2] = cl3;
        cUpper[0][2] = cu3;
    }

    void initCBackLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3,
                             double cu3)
    {
        cBackLower[0][0] = cl1;
        cBackUpper[0][0] = cu1;
        cBackLower[0][1] = cl2;
        cBackUpper[0][1] = cu2;
        cBackLower[0][2] = cl3;
        cBackUpper[0][2] = cu3;
    }

    public void releaseCVMats() {
        releaseCVMat(sampleColorMat);
        sampleColorMat = null;

        if (sampleColorMats!=null) {
            for (int i = 0; i < sampleColorMats.size(); i++)
            {
                releaseCVMat(sampleColorMats.get(i));
            }
        }
        sampleColorMats = null;

        if (sampleMats != null) {
            for (int i = 0; i < sampleMats.length; i++)
            {
                releaseCVMat(sampleMats[i]);
            }
        }
        sampleMats = null;

        releaseCVMat(rgbMat);
        rgbMat = null;

        releaseCVMat(bgrMat);
        bgrMat = null;

        releaseCVMat(interMat);
        interMat = null;

        releaseCVMat(binMat);
        binMat = null;

        releaseCVMat(binTmpMat0);
        binTmpMat0 = null;

        releaseCVMat(binTmpMat3);
        binTmpMat3 = null;

        releaseCVMat(binTmpMat2);
        binTmpMat2 = null;

        releaseCVMat(tmpMat);
        tmpMat = null;

        releaseCVMat(backMat);
        backMat = null;

        releaseCVMat(difMat);
        difMat = null;

        releaseCVMat(binDifMat);
        binDifMat = null;

    }

    public void releaseCVMat(Mat img) {
        if (img != null)
            img.release();
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
        //	releaseCVMats();
    }


    //Called when each frame data gets received
    //inputFrame contains the data for each frame
    //Mode flow: BACKGROUND_MODE --> SAMPLE_MODE --> DETECTION_MODE <--> TRAIN_REC_MODE
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        rgbaMat = inputFrame.rgba();

        Core.flip(rgbaMat, rgbaMat, 1);


        Imgproc.GaussianBlur(rgbaMat, rgbaMat, new Size(5,5), 5, 5);

        Imgproc.cvtColor(rgbaMat, rgbMat, Imgproc.COLOR_RGBA2RGB);

        //Convert original RGB colorspace to the colorspace indicated by COLR_SPACE
        Imgproc.cvtColor(rgbaMat, interMat, COLOR_SPACE);


          if (bm == ButtonMode.CALIBRATEHAND_INVISIBLE) {
            preSampleHand(rgbaMat);

          } else if (bm == ButtonMode.GENERATEBIN_INVISIBLE) {
            //segmented hand represented by white color
            produceBinImg(interMat, binMat);

            return binMat;

          } else if ((bm == ButtonMode.PERFORM_ADD_TRAIN)||(bm == ButtonMode.ADD_GESTRUE)
                  || (bm == ButtonMode.PERFORM_RECORD) ){

            produceBinImg(interMat, binMat);
            makeContours();


			String entry = gr.featureExtraction(rgbaMat, curLabel);

            //Collecting the frame data of a certain gesture and storing it in the file train_data.txt.
            //This mode stops when the number of frames processed equals GES_FRAME_MAX
              if (bm == ButtonMode.ADD_GESTRUE) {
				gesFrameCount++;
				Core.putText(rgbaMat, Integer.toString(gesFrameCount), new Point(10,
				10), Core.FONT_HERSHEY_SIMPLEX, 0.6, Scalar.all(0));



				feaStrs.add(entry);

				if (gesFrameCount == GES_FRAME_MAX) {
					 Runnable runnableShowBeforeAdd = new Runnable() {
				            @Override
				            public void run() {
				                {
				                	showDialogBeforeAdd("Add or not", "Add this new gesture labeled as "
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

                //Predicted labels are stored in returnedLabel
                //Since currently prediction is made for each frame, only returnedLabel[0] is useful.
                int r = doClassificationNative(values, indices, isProb, modelFile, returnedLabel, returnedProb);
                Log.i("returnedLabel"," "+returnedLabel[0]);
                Log.i("lastPredict"," "+lastPredict);

                if (r == 0) {
                    Core.putText(rgbaMat, Integer.toString(returnedLabel[0]), new Point(15,
                            15), Core.FONT_HERSHEY_SIMPLEX, 0.6, mColorsRGB[0]);
                    if (returnedLabel[0] != 0 && returnedLabel[0] != lastPredict) {
                        // Play music here
                        switch (returnedLabel[0]) {
                            case 1:
                                playC();lastPredict = returnedLabel[0];
                                break;
                            case 2:
                                playD();lastPredict = returnedLabel[0];
                                break;
                            case 3:
                                playE();lastPredict = returnedLabel[0];
                                break;
                            case 4:
                                playF();lastPredict = returnedLabel[0];
                                break;
                            case 5:
                                playG();lastPredict = returnedLabel[0];
                                break;
                            case 6:
                                playA();lastPredict = returnedLabel[0];
                                break;
                            case 7:
                                lastPredict = returnedLabel[0];
                                break;
                        }
                    }
                }
            }
          } else if (bm == ButtonMode.CALIBRATEBACK_INVISIBLE) { //First mode which presamples background colors
            preSampleBack(rgbaMat);
        }

		if (isPictureSaved) {
			savePicture();
			isPictureSaved = false;
		}
        return rgbaMat;
    }

    //Presampling hand colors.
    //Output is avgColor, which is essentially a 7 by 3 matrix storing the colors sampled by seven squares
    void preSampleHand(Mat img)
    {
        int cols = img.cols();
        int rows = img.rows();
        Log.e(TAG,Integer.toString(cols));
        Log.e(TAG,Integer.toString(rows));
        squareLen = rows/20;
        Scalar color = mColorsRGB[2];  //Blue Outline

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
            samplePoints[i][1].x = samplePoints[i][0].x+squareLen;
            samplePoints[i][1].y = samplePoints[i][0].y+squareLen;
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            Core.rectangle(img,  samplePoints[i][0], samplePoints[i][1], color, 1);
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                avgColor[i][j] = (interMat.get((int)(samplePoints[i][0].y+squareLen/2), (int)(samplePoints[i][0].x+squareLen/2)))[j];
            }
        }

    }

    //Presampling background colors.
    //Output is avgBackColor, which is essentially a 7 by 3 matrix storing the colors sampled by seven squares
    void preSampleBack(Mat img)
    {
        int cols = img.cols();
        int rows = img.rows();
        squareLen = rows/20;
        Scalar color = mColorsRGB[2];  //Blue Outline

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
            samplePoints[i][1].x = samplePoints[i][0].x+squareLen;
            samplePoints[i][1].y = samplePoints[i][0].y+squareLen;
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            Core.rectangle(img,  samplePoints[i][0], samplePoints[i][1], color, 1);
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                avgBackColor[i][j] = (interMat.get((int)(samplePoints[i][0].y+squareLen/2), (int)(samplePoints[i][0].x+squareLen/2)))[j];
            }
        }

    }

    void boundariesCorrection()
    {
        for (int i = 1; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                cLower[i][j] = cLower[0][j];
                cUpper[i][j] = cUpper[0][j];

                cBackLower[i][j] = cBackLower[0][j];
                cBackUpper[i][j] = cBackUpper[0][j];
            }
        }

        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                if (avgColor[i][j] - cLower[i][j] < 0)
                    cLower[i][j] = avgColor[i][j];

                if (avgColor[i][j] + cUpper[i][j] > 255)
                    cUpper[i][j] = 255 - avgColor[i][j];

                if (avgBackColor[i][j] - cBackLower[i][j] < 0)
                    cBackLower[i][j] = avgBackColor[i][j];

                if (avgBackColor[i][j] + cBackUpper[i][j] > 255)
                    cBackUpper[i][j] = 255 - avgBackColor[i][j];
            }
        }
    }

    void adjustBoundingBox(Rect initRect, Mat img)
    {

    }

    //Generates binary image containing user's hand
    void produceBinImg(Mat imgIn, Mat imgOut)
    {
        int colNum = imgIn.cols();
        int rowNum = imgIn.rows();
        int boxExtension = 0;

        boundariesCorrection();
        produceBinHandImg(imgIn, binTmpMat);
        produceBinBackImg(imgIn, binTmpMat2);

        Core.bitwise_and(binTmpMat, binTmpMat2, binTmpMat);
        binTmpMat.copyTo(tmpMat);
        binTmpMat.copyTo(imgOut);

        Rect roiRect = makeBoundingBox(tmpMat);
        adjustBoundingBox(roiRect, binTmpMat);

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

    //Generates binary image thresholded only by sampled hand colors
    void produceBinHandImg(Mat imgIn, Mat imgOut)
    {
        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            lowerBound.set(new double[]{avgColor[i][0]-cLower[i][0], avgColor[i][1]-cLower[i][1],
                    avgColor[i][2]-cLower[i][2]});
            upperBound.set(new double[]{avgColor[i][0]+cUpper[i][0], avgColor[i][1]+cUpper[i][1],
                    avgColor[i][2]+cUpper[i][2]});
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

    //Generates binary image thresholded only by sampled background colors
    void produceBinBackImg(Mat imgIn, Mat imgOut)
    {
        for (int i = 0; i < SAMPLE_NUM; i++)
        {
            lowerBound.set(new double[]{avgBackColor[i][0]-cBackLower[i][0], avgBackColor[i][1]-cBackLower[i][1],
                    avgBackColor[i][2]-cBackLower[i][2]});
            upperBound.set(new double[]{avgBackColor[i][0]+cBackUpper[i][0], avgBackColor[i][1]+cBackUpper[i][1],
                    avgBackColor[i][2]+cBackUpper[i][2]});
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



    void makeContours()
    {
        gr.contours.clear();
        Imgproc.findContours(binMat, gr.contours, gr.hie, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        //Find biggest contour and return the index of the contour, which is gr.cMaxId
        gr.findBiggestContour();
        Log.e(TAG, "READY TO GO INSIDE IF BRANCH");
        if (gr.cMaxId > -1) {
            Log.e(TAG, "INSIDE IF BRANCH");

            gr.approxContour.fromList(gr.contours.get(gr.cMaxId).toList());
            Imgproc.approxPolyDP(gr.approxContour, gr.approxContour, 2, true);
            gr.contours.get(gr.cMaxId).fromList(gr.approxContour.toList());

            //gr.contours.get(gr.cMaxId) represents the contour of the hand
            Imgproc.drawContours(rgbaMat, gr.contours, gr.cMaxId, mColorsRGB[0], 1);

            Log.e(TAG, "READY TO CALL NATIVE FINDCIRCLE FUNC");
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
                //Core.circle(rgbaMat, contourPts[cId[i]], 2, new Scalar(241, 247, 45), -3);
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
        releaseCVMats();
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        SharedPreferences numbers = getSharedPreferences("Numbers", 0);
        SharedPreferences.Editor editor = numbers.edit();
        editor.putInt("imgNum", imgNum);
        editor.commit();
    }

/*Non-Used code*/
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

    //Called when user clicks "Add Gesture" button
    //Prepare train_data.txt file and set the mode to be ADD_MODE
 	public void addNewGesture(View view) {

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
    //				 mode = ADD_MODE;
                     bm = ButtonMode.ADD_GESTRUE;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.i(TAG, "******* File not found. Did you" +
                            " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
 		} else {
 			Toast.makeText(getApplicationContext(), "Please do it in TRAIN_REC mode"
					, Toast.LENGTH_SHORT).show();
 		}
 	}

    //Write the strings of features to the file train_data.txt
    //Save the screenshot of the gesture
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
				  Log.d(TAG, "Succeed writing image to" + filename);
			  } else
			    Log.d(TAG, "Fail writing image to external storage");

			  return bool;
		}
		return false;
	}

}
