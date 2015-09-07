package org.tbadg.memory;

import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ScoresFragment extends ListFragment {

    private static final String TAG = "ScoresFragment";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        LinearLayout scoresView = (LinearLayout) inflater.inflate(R.layout.scores, container, false);

        ScoresCursorAdapter mAdapter = new ScoresCursorAdapter(getActivity(), R.layout.row, null,
                new String[] { DatabaseHelper.SCORE, DatabaseHelper.MATCHES,
                               DatabaseHelper.GUESSES, DatabaseHelper.ELAPSED_TIME},
                new int[] {R.id.score, R.id.matches, R.id.guesses, R.id.time}, 0);

        setListAdapter(mAdapter);
        new LoadCursorTask().execute();

        return scoresView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((MemoryActivity)getActivity()).dismissScores(v);
    }

    private class ScoresCursorAdapter extends SimpleCursorAdapter {

        public ScoresCursorAdapter(Context context, int layout, Cursor c,
                                   String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView rankVw = (TextView) view.findViewById(R.id.rank);
            rankVw.setText(String.valueOf(position + 1));

            TextView timeVw = (TextView) view.findViewById(R.id.time);
            int time = Integer.valueOf(timeVw.getText().toString());
            timeVw.setText(String.format("%02d:%02d", time / 60, time % 60));

            return view;
        }
    }

    private class LoadCursorTask extends AsyncTask<ContentValues, Void, Cursor> {
        DatabaseHelper mDb = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.e(TAG, "Loading scores into cursor...");
            mDb = new DatabaseHelper(getActivity());
        }

        @Override
        protected Cursor doInBackground(ContentValues... cv) {
            return mDb.getReadableDatabase()
                    .query(DatabaseHelper.TABLE,
                            new String[]{ "ROWID AS _id",
                                          DatabaseHelper.SCORE, DatabaseHelper.MATCHES,
                                          DatabaseHelper.GUESSES, DatabaseHelper.ELAPSED_TIME},
                            null, null, null, null, DatabaseHelper.SCORE + " desc", "8");
        }

        @Override
        protected void onPostExecute(Cursor result) {
            Log.e(TAG, "Rows loaded = " + result.getCount());
            ((SimpleCursorAdapter)getListAdapter()).changeCursor(result);

            mDb.close();
        }
    }
}
