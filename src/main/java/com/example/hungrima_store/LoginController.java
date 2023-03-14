package com.example.hungrima_store;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button bt_login;
    @FXML
    private Button button_signup;
    @FXML
    private ImageView exit;
    @FXML
    private TextField tf_username;
    @FXML
    private TextField tf_password;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bt_login.setOnAction(event -> DBUtils.logInUser(event, tf_username.getText(),tf_password.getText()));

        button_signup.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "SignUp.fxml", "Sign Up!",625,400);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        exit.setOnMouseClicked(actionEvent -> System.exit(0));
    }
}
