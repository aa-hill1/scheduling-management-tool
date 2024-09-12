package com.guiLayout.controllers.admin;

import com.oopModel.storage.DataStructures;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class AdminLandingController {
    @FXML
    private Label logoutTimer;
    @FXML
    private Label currentAccount;
    @FXML
    private Button logoutButton;
    @FXML
    private Button adminViewButton;
    @FXML
    private Button adminEditButton;
    @FXML
    private AnchorPane anchorPane;

    private Scene loginScene;
    private Scene adminEditScene;
    private Scene adminViewScene;
    private Stage stage;

    private boolean onSceneOpen;
    DataStructures dataStructures;

    public void initialize() {
        onSceneOpen = true;
    }
    @FXML protected void setUpScene() {
        if (onSceneOpen) {
            stage = (Stage) currentAccount.getScene().getWindow();
            dataStructures = (DataStructures) stage.getUserData();
            currentAccount.setText(dataStructures.getAccountLoggedIn().getUsername());
            onSceneOpen = false;
        }
        currentAccount.setText(dataStructures.getAccountLoggedIn().getUsername());
    }


    //The methods below are borrowed code from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class
    public void setLoginScene(Scene scene) {loginScene=scene;}
    @FXML protected void openLoginScene() {
        stage.setScene(loginScene);
    }

    public void setAdminEditScene(Scene scene) {adminEditScene=scene;}
    @FXML protected void openAdminEditScene() {
        stage.setScene(adminEditScene);
    }

    public void setAdminViewScene(Scene scene) {adminViewScene=scene;}
    @FXML protected void openAdminViewScene() {
        stage.setScene(adminViewScene);
    }
}
