package com.guiLayout.controllers;

import com.oopModel.account.AdminAccount;
import com.oopModel.account.EndUserAccount;
import com.oopModel.storage.DataStructures;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;

public class LoginController {
    @FXML
    private Label loginErrorLab;
    @FXML
    private TextField uNameField;
    @FXML
    private PasswordField pWordField;
    @FXML
    private Button loginButton;


    private Scene euLandingScene;
    private Scene adminLandingScene;


    private DataStructures dataStructures;
    private HashMap<String, EndUserAccount> accountsDictionary;
    private boolean setDataStructures;

    public void initialize() {
        dataStructures = new DataStructures();
        accountsDictionary = dataStructures.getAllAccountsUNameDict();
        setDataStructures = true;
    }

    @FXML protected void attemptLogin() throws SQLException, NoSuchAlgorithmException {
        loginErrorLab.setText("");
        if (validateLogin()) {
            EndUserAccount accountToLoginTo = accountsDictionary.get(uNameField.getText());
            if (!accountToLoginTo.compareToPassword(pWordField.getText())) {
                loginErrorLab.setText("Error - incorrect password");
            } else {
                openLandingScene(accountToLoginTo);
            }
        }

        uNameField.clear();
        pWordField.clear();
    }

    public boolean validateLogin() {
        boolean loginValid = false;
        String uName = uNameField.getText();
        if (uName.equals("")) {
            loginErrorLab.setText("Error - no username entered");
        } else if (pWordField.getText().equals("")) {
            loginErrorLab.setText("Error - no password entered");
        } else if (!accountsDictionary.containsKey(uName)) {
            loginErrorLab.setText("Error - account with username '" + uName + "' does not exist");
        } else {
            loginValid = true;
        }
        return loginValid;
    }

    //Parts of the methods below are borrowed code from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class
    public void setAdminLandingScene(Scene scene) {adminLandingScene = scene;}
    public void openLandingScene(EndUserAccount account) throws SQLException {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        dataStructures.setAccountLoggedIn(account);
        dataStructures.initialiseStructures();
        Scene sceneToSet;
        if (account instanceof AdminAccount) {
            sceneToSet = adminLandingScene;
        } else {
            sceneToSet = euLandingScene;
        }
        if (setDataStructures) {
            stage.setUserData(dataStructures);
            setDataStructures = false;
        }
        stage.setScene(sceneToSet);
    }


}