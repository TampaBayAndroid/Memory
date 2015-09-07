package org.tbadg.memory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class MemoryActivity extends Activity implements TextView.OnEditorActionListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MemoryActivity";
    public static final int MAX_MATCHES = 24;

    private Board mBoard;
    private Button mPopupBtn;
    private ImageView mSplashImg;

    @SuppressWarnings("FieldCanBeLocal")
    private SoundsEffects mSoundsEffects;
    private Music mMusic;

    private int mPrevOrientation = -1;
    private Ads mAds = null;
    private DatabaseHelper mDb = null;

    private ScoresFragment mScores = null;
    //
    // Life-cycle methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        mDb = new DatabaseHelper(this);

        mAds = new Ads(findViewById(R.id.adView));
        mAds.showAd();

        setVolumeControlStream(SoundsEffects.AUDIO_STREAM_TYPE);
        mSoundsEffects = new SoundsEffects(this);

        mMusic = new Music();
        mMusic.play(this, R.raw.music);

        // Clicking the popup or newGame buttons starts a new game:
        mPopupBtn = (Button) findViewById(R.id.popup);
        mSplashImg = (ImageView) findViewById(R.id.splash);
        mBoard = (Board) findViewById(R.id.board);
        mBoard.setup(mSoundsEffects, mOnWinnerRunnable);
        newGame();

        new WaitForResourcesRunnable().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAds != null)
            mAds.resume();
        mMusic.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAds != null)
            mAds.pause();
        mMusic.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMusic.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDb.close();

        if (mAds != null)
            mAds.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_UNDEFINED
                || newConfig.orientation == mPrevOrientation)
            return;

        mPrevOrientation = newConfig.orientation;
        mBoard.flipOrientation();
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
        matches.setText(String.valueOf(mBoard.getNumberOfMatches()));

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

            case R.id.menu_scores:
                handleScores();
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
        if (v.getId() != R.id.matches)
            return false;

        int matches = mBoard.getNumberOfMatches();

        boolean keyEventEnterUp = actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                && event.getAction() == KeyEvent.ACTION_UP
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
        if (!(actionId == EditorInfo.IME_ACTION_DONE || keyEventEnterUp))
            return true;

        try {
            matches = Integer.valueOf(v.getText().toString());
            if (matches < 2)
                matches = 2;
            else if (matches > MAX_MATCHES)
                matches = MAX_MATCHES;
            v.setText(String.valueOf(matches));

        } catch (NumberFormatException e) {
                    /* Shouldn't be able to get here */
        }

        InputMethodManager imm
                = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        mBoard.setNumberOfMatches(matches);
        newGame();

        v.clearFocus();

        return true;
    }

    private void handleScores() {

        Log.e(TAG, "Showing best scores.");

            mScores = new ScoresFragment();
            if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
                getFragmentManager().beginTransaction().add(android.R.id.content,mScores).commit();
            } else {
                getFragmentManager().beginTransaction().replace(android.R.id.content,mScores).commit();
            }
    }

    public void dismissScores(View vw) {

        if (getFragmentManager().findFragmentById(android.R.id.content) != null) {
            getFragmentManager().beginTransaction().remove(mScores).commit();
        }
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

        AlertDialog options = dialog.create();
        options.setCanceledOnTouchOutside(true);

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

    @SuppressWarnings("UnusedParameters")
    public void onPopupButtonClicked(View v) {
        newGame();
    }

    private void newGame() {
        mBoard.reset();
        mPopupBtn.setVisibility(View.INVISIBLE);
    }

    private final Runnable mOnWinnerRunnable = new Runnable() {
        @Override
        public void run() {
            ContentValues cv = mBoard.getResult();
            mPopupBtn.setText(getString(R.string.winner_popup)
                    + cv.get(DatabaseHelper.SCORE));
            mPopupBtn.setVisibility(View.VISIBLE);

            new InsertScoreTask().execute(cv);
        }
    };

    class InsertScoreTask extends AsyncTask<ContentValues, Void, Boolean> {
        @Override
        protected Boolean doInBackground(ContentValues... cv) {
            mDb.getWritableDatabase().insert(DatabaseHelper.TABLE,
                    DatabaseHelper.SCORE, cv[0]);

            Log.e(TAG, "Inserted score row into datbase.");
            return true;
        }
    }


    class WaitForResourcesRunnable extends AsyncTask<Void, Void, Boolean> {
        final static int DELAY_MSECS = 250;
        final static int MIN_WAIT_MSECS = 2000;
        final static int MAX_WAIT_MSECS = 10000;

        protected Boolean doInBackground(Void... params) {

            int msecs = 0;
            while (msecs < MAX_WAIT_MSECS) {
                //noinspection ResourceType
                if (msecs >= MIN_WAIT_MSECS
                        && Card.isResourceLoadingFinished()
                        && SoundsEffects.isResourceLoadingFinished()
                        && Music.isResourceLoadingFinished())
                    return true;

                try {
                    Thread.sleep(DELAY_MSECS);
                } catch (InterruptedException e) {
                    // Ignore interruption
                }

                msecs += DELAY_MSECS;
            }

            return false;
        }

        protected void onPostExecute(Boolean result) {
            mSplashImg.setVisibility(View.INVISIBLE);
        }
    }
}
