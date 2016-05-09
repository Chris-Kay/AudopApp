package com.example.kayc06.audipplayer;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    HlsRendererBuilder hlsRendererBuilder;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exoPlayer = ExoPlayer.Factory.newInstance(1);
        exoPlayer.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onPlayWhenReadyCommitted() {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }
        });

        hlsRendererBuilder = new HlsRendererBuilder(this, "android_sample_app", new Handler());

        final Button button = (Button) findViewById(R.id.play_button);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                play();
            }
        });
    }

    public void play() {
        if(isPlaying) {
            URL url;
            try {
                url = new URL("");

                hlsRendererBuilder.requestRenderer(url, "android_sample_app", new AudioRendererBuilder.AudioRendererReceiver() {
                    @Override
                    public void onAudioRendererReady(MediaCodecAudioTrackRenderer renderer, String contentId) {
                        exoPlayer.prepare(renderer);
                        exoPlayer.setPlayWhenReady(true);
                    }

                    @Override
                    public void onError(Exception e, String contentId) {
                        exoPlayer.stop();
                        exoPlayer.release();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            exoPlayer.stop();
        }
        isPlaying = !isPlaying;
}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.stop();
        exoPlayer.release();
    }
}
