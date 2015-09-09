package org.tbadg.memory;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

public class Card extends Button {

    final public String TAG = "Card";

    private Integer mValue;
    private int images[] = new int[MemoryActivity.MAX_MATCHES];
    private static boolean resourceLoadingFinished = false;

    ValueAnimator start = null;
    ValueAnimator finish = null;
    ValueAnimator swap = null;

    private int mCurrentImage;


    public Card(Context context) {
        super(context);
        setup(context);
    }

    public void setup(Context context) {
        for (int x = 0; x < images.length; x++) {
            images[x] = getResources().getIdentifier("@drawable/card_" + String.valueOf(x), null,
                                                     context.getPackageName());
        }

        // Create the animator used to start the card flipping. At the end of this, the card has
        //   been rotated halfway, showing it's edge, making the current card image disappear:
        start = ObjectAnimator.ofFloat(this, "rotationY", 0, 90); // --> setRotationY
        start.setDuration(500);
        start.setInterpolator(new AccelerateInterpolator());

        // Create the animator used to finish the card flipping. At the start, the card edge appears
        //   to be facing the user. Then the new card image is rotated into view until it is fully
        //   displayed.
        finish = ObjectAnimator.ofFloat(this, "rotationY", -90, 0);
        finish.setDuration(500);
        finish.setInterpolator(new DecelerateInterpolator());

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
        // Create a regressive "animator" that merely swaps the front-back card images:
        swap = ObjectAnimator.ofObject(this, "backgroundResource",
                                       new ResourceIdEvaluator(), mCurrentImage, image);

        // Duration 0 seems to work fine to get a single frame, but might need to be a '1':
        swap.setDuration(0);
        mCurrentImage = image;

        // Create and start an Animator set with the sequence: start, swap, finish:
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(start).before(swap);
        animatorSet.play(finish).after(swap);
        animatorSet.start();
    }

    public class ResourceIdEvaluator implements TypeEvaluator<Integer> {
        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            // Always return the end value since we only expect to be run once:
            return endValue;
        }
    }
}
