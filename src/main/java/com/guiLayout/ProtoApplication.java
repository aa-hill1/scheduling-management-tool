package com.guiLayout;

import com.guiLayout.controllers.*;
import com.guiLayout.controllers.admin.AdminEditController;
import com.guiLayout.controllers.admin.AdminLandingController;
import com.guiLayout.controllers.admin.AdminViewController;
import com.guiLayout.controllers.endUser.EULandingController;
import com.guiLayout.controllers.endUser.EURoutesController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProtoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException, ClassNotFoundException {
        stage.setTitle("ProtoApplication");

        //Login Screen Loader
        FXMLLoader loginLoader = new FXMLLoader(ProtoApplication.class.getResource("loginPage.fxml"));
        Scene loginScene = new Scene(loginLoader.load(), 350, 250);

        //End User Landing Loader
        FXMLLoader euLandingLoader = new FXMLLoader(ProtoApplication.class.getResource("endUserLanding.fxml"));
        Scene euLandingScene = new Scene(euLandingLoader.load(), 1000, 700);

        //End User Route Loader
        FXMLLoader euRouteLoader = new FXMLLoader(ProtoApplication.class.getResource("endUserRoutes.fxml"));
        Scene euRouteScene = new Scene(euRouteLoader.load(), 1000, 500);

        //Admin Landing Loader
        FXMLLoader adminLandingLoader = new FXMLLoader(ProtoApplication.class.getResource("adminLanding.fxml"));
        Scene adminLandingScene = new Scene(adminLandingLoader.load(), 400, 250);

        //Admin View Loader
        FXMLLoader adminViewLoader = new FXMLLoader(ProtoApplication.class.getResource("adminView.fxml"));
        Scene adminViewScene = new Scene(adminViewLoader.load(), 1000, 500);

        //Admin Edit Loader
        FXMLLoader adminEditLoader = new FXMLLoader(ProtoApplication.class.getResource("adminEdit.fxml"));
        Scene adminEditScene = new Scene(adminEditLoader.load(), 650, 650);

        //The code used for the setScene() methods below is borrowed
        // from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class

        //Login Controller, setting EULanding and AdminLanding Scenes
        LoginController loginController = loginLoader.getController();
        loginController.setEUlandingScene(euLandingScene);
        loginController.setAdminLandingScene(adminLandingScene);

        //End User Landing Controller, setting EURoute and Login Scenes
        EULandingController euLandingController = euLandingLoader.getController();
        euLandingController.setLoginScene(loginScene);
        euLandingController.setEURouteScene(euRouteScene);

        //End User Route Controller, setting EULanding and Login Scenes
        EURoutesController euRoutesController = euRouteLoader.getController();
        euRoutesController.setLoginScene(loginScene);
        euRoutesController.setEULandingScene(euLandingScene);

        //AdminLanding Controller, setting AdminEdit, AdminView and Login Scenes
        AdminLandingController adminLandingController = adminLandingLoader.getController();
        adminLandingController.setAdminEditScene(adminEditScene);
        adminLandingController.setAdminViewScene(adminViewScene);
        adminLandingController.setLoginScene(loginScene);

        //Admin View Controller, setting AdminLanding and Login Scenes
        AdminViewController adminViewController = adminViewLoader.getController();
        adminViewController.setAdminLandingScene(adminLandingScene);
        adminViewController.setLoginScene(loginScene);

        //Admin Edit Controller, setting AdminLanding and Login Scenes
        AdminEditController adminEditController = adminEditLoader.getController();
        adminEditController.setAdminLandingScene(adminLandingScene);
        adminEditController.setLoginScene(loginScene);


        stage.setScene(loginScene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}