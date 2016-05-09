package com.example.kayc06.audipplayer.audioplayer;

public class AudioFocusPlayer implements AudioPlayer {

    private final AudioPlayer audioPlayer;
    private final AudioFocusProvider audioFocusProvider;

    private boolean playWhenReady = false;

    public AudioFocusPlayer(final AudioPlayer audioPlayer, AudioFocusProvider audioFocusProvider){
        this.audioPlayer = audioPlayer;
        this.audioFocusProvider = audioFocusProvider;

        audioFocusProvider.addChangeListener(new AudioFocusProvider.StateChangedListener() {
            @Override
            public void onStateChanged(AudioFocusProvider.State state) {
                switch (state) {
                    case GAINING:
                        break;
                    case GAINED:
                        if(playWhenReady){
                            audioPlayer.play();
                            playWhenReady = false;
                        }

                        if(audioPlayer.isDucked()){
                            audioPlayer.unduckVolume();
                        }
                        break;
                    case LOST_TRANSIENT:
                        audioPlayer.pause();
                        break;
                    case LOST_TRANSIENT_CAN_DUCKED:
                        audioPlayer.duckVolume();
                        break;
                    case LOST:
                        audioPlayer.stop();
                        break;
                }
            }
        });
    }

    @Override
    public void play() {
        AudioFocusProvider.State state = audioFocusProvider.getState();

        switch(state){
            case GAINING:
                playWhenReady = true;
                break;
            case GAINED:
                audioPlayer.play();
                break;
            case LOST_TRANSIENT:
                audioPlayer.pause();
                break;
            case LOST_TRANSIENT_CAN_DUCKED:
                audioPlayer.play();
            case LOST:
                playWhenReady = true;
                audioFocusProvider.requestAudioFocus();
                break;
        }
    }

    @Override
    public void pause() {
        playWhenReady = false;
        audioPlayer.pause();
    }

    @Override
    public void stop() {
        playWhenReady = false;
        audioPlayer.stop();
    }

    @Override
    public void release() {
        audioPlayer.release();
    }

    @Override
    public State getState() {
        return audioPlayer.getState();
    }

    @Override
    public void addStateChangedListener(StateChangedListener stateChangedListener) {
        addStateChangedListener(stateChangedListener);
    }

    @Override
    public boolean isDucked() {
        return audioPlayer.isDucked();
    }

    @Override
    public void duckVolume() {
        audioPlayer.duckVolume();
    }

    @Override
    public void unduckVolume() {
        audioPlayer.unduckVolume();
    }
}
