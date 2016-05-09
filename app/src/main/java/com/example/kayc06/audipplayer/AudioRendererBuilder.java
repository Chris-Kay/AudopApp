package com.example.kayc06.audipplayer;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;

import java.net.URL;

public interface AudioRendererBuilder {
    interface AudioRendererReceiver {
        void onAudioRendererReady(MediaCodecAudioTrackRenderer renderer, String contentId);
        void onError(Exception e, final String contentId);
    }
    void requestRenderer(URL url, String contentId, AudioRendererReceiver renderReceiver);
}

