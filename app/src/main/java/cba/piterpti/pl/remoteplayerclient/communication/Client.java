package cba.piterpti.pl.remoteplayerclient.communication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    private int SocketServerPORT = 8888;
    private String ServerHost = "192.168.0.1";

    private static final String MSG = "MSG:";
    public static final String MSG_PLAY = "PLAY";
    public static final String MSG_PAUSE = "PAUSE";
    public static final String MSG_STOP = "STOP";
    public static final String MSG_NEXT = "NEXT";
    public static final String MSG_PREV = "PREV";
    @SuppressWarnings("unused")
    public static final String MSG_EXIST = "EXIST";
    private static final String MSG_SEND_MP3 = "SEND_MP3";

    private Object lock;

    private RetrieveFeedTask task;
    private ProgressDialog dialog;

    public Client(Object lock) {
        this.lock = lock;
    }

    /** Exception occurred when processing */
    private Exception exception;

    /**
     * Send file
     * @param path path to file
     */
    public synchronized void sendFile(String path) {
        task = new RetrieveFeedTask(path);
        task.setFile(true);
        task.execute();
    }

    /**
     * Send message
     * @param path message content
     */
    public synchronized void sendMessage(String path) {
        task = new RetrieveFeedTask(path);
        task.execute();
    }

    public void setDialog(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Async task
     */
    private class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        private String path;
        private boolean file = false;

        RetrieveFeedTask(String path) {
            this.path = path;
        }

        public void setFile(boolean file) {
            this.file = file;
        }

        protected Void doInBackground(String... urls) {
            setException(null);
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
                setException(e);
            }
            setProcessing(false);
            return null;
        }

        /**
         * Sending file
         * @param sock socket
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void sendFile(Socket sock) {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            ObjectOutputStream objectOutputStream = null;
            ObjectInputStream ois = null;
            OutputStream os = null;
            try {
                sock.setSoTimeout(30 * 1000);
                File myFile = new File(path);
                byte[] myByteArray = new byte[(int) myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(myByteArray, 0, myByteArray.length);

                System.out.println("Sending..." + path);
                os = sock.getOutputStream();
                objectOutputStream = new ObjectOutputStream(os);
                objectOutputStream.writeUTF(path);
                objectOutputStream.flush();

                ois = new ObjectInputStream(sock.getInputStream());
                String msg = ois.readUTF();

                if (msg.equals(MSG + MSG_SEND_MP3)) {
                    os.write(myByteArray, 0, myByteArray.length);
                    os.flush();
                }
            } catch (Exception exc) {
                setException(exc);
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                    if (ois != null) {
                        ois.close();
                    }

                    if (os != null) {
                        os.close();
                    }
                    sock.close();
                } catch (Exception e) {
                    System.out.println("Problem with closing resources");
                }
            }
            if (getException() == null) {
                System.out.println(path + " completed");
            }
        }

        /**
         * Send msg like PLAY, STOP, PAUSE, NEXT, PREV etc.
         * @param sock socket
         */
        private void sendMsg(Socket sock) {
            ObjectOutputStream objectOutputStream = null;
            try {
                OutputStream os = sock.getOutputStream();
                objectOutputStream= new ObjectOutputStream(os);
                objectOutputStream.writeUTF(MSG + path);
                objectOutputStream.flush();
            } catch (Exception exc) {
                setException(exc);
            } finally {
                try {
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                    sock.close();
                } catch (Exception e) {
                    System.out.println("Problem with closing resources");
                }
            }
            if (getException() == null) {
                System.out.println("Message sent:" + path);
            }
        }
    }

    /**
     * Sending file ending
     * @param processing processing or no
     */
    private void setProcessing(boolean processing) {
        if (!processing && dialog != null) {
            dialog.dismiss();
        }
    }

    public void setSocketServerPORT(int socketServerPORT) {
        SocketServerPORT = socketServerPORT;
    }

    public void setServerHost(String serverHost) {
        ServerHost = serverHost;
    }

    public synchronized Exception getException() {
        return exception;
    }

    public synchronized void setException(Exception exception) {
        if (exception != null) {
            synchronized (lock) {
                lock.notify();
            }
        }
        this.exception = exception;
    }
}
