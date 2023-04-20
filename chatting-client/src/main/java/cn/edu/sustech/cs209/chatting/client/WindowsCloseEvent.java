package cn.edu.sustech.cs209.chatting.client;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.WindowEvent;

public class WindowsCloseEvent implements EventHandler<WindowEvent> {
    private FXMLLoader fxmlLoader;
    public WindowsCloseEvent(FXMLLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }
    @Override
    public void handle(WindowEvent event) {
        event.consume();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure to exit?");
        alert.setContentText("Press OK to exit, or press Cancel to cancel.");
        ButtonType OKButtonType = new ButtonType("OK", ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(OKButtonType, cancelButtonType);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData().isDefaultButton()) {
                Controller controller = fxmlLoader.getController();
                controller.close();
                System.exit(0);
            }
        });
    }
}
