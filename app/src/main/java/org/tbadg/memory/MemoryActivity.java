package org.tbadg.memory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MemoryActivity extends Activity implements TextView.OnEditorActionListener {
    private static final String TAG = "MemoryActivity";

    private static final int DEFAULT_NUM_MATCHES = 8;
    private static final int CARDS_MATCHED_TIMEOUT_IN_MILLIS = 250;
    private static final int NO_MATCH_TIMEOUT_IN_MILLIS = 1000;


    private LinearLayout mCards;
    private Button mPopupBtn;
    private Button mFirstCard;
    private Button mSecondCard;

    private int mNumMatches = DEFAULT_NUM_MATCHES;
    private int mNumRows;
    private int mNumCols;
    private int mPrevRows;
    private int mPrevCols;

    private int mEmptySpots;
    private int mMatchesShown;

    private final Random mRandom = new Random();


    //
    // Life-cycle methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        // Clicking the popup or newGame buttons starts a new game:
        mPopupBtn = (Button) findViewById(R.id.popup);
        newGame();
    }


    //
    // Action bar and menu related methods
    //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        EditText matches = (EditText) menu.findItem(R.id.menu_matches)
                                       .getActionView().findViewById(R.id.matches);
        matches.setOnEditorActionListener(this);
        matches.setText(String.valueOf(mNumMatches));

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.menu_new:
                newGame();
                break;

            case R.id.menu_about:
                handleAbout();
                break;

            case R.id.menu_help:
                handleHelp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v.getId() == R.id.matches) {
            int matches = DEFAULT_NUM_MATCHES;

            boolean keyEventEnterUp = actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    && event.getAction() == KeyEvent.ACTION_UP
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (actionId == EditorInfo.IME_ACTION_DONE || keyEventEnterUp) {
                try {
                    matches = Integer.valueOf(v.getText().toString());
                    if (matches < 2)
                        matches = 2;
                    else if (matches > 24)
                        matches = 24;
                    v.setText(String.valueOf(matches));

                } catch (NumberFormatException e) {
                    /* Shouldn't be able to get here */
                }

                InputMethodManager imm
                        = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                mNumMatches = matches;
                newGame();

                v.clearFocus();
            }

            return (true);
        }

        return false;
    }

    private void handleAbout() {

        String version = null;

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_about))
                .setMessage(getString(R.string.msg_about) + version)
                .setIcon(R.drawable.ic_action_about);

        dialog.show();
    }

    private void handleHelp() {
        WebView help = new WebView(this);
        help.loadUrl(getString(R.string.url_help));

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setView(help)
                .setTitle(getString(R.string.title_help))
                .setIcon(R.drawable.ic_action_help);

        dialog.show();
    }


    //
    // Game methods
    //

    @SuppressWarnings("ObjectAllocationInLoop")
    private void createBoard() {
        mCards = (LinearLayout) findViewById(R.id.cards);
        mCards.removeAllViews();

        LinearLayout.LayoutParams params
                = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        Log.d(TAG, String.format("Building a %d x %d board", mNumRows, mNumCols));

        // Are there too few cards to fill the board?
        mEmptySpots = (mNumRows * mNumCols) - (mNumMatches * 2);

        // Create mNumRows rows of cards:
        for (int i = 0; i < mNumRows; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            mCards.addView(row, params);

            // Create mNumCols cards per row:
            for (int j = 0; j < mNumCols; j++) {
                Button card = getCard();
                row.addView(card, params);
                card.setOnTouchListener(cardOnTouchListener);
            }
        }
    }

    private void newGame() {
        Log.d(TAG, String.format("Starting new game for %d matches", mNumMatches));

        Pair<Integer, Integer> dims = getDimensions(mNumMatches);
        mNumRows = dims.first;
        mNumCols = dims.second;

        if (mNumRows != mPrevRows || mNumCols != mPrevCols) {
            mPrevRows = mNumRows;
            mPrevCols = mNumCols;
            createBoard();
        }

        mPopupBtn.setVisibility(View.INVISIBLE);

        // Reset the game state:
        mMatchesShown = mNumMatches;
        mFirstCard = null;
        mSecondCard = null;

        // Create a list with two copies of each possible card value. We'll randomly
        //  select and remove these values later to give them to the cards.
        // e.g. 3 matches == { 0, 0, 1, 1, 2, 2 }:
        List<Integer> values = new ArrayList<>(mMatchesShown);
        for (int i = 0; i < mMatchesShown; i++) {
            values.add(i);
            values.add(i);
        }

        // For each card:
        for (int i = mCards.getChildCount() - 1; i >= 0; --i) {
            LinearLayout row = (LinearLayout) mCards.getChildAt(i);

            for (int j = row.getChildCount() - 1; j >= 0; --j) {
                Button card = (Button) row.getChildAt(j);

                // Hide the bottom corners or the middle space if empty spots are required:
                if ((mEmptySpots == 2 && i == mNumRows - 1 && (j == 0 || j == mNumCols - 1))
                        || (mEmptySpots == 1 && i == mNumRows / 2 && j == mNumCols / 2)) {
                    hideCard(card);

                } else {
                    int index = mRandom.nextInt(values.size());
                    card.setTag("" + values.get(index));
                    values.remove(index);

                    showCardBack(card);
                }
            }
        }
    }

    private Button getCard() {
        return new Button(this);
    }

    private void showCardBack(Button card) {
        card.setVisibility(View.VISIBLE);
        card.setText("");
    }

    private void showCardFront(Button card) {
        card.setVisibility(View.VISIBLE);
        card.setText((String) card.getTag());
    }

    private void hideCard(Button card) {
        card.setVisibility(View.INVISIBLE);

        // Without this, the card will sometimes stay visible:
        card.requestLayout();
    }

    private void doMatch() {
        new Handler().postDelayed(hideCards, CARDS_MATCHED_TIMEOUT_IN_MILLIS);

        if (--mMatchesShown <= 0)
            mPopupBtn.setVisibility(View.VISIBLE);
    }

    private void doNoMatch() {
        new Handler().postDelayed(flipCards, NO_MATCH_TIMEOUT_IN_MILLIS);
    }

    private Pair<Integer, Integer> getDimensions(int numMatches) {
        // Returns a pair of dimensions for 2-24 matches. It is the caller's responsibility
        // to ensure that the input number is within the acceptable range.

        // The orientation of the results is tied to the current orientation of the device.

        final int[][] boardSizes= {
                // Boards for 4, 7, 17, and 22 matches will have 1 spot empty
                // Boards for 5, 11, 13, 19, and 23 matches will have 2 spots empty

                {2, 2}, {3, 2}, {3, 3}, {4, 3}, {4, 3}, {5, 3}, {4, 4}, {6, 3},  //  2 -  9 matches
                {5, 4}, {6, 4}, {6, 4}, {7, 4}, {7, 4}, {6, 5}, {8, 4}, {7, 5},  // 10 - 17 matches
                {6, 6}, {8, 5}, {8, 5}, {7, 6}, {9, 5}, {8, 6}, {8, 6}};         // 18 - 24 matches

//        if (numMatches < 2 || numMatches > 24)
//            return new Pair<>(-1, -1);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return new Pair<>(boardSizes[numMatches-2][0], boardSizes[numMatches-2][1]);
        else
            return new Pair<>(boardSizes[numMatches-2][1], boardSizes[numMatches-2][0]);
    }

    //
    // Listeners and runnables:
    //

    @SuppressWarnings("UnusedParameters")
    public void doNewGame(View v) {
        newGame();
    }

    @SuppressWarnings("CanBeFinal")
    private View.OnTouchListener cardOnTouchListener = new View.OnTouchListener() {
        @Override
        // TODO: synchronized?
        synchronized public boolean onTouch(View v, MotionEvent event) {
            // Return true to consume the event, false to pass it on for further handling.

            // Ignore everything but down events
            if (event.getAction() != MotionEvent.ACTION_DOWN)
                return true;

            // Don't allow the same card or more than two cards
            if (v.equals(mFirstCard) || mSecondCard != null)
                return true;

            Button card = (Button) v;
            showCardFront(card);

            if (mFirstCard == null) {
                Log.d(TAG, "First card is " + card.getTag());
                mFirstCard = card;

            } else {
                Log.d(TAG, "Second card is " + card.getTag());
                mSecondCard = card;
                handleMatch(mFirstCard, mSecondCard);
            }

            return true;
        }
    };

    private void handleMatch(Button firstCard, Button secondCard) {
        String first = (String) firstCard.getTag();
        String second = (String) secondCard.getTag();
        if (first.equals(second))
            doMatch();
        else
            doNoMatch();
    }

    private final Runnable hideCards = new Runnable() {
        @Override
        public void run() {
            // Hide selected cards
            hideCard(mFirstCard);
            hideCard(mSecondCard);

            mFirstCard = null;
            mSecondCard = null;
        }
    };

    private final Runnable flipCards = new Runnable() {
        @Override
        public void run() {
            // Flip selected cards
            showCardBack(mFirstCard);
            showCardBack(mSecondCard);

            mFirstCard = null;
            mSecondCard = null;
        }
    };

}
