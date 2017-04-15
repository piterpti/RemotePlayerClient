package cba.piterpti.pl.remoteplayerclient.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import cba.piterpti.pl.remoteplayerclient.R;
import cba.piterpti.pl.remoteplayerclient.activity.MainActivity;
import cba.piterpti.pl.remoteplayerclient.communication.Client;
import cba.piterpti.pl.remoteplayerclient.communication.PlaylistListener;
import cba.piterpti.pl.remoteplayerclient.component.Playlist;
import pl.piterpti.message.FlowArgs;
import pl.piterpti.message.Messages;

import static android.app.Activity.RESULT_OK;

public class PlayerFragment extends Fragment {

    private static final int ACTIVITY_CHOOSE_FILE = 3;
    public static final String IP_ADDRESS = "IP_ADDRESS";
    public static final String PORT = "PORT";

    private Client client;

    private Object exceptionLock;

    private Playlist playlistView;

    public PlayerFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ErrorListener listener = new ErrorListener(getActivity());

        client = ((MainActivity)getActivity()).getClient();
        exceptionLock = client.getLock();
        init(view);

        Thread playlistListener = new Thread(new PlaylistListener(playlistView));
        playlistListener.setDaemon(true);
        playlistListener.start();

        Thread t = new Thread(listener);
        t.setDaemon(true);
        t.start();

        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

        int permsRequestCode = 200;
        requestPermissions(perms, permsRequestCode);
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init(View view) {
        getConfig();

        Button getFilesBtn = (Button) view.findViewById(R.id.main_getFileBtn);
        Button pauseBtn = (Button) view.findViewById(R.id.main_pause);
        Button stopBtn = (Button) view.findViewById(R.id.main_stop);
        Button playBtn = (Button) view.findViewById(R.id.main_play);
        Button nextBtn = (Button) view.findViewById(R.id.main_next);
        Button prevBtn = (Button) view.findViewById(R.id.main_prev);
        Button configBtn = (Button) view.findViewById(R.id.main_config);
        Button volumePlus    = (Button) view.findViewById(R.id.main_volumePlus);
        Button volumeMinus = (Button) view.findViewById(R.id.main_volumeMinus);

        playlistView = (Playlist) view.findViewById(R.id.main_playlistView);

        getFilesBtn.setOnClickListener(v -> {
            Intent chooseFile;
            Intent intent;
            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("file/*");
            intent = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        });

        playBtn.setOnClickListener(v -> client.sendMessage(Messages.MSG_PLAY));
        stopBtn.setOnClickListener(v -> client.sendMessage(Messages.MSG_STOP));
        pauseBtn.setOnClickListener(v -> client.sendMessage(Messages.MSG_PAUSE));
        nextBtn.setOnClickListener(v -> client.sendMessage(Messages.MSG_NEXT));
        prevBtn.setOnClickListener(v -> client.sendMessage(Messages.MSG_PREV));
        volumePlus.setOnClickListener(v -> client.sendMessageWithArgs(Messages.MSG_SET_VOLUME, new FlowArgs("volume", 10)));
        volumeMinus.setOnClickListener(v -> client.sendMessageWithArgs(Messages.MSG_SET_VOLUME, new FlowArgs("volume", -10)));
        configBtn.setOnClickListener(v -> goToConfiguration());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if(requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri uri = data.getData();
            String filePath = getRealPathFromURI(uri);
            if (!filePath.isEmpty() && new File(filePath).exists()) {
                ProgressDialog pd = ProgressDialog.show(getActivity(), "Processing", "Sending mp3 file to your PC", true, false);
                client.setDialog(pd);
                client.sendFile(filePath);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] project = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query( contentUri, project, null, null,null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void goToConfiguration() {
        ConfigurationFragment configFragment = new ConfigurationFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, configFragment, MainActivity.CONFIG_FRAGMENT);
        transaction.commit();
    }

    private void getConfig() {
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        String ipAddress = settings.getString(IP_ADDRESS, "192.168.0.1");
        String port = settings.getString(PORT, "8888");
        System.out.println("Client config: port: " + port + ", host: " + ipAddress);
        client.setServerHost(ipAddress);
        client.setSocketServerPORT(Integer.valueOf(port));
    }

    private class ErrorListener implements Runnable {

        private Activity activity;

        ErrorListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (exceptionLock){
                    try {
                        exceptionLock.wait();
                        activity.runOnUiThread(() -> Toast.makeText(activity,
                                "Problem with communication", Toast.LENGTH_SHORT).show());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
