package cba.piterpti.pl.remoteplayerclient.component;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import cba.piterpti.pl.remoteplayerclient.R;

/**
 * Created by piter on 13.04.17.
 */

public class Playlist extends ListView {

    private ArrayList<String> playlist;

    public Playlist(Context context) {
        super(context);
    }

    public Playlist(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Playlist(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPlaylist(ArrayList<String> playlist, final int current) {
        this.playlist = playlist;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.row, this.playlist) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                if (position == current) {
                    row.setBackgroundColor(Color.RED);
                } else {
                    row.setBackgroundColor(Color.BLACK);
                }
                return row;
            }
        };

        setAdapter(adapter);


    }



}
