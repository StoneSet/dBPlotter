module com.dlraudio.dbplotter {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.fazecast.jSerialComm;
    requires java.desktop;

    opens com.dlraudio.dbplotter to javafx.fxml;
    opens com.dlraudio.dbplotter.controller to javafx.fxml;

    exports com.dlraudio.dbplotter;
    exports com.dlraudio.dbplotter.controller;
    exports com.dlraudio.dbplotter.service;
    opens com.dlraudio.dbplotter.service to javafx.fxml;
}
