package se.ifmo.lab08.client.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import se.ifmo.lab08.client.resourcebundles.enums.ChangeRoleFormElements;
import se.ifmo.lab08.common.dto.Role;

public class ChangeRoleFormController {

    private Stage currentStage;

    private Role role;

    @FXML
    private Label roleLabel;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    @FXML
    protected void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
        roleComboBox.getItems().add(null);

        MainFormController.getCurrentLocale().addListener(change -> updateLocale());
        updateLocale();
    }

    protected void updateLocale() {
        roleLabel.setText(ChangeRoleFormElements.ROLE_LABEL.toString());
        roleComboBox.setPromptText(ChangeRoleFormElements.ROLE_COMBOBOX.toString());
        okButton.setText(ChangeRoleFormElements.OK_BUTTON.toString());
        cancelButton.setText(ChangeRoleFormElements.CANCEL_BUTTON.toString());
    }

    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
    }

    @FXML
    protected void onButtonMouseEntered(MouseEvent event) {
        Button button = (Button) event.getTarget();
        button.setStyle("""
                -fx-background-color: null;
                -fx-border-width: 2;
                -fx-border-radius: 50;
                -fx-border-color: brown
                """);
    }

    @FXML
    protected void onButtonMouseExited(MouseEvent event) {
        Button button = (Button) event.getTarget();
        button.setStyle("""
                -fx-background-color: null;
                -fx-border-width: 1;
                -fx-border-radius: 50;
                -fx-border-color: brown
                """);
    }

    @FXML
    protected void onOkButtonPressed(ActionEvent actionEvent) {
        role = roleComboBox.getValue();
        if (role == null) {
            return;
        }
//        Notifications.create().text(RuntimeOutputs.FIELDS_DOES_NOT_VALID.toString()).position(Pos.TOP_CENTER).show();
        currentStage.close();
    }

    @FXML
    protected void onCloseButtonPressed(ActionEvent actionEvent) {
        currentStage.close();
    }

    public Role getRole() {
        return role;
    }
}


