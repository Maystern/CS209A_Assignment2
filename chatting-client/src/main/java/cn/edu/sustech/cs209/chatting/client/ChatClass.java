package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.ArrayList;
import java.util.List;

public class ChatClass {
    public enum ChatType {
        oneToOne, group;
    };
    private ChatType chatType;
    private String chatIndex;
    List<Message> messages;
    List<String> users;
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
    public void setUsers(List<String> users) {
        this.users = users;
    }
}
