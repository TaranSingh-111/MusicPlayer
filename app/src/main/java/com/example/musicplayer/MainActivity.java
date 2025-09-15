package com.example.musicplayer;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.media.MediaMetadataRetriever;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private SeekBar seekBar;
    private TextView currentTime;
    private TextView endTime;
    private ImageView playBtn;
    private ImageView pauseBtn;
    private ImageView stopBtn;
    private ImageView albumArt;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                //repeat task
                handler.postDelayed(this, 1000);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        seekBar = findViewById(R.id.progressBar);
        currentTime = findViewById(R.id.currentTime);
        endTime = findViewById(R.id.endTime);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        stopBtn = findViewById(R.id.stopBtn);
        albumArt = findViewById(R.id.albumArt);

        mediaPlayer = MediaPlayer.create(this, R.raw.sound);

        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration());
            currentTime.setText("0:00");
            endTime.setText(formatTime(mp.getDuration()));
        });

        playBtn.setOnClickListener(v -> {
            mediaPlayer.start();
            handler.post(updateSeekBar);
        });

        pauseBtn.setOnClickListener(v -> mediaPlayer.pause());

        stopBtn.setOnClickListener(v -> {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            seekBar.setProgress(0);
            currentTime.setText("0:00");
            endTime.setText(formatTime(mediaPlayer.getDuration()));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
//        metaRetriver.setDataSource("android.resource://"+getPackageName()+"/"+R.raw.sound);
//        try {
//            byte[] art = metaRetriver.getEmbeddedPicture();
//            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
//            albumArt.setImageBitmap(songImage);
//        } catch (Exception e) {
//            albumArt.setBackgroundColor(Color.GRAY);
//        }

        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.sound);
        metaRetriver.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

        byte[] art = metaRetriver.getEmbeddedPicture();
        if (art != null) {
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            albumArt.setImageBitmap(songImage);
        } else {
            albumArt.setBackgroundColor(Color.GRAY);
        }

    }
    private String formatTime(int milliseconds) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)%60;
            return String.format("%d:%02d", minutes, seconds);
        }

    @Override
    protected void onDestroy(){
            super.onDestroy();
            handler.removeCallbacks(updateSeekBar);
            if(mediaPlayer != null)
                mediaPlayer.release();
        }


}