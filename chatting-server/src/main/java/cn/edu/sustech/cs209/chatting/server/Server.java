package cn.edu.sustech.cs209.chatting.server;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.User;
import cn.edu.sustech.cs209.chatting.common.User.UserType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
  private ServerSocket serverSocket;
  ArrayList<User> users = new ArrayList<>();
  private boolean isUserLoginValid(String userName, String password) {
    for (User user : users) {
      if (user.getUsername().equals(userName) && user.getPassword().equals(password)) {
        return true;
      }
    }
    return false;
  }
  private boolean isUserRegisterValid(String userName, String password) {
    if (password.length() <= 0) {
      return false;
    }
    for (User user : users) {
      if (user.getUsername().equals(userName)) {
        return false;
      }
    }
    return true;
  }
  public Server() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream("UsernameAndPassword.csv"), "UTF-8"))) {
        String line;
        int index = 0;
        while ((line = reader.readLine()) != null) {
            if (index == 0) {
                index++;
                continue;
            }
            String[] split = line.split(",");
            users.add(new User(split[0], split[1], User.UserType.LOGIN));
            index++;
        }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try {
      System.out.println("Starting server");
      serverSocket = new ServerSocket(9999);
      while (true) {
        Socket socket = serverSocket.accept();
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        User user = (User) ois.readObject();
        if (user.getUserType() == UserType.LOGIN) {
          Message message = new Message(System.currentTimeMillis(), "Server", user.getUsername(), "Login", MessageType.MESSAGE_LOGIN_SUCCEED);
          if (isUserLoginValid(user.getUsername(), user.getPassword())){
            message.setMessageType(MessageType.MESSAGE_LOGIN_SUCCEED);
            oos.writeObject(message);
            ServerConnectClientThread serverConnectClientThread = new ServerConnectClientThread(socket, user.getUsername());
            serverConnectClientThread.start();
            ManageClientThreads.addClientThread(user.getUsername(), serverConnectClientThread);
            ManageClientThreads.sendAllOnlineUsers();
          } else {
            message.setMessageType(MessageType.MESSAGE_LOGIN_FAILED);
            oos.writeObject(message);
            socket.close();
          }
        } else if (user.getUserType() == UserType.REGISTER) {
          Message message = new Message(System.currentTimeMillis(), "Server", user.getUsername(), "Register", MessageType.MESSAGE_REGISTER_SUCCEED);
          if (isUserRegisterValid(user.getUsername(), user.getPassword())) {
            message.setMessageType(MessageType.MESSAGE_REGISTER_SUCCEED);
            oos.writeObject(message);
            users.add(user);
            try {
              FileWriter fw = new FileWriter("UsernameAndPassword.csv", true);
              PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
              pw.print(user.getUsername());
              pw.print(",");
              pw.print(user.getPassword());
              pw.println();
              pw.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            message.setMessageType(MessageType.MESSAGE_REGISTER_FAILED);
            oos.writeObject(message);
            socket.close();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {

    }
  }

}
