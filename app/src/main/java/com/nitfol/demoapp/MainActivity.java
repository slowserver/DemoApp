package com.nitfol.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

//    private static final int RECORDER_SAMPLERATE = 8000;
//    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
//    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
//    private AudioRecord recorder = null;
//    private Thread recordingThread = null;
//    private boolean isRecording = false;
//    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
//    int BytesPerElement = 2; // 2 bytes in 16bit format

    /*2nd example from https://stackoverflow.com/questions/17429407/get-frequency-wav-audio-using-fft-and-complex-class*/
    private static final int RECORDER_SAMPLERATE = 44100;//22050
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SAMPLE_POINTS = 8192;

    private static final int FFT_POINTS  = 1024;
    private static final int MAGIC_SCALE = 10;

    short[] audioData;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    double mMaxFFTSample;
    double mMaxFFTSample2;
    int mPeakPos = 0;
    int peakPos2 = 0;
    byte[] buff = new byte[SAMPLE_POINTS];
    //byte[] yByte = new byte[1];
    double[] yByte = new double[1];

    String randomNote = "C";
    String frequencyNote = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Info", "started new");
        //foreverLoop();

        //2nd example
        bufferSize = AudioRecord.getMinBufferSize
                (RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

        audioData = new short[bufferSize]; //short array that pcm data is put into.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void foreverLoop() {
        boolean b = new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        onChange();
                        foreverLoop();
                    }
                }, 5000
        );
    }

    public void onTest(View v) {
        Log.i("onTest","clicked");

        try {
            startRecording();
        } catch(Exception e) {

        }
    }

    public void onTest2(View v) {
        Log.i("Info","onTest2 called");
        stopRecording();
        String message = "Recording Stopped";

        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void onChange(View v) {
        onChange();
    }

    public void onChange() {
        ImageView imageView = findViewById(R.id.imageView4);
        Integer random = getRandomResource();
        imageView.setImageResource(random);
        TextView tv = findViewById(R.id.outputText);
        tv.setText(randomNote);
    }

    public int getRandomResource() {
        TypedArray i = getResources().obtainTypedArray(R.array.notes);
        Random r = new Random();
        int next = r.nextInt(i.length());
        Log.i("Info", "random length " + Integer.toString(next));
        randomNote = NoteFreq.getNoteFromRandom(next);
        return i.getResourceId(next, R.drawable.a2);
    }

    private void startRecording() throws InterruptedException {
        isRecording = true;
        TextView tv = findViewById(R.id.outputText);
        tv.setText("startedRecording");
        Log.i("startRecording","called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.i("Info","inside permissions request");

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                    //Give user option to still opt-in the permissions
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_RECORD_AUDIO);

                } else {
                    // Show user dialog to grant permission to record audio
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_RECORD_AUDIO);
                }
            }
        }
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, RECORDER_SAMPLERATE);
        final int bufferSize = 2 * AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        recorder.startRecording();
        processing();
    }
    private void processing() {

        int read = recorder.read(buff, 0, buff.length);
        if (read > 0) {
            yByte = calculateFFT(buff);
        }

        TextView tv = findViewById(R.id.outputText);
        if (mMaxFFTSample > 50) {
            int approxFreq = mPeakPos * RECORDER_SAMPLERATE / buff.length;
            int approx2 = peakPos2 * RECORDER_SAMPLERATE / buff.length;
            this.frequencyNote = NoteFreq.getNoteFromFrequency(approxFreq);
            tv.setText(Integer.toString(approxFreq) + ":" + NoteFreq.getNoteFromFrequency(approxFreq));
            if (frequencyNote.equals(randomNote)) {
                onChange();
            }
        }
        if (isRecording) {
            boolean b = new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            processing();
                        }
                    }, 50
            );
        }
    }
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void stopRecording(){
        isRecording = false;
        if(null != recorder){
            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
    }

    public byte[] calculateFFTBack(byte[] audioBuffer) {
        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[FFT_POINTS];

        for (int i=0; i<FFT_POINTS; i++) {
            temp = (double)((audioBuffer[2*i] & 0xFF) | (audioBuffer[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp * MAGIC_SCALE, 0d);
        }

        y = FFT.fft(complexSignal);

        /*
         * See http://developer.android.com/reference/android/media/audiofx/Visualizer.html#getFft(byte[]) for format explanation
         */

        final byte[] y_byte = new byte[y.length*2];
        y_byte[0] = (byte) y[0].re();
        y_byte[1] = (byte) y[y.length - 1].re();
        for (int i = 1; i < y.length - 1; i++) {
            y_byte[i*2]   = (byte) y[i].re();
            y_byte[i*2+1] = (byte) y[i].im();
        }
        return y_byte;

    }

    public double[] calculateFFT(byte[] signal) {
        final int mNumberOfFFTPoints = signal.length/2;


        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];

        for(int i = 0; i < mNumberOfFFTPoints; i++){
            temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp * MAGIC_SCALE,0.0);
        }

        y = FFT.fft(complexSignal);

        mMaxFFTSample = 0.0;
        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if(absSignal[i] > mMaxFFTSample)
            {
                mMaxFFTSample2 = mMaxFFTSample;
                mMaxFFTSample = absSignal[i];
                peakPos2 = mPeakPos;
                mPeakPos = i;
            }
        }

        return absSignal;

    }
}