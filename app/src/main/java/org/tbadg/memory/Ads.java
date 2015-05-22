package org.tbadg.memory;

import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Ads {

    public final String TAG = "Ads";

    AdView mAdView = null;


    //
    // Constructor:
    //

    Ads(View adView) {
        mAdView = (AdView) adView;
    }

    //
    // Public methods:
    //

    public void showAd() {
        Log.v(TAG, "in showAd()");

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.v(TAG, "in onAdLoaded()");
            }

            public void onAdFailedToLoad(int errorCode) {
                Log.v(TAG, "in onAdFailedToLoad()");
            }

            public void onAdOpened() {
                Log.v(TAG, "in onAdOpened()");
            }

            public void onAdClosed() {
                Log.v(TAG, "in onAdClosed()");
            }

            public void onAdLeftApplication() {
                Log.v(TAG, "in onAdLeftApplication()");
            }
        });

        AdRequest adRequest = new AdRequest.Builder().addTestDevice("my_id_num").build();
        mAdView.loadAd(adRequest);
    }

    public void pause() {
        Log.v(TAG, "in pause()");
        mAdView.pause();
    }

    public void resume() {
        Log.v(TAG, "in resume()");
        mAdView.resume();
    }

    public void destroy() {
        Log.v(TAG, "in destroy()");
        mAdView.pause();
        mAdView.destroy();
    }
}
