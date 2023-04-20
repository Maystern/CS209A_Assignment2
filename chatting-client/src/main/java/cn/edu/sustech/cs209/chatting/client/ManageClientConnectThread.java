package cn.edu.sustech.cs209.chatting.client;

import java.util.HashMap;

public class ManageClientConnectThread {
  private static HashMap<String, ClientConnectServerThread> clientConnectServerThreads = new HashMap<>();
  public static void addClientConnectServerThread(String userId, ClientConnectServerThread clientConnectServerThread) {
    clientConnectServerThreads.put(userId, clientConnectServerThread);
  }
  public static ClientConnectServerThread getClientConnectServerThread(String userId) {
    return clientConnectServerThreads.get(userId);
  }
}
