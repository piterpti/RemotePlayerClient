package cba.piterpti.pl.remoteplayerclient.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import cba.piterpti.pl.remoteplayerclient.R;
import cba.piterpti.pl.remoteplayerclient.communication.Client;
import cba.piterpti.pl.remoteplayerclient.fragment.PlayerFragment;

public class MainActivity extends FragmentActivity {

    public final static String PLAYER_FRAGMENT = "PLAYER_FRAGMENT";
    public final static String CONFIG_FRAGMENT = "PLAYER_FRAGMENT";

    private Object lock = new Object();
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PlayerFragment playerFragment = new PlayerFragment();
        client = new Client(lock);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(savedInstanceState == null) {
            transaction.add(R.id.fragment_container, playerFragment, PLAYER_FRAGMENT);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public Client getClient() {
        return client;
    }
}