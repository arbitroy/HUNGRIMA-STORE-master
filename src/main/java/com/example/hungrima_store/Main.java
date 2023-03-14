package com.example.hungrima_store;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600,400);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.centerOnScreen();
        String imagePath = "/com/example/hungrima_store/Images/Lg.png";
        URL imageURL = getClass().getResource(imagePath);
        if (imageURL == null) {
            // Handle the case when the image is not found
            System.out.println("no icon");
        } else {
            Image image = new Image(imageURL.toExternalForm());
            stage.getIcons().add(image);
        }
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}