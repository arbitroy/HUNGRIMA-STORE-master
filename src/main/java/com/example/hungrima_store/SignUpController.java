package com.example.hungrima_store;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.example.hungrima_store.DBUtils.changeScene;

public class SignUpController implements Initializable {
    @FXML
    private Button button_signup;
    @FXML
    private Button button_login;
    @FXML
    private ImageView exit;
    @FXML
    private RadioButton rb_admin;
    @FXML
    private RadioButton rb_user;
    @FXML
    private TextField tf_username;
    @FXML
    private TextField tf_password;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup toggleGroup = new ToggleGroup();
        rb_admin.setToggleGroup(toggleGroup);
        rb_user.setToggleGroup(toggleGroup);

        rb_user.setSelected(true);
        button_signup.setOnAction(event -> {
            String toggleName = ((RadioButton) toggleGroup.getSelectedToggle()).getText();

            if(!tf_username.getText().trim().isEmpty() && !tf_password.getText().trim().isEmpty()){
                try {
                    DBUtils.signUpUser(event, tf_username.getText(), tf_password.getText(), toggleName);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }else{
                System.out.println("Please fill in all information");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please fill in all information to sign up!");
                alert.show();
            }
        });

        button_login.setOnAction(event -> {
            try {
                changeScene(event, "Login.fxml", "Log in", 625, 400);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        exit.setOnMouseClicked(actionEvent -> System.exit(0));
    }
}
