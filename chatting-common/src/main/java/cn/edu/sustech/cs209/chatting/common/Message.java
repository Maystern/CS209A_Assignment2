package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Message implements Serializable {

  private Long timestamp;

  private String sentBy;

  private String sendTo;

  private String data;

  private String MessageType;

  private String attachmentName;
  private byte[] attachment;

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public byte[] getAttachment() {
    return attachment;
  }

  public void setAttachment(byte[] attachment) {
    this.attachment = attachment;
  }

  public Message(Long timestamp, String sentBy, String sendTo, String data, String MessageType) {
    this.timestamp = timestamp;
    this.sentBy = sentBy;
    this.sendTo = sendTo;
    this.data = data;
    this.MessageType = MessageType;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getSentBy() {
    return sentBy;
  }

  public String getSendTo() {
    return sendTo;
  }

  public String getData() {
    return data;
  }

  public void setMessageType(String messageType) {
    MessageType = messageType;
  }

  public String getMessageType() {
    return MessageType;
  }

  @Override
  public String toString() {
    String timestampString = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
        new java.util.Date(timestamp));
    String messageTypeString = MessageType;
    return "MessageType = " + messageTypeString + "Time = " + timestampString + " sentBy = "
        + sentBy + " sendTo = " + sendTo + " data = " + data;
  }
}
