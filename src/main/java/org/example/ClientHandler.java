
package org.example;

import org.example.utils.SocketUtil;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
  public static List<ClientHandler> clientHandlers = new ArrayList<>();

  private Socket clientSocket;
  private BufferedReader bufferedReader; // for receiving messages
  private BufferedWriter bufferedWriter; // for sending messages
  private String clientUsername;

  public ClientHandler(Socket socket) {
    try {
      this.clientSocket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      clientHandlers.add(this);
    } catch (IOException exception) {
      removeClientHandler();
      SocketUtil.closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  private void broadcastMessage(String messageToSend) {
    for (ClientHandler clientHandler : clientHandlers) {
      try {
        if (!clientHandler.clientUsername.equals(clientUsername)) {
          clientHandler.bufferedWriter.write(messageToSend);
          clientHandler.bufferedWriter.newLine(); // to terminate the message
          clientHandler.bufferedWriter.flush(); // to send out the message even that the buffer is not full
        }
      } catch (IOException exception) {
        removeClientHandler();
        SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
      }
    }
  }

  private void removeClientHandler() {
    clientHandlers.remove(this);
    broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
  }

  @Override
  public void run() {
    String messageFromClient;

    while (clientSocket.isConnected()) {
      try {
        messageFromClient = bufferedReader.readLine();
        broadcastMessage(messageFromClient);
      } catch (IOException exception) {
        removeClientHandler();
        SocketUtil.closeEverything(clientSocket, bufferedReader, bufferedWriter);
        break;
      }
    }
  }
}
