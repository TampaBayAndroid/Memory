package org.tbadg.memory;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board extends LinearLayout {

    private static final String TAG = "Board";

    private static final int DEFAULT_NUM_MATCHES = 2;
    private static final int CARDS_MATCHED_TIMEOUT_IN_MILLIS = 250;
    private static final int NO_MATCH_TIMEOUT_IN_MILLIS = 1000;
    private static final int WINNER_NOTIFICATION_DELAY_IN_MILLIS = 500;

    private int mNumMatches;
    private Runnable mOnWinnerRunnable;

    private int mNumRows;
    private int mNumCols;
    private int mEmptySpots;

    private int mMatchesShown;
    private Card mFirstCard;
    private Card mSecondCard;

    private int mGuessesMade;
//    private int mMatchesMade;
    private long mStartTime;

    private final Random mRandom = new Random();
    private SoundsEffects mSoundsEffects = null;

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setup(SoundsEffects soundsEffects, Runnable onWinnerRunnable) {
        mSoundsEffects = soundsEffects;
        mOnWinnerRunnable = onWinnerRunnable;
        setNumberOfMatches(DEFAULT_NUM_MATCHES);
    }

    public int getNumberOfMatches() {
        return mNumMatches;
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    public void setNumberOfMatches(int numberOfMatches) {
        if (numberOfMatches == mNumMatches)
            return;

        if (numberOfMatches < 2 || numberOfMatches > 24)
            throw new IllegalArgumentException("Number of matches must be between 2 and 24 inclusive.");

        mNumMatches = numberOfMatches;
        removeAllViews();
        setupDimensions();

        LinearLayout.LayoutParams params
                = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setOrientation(LinearLayout.HORIZONTAL);
        else
            setOrientation(LinearLayout.VERTICAL);

        Log.d(TAG, String.format("Building a %d x %d board", mNumRows, mNumCols));

        // Are there too few cards to fill the board?
        mEmptySpots = (mNumRows * mNumCols) - (mNumMatches * 2);

        // Create mNumRows rows of cards:
        for (int i = 0; i < mNumRows; i++) {
            LinearLayout row = new LinearLayout(getContext());
            //noinspection ResourceType
            row.setOrientation(otherOrientation(this.getOrientation()));
            addView(row, params);

            // Create mNumCols cards per row:
            for (int j = 0; j < mNumCols; j++) {
                Card card = new Card(getContext());
                row.addView(card, params);
                card.setOnTouchListener(cardOnTouchListener);
            }
        }

        reset();
    }

    public void reset() {
        Log.d(TAG, String.format("Starting new game for %d matches", mNumMatches));

        // Reset the game state:
        mMatchesShown = mNumMatches;
        mFirstCard = null;
        mSecondCard = null;
        mGuessesMade = 0;
//        mMatchesMade = 0;

        // Create a list with two copies of each possible card value. We'll randomly
        //  select and remove these values later to give them to the cards.
        // e.g. 3 matches == { 0, 0, 1, 1, 2, 2 }:
        List<Integer> values = new ArrayList<>(mMatchesShown);
        for (int i = 0; i < mMatchesShown; i++) {
            values.add(i);
            values.add(i);
        }

        // For each card:
        for (int i = getChildCount() - 1; i >= 0; --i) {
            LinearLayout row = (LinearLayout) getChildAt(i);

            for (int j = row.getChildCount() - 1; j >= 0; --j) {
                Card card = (Card) row.getChildAt(j);

                // Hide two corners or the middle space if empty spots are required:
                if ((mEmptySpots == 2 && j == mNumCols - 1 && (i == 0 || i == mNumRows - 1))
                        || (mEmptySpots == 1 && i == mNumRows / 2 && j == mNumCols / 2)) {
                    card.hide();

                } else {
                    int index = mRandom.nextInt(values.size());
                    card.setValue(values.get(index));
                    values.remove(index);

                    card.showBack();
                }
            }
        }

        mSoundsEffects.play(SoundsEffects.Type.START);
    }

    @SuppressWarnings("ResourceType")
    public void flipOrientation() {
        Log.d(TAG, "Flipping board");

        LinearLayout board = (LinearLayout) findViewById(R.id.board);

        int childCount = board.getChildCount();
        LinearLayout rows[] = new LinearLayout[childCount];
        for (int i = 0; i < childCount; i++)
            rows[i] = (LinearLayout) board.getChildAt(i);

        int orientation = this.getOrientation();
        board.removeAllViews();
        board.setOrientation(otherOrientation(orientation));

        for (int i = childCount - 1; i >= 0; i--) {
            rows[i].setOrientation(orientation);
            board.addView(rows[i]);
        }
    }

        public ContentValues getResult() {
        ContentValues cv = new ContentValues();

        cv.put(DatabaseHelper.SCORE,
               mGuessesMade > 0 ? (int) (150.0 * mNumMatches / mGuessesMade + 0.5) : 0);
        cv.put(DatabaseHelper.MATCHES, mNumMatches);
        cv.put(DatabaseHelper.GUESSES, mGuessesMade);
        cv.put(DatabaseHelper.ELAPSED_TIME, (System.currentTimeMillis() - mStartTime + 500) / 1000);

        return cv;
    }

    private int otherOrientation(int orientation) {
        if (orientation == LinearLayout.VERTICAL)
            return LinearLayout.HORIZONTAL;
        else
            return LinearLayout.VERTICAL;
    }

    private void setupDimensions() {
        // Returns a pair of dimensions for 2-24 matches. It is the caller's responsibility
        // to ensure that the input number is within the acceptable range.

        // The orientation of the results is tied to the current orientation of the device.

        final int[][] boardSizes = {
                // Boards for 4, 7, 17, and 22 matches will have 1 spot empty
                // Boards for 5, 11, 13, 19, and 23 matches will have 2 spots empty

                {2, 2}, {2, 3}, {3, 3}, {3, 4}, {3, 4}, {3, 5}, {4, 4}, {3, 6},  //  2 -  9 matches
                {4, 5}, {4, 6}, {4, 6}, {4, 7}, {4, 7}, {5, 6}, {4, 8}, {5, 7},  // 10 - 17 matches
                {6, 6}, {5, 8}, {5, 8}, {6, 7}, {5, 9}, {6, 8}, {6, 8}};         // 18 - 24 matches

        mNumRows = boardSizes[mNumMatches - 2][0];
        mNumCols = boardSizes[mNumMatches - 2][1];
    }


    //
    // Game methods
    //

    private void doMatch() {
//        mMatchesMade++;
        postDelayed(hideCards, CARDS_MATCHED_TIMEOUT_IN_MILLIS);
        mSoundsEffects.play(SoundsEffects.Type.MATCH);

        if (--mMatchesShown <= 0 && mOnWinnerRunnable != null)
            doWin();
    }

    private void doNoMatch() {
        postDelayed(flipCards, NO_MATCH_TIMEOUT_IN_MILLIS);
        mSoundsEffects.play(SoundsEffects.Type.NO_MATCH);
    }

    private void doWin() {
        postDelayed(showWin, WINNER_NOTIFICATION_DELAY_IN_MILLIS);
    }
    //
    // Listeners and runnables:
    //

    private final View.OnTouchListener cardOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Return true to consume the event, false to pass it on for further handling.

            // Ignore everything but down events
            if (event.getAction() != MotionEvent.ACTION_DOWN)
                return true;

            // Don't allow the same card or more than two cards
            if (v.equals(mFirstCard) || mSecondCard != null)
                return true;

            Card card = (Card) v;
            card.flipToFront();
            mSoundsEffects.play(SoundsEffects.Type.FLIP);

            if (mFirstCard == null) {
                Log.d(TAG, "First card is " + card.getTag());
                mFirstCard = card;

                if (mGuessesMade == 0)
                    mStartTime = System.currentTimeMillis();

                return true;
            }

            Log.d(TAG, "Second card is " + card.getTag());
            mSecondCard = card;
            handleMatch(mFirstCard, mSecondCard);

            return true;
        }
    };

    private void handleMatch(Card firstCard, Card secondCard) {
        mGuessesMade++;

        if (firstCard.equals(secondCard))
            doMatch();
        else
            doNoMatch();
    }

    private final Runnable hideCards = new Runnable() {
        @Override
        public void run() {
            // Hide selected cards
            mFirstCard.hide();
            mSecondCard.hide();

            mFirstCard = null;
            mSecondCard = null;
        }
    };

    private final Runnable flipCards = new Runnable() {
        @Override
        public void run() {
            // Flip selected cards
            mFirstCard.flipToBack();
            mSecondCard.flipToBack();

            mFirstCard = null;
            mSecondCard = null;
        }
    };

    private final Runnable showWin = new Runnable() {
        @Override
        public void run() {
            mOnWinnerRunnable.run();
            mSoundsEffects.play(SoundsEffects.Type.WIN);
        }
    };
}
