package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class User implements Serializable {

  private String userName;
  private String password;

  public enum UserType {LOGIN, REGISTER}

  ;
  private UserType userType;

  public User(String userName, String password, UserType userType) {
    this.userName = userName;
    this.password = password;
    this.userType = userType;
  }

  public String getUsername() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public UserType getUserType() {
    return userType;
  }
}
