package org.tbadg.memory;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;


public class Card extends ImageView {
    public static final int CARD_FLIP_MSECS = 750;

    private static final String TAG = "Card";
    private static final int CARD_FLIP_DEGREES = 180;
    private static final int HALF_CARD_FLIP_DEGREES = CARD_FLIP_DEGREES / 2;
    private static final int HALF_CARD_FLIP_MSECS = CARD_FLIP_MSECS / 2;
    private static final int CARD_REMOVE_MSECS = 750;

    private Integer mValue;
    private int mImages[] = new int[MemoryActivity.MAX_MATCHES];
    private static boolean resourceLoadingFinished = false;

    // Animators used for flipping a card:
    ValueAnimator mStartFlip = null;
    ValueAnimator mFinishFlip = null;
    ValueAnimator mSwapCardImages = null;
    private int mCurrentImage;

    // Animator set used for removing a card:
    AnimatorSet mRemoveCardAnimSet = null;


    public Card(Context context) {
        super(context);
        setup(context);

        setScaleType(ScaleType.FIT_CENTER);
        setBackgroundResource(R.drawable.card_bg);
    }

    public void setup(Context context) {
        for (int x = 0; x < mImages.length; x++) {
            mImages[x] = getResources().getIdentifier("@drawable/card_" + String.valueOf(x), null,
                                                      context.getPackageName());
        }

        // Create animator used to start flipping a card. At the end of this, the card has
        //   been rotated halfway, showing it's edge, making the current card image disappear:
        mStartFlip = ObjectAnimator.ofFloat(this, "rotationY", 0, HALF_CARD_FLIP_DEGREES);
        mStartFlip.setDuration(HALF_CARD_FLIP_MSECS);
        mStartFlip.setInterpolator(new AccelerateInterpolator());

        // Create animator used to finish flipping a card. At the start, the card edge appears
        //   to be facing the user. Then the new card image is rotated into view until it is fully
        //   displayed.
        mFinishFlip = ObjectAnimator.ofFloat(this, "rotationY", -HALF_CARD_FLIP_DEGREES, 0);
        mFinishFlip.setDuration(HALF_CARD_FLIP_MSECS);
        mFinishFlip.setInterpolator(new DecelerateInterpolator());

        // Create animators to remove a card by shrinking it to nothing:
        ValueAnimator removeCardX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0f);
        ValueAnimator removeCardY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0f);

        // Create animator set used to remove a card:
        mRemoveCardAnimSet = new AnimatorSet();
        mRemoveCardAnimSet.setDuration(CARD_REMOVE_MSECS);
        mRemoveCardAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        mRemoveCardAnimSet.play(removeCardX).with(removeCardY);

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

    public void remove() {
        mRemoveCardAnimSet.start();
    }

    public void showBack() {
        setScaleX(1f);
        setScaleY(1f);
        setVisibility(View.VISIBLE);
        setImageResource(R.drawable.card_back);
    }

    public void showFront() {
        setScaleX(1f);
        setScaleY(1f);
        setVisibility(View.VISIBLE);
        Log.d(TAG, "Resource ID = " + mValue);
        setImageResource(mImages[mValue]);
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
        flipCard(mImages[mValue]);
    }

    private void flipCard(int image) {
        // Create a regressive "animator" that merely swaps the front-back card mImages:
        mSwapCardImages = ObjectAnimator.ofObject(this, "imageResource",
                                                  new ResourceIdEvaluator(), mCurrentImage, image);

        // Duration 0 seems to work fine to get a single frame, but might need to be a '1':
        mSwapCardImages.setDuration(0);
        mCurrentImage = image;

        // Create and mStartFlip an Animator set with the sequence: mStartFlip, mSwapCardImages, mFinishFlip:
        AnimatorSet flipCardAnim = new AnimatorSet();
        flipCardAnim.play(mStartFlip).before(mSwapCardImages);
        flipCardAnim.play(mFinishFlip).after(mSwapCardImages);
        flipCardAnim.start();
    }

    public class ResourceIdEvaluator implements TypeEvaluator<Integer> {
        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            // Always return the end value since we only expect to be run once:
            return endValue;
        }
    }
}
