package cn.edu.sustech.cs209.chatting.common;

public interface MessageType {
  String MESSAGE_LOGIN_SUCCEED = "1"; // 登录成功
  String MESSAGE_LOGIN_FAILED = "2"; // 登录失败
  String MESSAGE_REGISTER_SUCCEED = "3"; // 注册成功
  String MESSAGE_REGISTER_FAILED = "4"; // 注册失败
  String MESSAGE_GET_ONLINE_USER_LISTS = "5"; // 获取在线用户列表
  String MESSAGE_LOGOUT = "6"; // 注销
  String MESSAGE_SEND_TO_ONE = "7"; // 发送消息给某人
}
