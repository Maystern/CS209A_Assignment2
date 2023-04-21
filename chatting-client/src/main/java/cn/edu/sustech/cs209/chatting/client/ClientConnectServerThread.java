package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.ChatClass.ChatType;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
  @Override
  public void run() {
    boolean serverClosed = false;
    while (true) {
      try {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Message message = (Message) ois.readObject();
        System.out.println("[MessageInfo] " + message);
        if (message.getMessageType().equals(MessageType.MESSAGE_GET_ONLINE_USER_LISTS)) {
          String[] onlineUserLists =  ((String) message.getData()).split(",");
          nowOnlineUserCount = onlineUserLists.length;
          nowOnlineUsers = (String) message.getData();
        } else if (message.getMessageType().equals(MessageType.MESSAGE_SEND_TO_ONE) || message.getMessageType().equals(MessageType.File_MESSAGE_SEND_TO_ONE)) {
          if (!controller.chatExistInChatList(message.getSentBy())) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                ChatClass chatClass = new ChatClass(ChatType.oneToOne, message.getSentBy());
                chatClass.addUsers(message.getSendTo());
                chatClass.addUsers(message.getSentBy());
                controller.addChat(chatClass);
                controller.chatList.refresh();
              }
            });
          }
          Message tmp = new Message(System.currentTimeMillis(), message.getSentBy(), message.getSendTo(), (String) message.getData(), MessageType.MESSAGE_SEND_TO_ONE);
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              controller.addMessageToChat(message.getSentBy(), tmp);
              controller.chatList.refresh();
            }
          });
          System.out.println("fine.");
          System.out.println("[Message] " + message.getSentBy() + " said to you: " + message.getData());

          if (message.getMessageType().equals(MessageType.File_MESSAGE_SEND_TO_ONE)) {
            try {
              File folder = new File("ChatFiles");
              if (!folder.exists()) {
                folder.mkdir();
              }
              folder = new File(folder, controller.getUsername());
              if (!folder.exists()) {
                folder.mkdir();
              }
              folder = new File(folder, "FileRecv");
              if (!folder.exists()) {
                folder.mkdir();
              }
              File file = new File(folder, message.getAttachmentName());
              if (!file.exists()) {
                file.createNewFile();
              }
              FileOutputStream fos = new FileOutputStream(file);
              fos.write(message.getAttachment());
              fos.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }


        } else if (message.getMessageType().equals(MessageType.MESSAGE_SEND_TO_GROUP) || message.getMessageType().equals(MessageType.File_MESSAGE_SEND_TO_GROUP)) {
          String[] selectedUsersArray = message.getSendTo().split(",");
          List<String> allUsers = new ArrayList<String>();

          for (int i = 0; i < selectedUsersArray.length; i++)
            allUsers.add(selectedUsersArray[i]);
          allUsers.add(message.getSentBy());

          // sort the allUsers by alphabetical order
          String[] allUsersArray = allUsers.toArray(new String[0]);
          Arrays.sort(allUsersArray);

          String chatIndex = "";

          for (String selectedUser: allUsersArray) {
            chatIndex += selectedUser + ",";
          }
          if (!controller.chatExistInChatList(chatIndex)) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                String[] selectedUsersArray = message.getSendTo().split(",");
                List<String> allUsers = new ArrayList<String>();

                for (int i = 0; i < selectedUsersArray.length; i++)
                  allUsers.add(selectedUsersArray[i]);
                allUsers.add(message.getSentBy());

                // sort the allUsers by alphabetical order
                String[] allUsersArray = allUsers.toArray(new String[0]);
                Arrays.sort(allUsersArray);

                String chatIndex = "";

                for (String selectedUser: allUsersArray) {
                  chatIndex += selectedUser + ",";
                }
                List<String> tmpAllUsers = new ArrayList<String>();
                for (String selectedUser: allUsersArray) {
                  tmpAllUsers.add(selectedUser);
                }
                if (!controller.chatExistInChatList(chatIndex)) {
                  ChatClass chatClass = new ChatClass(ChatType.group, chatIndex);
                  chatClass.addUsersAll(tmpAllUsers);
                  controller.getChatInfo().add(chatClass);
                  controller.chatList.getItems().add(chatClass);
                  controller.sortChatList();
                }
                ChatClass tmpChatClass = null;
                for (ChatClass chatClass: controller.getChatInfo()) {
                  if (chatClass.getChatIndex().equals(chatIndex)) {
                    tmpChatClass = chatClass;
                    break;
                  }
                }
                tmpChatClass.addMessage(message);
                controller.chatList.refresh();
              }
            });
          } else {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                String[] selectedUsersArray = message.getSendTo().split(",");
                List<String> allUsers = new ArrayList<String>();

                for (int i = 0; i < selectedUsersArray.length; i++)
                  allUsers.add(selectedUsersArray[i]);
                allUsers.add(message.getSentBy());

                // sort the allUsers by alphabetical order
                String[] allUsersArray = allUsers.toArray(new String[0]);
                Arrays.sort(allUsersArray);

                String chatIndex = "";

                for (String selectedUser: allUsersArray) {
                  chatIndex += selectedUser + ",";
                }

                controller.addMessageToChat(chatIndex, message);
                controller.chatList.refresh();
              }
            });
          }


          if (message.getMessageType().equals(MessageType.File_MESSAGE_SEND_TO_GROUP)) {
            try {
              File folder = new File("ChatFiles");
              if (!folder.exists()) {
                folder.mkdir();
              }
              folder = new File(folder, controller.getUsername());
              if (!folder.exists()) {
                folder.mkdir();
              }
              folder = new File(folder, "FileRecv");
              if (!folder.exists()) {
                folder.mkdir();
              }
              File file = new File(folder, message.getAttachmentName());
              if (!file.exists()) {
                file.createNewFile();
              }
              FileOutputStream fos = new FileOutputStream(file);
              fos.write(message.getAttachment());
              fos.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }

          System.out.println("[Message] " + message.getSentBy() + " said to group " + message.getSendTo() + ": " + message.getData());
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
