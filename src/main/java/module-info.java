module com.dlraudio.dbplotter {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.dlraudio.dbplotter to javafx.fxml;
    exports com.dlraudio.dbplotter;
}