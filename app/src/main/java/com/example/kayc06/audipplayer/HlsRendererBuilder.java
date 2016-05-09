package com.example.kayc06.audipplayer;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.google.android.exoplayer.upstream.UriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;

import java.io.IOException;
import java.net.URL;


public final class HlsRendererBuilder implements AudioRendererBuilder {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENTS = 128;
    private final Context context;
    private String userAgent;
    private Handler handler;
    private HlsManifestCallback hlsManifestCallback;

    public HlsRendererBuilder(final Context context, final String userAgent, Handler handler) {
        this.context = context;
        this.userAgent = userAgent;
        this.handler = handler;
    }

    public void requestRenderer(final URL url, final String contentId, final AudioRendererBuilder.AudioRendererReceiver renderReceiver) {
        hlsManifestCallback = new HlsManifestCallback(context, userAgent, renderReceiver, url, contentId, handler);
        hlsManifestCallback.init();
    }

    private static class HlsManifestCallback implements ManifestFetcher.ManifestCallback<HlsPlaylist> {

        private final Context context;
        private final String userAgent;
        private final AudioRendererReceiver renderReceiver;
        private final String url;
        private final ManifestFetcher<HlsPlaylist> manifestFetcher;
        private final String contentId;
        private final Handler handler;
        private HlsSampleSource.EventListener eventListener;

        public HlsManifestCallback(final Context context, String userAgent, final AudioRendererReceiver renderReceiver, final URL url, final String contentId, Handler handler) {
            this.context = context;
            this.userAgent = userAgent;
            this.renderReceiver = renderReceiver;
            this.contentId = contentId;
            this.handler = handler;
            final HlsPlaylistParser parser = new HlsPlaylistParser();
            this.url = url.toExternalForm();
            final UriDataSource uriDataSource = new DefaultUriDataSource(context, userAgent);
            manifestFetcher = new ManifestFetcher<>(this.url, uriDataSource, parser);
            eventListener = new HlsSampleSource.EventListener() {
                //we don't care about these.
                @Override public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {}
                @Override public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {}
                @Override public void onLoadCanceled(int sourceId, long bytesLoaded) {}
                @Override public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {}
                @Override public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {}

                //but we probably want to pass back error events.
                @Override
                public void onLoadError(int sourceId, IOException e) {
                    renderReceiver.onError(e, contentId);
                }
            };
        }

        public void init() {
            manifestFetcher.singleLoad(context.getMainLooper(), this);
        }

        @Override
        public void onSingleManifest(final HlsPlaylist hlsPlaylist) {
            LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            final HttpDataSource httpDataSource = new DefaultHttpDataSource(userAgent, null, bandwidthMeter);

            setNoGzipEncodingHeader(httpDataSource); /*
                set the Accept-Encoding header to 'identity' so that
                we're not served gzip encoded data since this breaks
                some clients.  This will mean slightly bigger manifest
                downloads, but already-compressed audio segments should
                not be affected.
                */

            DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, httpDataSource);
            PtsTimestampAdjusterProvider ptsTimestampAdjusterProvider = new PtsTimestampAdjusterProvider();
            HlsChunkSource chunkSource = new HlsChunkSource(true, dataSource, url, hlsPlaylist, DefaultHlsTrackSelector.newDefaultInstance(context), bandwidthMeter, ptsTimestampAdjusterProvider, HlsChunkSource.ADAPTIVE_MODE_NONE);
            HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl, BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, handler,  eventListener, 0);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
            renderReceiver.onAudioRendererReady(audioRenderer, contentId);
        }

        @Override
        public void onSingleManifestError(IOException e) {
            renderReceiver.onError(e, contentId);
        }

        private void setNoGzipEncodingHeader(final HttpDataSource httpDataSource) {
            httpDataSource.setRequestProperty("Accept-Encoding", "identity");
        }
    }
}