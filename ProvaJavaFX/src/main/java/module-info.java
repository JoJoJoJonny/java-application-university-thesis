module client_group.provajavafx {
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.json;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires fontawesomefx;
    requires java.desktop;
    requires com.flexganttfx.view;
    requires com.flexganttfx.model;
    requires com.flexganttfx.core;
    requires javafx.graphics;
    requires javafx.controls;

    opens client_group to javafx.fxml;
    exports client_group;
    exports client_group.controller;
    exports client_group.dto;
    opens client_group.controller to javafx.fxml;
    opens client_group.model to javafx.base;
    requires javafx.swing;

    exports client_group.model to com.fasterxml.jackson.databind;

}