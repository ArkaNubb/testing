package common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketWrapper {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public SocketWrapper(String s, int port) throws IOException { // used by the client
        this.socket = new Socket(s, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public SocketWrapper(Socket s) throws IOException { // used by the server
        this.socket = s;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public Object read() throws IOException, ClassNotFoundException {
        return ois.readObject();
    }

    /**
     * This method is now synchronized to prevent multiple threads from
     * writing to the same stream at the same time, which would corrupt it.
     * oos.reset() is called to clear the output stream's object cache,
     * ensuring that any updates to an object are sent.
     */
    public synchronized void write(Object o) throws IOException {
        oos.writeObject(o);
        oos.reset(); // Prevents caching issues with updated objects
        oos.flush();
    }

    public void closeConnection() throws IOException {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket wrapper: " + e.getMessage());
        }
    }
}