package org.tbadg.memory;

import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Ads {

    public final String TAG = "Ads";

    AdView adView = null;


    //
    // Constructor:
    //

    Ads(View adView) {

        this.adView = (AdView) adView;
    }

    //
    // Public methods:
    //

    public void showAd() {
        Log.v(TAG, "in showAd()");

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(){
                Log.v(TAG, "in onAdLoaded()");
            }

            public void onAdFailedToLoad(int errorCode){
                Log.v(TAG, "in onAdFailedToLoad()");
            }

            public void onAdOpened(){
                Log.v(TAG, "in onAdOpened()");
            }

            public void onAdClosed(){
                Log.v(TAG, "in onAdClosed()");
            }

            public void onAdLeftApplication(){
                Log.v(TAG, "in onAdLeftApplication()");
            }
          });

        AdRequest adRequest = new AdRequest.Builder().addTestDevice("my_id_num").build();
        adView.loadAd(adRequest);
    }

    public void pause() {
        Log.v(TAG, "in pause()");
        adView.pause();
    }

    public void resume() {
        Log.v(TAG, "in resume()");
        adView.resume();
    }

    public void destroy() {
        Log.v(TAG, "in destroy()");
        adView.pause();
        adView.destroy();
    }
}
