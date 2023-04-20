package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class UserClientService {
  private User user;
  private Socket socket;
  Controller controller;
  public boolean checkUser(String username, String password, Controller controller) {
    boolean result = false;
    this.user = new User(username, password, User.UserType.LOGIN);
    try {
      socket = new Socket(InetAddress.getByName("localhost"), 9999);
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      oos.writeObject(user);
      ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      Message ms =  (Message) ois.readObject();
      if (ms.getMessageType().equals(MessageType.MESSAGE_LOGIN_SUCCEED)) {
        ClientConnectServerThread clientConnectServerThread = new ClientConnectServerThread(socket, controller);
        clientConnectServerThread.start();
        ManageClientConnectThread.addClientConnectServerThread(username, clientConnectServerThread);
        this.controller = controller;
        result = true;
      } else {
        result = false;
        socket.close();
      }
    } catch (Exception e) {
      result = false;
  }
    return result;
  }
  public boolean registerUser(String username, String password) {
    boolean result = false;
    this.user = new User(username, password, User.UserType.REGISTER);
    try {
      socket = new Socket(InetAddress.getByName("localhost"), 9999);
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      oos.writeObject(user);
      ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      Message ms = (Message) ois.readObject();
      if (ms.getMessageType().equals(MessageType.MESSAGE_REGISTER_SUCCEED)) {
//        ClientConnectServerThread clientConnectServerThread = new ClientConnectServerThread(socket, chatUsers);
//        clientConnectServerThread.start();
//        ManageClientConnectThread.addClientConnectServerThread(username, clientConnectServerThread);
        socket.close();
        result = true;
      } else {
        result = false;
        socket.close();
      }
    } catch (Exception e) {
      result = false;
    }
    return result;
  }
  public int getCurrentOnlineCnt() {
    return ManageClientConnectThread.getClientConnectServerThread(user.getUsername()).getCurrentOnlineCnt();
  }
  public String getChatList() {
    return ManageClientConnectThread.getClientConnectServerThread(user.getUsername()).getCurrentOnlineUsers();
  }
  public void Logout() {
    Message message;
    for (String username: controller.getChatUsers()) {
      if (controller.existInOnlineUsers(username)) {
        message = new Message(System.currentTimeMillis(), user.getUsername(), username, "[System Message] The user is offline and the chat has temporarily ended.", MessageType.MESSAGE_SEND_TO_ONE);
        try {
          ObjectOutputStream oos = new ObjectOutputStream(ManageClientConnectThread.getClientConnectServerThread(user.getUsername()).getSocket().getOutputStream());
          oos.writeObject(message);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    message = new Message(System.currentTimeMillis(), user.getUsername(), "Server", "Logout", MessageType.MESSAGE_LOGOUT);
    try {
      ObjectOutputStream oos = new ObjectOutputStream(ManageClientConnectThread.getClientConnectServerThread(user.getUsername()).getSocket().getOutputStream());
      oos.writeObject(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public void sendMessage(String receiver, String message) {
    Message ms = new Message(System.currentTimeMillis(), user.getUsername(), receiver, message, MessageType.MESSAGE_SEND_TO_ONE);
    try {
      ObjectOutputStream oos = new ObjectOutputStream(ManageClientConnectThread.getClientConnectServerThread(user.getUsername()).getSocket().getOutputStream());
      oos.writeObject(ms);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
