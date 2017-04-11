package cba.piterpti.pl.remoteplayerclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    private Button getFilesBtn;
    private Button sendBtn;
    private TextView urlTextView;
    private static final int ACTIVITY_CHOOSE_FILE = 3;

    private Client client;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

        int permsRequestCode = 200;

        requestPermissions(perms, permsRequestCode);
        client = new Client();
    }

    private void init() {
        getFilesBtn = (Button) findViewById(R.id.main_getFileBtn);
        sendBtn = (Button) findViewById(R.id.main_send);
        urlTextView = (TextView) findViewById(R.id.main_url);

        Button pauseBtn = (Button) findViewById(R.id.main_pause);
        Button stopBtn = (Button) findViewById(R.id.main_stop);
        Button playBtn = (Button) findViewById(R.id.main_play);
        Button nextBtn = (Button) findViewById(R.id.main_next);
        Button prevBtn = (Button) findViewById(R.id.main_prev);

        getFilesBtn.setOnClickListener(v -> {
            Intent chooseFile;
            Intent intent;
            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("file/*");
            intent = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
        });

        sendBtn.setOnClickListener(v -> {
            pd = ProgressDialog.show(this, "Processing", "Sending mp3 file to your PC", true, false);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        Cursor cursor = getContentResolver().query( contentUri, proj, null, null,null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void load() {

    }
}
