package org.tbadg.memory;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class Card extends Button {

    final public String TAG = "Card";

    private Integer mValue;
    private int images[] = new int[MemoryActivity.MAX_MATCHES];
    private static boolean resourceLoadingFinished = false;

    private Animation mStartFlip;
    private Animation mFinishFlip;
    private int mNextImage;


    public Card(Context context) {
        super(context);
        setup(context);
    }

    public void setup(Context context) {
        for (int x = 0; x < images.length; x++) {
            images[x] = getResources().getIdentifier("@drawable/card_" + String.valueOf(x), null,
                                                     context.getPackageName());
        }

        mStartFlip = AnimationUtils.loadAnimation(getContext(), R.anim.flip_out);
        mStartFlip.setAnimationListener(mAnimationListener);
        mFinishFlip = AnimationUtils.loadAnimation(getContext(), R.anim.flip_in);
        mFinishFlip.setAnimationListener(mAnimationListener);

        resourceLoadingFinished = true;
    }


    public void setValue(Integer value) {
        mValue = value;
    }

    public Integer getValue() {
        return mValue;
    }

    public void hide() {
        setVisibility(View.INVISIBLE);

        // Without this, the card will sometimes stay visible:
        requestLayout();
    }

    public void showBack() {
        setVisibility(View.VISIBLE);
//        setText("");
        setBackgroundResource(R.drawable.card_back);
    }

    public void showFront() {
        setVisibility(View.VISIBLE);
//        setText(String.valueOf(mValue));
        Log.d(TAG, "Resource ID = " + mValue);
        setBackgroundResource(images[mValue]);
    }

    public boolean equals(Card other) {
        return mValue.equals(other.mValue);
    }

    static public boolean isResourceLoadingFinished() {
        return resourceLoadingFinished;
    }

    public void flipToBack() {
        Log.d(TAG, String.format("Flipping card %d to back", mValue));
        flipCard(R.drawable.card_back);
    }

    public void flipToFront() {
        Log.d(TAG, String.format("Flipping card %d to front", mValue));
        flipCard(images[mValue]);
    }

    private void flipCard(int image) {
        mNextImage = image;

        clearAnimation();
        setAnimation(mStartFlip);
        startAnimation(mStartFlip);
    }

    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            if (animation == mStartFlip) {
                Log.v(TAG, "start of flip finished");

                Card.this.setBackgroundResource(mNextImage);
                Card.this.clearAnimation();
                Card.this.setAnimation(mFinishFlip);
                Card.this.startAnimation(mFinishFlip);
            }
        }

        @Override
        public void onAnimationStart(Animation animation) { }

        @Override
        public void onAnimationRepeat(Animation animation) { }
    };
}
