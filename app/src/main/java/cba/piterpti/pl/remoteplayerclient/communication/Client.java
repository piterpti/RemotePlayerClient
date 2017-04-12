package cba.piterpti.pl.remoteplayerclient.communication;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by piter on 10.04.17.
 */

public class Client {

    private static final int SocketServerPORT = 8888;
    private static final String ServerHost = "192.168.0.103";

    private boolean processing = true;

    public static final String MSG = "MSG:";
    public static final String MSG_PLAY = "PLAY";
    public static final String MSG_PAUSE = "PAUSE";
    public static final String MSG_STOP = "STOP";
    public static final String MSG_NEXT = "NEXT";
    public static final String MSG_PREV = "PREV";
    public static final String MSG_EXIST = "EXIST";
    public static final String MSG_SEND_MP3 = "SEND_MP3";

    private RetrieveFeedTask task;
    private ProgressDialog dialog;

    public Client() {

    }

    public synchronized void sendFile(String path) {
        task = new RetrieveFeedTask(path);
        task.setFile(true);
        task.execute();
    }

    public synchronized void sendMessage(String path) {
        task = new RetrieveFeedTask(path);
        task.execute();
    }

    public void setDialog(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        private String path;
        private boolean file = false;

        public RetrieveFeedTask(String path) {
            this.path = path;
        }

        public void setFile(boolean file) {
            this.file = file;
        }

        protected Void doInBackground(String... urls) {
            setProcessing(true);
            if (path == null) {
                throw new IllegalArgumentException("Path can not be null");
            }

            Socket sock;
            try {
                sock = new Socket(ServerHost, SocketServerPORT);
                System.out.println("Connecting...");

                if (file) {
                    sendFile(sock);
                } else {
                    sendMsg(sock);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            setProcessing(false);
            return null;
        }

        private void sendFile(Socket sock) throws Exception{
            File myFile = new File(path);
            byte[] mybytearray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);

            System.out.println("Sending..." + path);
            OutputStream os = sock.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            objectOutputStream.writeUTF(path);
            objectOutputStream.flush();


            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            String msg = ois.readUTF();

            if (msg.equals(MSG + MSG_SEND_MP3)) {
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();
            }

            bis.close();
            fis.close();
            objectOutputStream.close();
            ois.close();
            os.close();
            sock.close();
            System.out.println(path + " completed");
        }

        private void sendMsg(Socket sock) throws Exception {
            OutputStream os = sock.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            objectOutputStream.writeUTF(MSG + path);
            objectOutputStream.flush();
            objectOutputStream.close();
            sock.close();
            System.out.println("Message sent:" + path);
        }
    }

    public synchronized boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
        if (!this.processing && dialog != null) {
            dialog.dismiss();
        }
    }
}
