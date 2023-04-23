package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectClientThread extends Thread {

  private Socket socket;
  private String username;

  public ServerConnectClientThread(Socket socket, String username) {
    this.socket = socket;
    this.username = username;
  }

  void sendMessage(Message message) {
    try {
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      oos.writeObject(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Message message = (Message) ois.readObject();
        if (message.getMessageType().equals(MessageType.MESSAGE_GET_ONLINE_USER_LISTS)) {
          String onlineUsers = ManageClientThreads.getAllOnlineUsers();
          Message returnMessage = new Message(System.currentTimeMillis(), "server",
              message.getSentBy(), onlineUsers, MessageType.MESSAGE_GET_ONLINE_USER_LISTS);
          ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
          oos.writeObject(returnMessage);
        } else if (message.getMessageType().equals(MessageType.MESSAGE_LOGOUT)) {
          ManageClientThreads.removeClientThread(message.getSentBy());
          ManageClientThreads.sendAllOnlineUsers(message.getSentBy(), true, false);
          break;
        } else if (message.getMessageType().equals(MessageType.MESSAGE_SEND_TO_ONE)) {
          Message returnMessage = new Message(System.currentTimeMillis(), message.getSentBy(),
              message.getSendTo(), (String) message.getData(), MessageType.MESSAGE_SEND_TO_ONE);
          ManageClientThreads.sendMessageToOne(message.getSendTo(), returnMessage);
        } else if (message.getMessageType().equals(MessageType.MESSAGE_SEND_TO_GROUP)) {
          Message returnMessage = new Message(System.currentTimeMillis(), message.getSentBy(),
              message.getSendTo(), (String) message.getData(), MessageType.MESSAGE_SEND_TO_GROUP);
          ManageClientThreads.sendMessageToGroup(message.getSendTo(), returnMessage);
        } else if (message.getMessageType().equals(MessageType.File_MESSAGE_SEND_TO_ONE)) {
          Message returnMessage = new Message(System.currentTimeMillis(), message.getSentBy(),
              message.getSendTo(), (String) message.getData(),
              MessageType.File_MESSAGE_SEND_TO_ONE);
          returnMessage.setAttachment(message.getAttachment());
          returnMessage.setAttachmentName(message.getAttachmentName());
          ManageClientThreads.sendMessageToOne(message.getSendTo(), returnMessage);
        } else if (message.getMessageType().equals(MessageType.File_MESSAGE_SEND_TO_GROUP)) {
          Message returnMessage = new Message(System.currentTimeMillis(), message.getSentBy(),
              message.getSendTo(), (String) message.getData(),
              MessageType.File_MESSAGE_SEND_TO_GROUP);
          returnMessage.setAttachment(message.getAttachment());
          returnMessage.setAttachmentName(message.getAttachmentName());
          ManageClientThreads.sendMessageToGroup(message.getSendTo(), returnMessage);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
