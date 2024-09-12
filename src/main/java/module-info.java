module com.neaproto {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;


    opens com.guiLayout to javafx.fxml;
    exports com.guiLayout;
    exports com.guiLayout.controllers;
    opens com.guiLayout.controllers to javafx.fxml;
    exports com.guiLayout.controllers.admin;
    opens com.guiLayout.controllers.admin to javafx.fxml;
    exports com.guiLayout.controllers.endUser;
    opens com.guiLayout.controllers.endUser to javafx.fxml;
}