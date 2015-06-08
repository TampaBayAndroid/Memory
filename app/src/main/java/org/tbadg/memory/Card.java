package org.tbadg.memory;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Card extends Button {

    private Integer mValue;
    private int images[] = new int[MemoryActivity.MAX_MATCHES];
    private static boolean resourceLoadingFinished = false;

    public Card(Context context) {
        super(context);
//        this.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        setup(context);
    }

    public void setup(Context context) {
        for (int x = 0; x < images.length; x++) {
            images[x] = getResources().getIdentifier("@drawable/card_" + String.valueOf(x), null,
                                                     context.getPackageName());
        }
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
        setText("");
        setBackgroundResource(R.drawable.card_back);
    }

    public void showFront() {
        setVisibility(View.VISIBLE);
        setText(String.valueOf(mValue));
        Log.e("Card", "Resource ID = " + mValue);
        setBackgroundResource(images[mValue]);
    }

    public boolean equals(Card other) {
        return mValue.equals(other.mValue);
    }

    static public boolean isResourceLoadingFinished() {
        return resourceLoadingFinished;
    }
}
