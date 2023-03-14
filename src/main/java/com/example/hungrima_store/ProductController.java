package com.example.hungrima_store;

import com.example.hungrima_store.model.productsModel;
import io.github.palexdev.materialfx.controls.*;

import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class ProductController implements Initializable {
    @FXML
    private MFXButton bt_home;
    @FXML
    private MFXButton bt_add;
    @FXML
    private MFXButton bt_update;
    @FXML
    private MFXButton bt_delete;
    @FXML
    private MFXTextField tf_productname;
    @FXML
    private MFXTextField tf_price;

    @FXML
    private MFXTextField tf_productid;

    @FXML
    private MFXTableView tb_product;
    static ObservableList<productsModel> record;
    Connection connection = null;
    PreparedStatement psInsert = null;
    PreparedStatement psCheckProductExists = null;
    PreparedStatement psCheckId = null;
    ResultSet resultSet = null;
    ResultSet rs = null;

    public ObservableList<productsModel> loadTable() throws SQLException {
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        psCheckProductExists = connection.prepareStatement("SELECT * FROM products ORDER BY p_id DESC");
        resultSet = psCheckProductExists.executeQuery();
        List<productsModel> products= new ArrayList<>();
        while(resultSet.next()){
            String pName = resultSet.getString("pname");
            String price = String.valueOf(resultSet.getInt("price"));
            String p_id = String.valueOf(resultSet.getInt("p_id"));
            products.add(new productsModel(pName,price,p_id));
        }
        ObservableList<productsModel> p = FXCollections.observableArrayList(products);
        return p;
    };
    public void populateTable(){
            MFXTableColumn<productsModel> nameColumn = new MFXTableColumn<>("Product Name", true, Comparator.comparing(productsModel::getPname));
            MFXTableColumn<productsModel> priceColumn = new MFXTableColumn<>("price", true, Comparator.comparing(productsModel::getPrice));
            MFXTableColumn<productsModel> idColumn = new MFXTableColumn<>("Product Id", true, Comparator.comparing(productsModel::getP_id));
            idColumn.setRowCellFactory(product -> new MFXTableRowCell<>(productsModel::getP_id));
            nameColumn.setRowCellFactory(product -> new MFXTableRowCell<>(productsModel::getPname));
            priceColumn.setRowCellFactory(product -> new MFXTableRowCell<>(productsModel::getPrice));
            tb_product.getTableColumns().addAll(idColumn,nameColumn,priceColumn);
            tb_product.getFilters().addAll(
                    new StringFilter<>("Product Id", productsModel::getP_id),
                    new StringFilter<>("Product Name", productsModel::getPname),
                    new StringFilter<>("Price", productsModel::getPrice)
            );
            tb_product.setItems(record);
            tb_product.autosizeColumnsOnInitialization();
    }
    public void reload() throws SQLException {
        record.removeAll();
        record = loadTable();
        tb_product.setItems(record);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            record = loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        populateTable();
        //get table values
        tb_product.getSelectionModel().selectionProperty().addListener((MapChangeListener) e->{
            ObservableMap listValues = tb_product.getSelectionModel().getSelection();
            ObservableList<productsModel> namesList = FXCollections.observableArrayList(listValues.values());
            tf_productname.setText(namesList.listIterator().next().getPname());
            tf_price.setText(namesList.listIterator().next().getPrice());
            tf_productid.setText(namesList.listIterator().next().getP_id());
        });

        bt_home.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "home.fxml", "Home",1286,800);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bt_add.setOnAction(event -> {
            if(tf_productname.getText().isEmpty() && tf_price.getText().isEmpty()){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Fill in the fields");
                    alert.show();

                }else{
                    try {
                        try {
                            connection = new DBConnector().getConnection();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        psCheckProductExists = connection.prepareStatement("SELECT * FROM products WHERE pname = ?");
                        psCheckProductExists.setString(1, tf_productname.getText());
                        resultSet = psCheckProductExists.executeQuery();

                        if (resultSet.isBeforeFirst()){
                            System.out.println("Product already exists");
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setContentText("You cannot use this product twice.");
                            alert.show();
                        }else{
                            psInsert = connection.prepareStatement("INSERT INTO products(pname, price) VALUES (?, ?)");
                            psInsert.setString(1, tf_productname.getText());
                            psInsert.setInt(2, Integer.parseInt(tf_price.getText()));
                            psInsert.executeUpdate();
                            // Commit the changes to the database
                            connection.commit();
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("Product added successfully");
                            alert.show();
                        }
                        tf_price.setText("");
                        tf_productname.setText("");
                        tb_product.update();
                        reload();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if(resultSet != null){
                            try {
                                resultSet.close();
                            }catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        if (psCheckProductExists!= null) {
                            try {
                                psCheckProductExists.close();
                            }catch (SQLException e){
                                e.printStackTrace();
                            }
                        }
                        if (psInsert != null) {
                            try {
                                psInsert.close();
                            }catch (SQLException e){
                                e.printStackTrace();
                            }
                        }
                        if (connection != null) {
                            try{
                                connection.close();
                            }catch (SQLException e){
                                e.printStackTrace();
                            }
                        }
                    }
        }});

        bt_delete.setOnAction(event -> {

            if(tf_productname.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter product to be deleted");
                alert.show();
            }else{
            try {
                try {
                    connection = new DBConnector().getConnection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                psCheckProductExists = connection.prepareStatement("SELECT * FROM products WHERE pname = ?");
                psCheckProductExists.setString(1, tf_productname.getText());
                resultSet = psCheckProductExists.executeQuery();

                if (resultSet.isBeforeFirst()){
                    psInsert = connection.prepareStatement("DELETE FROM products WHERE pname= ?");
                    psInsert.setString(1, tf_productname.getText());
                    psInsert.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Product deleted successfully");
                    alert.show();
                    reload();

                }else{
                    System.out.println("Product doesn't exists");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Not found!!!");
                    alert.show();
                }
                tf_price.setText("");
                tf_productname.setText("");
                tf_productid.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if(resultSet != null){
                    try {
                        resultSet.close();
                    }catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (psCheckProductExists!= null) {
                    try {
                        psCheckProductExists.close();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }
                if (psInsert != null) {
                    try {
                        psInsert.close();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    try{
                        connection.close();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }
            }
        }});
        bt_update.setOnAction( event ->{
            if(tf_productname.getText().isEmpty()||tf_price.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Fill in the fields");
                alert.show();

            }else{
                try {

                        psCheckProductExists = connection.prepareStatement("UPDATE products SET pname = ?, price = ? WHERE p_id = ?");
                        psCheckProductExists.setString(1, tf_productname.getText());
                        psCheckProductExists.setInt(2, Integer.parseInt(tf_price.getText()));
                        psCheckProductExists.setInt(3, Integer.parseInt(tf_productid.getText()));
                        psCheckProductExists.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Product updated successfully");
                        alert.show();
                        tf_price.setText("");
                        tf_productname.setText("");
                        tf_productid.setText("");
                        reload();



                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if(psCheckId != null){
                        try {
                            psCheckId.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(resultSet != null){
                        try {
                            resultSet.close();
                        }catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (psCheckProductExists!= null) {
                        try {
                            psCheckProductExists.close();
                        }catch (SQLException e){
                            e.printStackTrace();
                        }
                    }
                    if (psInsert != null) {
                        try {
                            psInsert.close();
                        }catch (SQLException e){
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        try{
                            connection.close();
                        }catch (SQLException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
