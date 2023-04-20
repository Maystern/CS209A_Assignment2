package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
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
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Pair;
import jdk.vm.ci.meta.Value;

public class Controller implements Initializable {
    @FXML
    Label currentOnlineCnt;
    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<String> chatList;

    private ArrayList<String> OnlineUsers = new ArrayList<>();

    private ArrayList<String> chatUsers = new ArrayList<>();

    Map<String, ArrayList<Message>> chatContents = new HashMap<>();

    @FXML
    Label currentUsername;

    String username;

    private UserClientService userClientService = new UserClientService();

    private Thread currentOnlineCntThread;

    @FXML
    private TextArea inputArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AtomicBoolean isLogin = new AtomicBoolean(false);
        AtomicInteger ButtonPressed = new AtomicInteger(0);
        while (!isLogin.get() && ButtonPressed.get() != 3) {
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
                if (ButtonPressed.get() == 3) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Cancel Information");
                    alert.setHeaderText("CANCEL");
                    alert.setContentText("You have cancelled the login and register.");
                    alert.showAndWait();
                    isLogin.set(false);
                } else if (ButtonPressed.get() == 2) {
                    if (userClientService.registerUser(usernamePassword.getKey(), usernamePassword.getValue())) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Register Information");
                        alert.setHeaderText("SUCCESSFUL REGISTER");
                        alert.setContentText("User " + usernamePassword.getKey() + " has successfully registered.");
                        alert.showAndWait();
                        isLogin.set(false);
                    } else {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Register Information");
                        alert.setHeaderText("FAILED REGISTER");
                        alert.setContentText("User " + usernamePassword.getKey() + " fails to register.\nPlease check your account and password.");
                        alert.showAndWait();
                        isLogin.set(false);
                    }
                } else if (ButtonPressed.get() == 1) {
                    if (userClientService.checkUser(usernamePassword.getKey(), usernamePassword.getValue(), this)) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Login Information");
                        alert.setHeaderText("SUCCESSFUL LOGIN");
                        alert.setContentText("User " + usernamePassword.getKey() + " has successfully logged in.");
                        alert.showAndWait();
                        this.username = usernamePassword.getKey();
                        isLogin.set(true);
                    } else {
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
            chatContentList.setCellFactory(new MessageCellFactory());
            currentUsername.setText("Current User: " + username);
            currentOnlineCntThread = new Thread(() -> {
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
//                        String selectedUser = chatList.getSelectionModel().getSelectedItem();
//                        if (selectedUser == null) {
//                            selectedUser = "";
//                        }
//                        for (int i = 0; i < chatUsers.size(); i++) {
//                            if (!existInOnlineUsers(chatUsers.get(i))) {
//                                System.out.println("Remove " + chatUsers.get(i));
//                                chatList.getItems().remove(chatUsers.get(i));
//                                chatUsers.remove(i);
//                            }
//                        }
//                        if (existInChatList(selectedUser)) {
//                            chatList.getSelectionModel().select(selectedUser);
//                        }
                    });
                }
            });
            currentOnlineCntThread.start();
            chatList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                chatContentList.getItems().clear();
                if (newValue == null) return;
                if (chatContents.get(newValue) == null) return;
                for (Message message: chatContents.get(newValue)) {
                    chatContentList.getItems().add(message);
                }
            });
        } else {
            System.exit(0);
        }
    }
    public boolean existInChatList(String username) {
        for (String chatUser : chatUsers) {
            if (chatUser.equals(username)) return true;
        }
        return false;
    }
    public boolean existInOnlineUsers(String username) {
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

        if (!existInChatList(user.get())) {
            chatUsers.add(user.get());
            chatList.getItems().clear();
            for (String chatUser : chatUsers) {
                chatList.getItems().add(chatUser);
            }
        }

        chatList.getSelectionModel().select(user.get());

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
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        String nowSelected = chatList.getSelectionModel().getSelectedItem();
        if (!existInOnlineUsers(nowSelected)) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND MESSAGE");
            alert.setContentText("The user you are chatting with is offline.");
            alert.showAndWait();
            return;
        }
        String msg = inputArea.getText();
        if (msg.equals("")) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND MESSAGE");
            alert.setContentText("Please input message first.");
            alert.showAndWait();
            return;
        }
        String chatUser = chatList.getSelectionModel().getSelectedItem();
        if (chatUser == null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chat Information");
            alert.setHeaderText("FAILED TO SEND MESSAGE");
            alert.setContentText("Please select a chat first.");
            alert.showAndWait();
            return;
        }
        userClientService.sendMessage(chatUser, msg);
        if (chatContents.get(chatUser) == null) {
            chatContents.put(chatUser, new ArrayList<>());
        };
        Message message = new Message(System.currentTimeMillis(), username, chatUser, msg);
        chatContents.get(chatUser).add(message);
        chatContentList.getItems().add(message);
        inputArea.clear();
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
                    Label msgLabel = new Label(msg.getData());

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
    // get charUsers
    public ArrayList<String> getChatUsers() {
        return chatUsers;
    }
    // get chatList
    public ListView<String> getChatList() {
        return chatList;
    }


    public void addMessage(String User, Message msg) {
        if (chatContents.get(User) == null) {
            chatContents.put(User, new ArrayList<>());
        }
        chatContents.get(User).add(msg);
        if (User.equals(chatList.getSelectionModel().getSelectedItem())) {
            chatContentList.getItems().add(msg);
        }
    }
}
