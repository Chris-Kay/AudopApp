package com.example.kayc06.audipplayer.audioplayer;

public interface AudioPlaybackStateProvider {

    enum State {
        CREATED,
        PLAYING,
        PAUSED,
        STOPPED,
        DESTROYED
    }

    interface StateChangedListener {
        void onStateChanged(State state);
    }

    State getState();
    void addStateChangedListener(StateChangedListener stateChangedListener);

}
