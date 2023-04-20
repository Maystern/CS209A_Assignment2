package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

public class ClientConnectServerThread extends Thread{
  private int nowOnlineUserCount;
  private String nowOnlineUsers;
  private Socket socket;
  Controller controller;
  public ClientConnectServerThread(Socket socket, Controller controller) {
    this.socket = socket;
    this.controller = controller;
  }
  private boolean isUserInChatUsers(String userName) {
    for (String user : controller.getChatUsers()) {
      if (user.equals(userName)) {
        return true;
      }
    }
    return false;
  }
  @Override
  public void run() {
    boolean serverClosed = false;
    while (true) {
      try {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Message message = (Message) ois.readObject();
        System.out.println("[MessageInfo] " + message);
        if (message.getMessageType().equals(MessageType.MESSAGE_GET_ONLINE_USER_LISTS)) {
          String[] onlineUserLists = message.getData().split(",");
          nowOnlineUserCount = onlineUserLists.length;
          nowOnlineUsers = message.getData();
        } else if (message.getMessageType().equals(MessageType.MESSAGE_SEND_TO_ONE)) {
          if (!isUserInChatUsers(message.getSentBy())) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                String selectedUser = controller.getChatList().getSelectionModel().getSelectedItem();
                controller.getChatUsers().add(message.getSentBy());
                ListView<String> chatList = controller.getChatList();
                chatList.getItems().clear();
                for (String chatUser : controller.getChatUsers()) {
                  chatList.getItems().add(chatUser);
                }
                if (selectedUser != null) {
                  chatList.getSelectionModel().select(selectedUser);
                }
              }
            });
          }
          Message tmp = new Message(System.currentTimeMillis(), message.getSentBy(), controller.username, message.getData());
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              controller.addMessage(message.getSentBy(), tmp);
            }
          });
          System.out.println("fine.");
          System.out.println("[Message] " + message.getSentBy() + " said to you: " + message.getData());
        }
      } catch (IOException e) {
            serverClosed = true;
            break;
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    if (serverClosed) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Error");
          alert.setHeaderText("Server closed");
          alert.setContentText("Server closed, please restart the client.");
          alert.showAndWait();
          System.exit(0);
        }
      });
    }
  }
  public Socket getSocket() {
    return socket;
  }
  public int getCurrentOnlineCnt() {
    return nowOnlineUserCount;
  }
  public String getCurrentOnlineUsers() {
    return nowOnlineUsers;
  }
}
