package cba.piterpti.pl.remoteplayerclient.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cba.piterpti.pl.remoteplayerclient.component.Playlist;
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
    private boolean appClosing;
    private Playlist playlist;
    private PlayerFragment fragment;

    public PlaylistListener(Playlist playlist, PlayerFragment fragment) {
        this.fragment = fragment;
        this.playlist = playlist;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        Socket socket = null;
        while (!appClosing) {
            try {
                socket = serverSocket.accept();
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
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

        }


        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAppClosing(boolean appClosing) {
        this.appClosing = appClosing;
    }
}
