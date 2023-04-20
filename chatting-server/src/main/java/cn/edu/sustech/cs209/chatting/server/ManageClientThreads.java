package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.util.HashMap;

public class ManageClientThreads {
  private static HashMap<String, ServerConnectClientThread> hashMap = new HashMap<>();
  public static void addClientThread(String username, ServerConnectClientThread serverConnectClientThread) {
    hashMap.put(username, serverConnectClientThread);
  }
  public static ServerConnectClientThread getClientThread(String username) {
    return hashMap.get(username);
  }
  public static void sendAllOnlineUsers() {
    String onlineUserLists = ManageClientThreads.getAllOnlineUsers();
    Message message = new Message(System.currentTimeMillis(), "Server", "All", onlineUserLists);
    message.setMessageType(MessageType.MESSAGE_GET_ONLINE_USER_LISTS);
    for (String username : hashMap.keySet()) {
      ServerConnectClientThread serverConnectClientThread = hashMap.get(username);
      serverConnectClientThread.sendMessage(message);
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
    serverConnectClientThread.sendMessage(message);
  }
}
