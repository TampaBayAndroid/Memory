package org.tbadg.memory;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@SuppressWarnings("FieldCanBeLocal")
public class MemoryActivity extends ActionBarActivity {

    private static int NUM_MATCHES = 8;
    private static int NUM_CARDS_PER_ROW = 4;
    private static int SHOW_CARDS_TIMEOUT_IN_MILLIS = 1000;

    private Button mPopupBtn;
    private Button mFirstCard;
    private Button mSecondCard;
    private int mMatchesShown;
    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        // Clicking the popup or newGame buttons should start a new game
        mPopupBtn = (Button) findViewById(R.id.popup);
        mPopupBtn.setOnClickListener(newGameOnClickListener);
        Button newGameBtn = (Button) findViewById(R.id.new_game);
        newGameBtn.setOnClickListener(newGameOnClickListener);

        createBoard();
        newGame();
    }

    private Button getCard() {
        return new Button(this);
    }

    private void createBoard() {
        LinearLayout cards = (LinearLayout) findViewById(R.id.cards);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1.0f);
        // Create 2 * NUM_MATCHES cards and put them in rows
        for (int i = 0; i < 2 * NUM_MATCHES; ) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            cards.addView(row, params);
            for (int j = 0; j < NUM_CARDS_PER_ROW; j++, i++) {
                Button button = getCard();
                row.addView(button, params);
                button.setOnTouchListener(onTouchListener);
                button.setOnClickListener(onClickListener);
            }
        }
    }

    private void newGame() {
        mPopupBtn.setVisibility(View.INVISIBLE);

        // Create a list of all possible card values. We'll randomly select and
        // remove these values later to give them to the cards.
        // e.g. 3 matches == { 0, 0, 1, 1, 2, 2 }
        List<Integer> values = new ArrayList<>(2 * NUM_MATCHES);
        for (int i = 0; i < NUM_MATCHES; i++) {
            values.add(i);
            values.add(i);
        }

        // for each card in each row, make it visible, make it blank, and give
        // it a value
        LinearLayout cards = (LinearLayout) findViewById(R.id.cards);
        for (int i = cards.getChildCount() - 1; i >= 0; --i) {
            LinearLayout row = (LinearLayout) cards.getChildAt(i);
            for (int j = row.getChildCount() - 1; j >= 0; --j) {
                Button button = (Button) row.getChildAt(j);
                button.setVisibility(View.VISIBLE);
                button.setText("");
                int index = mRandom.nextInt(values.size());
                button.setTag("" + values.get(index));
                values.remove(index);
            }
        }

        // Reset the game state
        mMatchesShown = NUM_MATCHES;
        mFirstCard = null;
        mSecondCard = null;
    }

    private View.OnClickListener newGameOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newGame();
                }
            };

    private Runnable hideCardsRunnable = new Runnable() {
        @Override
        public void run() {
            // Was newGame() called just before this?
            if (mFirstCard == null)
                return;
            if (mFirstCard.getText().equals(mSecondCard.getText())) {
                // Match
                mFirstCard.setVisibility(View.INVISIBLE);
                mSecondCard.setVisibility(View.INVISIBLE);
                --mMatchesShown;
            }
            mFirstCard.setText("");
            mSecondCard.setText("");
            mFirstCard = null;
            mSecondCard = null;

            if (mMatchesShown <= 0)
                mPopupBtn.setVisibility(View.VISIBLE);
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // return true to ignore touch events
            // Can't click mFirstCard more than once
            if (v.equals(mFirstCard))
                return true;
            // Can't click more than two buttons at a time
            return mFirstCard != null && mSecondCard != null;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            // When a card is clicked, reveal it
            button.setText((String) button.getTag());
            // Is this the first card?
            if (mFirstCard == null) {
                mFirstCard = button;
                return;
            }
            // This is the second card, leave it visible for a while
            // and then compare and handle the two cards
            mSecondCard = button;
            new Handler().postDelayed(hideCardsRunnable,
                    SHOW_CARDS_TIMEOUT_IN_MILLIS);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_memory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
