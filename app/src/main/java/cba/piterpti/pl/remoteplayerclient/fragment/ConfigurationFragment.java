package cba.piterpti.pl.remoteplayerclient.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import cba.piterpti.pl.remoteplayerclient.R;
import cba.piterpti.pl.remoteplayerclient.activity.MainActivity;

import static cba.piterpti.pl.remoteplayerclient.fragment.PlayerFragment.IP_ADDRESS;
import static cba.piterpti.pl.remoteplayerclient.fragment.PlayerFragment.PORT;

public class ConfigurationFragment extends Fragment {

    private EditText portConfig;
    private EditText ipAddress;

    public ConfigurationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);
        init(view);
        return view;
    }

    public void init(View view) {
        portConfig = (EditText) view.findViewById(R.id.config_portText);
        ipAddress = (EditText) view.findViewById(R.id.config_ipText);
        Button backBtn = (Button) view.findViewById(R.id.config_back);
        Button saveBtn = (Button) view.findViewById(R.id.config_save);

        getConfig();

        backBtn.setOnClickListener(v -> backToPlayer());

        saveBtn.setOnClickListener(v -> {
            String port = portConfig.getText() + "";
            String host = ipAddress.getText() + "";
            saveConfig(port, host);
        });
    }

    private void saveConfig(String port, String host) {
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PORT, port);
        editor.putString(IP_ADDRESS, host);
        editor.apply();
        backToPlayer();
    }

    private void backToPlayer() {
        PlayerFragment playerFragment = new PlayerFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, playerFragment, MainActivity.PLAYER_FRAGMENT);
        transaction.commit();
    }

    public void getConfig() {
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        String ipAddressStr = settings.getString(IP_ADDRESS, "192.168.0.1");
        String port = settings.getString(PORT, "8888");
        portConfig.setText(port);
        ipAddress.setText(ipAddressStr.trim());
    }
}
