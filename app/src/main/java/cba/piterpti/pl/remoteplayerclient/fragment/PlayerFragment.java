package cba.piterpti.pl.remoteplayerclient.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cba.piterpti.pl.remoteplayerclient.R;
import cba.piterpti.pl.remoteplayerclient.communication.Client;

import static android.app.Activity.RESULT_OK;

public class PlayerFragment extends Fragment {

    private Button getFilesBtn;
    private Button sendBtn;
    private TextView urlTextView;
    private static final int ACTIVITY_CHOOSE_FILE = 3;

    private Client client;
    private ProgressDialog pd;

    public PlayerFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        init(view);

        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

        int permsRequestCode = 200;

        requestPermissions(perms, permsRequestCode);
        client = new Client();

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    private void init(View view) {
        getFilesBtn = (Button) view.findViewById(R.id.main_getFileBtn);
        sendBtn = (Button) view.findViewById(R.id.main_send);
        urlTextView = (TextView) view.findViewById(R.id.main_url);

        Button pauseBtn = (Button) view.findViewById(R.id.main_pause);
        Button stopBtn = (Button) view.findViewById(R.id.main_stop);
        Button playBtn = (Button) view.findViewById(R.id.main_play);
        Button nextBtn = (Button) view.findViewById(R.id.main_next);
        Button prevBtn = (Button) view.findViewById(R.id.main_prev);

        getFilesBtn.setOnClickListener(v -> {
            Intent chooseFile;
            Intent intent;
            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("file/*");
            intent = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        });

        sendBtn.setOnClickListener(v -> {
            pd = ProgressDialog.show(getActivity(), "Processing", "Sending mp3 file to your PC", true, false);
            client.setDialog(pd);
            client.sendFile(urlTextView.getText() + "");
            load();
        });

        playBtn.setOnClickListener(v -> {
            client.sendMessage(Client.MSG_PLAY);
        });

        stopBtn.setOnClickListener(v -> {
            client.sendMessage(Client.MSG_STOP);
        });

        pauseBtn.setOnClickListener(v -> {
            client.sendMessage(Client.MSG_PAUSE);
        });

        nextBtn.setOnClickListener(v -> {
            client.sendMessage(Client.MSG_NEXT);
        });

        prevBtn.setOnClickListener(v -> {
            client.sendMessage(Client.MSG_PREV);
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if(requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri uri = data.getData();
            String filePath = getRealPathFromURI(uri);
            urlTextView.setText(filePath);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void load() {

    }
}
