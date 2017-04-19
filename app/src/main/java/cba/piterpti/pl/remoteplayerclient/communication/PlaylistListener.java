package cba.piterpti.pl.remoteplayerclient.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cba.piterpti.pl.remoteplayerclient.fragment.PlayerFragment;
import pl.piterpti.message.Message;
import pl.piterpti.message.MessagePlaylist;
import pl.piterpti.message.MessageType;

/**
 * Created by piter on 13.04.17.
 */

public class PlaylistListener implements Runnable {

    private ServerSocket serverSocket;
    private final int PORT = 8889;
    private boolean stop;
    private PlayerFragment fragment;

    public PlaylistListener(PlayerFragment fragment) {
        this.fragment = fragment;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Socket socket;
        while (!stop) {
            try {
                socket = serverSocket.accept();
                if (socket.isClosed()) {
                    break;
                }
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                Object objTmp = ois.readObject();

                if (objTmp instanceof Message) {

                    if (((Message)objTmp).getMessageType() == MessageType.PLAYLIST) {
                        MessagePlaylist mpl = (MessagePlaylist) objTmp;
                        fragment.updatePlaylist(mpl);
                    } else {
                        System.out.println("Unhandled message type: " +
                                ((Message)objTmp).getMessageType().toString());
                    }
                } else {
                    System.out.println("Unknown message");
                }
                ois.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStop(boolean stop) {
        this.stop = stop;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
