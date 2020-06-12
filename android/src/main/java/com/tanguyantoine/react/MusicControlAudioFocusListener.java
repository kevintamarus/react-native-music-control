package com.tanguyantoine.react;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;



public class MusicControlAudioFocusListener implements AudioManager.OnAudioFocusChangeListener {
    private final MusicControlEventEmitter emitter;
    private final MusicControlVolumeListener volume;

    private static final String TAG = "WonderyDebug";
    private AudioManager mAudioManager;
    private AudioFocusRequest mFocusRequest;

    private boolean mPlayOnAudioFocus = false;

    MusicControlAudioFocusListener(ReactApplicationContext context, MusicControlEventEmitter emitter,
                                   MusicControlVolumeListener volume) {
        this.emitter = emitter;
        this.volume = volume;

        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "hitting audio focus change" + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            Log.d(TAG, "audioFocusChange loss");
            abandonAudioFocus();
            mPlayOnAudioFocus = false;
            // emitter.onStop();
            emitter.onPause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            Log.d(TAG, "audioFocusChange Loss Transient");
            if (MusicControlModule.INSTANCE.isPlaying()) {
                mPlayOnAudioFocus = true;
                emitter.onPause();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            Log.d(TAG, "audioFocusChange Loss Transient Can Duck");
            volume.setCurrentVolume(40);
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (volume.getCurrentVolume() != 100) {
                volume.setCurrentVolume(100);
            }
            // if (mPlayOnAudioFocus) {
                Log.d(TAG, "audioFocusChange gain and play");
                requestAudioFocus();
                emitter.onPlay();
            // }
            mPlayOnAudioFocus = false;
        } else {
        Log.d(TAG, "audioFocusChange bypass");
        }
    }

//     public void checkAudioFocus() {
//     AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);    

// if (mAudioManager.isMusicActive()) {

//     Intent i = new Intent("com.android.music.musicservicecommand");

//     i.putExtra("command", "pause");
//     YourApplicationClass.this.sendBroadcast(i);
// } 
// }

    public void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "request audio focus if");

            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .build();

            mAudioManager.requestAudioFocus(mFocusRequest);
        } else {
            Log.d(TAG, "requesting audio focus else");
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioManager != null) {
            Log.d(TAG, "abandoning audio focus if");
            mAudioManager.abandonAudioFocusRequest(mFocusRequest);
        } else if ( mAudioManager != null ) {
            Log.d(TAG, "abandoning audio focus else");
            mAudioManager.abandonAudioFocus(this);
        }
    }
}
