package org.tbadg.memory;

import android.content.Context;
import android.view.View;
import android.widget.Button;

public class Card extends Button {

    private Integer mValue;

    public Card(Context context) {
        super(context);
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
    }

    public void showFront() {
        setVisibility(View.VISIBLE);
        setText(String.valueOf(mValue));
    }

    public boolean equals(Card other) {
        return mValue.equals(other.mValue);
    }
}
