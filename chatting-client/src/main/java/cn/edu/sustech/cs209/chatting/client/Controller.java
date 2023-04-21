package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.ChatClass.ChatType;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Pair;

public class Controller implements Initializable {
    @FXML
    Label currentOnlineCnt; // 当前在线用户数目,与JavaFX绑定
    @FXML
    ListView<Message> chatContentList; // 右侧消息面板,与JavaFX绑定
    @FXML
    ListView<ChatClass> chatList; // 左侧聊天列表,与JavaFX绑定
    @FXML
    Label currentUsername; // 当前登录用户名,与JavaFX绑定

    private ArrayList<String> OnlineUsers = new ArrayList<>(); // 当前在线用户列表

    private ArrayList<ChatClass> chatInfo = new ArrayList<>(); // 当前用户的聊天信息

    private String username; // 当前登录用户名

    private UserClientService userClientService = new UserClientService(); // 当前用户服务

    private Thread currentOnlineCntThread; // 当前用户在线数目线程,用于实时更新在线用户数目和在线用户列表

    @FXML
    private TextArea inputArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AtomicBoolean isLogin = new AtomicBoolean(false);
        AtomicInteger ButtonPressed = new AtomicInteger(0);
        while (!isLogin.get() && ButtonPressed.get() != 3) {
            // 创建登录\注册对话框
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Login and Register Dialog");
            dialog.setHeaderText("Please login or register your chat account.");
            ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
            ButtonType registerButtonType = new ButtonType("Register", ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, cancelButtonType);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField username = new TextField();
            username.setPromptText("Username");
            PasswordField password = new PasswordField();
            password.setPromptText("Password");

            grid.add(new Label("Username:"), 0, 0);
            grid.add(username, 1, 0);
            grid.add(new Label("Password:"), 0, 1);
            grid.add(password, 1, 1);

            Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
            Node registerButton = dialog.getDialogPane().lookupButton(registerButtonType);
            loginButton.setDisable(true);

            username.textProperty().addListener((observable, oldValue, newValue) -> {
                loginButton.setDisable(newValue.trim().isEmpty());
                registerButton.setDisable(newValue.trim().isEmpty());
            });


            dialog.getDialogPane().setContent(grid);

            Platform.runLater(() -> username.requestFocus());

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    ButtonPressed.set(1);
                    return new Pair<>(username.getText(), password.getText());
                }
                if (dialogButton == registerButtonType) {
                    ButtonPressed.set(2);
                    return new Pair<>(username.getText(), password.getText());
                }
                if (dialogButton == cancelButtonType) {
                    ButtonPressed.set(3);
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();
            isLogin.set(false);
            result.ifPresent(usernamePassword -> {
                if (ButtonPressed.get() == 3) { // 取消登录
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Cancel Information");
                    alert.setHeaderText("CANCEL");
                    alert.setContentText("You have cancelled the login and register.");
                    alert.showAndWait();
                    isLogin.set(false);
                } else if (ButtonPressed.get() == 2) { // 注册
                    if (userClientService.registerUser(usernamePassword.getKey(), usernamePassword.getValue())) {
                        // 注册成功
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Register Information");
                        alert.setHeaderText("SUCCESSFUL REGISTER");
                        alert.setContentText("User " + usernamePassword.getKey() + " has successfully registered.");
                        alert.showAndWait();
                        isLogin.set(false);
                    } else {
                        // 注册失败
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Register Information");
                        alert.setHeaderText("FAILED REGISTER");
                        alert.setContentText("User " + usernamePassword.getKey() + " fails to register.\nPlease check your account and password.");
                        alert.showAndWait();
                        isLogin.set(false);
                    }
                } else if (ButtonPressed.get() == 1) { // 登录
                    if (userClientService.checkUser(usernamePassword.getKey(), usernamePassword.getValue(), this)) {
                        // 登录成功
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Login Information");
                        alert.setHeaderText("SUCCESSFUL LOGIN");
                        alert.setContentText("User " + usernamePassword.getKey() + " has successfully logged in.");
                        alert.showAndWait();
                        this.username = usernamePassword.getKey();
                        isLogin.set(true);
                    } else {
                        // 登录失败
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Login Information");
                        alert.setHeaderText("FAILED LOGIN");
                        alert.setContentText("User " + usernamePassword.getKey() + " fails to login.\nPlease register the user or check your account and password.");
                        alert.showAndWait();
                        isLogin.set(false);
                    }
                }

            });
        }
        if (isLogin.get()) {
            // 登录成功
            chatContentList.setCellFactory(new MessageCellFactory());
            currentUsername.setText("Current User: " + username);
            currentOnlineCntThread = new Thread(() -> {
                // 实时更新在线用户数目和在线用户列表
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Platform.runLater(() -> {
                        currentOnlineCnt.setText("Current Online: " + userClientService.getCurrentOnlineCnt());
                        OnlineUsers.clear();
                        String[] onlineChatUsers = userClientService.getChatList().split(",");
                        for (String chatUser : onlineChatUsers) {
                            OnlineUsers.add(chatUser);
                        }
                    });
                }
            });
            currentOnlineCntThread.start();
            chatList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // 切换聊天窗口
                chatContentList.getItems().clear();
                if (newValue == null) return;

