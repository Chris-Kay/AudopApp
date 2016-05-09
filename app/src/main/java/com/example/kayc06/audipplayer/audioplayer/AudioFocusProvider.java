package com.example.kayc06.audipplayer.audioplayer;

public interface AudioFocusProvider {

    enum State {
        GAINING,
        GAINED,
        LOST_TRANSIENT,
        LOST_TRANSIENT_CAN_DUCKED,
        LOST,
    }

    State getState();
    interface StateChangedListener {
        void onStateChanged(State state);
    }
    void addChangeListener(StateChangedListener stateChangedListener);

    boolean requestAudioFocus();
}
