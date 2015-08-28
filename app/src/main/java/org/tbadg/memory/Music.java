package org.tbadg.memory;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

public class Music {

    public void play(Context context, int resourceId) {
        Uri uri = new Uri.Builder()
                .scheme("android.resource")
                .authority(context.getPackageName())
                .path(String.valueOf(resourceId))
                .build();
        play(context, uri);
    }

    public void play(Context context, Uri musicUri) {
        reset();

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            final float volumeLevel = currentVolumeLevel(context) * 0.15f;
            mediaPlayer.setVolume(volumeLevel, volumeLevel);
            mediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mediaPlayer.setOnErrorListener(mOnErrorListener);
            mediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(context, musicUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            Log.e("Music", "Failed to open the darn music uri", e);
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    public void resume() {
        if (mMediaPlayer == null) {
            return;
        }

        try {
            mMediaPlayer.start();
        } catch (Exception ignored) {
        }
    }

    public void stop() {
        reset();
    }

    public static boolean isResourceLoadingFinished() {
        return resourceLoadingFinished;
    }

    /*
     * Implementation below
     */

    private MediaPlayer mMediaPlayer;
    private static boolean resourceLoadingFinished = true;


    private void reset() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();
        mMediaPlayer = null;
    }

    private float currentVolumeLevel(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return maxVolume <= 0f ? 0f : actualVolume / maxVolume;
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayer = mp;
            mp.start();
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e("Music", "onError(): " + what + "  " + extra);
            // return true to avoid a pop-up
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            // as long as we loop the same media source, this should never be called
            Log.i("Music", "onCompletion()");
        }
    };
}
