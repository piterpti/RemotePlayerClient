package cba.piterpti.pl.remoteplayerclient.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import cba.piterpti.pl.remoteplayerclient.R;

/**
 * Created by piter on 13.04.17.
 */

public class Playlist extends ListView {

    private ArrayList<String> playlist;
    private ArrayAdapter<String> adapter ;

    public Playlist(Context context) {
        super(context);
    }

    public Playlist(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Playlist(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPlaylist(ArrayList<String> playlist) {
        this.playlist = playlist;
        adapter = new ArrayAdapter<String>(getContext(), R.layout.row, this.playlist);
        setAdapter(adapter);
    }

}
