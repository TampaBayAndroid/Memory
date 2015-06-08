package org.tbadg.memory;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;

public class SoundsEffects {

    public static int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    public enum Type {
        FLIP,
        NO_MATCH,
        MATCH,
        WIN,
        START
    }

    public SoundsEffects(Context context) {
        mContext = context;
        setup();
    }

    public void play(Type type) {
        if (!isLoaded(type))
            return;

        // TODO: no failure detection (== 0)
        mSoundPool.play(soundIdForType(type), mVolume, mVolume, NORMAL_PRIORITY, NO_LOOP, NORMAL_SPEED);
    }

    public void playLoop(Type type) {
        if (!isLoaded(type))
            return;

        // TODO: no failure detection (== 0)
        mSoundPool.play(soundIdForType(type), mVolume, mVolume, NORMAL_PRIORITY, LOOP_FOREVER, NORMAL_SPEED);
    }

    public void pause(Type type) {
        mSoundPool.pause(soundIdForType(type));
    }

    public void stop(Type type) {
        mSoundPool.stop(soundIdForType(type));
    }

    public static boolean isResourceLoadingFinished() {
        return resourceLoadingFinished;
    }

    /**
     * ********** Implementation below ************
     */


    private final int MAX_SIMULTANEOUS_SOUNDS = 3;

    private final int NO_LOOP = 0;
    private final int LOOP_FOREVER = -1;
    private final float NORMAL_SPEED = 1f;
    private final int NORMAL_PRIORITY = 1;

    private SoundPool mSoundPool;
    private float mActualVolume;
    private float mMaxVolume;
    private float mVolume;
    private AudioManager mAudioManager;

    private HashMap<Type, Integer> mTypeToSoundIdMap = new HashMap<>();
    private HashMap<Integer, Boolean> mTypeIsLoadedMap = new HashMap<>();

    private int soundsLoaded = 0;
    private static boolean resourceLoadingFinished = false;

    private final Context mContext;

    private void setup() {
        // AudioManager audio settings for adjusting the volume
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mActualVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = mActualVolume / mMaxVolume;

        // Load the sounds
        int srcQualityNotUsed = 0;
        mSoundPool = new SoundPool(MAX_SIMULTANEOUS_SOUNDS, AUDIO_STREAM_TYPE, srcQualityNotUsed);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
                final int SUCCESS = 0;
                if (status == SUCCESS)
                    mTypeIsLoadedMap.put(soundId, true);

                if (++soundsLoaded >= Type.values().length)
                    resourceLoadingFinished = true;

                Log.d("SE", String.format("soundsLoaded=%d, values=%s, Type.values().length= %d, resourceLoadingFinished=%b",
                                          soundsLoaded, Type.values(), Type.values().length, resourceLoadingFinished));
            }
        });

        loadSound(Type.FLIP, R.raw.flip);
        loadSound(Type.NO_MATCH, R.raw.no_match);
        loadSound(Type.MATCH, R.raw.match);
        loadSound(Type.WIN, R.raw.win);
        loadSound(Type.START, R.raw.start);
    }

    private void loadSound(Type type, int resourceId) {
        int priorityNotUsed = 1;
        int soundId = mSoundPool.load(mContext, resourceId, priorityNotUsed);
        mTypeToSoundIdMap.put(type, soundId);
        mTypeIsLoadedMap.put(soundId, false);
    }

    private boolean isLoaded(Type type) {
        return mTypeIsLoadedMap.get(mTypeToSoundIdMap.get(type));
    }

    private int soundIdForType(Type type) {
        return mTypeToSoundIdMap.get(type);
    }
}
