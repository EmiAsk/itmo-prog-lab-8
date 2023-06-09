package se.ifmo.lab08.client.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import se.ifmo.lab08.client.App;
import se.ifmo.lab08.client.manager.CommandManager;
import se.ifmo.lab08.client.network.Client;
import se.ifmo.lab08.client.resourcebundles.enums.AvailableLocales;
import se.ifmo.lab08.client.resourcebundles.enums.MainFormElements;
import se.ifmo.lab08.client.resourcebundles.enums.RuntimeOutputs;
import se.ifmo.lab08.client.tablehandlers.TableViewHandler;
import se.ifmo.lab08.client.util.AlertPrinter;
import se.ifmo.lab08.client.util.BufferPrinter;
import se.ifmo.lab08.client.util.NotificationPrinter;
import se.ifmo.lab08.common.dto.Role;
import se.ifmo.lab08.common.dto.request.FlatCollectionRequest;
import se.ifmo.lab08.common.entity.Flat;
import se.ifmo.lab08.common.util.IOProvider;
import se.ifmo.lab08.common.util.Printer;

import javax.naming.TimeLimitExceededException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainFormController {

    @FXML
    private Menu settingsMenu;

    private Scene primaryScene;

    @FXML
    private MenuItem languageMenuItem;

    @FXML
    private MenuItem logOutMenuItem;

    @FXML
    private HBox filtersHBox;

    @FXML
    private TableView<Flat> tableView;

    private volatile static ObservableList<Flat> modelsCollection = FXCollections.observableArrayList();

    @FXML
    private Button removeFiltersButton;

    @FXML
    private Button executeScriptButton;

    @FXML
    protected Button createFilterButton;

    @FXML
    protected Button addButton;

    @FXML
    protected Button updateButton;

    @FXML
    protected Button removeByIdButton;

    @FXML
    protected Button clearButton;

    @FXML
    protected Button shuffleButton;

    @FXML
    protected Button removeByFurnishButton;

    @FXML
    protected Button removeLastButton;

    @FXML
    protected Button userButton;

    @FXML
    protected Label controllersLabel;

    @FXML
    private Menu userMenu;

    @FXML
    private Button visualizeButton;

    private Map<String, Button> buttons = new HashMap<>();


    private static SimpleObjectProperty<AvailableLocales> currentLocale = new SimpleObjectProperty<>(AvailableLocales.RUSSIAN);

    private final int VISUALIZATION_FORM_WIDTH = 800;

    private final int VISUALIZATION_FORM_HEIGHT = 600;

    private final int FILTER_CREATING_FROM_WIDTH = 400;

    private final int FILTER_CREATING_FORM_HEIGHT = 300;

    private final int ADDITIONAL_FORM_WIDTH = 300;

    private final int ADDITIONAL_FORM_HEIGHT = 200;

    private final int MUSIC_BAND_CREATING_AND_UPDATING_FORM_HEIGHT = 600;

    private final int MUSIC_BAND_CREATING_AND_UPDATING_FORM_WIDTH = 400;

    private final int LANGUAGE_CHANGING_FORM_WIDTH = 300;

    private final int LANGUAGE_CHANGING_FORM_HEIGHT = 200;

    private volatile TableViewHandler tableViewHandler;

    private static MainFormController mainFormController;

    private IOProvider provider = new IOProvider(new Scanner(System.in), new NotificationPrinter());

    private CommandManager commandManager = new CommandManager(Client.getInstance(), provider, 0);

    private Client client = Client.getInstance();

    private Printer errorPrinter = new AlertPrinter();


    @FXML
    public void initialize() {
        buttons.put("add", addButton);
        buttons.put("update", updateButton);
        buttons.put("remove_by_id", removeByIdButton);
        buttons.put("clear", clearButton);
        buttons.put("remove_all_by_furnish", removeByFurnishButton);
        buttons.put("remove_last", removeLastButton);
        buttons.put("shuffle", shuffleButton);
        buttons.put("execute_script", executeScriptButton);

        handleRoleChange(client.credentials().getUsername(), client.credentials().getRole());

        mainFormController = this;
        tableViewHandler = new TableViewHandler(tableView, modelsCollection);
        tableViewHandler.initializeColumns();
        currentLocale.addListener(change -> updateLocale());
        updateLocale();
        try {
            Client.getInstance().send(new FlatCollectionRequest());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(RuntimeOutputs.FAILED_TO_LOAD_DATA.toString());
            alert.show();
            throw new RuntimeException(e);
        }
    }

    private void updateLocale() {
        tableViewHandler.initializeColumns();

        removeFiltersButton.setText(MainFormElements.REMOVE_FILTERS_BUTTON.toString());
        createFilterButton.setText(MainFormElements.CREATE_FILTER_BUTTON.toString());
        addButton.setText(MainFormElements.ADD_BUTTON.toString());
        updateButton.setText(MainFormElements.UPDATE_BUTTON.toString());
        removeLastButton.setText(MainFormElements.REMOVE_LAST_BUTTON.toString());
        removeByFurnishButton.setText(MainFormElements.REMOVE_BY_FURNISH_BUTTON.toString());
        removeByIdButton.setText(MainFormElements.REMOVE_BY_ID_BUTTON.toString());
        shuffleButton.setText(MainFormElements.SHUFFLE_BUTTON.toString());
        clearButton.setText(MainFormElements.CLEAR_BUTTON.toString());
        controllersLabel.setText(MainFormElements.CONTROLLERS_LABEL.toString());
        settingsMenu.setText(MainFormElements.SETTINGS_MENU.toString());
        logOutMenuItem.setText(MainFormElements.LOG_OUT_MENU_ITEM.toString());
        languageMenuItem.setText(MainFormElements.LANGUAGE_MENU_ITEM.toString());
        executeScriptButton.setText(MainFormElements.EXECUTE_SCRIPT_BUTTON.toString());
        visualizeButton.setText(MainFormElements.VISUALIZE_BUTTON.toString());
        userMenu.setText(MainFormElements.USER_INFO_LABEL.toString().formatted(client.credentials().getUsername(), client.credentials().getRole()));
        userButton.setText(MainFormElements.USER_BUTTON.toString());
    }

    @FXML
    protected void onAddButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        try {
            button.setDisable(true);
            FlatCreatingAndUpdatingFormController flatCreatingAndUpdatingFormController = initCreatingForm();
            Flat flat = flatCreatingAndUpdatingFormController.getFlat();
            if (flat == null) return;
            commandManager.executeServerCommand("add", new String[]{}, flat);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
//        Button button = (Button) actionEvent.getSource();
//        try {
//            button.setDisable(true);
//            FlatCreatingAndUpdatingFormController musicBandCreatingAndUpdatingFormController = initCreatingForm();
//            Map<DataField, Object> data = musicBandCreatingAndUpdatingFormController.getData();
//            if (data == null) return;
//            Command command = new AddCommand(data, Invoker.getInstance());
//            Invoker.getInstance().invokeCommand(command);
//        } catch (IOException exception) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        } finally {
//            button.setDisable(false);
//        }
    }

    @FXML
    protected void onRemoveLastButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        try {
            button.setDisable(true);
            if (modelsCollection.isEmpty()) {
                return;
            }
            Flat flat = tableView.getItems().get(tableView.getItems().size() - 1);
            if (!checkModelUserId(flat)) return;
            commandManager.executeServerCommand("remove_by_id", new String[]{String.valueOf(flat.getId())}, null);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
    }

    @FXML
    protected void onClearButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        try {
            button.setDisable(true);
            commandManager.executeServerCommand("clear", new String[]{}, null);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
    }

    private void prepareAndInvokeRemoveByIdCommand(String value) {
//        if (FlatFieldsValidators.bandIdValidate(value)) {
//            long id = Long.parseLong(value);
//            if (modelsCollection.stream().map(Flat::getId).noneMatch(x -> x == id)) {
//                Notifications.create().position(Pos.TOP_CENTER).text(CommandsAnswers.ID_DOES_NOT_EXIST.toString()).show();
//                return;
//            }
//            Command command = new RemoveByIdCommand(Invoker.getInstance());
//            Invoker.getInstance().invokeCommand(command, value);
//        }
//        else {
//            Notifications.create().position(Pos.TOP_CENTER).text(CommandsAnswers.ADD_IF_MIN_COMMAND_ID_IN_WRONG_FORMAT.toString()).show();
//        }
    }

    @FXML
    protected void onFilterCreatingButtonPressed(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controller/FilterCreatorForm.fxml"));
            Parent node = fxmlLoader.load();
            FilterCreatorFormController controller = fxmlLoader.getController();
            Scene scene = new Scene(node, FILTER_CREATING_FROM_WIDTH, FILTER_CREATING_FORM_HEIGHT);
            Stage stage = new Stage();
            stage.getIcons().add(new Image("main.ico"));
            stage.setTitle("Flat Realtor");
            stage.setResizable(false);
            controller.setCurrentStage(stage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        }
//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FilterCreatorForm.fxml"));
//            Parent node = fxmlLoader.load();
//            FilterCreatorFormController filterCreatorFormController = fxmlLoader.getController();
//            Scene scene = new Scene(node, FILTER_CREATING_FROM_WIDTH, FILTER_CREATING_FORM_HEIGHT);
//            Stage stage = new Stage();
//            stage.setResizable(false);
//            filterCreatorFormController.setCurrentStage(stage);
//            stage.setScene(scene);
//            stage.show();
//        } catch (IOException exception) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        }
    }

    @FXML
    protected void onFilterRemovingButtonPressed(ActionEvent actionEvent) {
        filtersHBox.getChildren().clear();
    }

    @FXML
    protected void onFilterNameButtonPressed(ActionEvent actionEvent) {
//        Button button = (Button) actionEvent.getSource();
//        try {
//            button.setDisable(true);
//            AdditionalFormOfDataCollectionController additionalFormOfDataCollectionController = initAdditionalForm();
//            String value = additionalFormOfDataCollectionController.getResult();
//            if (!value.isBlank() && FlatFieldsValidators.personHeightCheck(value)) {
//                value = value.replace(",", ".");
//                Command command = new FilterLessThanFrontManCommand(filtersHBox);
//                Invoker.getInstance().invokeCommand(command, value);
//            } else {
//                Notifications.create().text(CommandsAnswers.COUNT_GREATER_THAN_FRONT_MAN_NOT_EXECUTED.toString()).position(Pos.TOP_CENTER).show();
//            }
//        } catch (IOException exception) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        } finally {
//            button.setDisable(false);
//        }
    }

    protected ChooseFurnishFormController initChooseFurnishForm() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controller/ChooseFurnishForm.fxml"));
        Parent node = fxmlLoader.load();
        Scene scene = new Scene(node);
        Stage stage = new Stage();
        stage.getIcons().add(new Image("main.ico"));
        stage.setTitle("Flat Realtor");
        ChooseFurnishFormController controller = fxmlLoader.getController();
        controller.setCurrentStage(stage);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
        return controller;
    }

    @FXML
    protected void onRemoveByIdButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        try {
            button.setDisable(true);
            Flat flat = tableView.getSelectionModel().getSelectedItem();
            if (flat == null) {
                provider.getPrinter().print(RuntimeOutputs.FLAT_WAS_NOT_SELECTED_IN_TABLE.toString());
                return;
            }
            if (!checkModelUserId(flat)) return;
            commandManager.executeServerCommand("remove_by_id", new String[]{String.valueOf(flat.getId())}, null);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
//        Button button = (Button) actionEvent.getSource();
//        try {
//            button.setDisable(true);
//            Flat musicBand = (Flat) tableView.getSelectionModel().getSelectedItem();
//            if (musicBand != null) {
//                if (!checkModelUserId(musicBand)) return;
//                Command command = new RemoveByIdCommand(Invoker.getInstance());
//                Invoker.getInstance().invokeCommand(command, String.valueOf(musicBand.getId()));
//            } else {
//                IPrinter printer = Invoker.getInstance().getPrinter();
//                printer.print(RuntimeOutputs.MODEL_WAS_NOT_SELECTED_IN_TABLE.toString());
//            }
//        } finally {
//            button.setDisable(false);
//        }
    }

    @FXML
    protected void onShuffleButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        try {
            button.setDisable(true);
            commandManager.executeServerCommand("shuffle", new String[]{}, null);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
//        Button button = (Button) actionEvent.getSource();
//        try {
//            button.setDisable(true);
//            AdditionalFormOfDataCollectionController additionalFormOfDataCollectionController = initAdditionalForm();
//            String value = additionalFormOfDataCollectionController.getResult();
//            Command command = new CountGreaterThanFrontManCommand(tableViewHandler.getSortedList().toArray(Flat[]::new));
//            Invoker.getInstance().invokeCommand(command, value);
//        } catch (IOException exception) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        } finally {
//            button.setDisable(false);
//        }
    }

    @FXML
    protected void onExecuteScriptButtonPressed(ActionEvent actionEvent) {
        var button = (Button) actionEvent.getSource();
        try {
            button.setDisable(true);
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(new Stage());
            if (file == null || !file.exists()) {
                return;
            }
            var builder = new StringBuilder();
            var provider = new IOProvider(new Scanner(System.in), new BufferPrinter(builder));
            var manager = new CommandManager(Client.getInstance(), provider, 0);
            if (!manager.executeClientCommand("execute_script", new String[]{file.getAbsolutePath()})) {
                this.provider.getPrinter().print(RuntimeOutputs.FAILED_TO_EXECUTE_COMMAND.toString());
                return;
            }
            this.provider.getPrinter().print(RuntimeOutputs.COMMAND_EXECUTED.toString());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controller/TextAreaForm.fxml"));
            Parent node = fxmlLoader.load();
            TextAreaForm controller = fxmlLoader.getController();
            controller.setTextArea(builder.toString());
            Scene scene = new Scene(node);
            Stage stage = new Stage();
            stage.setTitle("Flat Realtor");
            stage.setResizable(false);
            stage.getIcons().add(new Image("main.ico"));
            stage.setScene(scene);
            stage.show();
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
//        FileChooser fileChooser = new FileChooser();
//        File file = fileChooser.showOpenDialog(new Stage());
//        if (file != null && file.exists()) {
//            Command command = new ExecuteScriptCommand(Invoker.getInstance().getPrinter());
//            Invoker.getInstance().invokeCommand(command, file.getAbsolutePath());
//        }
    }

//    @FXML
//    protected void onAddIfMinButtonPressed(ActionEvent actionEvent) {
//        Button button = (Button) actionEvent.getSource();
//        try {
//            button.setDisable(true);
//            FlatCreatingAndUpdatingFormController flatCreatingAndUpdatingFormController = initCreatingForm();
//            Flat flat = flatCreatingAndUpdatingFormController.getFlat();
//            if (flat == null) return;
//            commandManager.executeServerCommand("add_if_min", new String[]{}, flat);
//        } catch (IOException | TimeLimitExceededException | ClassNotFoundException e) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText("Error: " + e);
//            alert.show();
//            throw new RuntimeException(e);
//        } finally {
//            button.setDisable(false);
//        }
//    }

    @FXML
    protected void onRemoveByFurnishButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        button.setDisable(true);

        try {
            var controller = initChooseFurnishForm();
            var furnish = controller.getFurnish();
            if (furnish == null) {
                return;
            }
            commandManager.executeServerCommand("remove_all_by_furnish", new String[]{furnish.name()}, null);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
    }

    @FXML
    protected void onVisualizationButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Stage stage = App.getPrimaryStage();
        try {
            button.setDisable(true);
            FXMLLoader fxmlLoader = new FXMLLoader(VisualizerFormController.class.getResource("/controller/VisualizerForm.fxml"));
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent, VISUALIZATION_FORM_WIDTH, VISUALIZATION_FORM_HEIGHT);
            stage.setResizable(false);
            stage.setScene(scene);
        } catch (IOException exception) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(exception);
        } finally {
            button.setDisable(false);
        }
