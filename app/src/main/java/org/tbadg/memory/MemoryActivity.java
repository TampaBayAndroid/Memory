package org.tbadg.memory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    private static final int DEFAULT_NUM_ROWS = 3;
    private static final int DEFAULT_NUM_COLS = 5;
    private static final int CARDS_MATCHED_TIMEOUT_IN_MILLIS = 250;
    private static final int NO_MATCH_TIMEOUT_IN_MILLIS = 1000;


    private LinearLayout mCards;
    private Button mPopupBtn;
    private Button mFirstCard;
    private Button mSecondCard;

    private int mNumRows = DEFAULT_NUM_ROWS;
    private int mNumCols = DEFAULT_NUM_COLS;
    private int mPrevRows;
    private int mPrevCols;

    private boolean mEmptySpot;
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

        EditText rows = (EditText) menu.findItem(R.id.menu_rows)
                                       .getActionView().findViewById(R.id.rows);
        rows.setOnEditorActionListener(this);

        EditText cols = (EditText) menu.findItem(R.id.menu_cols)
                                        .getActionView().findViewById(R.id.cols);
        cols.setOnEditorActionListener(this);

        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

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
        int num = DEFAULT_NUM_ROWS;
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            try {
                num  = Integer.valueOf(v.getText().toString());
                if (num < 2)
                    num = 2;
                else if (num > 10)
                    num = 10;
                v.setText(String.valueOf(num));

            } catch (NumberFormatException e) {
                /* Shouldn't be able to get here */
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            if (v.getId() == R.id.rows)
                mNumRows = num;
            else if (v.getId() == R.id.cols)
                mNumCols = num;
        }

        return(true);
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

        // Is there an odd number of mCards, requiring an empty space?
        mEmptySpot = (mNumRows * mNumCols) % 2 == 1;

        // Create mNumRows rows of mCards:
        for (int i = 0; i < mNumRows; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            mCards.addView(row, params);

            // Create mNumCols mCards per row:
            for (int j = 0; j < mNumCols; j++) {
                Button card = getCard();
                row.addView(card, params);
                card.setOnTouchListener(cardOnTouchListener);
            }
        }
    }

    private void newGame() {
        if (mNumRows != mPrevRows || mNumCols != mPrevCols) {
            Log.d(TAG, String.format("Creating new %dx%d board ", mNumRows, mNumCols));
            createBoard();

            mPrevRows = mNumRows;
            mPrevCols = mNumCols;
        }

        mPopupBtn.setVisibility(View.INVISIBLE);

        // Reset the game state:
        mMatchesShown = mNumRows * mNumCols / 2;
        mFirstCard = null;
        mSecondCard = null;

        // Create a list with two copies of each possible card value. We'll randomly
        //  select and remove these values later to give them to the mCards.
        // e.g. 3 matches == { 0, 0, 1, 1, 2, 2 }:
        List<Integer> values = new ArrayList<>(mMatchesShown);
        for (int i = 0; i < mMatchesShown; i++) {
            values.add(i);
            values.add(i);
        }

        Log.e(TAG, "mNumRows=" + mNumRows);
        Log.e(TAG, "mNumCols=" + mNumCols);
        Log.e(TAG, "mMatchesShown=" + mMatchesShown);
        Log.e(TAG, "values.size()=" + values.size());

        // For each card:
        for (int i = mCards.getChildCount() - 1; i >= 0; --i) {
            LinearLayout row = (LinearLayout) mCards.getChildAt(i);

            for (int j = row.getChildCount() - 1; j >= 0; --j) {
                Button card = (Button) row.getChildAt(j);

                // Leave the bottom middle space empty if there's an odd number of mCards:
                if (mEmptySpot && i == mNumRows - 1 && j == mNumCols / 2) {
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
        synchronized public boolean onTouch(View v, MotionEvent event) {
            // Return true to consume the event, false to pass it on for further handling.

            // Ignore everything but up events:
            if (event.getAction() != MotionEvent.ACTION_DOWN)
                return true;

            // Don't allow the same or third card to be selected again:
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

                // Do mCards match?
                if (mFirstCard.getTag().equals(mSecondCard.getTag()))
                    doMatch();
                else
                    doNoMatch();
            }

            return true;
        }
    };

    private final Runnable hideCards = new Runnable() {
        @Override
        public void run() {
            // Hide selected mCards:
            hideCard(mFirstCard);
            hideCard(mSecondCard);

            mFirstCard = null;
            mSecondCard = null;
        }
    };

    private final Runnable flipCards = new Runnable() {
        @Override
        public void run() {
            // Flip selected mCards:
            showCardBack(mFirstCard);
            showCardBack(mSecondCard);

            mFirstCard = null;
            mSecondCard = null;
        }
    };
}
