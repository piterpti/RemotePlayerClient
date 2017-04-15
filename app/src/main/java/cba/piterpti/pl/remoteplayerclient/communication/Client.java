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

import pl.piterpti.message.FlowArgs;
import pl.piterpti.message.Message;
import pl.piterpti.message.MessagePlayerControl;
import pl.piterpti.message.MessageSendFile;
import pl.piterpti.message.MessageType;
import pl.piterpti.message.Messages;

public class Client {

    private int SocketServerPORT = 8888;
    private String ServerHost = "192.168.0.1";

    private final Object lock;

    private ProgressDialog dialog;
    private BackgroundTask backgroundTask;

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
        backgroundTask = new BackgroundTask(new MessageSendFile(path));
        backgroundTask.execute();
}

    /**
     * Send message
     * @param msg message content
     */
    public synchronized void sendMessage(String msg) {
        backgroundTask = new BackgroundTask(new MessagePlayerControl(msg));
        backgroundTask.execute();
    }

    public synchronized void sendMessageWithArgs(String msg, FlowArgs args) {
        MessagePlayerControl mpc = new MessagePlayerControl(msg);
        mpc.setArgs(args);
        backgroundTask = new BackgroundTask(mpc);
        backgroundTask.execute();
    }

    public void setDialog(ProgressDialog dialog) {
        this.dialog = dialog;
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

    private synchronized Exception getException() {
        return exception;
    }

    private synchronized void setException(Exception exception) {
        if (exception != null) {
            synchronized (lock) {
                lock.notify();
            }
        }
        this.exception = exception;
    }

    public synchronized Object getLock() {
        return lock;
    }

    private class BackgroundTask extends AsyncTask<String, Void, Void> {

        private Message msg;

        BackgroundTask(Message msg) {
            this.msg = msg;
        }

        protected Void doInBackground(String... urls) {
            setException(null);
            setProcessing(true);
            Socket sock;
            try {
                if (msg == null) {
                    throw new IllegalArgumentException("Message can not be null");
                }
                sock = new Socket(ServerHost, SocketServerPORT);
                System.out.println("Connecting...");

                if (msg.getMessageType() == MessageType.SEND_FILE) {
                    sendFile(sock);
                } else if (msg.getMessageType() == MessageType.PLAYER_CONTROL){
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
         *
         * @param sock socket
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void sendFile(Socket sock) {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            ObjectOutputStream objectOutputStream = null;
            ObjectInputStream ois = null;
            OutputStream os = null;
            MessageSendFile msf = (MessageSendFile) msg;
            try {
                sock.setSoTimeout(30 * 1000);
                File myFile = new File(msf.getFileName());
                byte[] myByteArray = new byte[(int) myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(myByteArray, 0, myByteArray.length);

                System.out.println("Sending..." + msf.getFileName());
                os = sock.getOutputStream();
                objectOutputStream = new ObjectOutputStream(os);
                objectOutputStream.writeObject(msf);
                objectOutputStream.flush();

                ois = new ObjectInputStream(sock.getInputStream());
                Object tmp = ois.readObject();
                Message receivedMsg = null;
                if (tmp instanceof Message) {
                    receivedMsg = (Message) tmp;
                } else {
                    System.out.println("Unknown response");
                }

                if (receivedMsg != null && receivedMsg.getMessageType() == MessageType.PLAYER_CONTROL) {
                    MessagePlayerControl mpc = (MessagePlayerControl) receivedMsg;
                    if (mpc.getMsg().equals(Messages.MSG_SEND_MP3)) {
                        os.write(myByteArray, 0, myByteArray.length);
                        os.flush();
                    }
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
                System.out.println(msf.getFileName() + " completed");
            }
        }

        /**
         * Send msg like PLAY, STOP, PAUSE, NEXT, PREV etc.
         * @param sock socket
         */
        private void sendMsg(Socket sock) {
            ObjectOutputStream objectOutputStream = null;
            MessagePlayerControl msgPc = (MessagePlayerControl) msg;
            try {
                OutputStream os = sock.getOutputStream();
                objectOutputStream= new ObjectOutputStream(os);
                objectOutputStream.writeObject(msgPc);
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
                System.out.println("Message sent:" + msgPc.getMsg());
            }
        }
    }
}