                System.out.println("all: ");
                for (ChatClass chatClass: chatInfo) {
                    System.out.println(chatClass.getChatIndex());
                }
                System.out.println("select: ");
                System.out.println(newValue.getChatIndex());


                for (ChatClass chatClass: chatInfo) {
                    if (chatClass.getChatIndex().equals(newValue.getChatIndex())) {
                        for (Message message: chatClass.getMessages()) {
                            chatContentList.getItems().add(message);
                        }
                        break;
                    }
                }
            });
        } else {
            System.exit(0);
        }
    }
    public boolean chatExistInChatList(String chatName) {
        for (ChatClass chatClass: chatInfo) {
            if (chatClass.getChatIndex().equals(chatName)) return true;
        }
        return false;
    }
    public boolean userExistInOnlineUsers(String username) {
        for (String onlineUser : OnlineUsers) {
            if (onlineUser.equals(username)) return true;
        }
        return false;
    }
    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();


        for (String OnlineUser: OnlineUsers) {
            if (!OnlineUser.equals(username)) userSel.getItems().add(OnlineUser);
        }


        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
        if (user.get() == null) return;

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name

        if (!chatExistInChatList(user.get())) {
            ChatClass chatClass = new ChatClass(ChatType.oneToOne, user.get());
            chatClass.addUsers(username);
            chatClass.addUsers(user.get());
            chatInfo.add(chatClass);
            chatList.getItems().add(chatClass);
            sortChatList();
        }
        ChatClass tmpChatClass = null;
        for (ChatClass chatClass: chatInfo) {
            if (chatClass.getChatIndex().equals(user.get())) {
                tmpChatClass = chatClass;
                break;
            }
        }
        chatList.getSelectionModel().select(tmpChatClass);
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        Stage stage = new Stage();
        ListView<String> userSel = new ListView<>();
        userSel.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        for (String OnlineUser: OnlineUsers) {
            userSel.getItems().add(OnlineUser);
        }
        Button okBtn = new Button("OK");
        AtomicBoolean createGroupChatFlag = new AtomicBoolean(false);
        okBtn.setOnAction(e -> {
            ObservableList<String> users = userSel.getSelectionModel().getSelectedItems();
            if (users.size() == 0) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Chat Information");
                alert.setHeaderText("FAILED TO CREATE GROUP CHAT");
                alert.setContentText("Please select at least one user.");
                alert.showAndWait();
                return;
            }
            boolean includeSelf = false;
            for (String user: users) {
                if (user.equals(username)) {
                    includeSelf = true;
                    break;
                }
            }
            if (!includeSelf) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Chat Information");
                alert.setHeaderText("FAILED TO CREATE GROUP CHAT");
                alert.setContentText("Please select yourself.");
                alert.showAndWait();
                return;
            }
            createGroupChatFlag.set(true);
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
        ObservableList<String> selectedUsers = userSel.getSelectionModel().getSelectedItems();
        if (!createGroupChatFlag.get()) return;
        String chatIndex = "";

        // order the selected users
        String[] selectedUsersArray = new String[selectedUsers.size()];
        for (int i = 0; i < selectedUsers.size(); i++) {
            selectedUsersArray[i] = selectedUsers.get(i);
        }
        Arrays.sort(selectedUsersArray);

        String selectUserWithoutItself = "";

        for (String str: selectedUsersArray) {
            if (!str.equals(username))
                selectUserWithoutItself += str + ",";
        }

        List<String> tmpSelectedUsers = new ArrayList<>();
        for (String selectedUser: selectedUsersArray) {
            tmpSelectedUsers.add(selectedUser);
        }

        for (String selectedUser: tmpSelectedUsers) {
            chatIndex += selectedUser + ",";
        }
        if (!chatExistInChatList(chatIndex)) {
            ChatClass chatClass = new ChatClass(ChatType.group, chatIndex);
            chatClass.addUsersAll(tmpSelectedUsers);
            chatInfo.add(chatClass);
            chatList.getItems().add(chatClass);
            sortChatList();
        }
        ChatClass tmpChatClass = null;
        for (ChatClass chatClass: chatInfo) {
            if (chatClass.getChatIndex().equals(chatIndex)) {
                tmpChatClass = chatClass;
                break;
            }
        }
        chatList.getSelectionModel().select(tmpChatClass);
        System.out.println("selectUserWithoutItself  =  " + selectUserWithoutItself);

        Message message = new Message(
            System.currentTimeMillis(),
            username, selectUserWithoutItself,
            (selectUserWithoutItself != null) ? "[System Message] " + username + " Username invites "+  selectUserWithoutItself +" and himself to join the group chat." : "[System Message] " + username + " Username invites himself to join the group chat.",
            MessageType.MESSAGE_SEND_TO_GROUP);


        userClientService.sendMessage(message);

        tmpChatClass.addMessage(message);
        chatContentList.getItems().add(message);
        sortChatList();
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        ChatClass chatClass = chatList.getSelectionModel().getSelectedItem();
        if (chatClass == null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND MESSAGE");
            alert.setContentText("Please select a chat first.");
            alert.showAndWait();
            return;
        }
        String [] sendToUsers = chatClass.getChatIndex().split(",");
        boolean allSendToUsersOnline = true;
        for (String sendToUser: sendToUsers) {
            if (sendToUser == null) continue;
            if (!userExistInOnlineUsers(sendToUser)) {
                allSendToUsersOnline = false;
                break;
            }
        }
        if (!allSendToUsersOnline && chatClass.getChatType() == ChatType.oneToOne) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND MESSAGE");
            alert.setContentText("The user you are chatting with is offline.");
            alert.showAndWait();
            return;
        }
        String msg = inputArea.getText();
        String msgType;
        Message message;
        if (chatClass.getChatType() == ChatType.group) {
            msgType = MessageType.MESSAGE_SEND_TO_GROUP;
            String sendTo = "";
            for (String user: chatClass.getUsers()) {
                if (user.equals(username)) continue;
                sendTo += user + ",";
            }
            message = new Message(System.currentTimeMillis(), username, sendTo, msg, MessageType.MESSAGE_SEND_TO_GROUP);
        } else {
            msgType = MessageType.MESSAGE_SEND_TO_ONE;
            String sendTo = chatClass.getUsers().get(1);
            message = new Message(System.currentTimeMillis(), username, sendTo, msg, MessageType.MESSAGE_SEND_TO_ONE);
        }
        if (msg.equals("")) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND MESSAGE");
            alert.setContentText("Please input message first.");
            alert.showAndWait();
            return;
        }
        userClientService.sendMessage(message);
        chatClass.addMessage(message);
        chatContentList.getItems().add(message);
        inputArea.clear();
        sortChatList();
        chatList.refresh();
    }

    @FXML
    public void doSendFile() {
        // open a dialog to select a file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to send");
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;
        ChatClass chatClass = chatList.getSelectionModel().getSelectedItem();
        if (chatClass == null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND FILE");
            alert.setContentText("Please select a chat first.");
            alert.showAndWait();
            return;
        }
        String [] sendToUsers = chatClass.getChatIndex().split(",");
        boolean allSendToUsersOnline = true;
        for (String sendToUser: sendToUsers) {
            if (sendToUser == null) continue;
            if (!userExistInOnlineUsers(sendToUser)) {
                allSendToUsersOnline = false;
                break;
            }
        }
        if (!allSendToUsersOnline && chatClass.getChatType() == ChatType.oneToOne) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND FILE");
            alert.setContentText("The user you are chatting with is offline.");
            alert.showAndWait();
            return;
        }
        String msgType;
        Message message1, message2;
        File directory = new File("");
        if (chatClass.getChatType() == ChatType.group) {
            msgType = MessageType.File_MESSAGE_SEND_TO_GROUP;
            String sendTo = "";
            for (String user: chatClass.getUsers()) {
                if (user.equals(username)) continue;
                sendTo += user + ",";
            }
            message1 = new Message(
                System.currentTimeMillis(),
                username,
                sendTo,
                "[File Delivery] User " + username + " sends you a file named \"" + file.getName() + "\".",
                MessageType.File_MESSAGE_SEND_TO_GROUP);
            message2 = new Message(
                System.currentTimeMillis(),
                username,
                sendTo,
                "[File Delivery] You send a file named \"" + file.getName() + "\" to the group.",
                MessageType.File_MESSAGE_SEND_TO_GROUP);
        } else {
            msgType = MessageType.File_MESSAGE_SEND_TO_ONE;
            String sendTo = chatClass.getUsers().get(1);
            message1 = new Message(
                System.currentTimeMillis(),
                username,
                sendTo,
                "[File Delivery] User " + username + " sends you a file named \"" + file.getName() + "\".",
                MessageType.File_MESSAGE_SEND_TO_ONE);
            message2 = new Message(
                System.currentTimeMillis(),
                username,
                sendTo,
                "[File Delivery] You send a file named \"" + file.getName() + "\" to user " + sendTo + ".",
                MessageType.File_MESSAGE_SEND_TO_ONE);
        }
        message1.setAttachmentName(file.getName());
        message2.setAttachmentName(file.getName());
        userClientService.sendFile(file, message1);
        chatClass.addMessage(message2);
        chatContentList.getItems().add(message2);
        sortChatList();
        chatList.refresh();
    }



    public void close() {
        userClientService.Logout();
        System.exit(0);
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label((String) msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    public ArrayList<String> getOneToOneChatUsers() {
        ArrayList<String> oneToOneChatUsers = new ArrayList<>();
        for (ChatClass chatClass: chatInfo) {
            if (chatClass.getChatType() == ChatType.oneToOne) {
                oneToOneChatUsers.add(chatClass.getUsers().get(1));
            }
        }
        return oneToOneChatUsers;
    }


    // get chatList
    public List<ChatClass> getChatInfo() {
        return chatInfo;
    }

    public void addChat(ChatClass chatClass) {
        chatInfo.add(chatClass);
        chatList.getItems().add(chatClass);
        sortChatList();
    }

    public void addMessageToChat(String chatName, Message msg) {
        for (ChatClass chatClass: chatInfo) {
            if (chatClass.getChatIndex().equals(chatName)) {
                chatClass.addMessage(msg);
                if (chatList.getSelectionModel().getSelectedItem() != null && chatClass.getChatIndex().equals(chatList.getSelectionModel().getSelectedItem().getChatIndex())) {
                    chatContentList.getItems().add(msg);
                }
                sortChatList();
                break;
            }
        }
    }
    public String getUsername() {
        return username;
    }
    public void sortChatList() {
        String selectedChatIndex = chatList.getSelectionModel().getSelectedItem() == null ? null : chatList.getSelectionModel().getSelectedItem().getChatIndex();

        chatList.getItems().sort(new Comparator<ChatClass>() {
            @Override
            public int compare(ChatClass o1, ChatClass o2) {
                // 按照时间大小降序排列
                if (o1.getLatestMessageStamp() == o2.getLatestMessageStamp()) {
                    return 0;
                } else if (o1.getLatestMessageStamp() < o2.getLatestMessageStamp()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        if (selectedChatIndex != null) {
            for (int i = 0; i < chatList.getItems().size(); i++) {
                if (chatList.getItems().get(i).getChatIndex().equals(selectedChatIndex)) {
                    chatList.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }
}