//        Button button = (Button) actionEvent.getSource();
//        Stage stage = MainApplication.getPrimaryStage();
//        try {
//            button.setDisable(true);
//            FXMLLoader fxmlLoader = new FXMLLoader(VisualizerFormController.class.getResource("VisualizerForm.fxml"));
//            Parent parent = fxmlLoader.load();
//            Scene scene = new Scene(parent, VISUALIZATION_FORM_WIDTH, VISUALIZATION_FORM_HEIGHT);
//            stage.setResizable(false);
//            stage.setScene(scene);
//
//        } catch (IOException exception) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        } finally {
//            button.setDisable(false);
//        }
    }


    private void prepareAndInvokeAddIfMinCommand(String value) {
//        try {
//            if (FlatFieldsValidators.bandIdValidate(value)) {
//                long id = Long.parseLong(value);
//                if (modelsCollection.stream().map(Flat::getId).anyMatch(x -> x <= id)) {
//                    Notifications.create().position(Pos.TOP_CENTER).text(CommandsAnswers.ADD_IF_MIN_COMMAND_ID_IS_NOT_MIN.toString()).show();
//                    return;
//                }
//                FlatCreatingAndUpdatingFormController musicBandCreatingAndUpdatingFormController = initCreatingForm();
//                Map<DataField, Object> data = musicBandCreatingAndUpdatingFormController.getData();
//                Command command = new AddIfMinCommand(Invoker.getInstance(), id, data);
//                Invoker.getInstance().invokeCommand(command, value);
//            } else {
//                Notifications.create().position(Pos.TOP_CENTER).text(CommandsAnswers.ADD_IF_MIN_COMMAND_ID_IN_WRONG_FORMAT.toString()).show();
//            }
//        } catch (IOException ioException) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        }
    }

    public void handleRoleChange(String username, Role role) {
        Platform.runLater(() -> {
            try {
                userMenu.setText(MainFormElements.USER_INFO_LABEL.toString().formatted(username, role));
                var names = commandManager.getCommandNamesByRole(role);

                userButton.setVisible(false);
                for (var button : buttons.values()) {
                    button.setVisible(false);
                }
                if (role == Role.ADMIN) {
                    userButton.setVisible(true);
                }
                for (var name : names) {
                    if (buttons.containsKey(name)) {
                        buttons.get(name).setVisible(true);
                    }
                }
            } catch (IOException e) {
                errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
                throw new RuntimeException(e);
            } catch (TimeLimitExceededException ignored) {
                errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
            }
        });
    }

    private AdditionalFormOfDataCollectionController initAdditionalForm() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controller/AdditionalFormOfDataCollection.fxml"));
        Parent node = fxmlLoader.load();
        AdditionalFormOfDataCollectionController additionalFormOfDataCollectionController = fxmlLoader.getController();
        Scene scene = new Scene(node, ADDITIONAL_FORM_WIDTH, ADDITIONAL_FORM_HEIGHT);
        Stage stage = new Stage();
        stage.getIcons().add(new Image("main.ico"));
        stage.setTitle("Flat Realtor");
        additionalFormOfDataCollectionController.setCurrentStage(stage);
        stage.setScene(scene);
        stage.showAndWait();
        return additionalFormOfDataCollectionController;
    }

    @FXML
    protected void onLogOutPressed(ActionEvent event) {
        try {
            commandManager.executeServerCommand("logout", new String[]{}, null);
            Parent parent = FXMLLoader.load(getClass().getResource("/controller/AuthorizationForm.fxml"));
            Scene scene = new Scene(parent, App.MAIN_SCENE_WIDTH, App.MAIN_SCENE_HEIGHT);
            App.getPrimaryStage().setScene(scene);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        }
//        try {
//            Command command = new LogOutCommand(Invoker.getInstance());
//            Invoker.getInstance().invokeCommand(command);
//            Parent parent = FXMLLoader.load(AuthorizationFormController.class.getResource("AuthorizationForm.fxml"));
//            Scene scene = new Scene(parent, MainApplication.MAIN_SCENE_WIDTH, MainApplication.MAIN_SCENE_HEIGHT);
//            MainApplication.getPrimaryStage().setScene(scene);
//        } catch (IOException exception) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//            alert.show();
//        }
    }

    @FXML
    protected void onUpdateButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        try {
            Flat flat = tableView.getSelectionModel().getSelectedItem();
            if (flat == null) {
                provider.getPrinter().print(RuntimeOutputs.FLAT_WAS_NOT_SELECTED_IN_TABLE.toString());
                return;
            }
            if (!checkModelUserId(flat)) return;
            FlatCreatingAndUpdatingFormController controller = initUpdatingForm(flat);

            Flat updatedFlat = controller.getFlat();
            if (updatedFlat == null) return;
            commandManager.executeServerCommand("update", new String[]{String.valueOf(flat.getId())}, updatedFlat);
        } catch (IOException | ClassNotFoundException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } catch (TimeLimitExceededException ignored) {
            errorPrinter.print(RuntimeOutputs.SERVER_DOES_NOT_RESPOND.toString());
        } finally {
            button.setDisable(false);
        }
