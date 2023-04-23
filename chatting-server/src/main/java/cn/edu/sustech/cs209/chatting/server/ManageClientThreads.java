package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.util.HashMap;

public class ManageClientThreads {

  private static HashMap<String, ServerConnectClientThread> hashMap = new HashMap<>();

  public static void addClientThread(String username,
      ServerConnectClientThread serverConnectClientThread) {
    hashMap.put(username, serverConnectClientThread);
  }

  public static ServerConnectClientThread getClientThread(String username) {
    return hashMap.get(username);
  }

  public static void sendAllOnlineUsers(String Username, boolean isLogout, boolean isLogin) {
    String onlineUserLists = ManageClientThreads.getAllOnlineUsers();
    Message message = new Message(System.currentTimeMillis(), "Server", "All", onlineUserLists,
        MessageType.MESSAGE_GET_ONLINE_USER_LISTS);
    for (String username : hashMap.keySet()) {
      ServerConnectClientThread serverConnectClientThread = hashMap.get(username);
      serverConnectClientThread.sendMessage(message);
    }
    if (isLogout) {
      Message returnMessage = new Message(System.currentTimeMillis(), "Server", "All", Username,
          MessageType.MESSAGE_LOGOUT);
      for (String username : hashMap.keySet()) {
        ServerConnectClientThread serverConnectClientThread = hashMap.get(username);
        serverConnectClientThread.sendMessage(returnMessage);
      }
    }
    if (isLogin) {
      Message returnMessage = new Message(System.currentTimeMillis(), "Server", "All", Username,
          MessageType.MESSAGE_LOGIN);
      for (String username : hashMap.keySet()) {
        ServerConnectClientThread serverConnectClientThread = hashMap.get(username);
        serverConnectClientThread.sendMessage(returnMessage);
      }
    }
  }

  public static void removeClientThread(String username) {
    hashMap.remove(username);
  }

  public static String getAllOnlineUsers() {
    StringBuilder stringBuilder = new StringBuilder();
    for (String username : hashMap.keySet()) {
      stringBuilder.append(username + ",");
    }
    return stringBuilder.toString();
  }

  public static void sendMessageToOne(String username, Message message) {

    ServerConnectClientThread serverConnectClientThread = hashMap.get(username);
    System.out.println("sendMessageToOne " + message);
    serverConnectClientThread.sendMessage(message);
  }

  public static void sendMessageToGroup(String group, Message message) {
    System.out.println(message);
    String[] groupMembers = group.split(",");
    for (String groupMember : groupMembers) {
      ServerConnectClientThread serverConnectClientThread = hashMap.get(groupMember);
      if (serverConnectClientThread != null) {
        serverConnectClientThread.sendMessage(message);
      }
    }
  }
}
