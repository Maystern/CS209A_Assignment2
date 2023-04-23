package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatClass implements Serializable {

  public enum ChatType {
    oneToOne,
    group;
  }

  private ChatType chatType;
  private String chatIndex;
  private List<Message> messages;
  private List<String> users;

  public ChatClass(ChatType chatType, String chatIndex) {
    this.chatType = chatType;
    this.chatIndex = chatIndex;
    messages = new ArrayList<>();
    users = new ArrayList<>();
  }

  public ChatType getChatType() {
    return chatType;
  }

  public String getChatIndex() {
    return chatIndex;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }

  @Override
  public String toString() {
    String str = "";
    if (chatType == ChatType.oneToOne) {
      if (Controller.userExistInOnlineUsers(users.get(1))) {
        str += "[Online] ";
      } else {
        str += "[Offline] ";
      }
      str = str + users.get(1);
    } else {
      if (users.size() > 3) {
        users.sort(String::compareTo);
        str =
            str + users.get(0) + ", " + users.get(1) + ", " + users.get(2) + "... (" + users.size()
                + ")";
      } else {
        users.sort(String::compareTo);
        str = str + String.join(", ", users) + " (" + users.size() + ")";
      }
    }
    Long latestMessageStamp = -1L;
    for (Message message : messages) {
      if (message.getTimestamp() > latestMessageStamp) {
        latestMessageStamp = message.getTimestamp();
      }
    }
    String latestTime = " [";
    if (latestMessageStamp.equals(-1L)) {
      latestTime = latestTime + "None]";
    } else {
      latestTime = latestTime + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
          new java.util.Date(latestMessageStamp)) + "]";
    }
    str += latestTime;
    return str;
  }

  public void addMessage(Message message) {
    messages.add(message);
  }

  public void addMessagesAll(List<Message> messages) {
    this.messages.addAll(messages);
  }

  public void addUsers(String user) {
    users.add(user);
  }

  public void addUsersAll(List<String> users) {
    this.users.addAll(users);
  }

  public List<String> getUsers() {
    return users;
  }

  public Long getLatestMessageStamp() {
    Long latestMessageStamp = -1L;
    for (Message message : messages) {
      if (message.getTimestamp() > latestMessageStamp) {
        latestMessageStamp = message.getTimestamp();
      }
    }
    return latestMessageStamp;
  }
}