//        Button button = (Button) actionEvent.getSource();
//        try {
//            Flat musicBand = (Flat) tableView.getSelectionModel().getSelectedItem();
//            if (musicBand != null) {
//                try {
//                    if (!checkModelUserId(musicBand)) return;
//                    FlatCreatingAndUpdatingFormController musicBandCreatingAndUpdatingFormController = initCreatingForm();
//                    Map<DataField, Object> data = musicBandCreatingAndUpdatingFormController.getData();
//                    if (data == null) return;
//                    Command command = new UpdateCommand(Invoker.getInstance(), musicBand.getId(), data);
//                    Invoker.getInstance().invokeCommand(command);
//                } catch (IOException exception) {
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setContentText(RuntimeOutputs.CAN_NOT_INIT_COMPONENT.toString());
//                    alert.show();
//                }
//            } else {
//                IPrinter printer = Invoker.getInstance().getPrinter();
//                printer.print(RuntimeOutputs.MODEL_WAS_NOT_SELECTED_IN_TABLE.toString());
//            }
//        } finally {
//            button.setDisable(false);
//        }
    }

    @FXML
    protected void onLanguageMenuItemPressed(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LanguageChangingFormController.class.getResource("/controller/LanguageChangingForm.fxml"));
            Parent parent = fxmlLoader.load();
            LanguageChangingFormController languageChangingFormController = fxmlLoader.getController();
            Scene scene = new Scene(parent, LANGUAGE_CHANGING_FORM_WIDTH, LANGUAGE_CHANGING_FORM_HEIGHT);
            Stage stage = new Stage();
            stage.getIcons().add(new Image("main.ico"));
            stage.setTitle("Flat Realtor");
            languageChangingFormController.setCurrentStage(stage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onInfoMenuItemPressed(ActionEvent actionEvent) {
//        Command command = new InfoCommand(modelsCollection);
//        Invoker.getInstance().invokeCommand(command);
    }

    @FXML
    protected void onUserButtonPressed(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Stage stage = App.getPrimaryStage();
        try {
            button.setDisable(true);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controller/UserForm.fxml"));
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
        } catch (IOException e) {
            errorPrinter.print(RuntimeOutputs.SOMETHING_WENT_WRONG.toString());
            throw new RuntimeException(e);
        } finally {
            button.setDisable(false);
        }
    }


    private FlatCreatingAndUpdatingFormController initCreatingForm() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FlatCreatingAndUpdatingFormController.class.getResource("/controller/MusicBandCreatingForm.fxml"));
        Parent node = fxmlLoader.load();
        Scene scene = new Scene(node, MUSIC_BAND_CREATING_AND_UPDATING_FORM_WIDTH, MUSIC_BAND_CREATING_AND_UPDATING_FORM_HEIGHT);
        Stage stage = new Stage();
        stage.getIcons().add(new Image("main.ico"));
        stage.setTitle("Flat Realtor");
        FlatCreatingAndUpdatingFormController musicBandCreatingAndUpdatingFormController = fxmlLoader.getController();
        musicBandCreatingAndUpdatingFormController.setCurrentStage(stage);
        stage.setScene(scene);
        stage.showAndWait();
        return musicBandCreatingAndUpdatingFormController;
    }

    private FlatCreatingAndUpdatingFormController initUpdatingForm(Flat flat) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FlatCreatingAndUpdatingFormController.class.getResource("/controller/MusicBandCreatingForm.fxml"));
        Parent node = fxmlLoader.load();
        Scene scene = new Scene(node, MUSIC_BAND_CREATING_AND_UPDATING_FORM_WIDTH, MUSIC_BAND_CREATING_AND_UPDATING_FORM_HEIGHT);
        Stage stage = new Stage();
        stage.getIcons().add(new Image("main.ico"));
        stage.setTitle("Flat Realtor");
        FlatCreatingAndUpdatingFormController controller = fxmlLoader.getController();
        controller.setCurrentStage(stage);
        controller.fillIn(flat);
        stage.setScene(scene);
        stage.showAndWait();
        return controller;
    }

    private boolean checkModelUserId(Flat flat) {
        if (!Client.getInstance().credentials().getUsername().equals(flat.getOwner().getUsername())) {
            provider.getPrinter().print(RuntimeOutputs.MANAGE_FLATS.toString());
            return false;
        }
        return true;
    }

    public static MainFormController getMainFormController() {
        return mainFormController;
    }

    public static void updateCollectionList(List<Flat> newList) {
        modelsCollection.clear();
        modelsCollection.addAll(newList);
    }

    public HBox getFiltersHBox() {
        return filtersHBox;
    }

    public TableView<Flat> getTableView() {
        return tableView;
    }

    public Button getRemoveFiltersButton() {
        return removeFiltersButton;
    }

    public TableViewHandler getTableViewHandler() {
        return tableViewHandler;
    }

    public Menu getUserMenu() {
        return userMenu;
    }

    public static SimpleObjectProperty<AvailableLocales> getCurrentLocale() {
        return currentLocale;
    }

    public static void setCurrentLocale(AvailableLocales availableLocales) {
        currentLocale.set(availableLocales);
    }

    public ObservableList<Flat> getModelsCollection() {
        return modelsCollection;
    }

    public void setPrimaryScene(Scene scene) {
        this.primaryScene = scene;
    }

    public Scene getPrimaryScene() {
        return primaryScene;
    }
}