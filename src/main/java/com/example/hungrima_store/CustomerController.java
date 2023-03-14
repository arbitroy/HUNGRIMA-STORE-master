package com.example.hungrima_store;
import com.example.hungrima_store.model.customerModel;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {
    @FXML
    private MFXButton bt_home;
    @FXML
    private MFXButton bt_add;
    @FXML
    private MFXButton bt_update;
    @FXML
    private MFXButton bt_delete;
    @FXML
    private MFXTextField tf_custname;
    @FXML
    private MFXTextField tf_custid;
    @FXML
    private MFXTextField tf_phoneno;

    @FXML
    private MFXTableView tb_customers;

    static ObservableList<customerModel> record;


    Connection connection = null;
    PreparedStatement psInsert = null;
    PreparedStatement psCheckId = null;
    PreparedStatement psCheckCustomerExists = null;
    ResultSet resultSet = null;


    public  ObservableList<customerModel> loadData() throws SQLException {
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        psCheckCustomerExists = connection.prepareStatement("SELECT * FROM customers ORDER BY cust_id DESC");
        resultSet = psCheckCustomerExists.executeQuery();
        List<customerModel> customers= new ArrayList<>();
        while(resultSet.next()){
            String custName = resultSet.getString("custname");
            String phoneNo = resultSet.getString("phoneno");
            String cust_id = String.valueOf(resultSet.getInt("cust_id"));
            customers.add(new customerModel(custName,phoneNo,cust_id));
        }
        return FXCollections.observableArrayList(customers);
    }

    public void setUpTable(){
        MFXTableColumn<customerModel> nameColumn = new MFXTableColumn<>("Customer Name", true, Comparator.comparing(customerModel::getCustname));
        MFXTableColumn<customerModel> phoneNoColumn = new MFXTableColumn<>("Phone No", true, Comparator.comparing(customerModel::getPhoneno));
        MFXTableColumn<customerModel> custIdColumn = new MFXTableColumn<>("Customer Id", true, Comparator.comparing(customerModel::getCust_id));
        nameColumn.setRowCellFactory(product -> new MFXTableRowCell<>(customerModel::getCustname));
        phoneNoColumn.setRowCellFactory(product -> new MFXTableRowCell<>(customerModel::getPhoneno));
        custIdColumn.setRowCellFactory(product -> new MFXTableRowCell<>(customerModel::getCust_id));
        tb_customers.getTableColumns().addAll(custIdColumn,nameColumn,phoneNoColumn);
        tb_customers.getFilters().addAll(
                new StringFilter<>("Customer Id", customerModel::getCust_id),
                new StringFilter<>("Customer Name", customerModel::getCustname),
                new StringFilter<>("Phone No", customerModel::getPhoneno)
        );
        tb_customers.setItems(record);
        tb_customers.autosizeColumnsOnInitialization();
    }
    public void reload() throws SQLException {
        record.removeAll();
        record = loadData();
        tb_customers.setItems(record);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            record = loadData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        setUpTable();
        tb_customers.getSelectionModel().selectionProperty().addListener((MapChangeListener) e->{
            ObservableMap listValues = tb_customers.getSelectionModel().getSelection();
            ObservableList<customerModel> namesList = FXCollections.observableArrayList(listValues.values());
            tf_custname.setText(namesList.listIterator().next().getCustname());
            tf_phoneno.setText(namesList.listIterator().next().getPhoneno());
            tf_custid.setText(namesList.listIterator().next().getCust_id());
        });
        bt_home.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "home.fxml", "Home",1286,800);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bt_add.setOnAction(event -> {
            try {
                try {
                    connection = new DBConnector().getConnection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                psCheckCustomerExists = connection.prepareStatement("SELECT * FROM customers WHERE phoneno = ?");
                psCheckCustomerExists.setString(1, tf_phoneno.getText());
                resultSet = psCheckCustomerExists.executeQuery();

                if (resultSet.isBeforeFirst()){
                    System.out.println("Customer already exists");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Customer already exists.Update in case of change of no.");
                    alert.show();
                }else{
                    psInsert = connection.prepareStatement("INSERT INTO customers(custname, phoneno) VALUES (?, ?)");
                    psInsert.setString(1, tf_custname.getText());
                    psInsert.setString(2, tf_phoneno.getText());
                    psInsert.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Customer added successfully");
                    alert.show();
                    reload();
                }
                tf_custname.setText("");
                tf_phoneno.setText("");
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
                if (psCheckCustomerExists!= null) {
                    try {
                        psCheckCustomerExists.close();
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
        });
        bt_update.setOnAction(event -> {
            if(tf_custname.getText().isEmpty()|| tf_phoneno.getText().isEmpty()){
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
                        psCheckCustomerExists = connection.prepareStatement("UPDATE customers SET custname = ?, phoneno = ? WHERE cust_id = ?");
                        psCheckCustomerExists.setString(1, tf_custname.getText());
                        psCheckCustomerExists.setString(2, tf_phoneno.getText());
                        psCheckCustomerExists.setInt(3, Integer.parseInt(tf_custid.getText()));
                        psCheckCustomerExists.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Customer updated successfully");
                        alert.show();
                        tf_custname.setText("");
                        tf_phoneno.setText("");
                        tf_custid.setText("");
                        reload();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }finally {
                    if(psCheckId!=null){
                        try {
                            psCheckId.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(psCheckCustomerExists != null){
                        try {
                            psCheckCustomerExists.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(resultSet != null){
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(connection != null){
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        bt_delete.setOnAction(event -> {
            if(tf_custname.getText().isEmpty() && tf_phoneno.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter Customer to be deleted");
                alert.show();
            }else{
                try {
                    try {
                        connection = new DBConnector().getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }                    psCheckCustomerExists = connection.prepareStatement("SELECT * FROM customers WHERE phoneno = ? AND custname = ?");
                    psCheckCustomerExists.setString(1, tf_phoneno.getText());
                    psCheckCustomerExists.setString(2, tf_custname.getText());
                    resultSet = psCheckCustomerExists.executeQuery();
                    if(resultSet.isBeforeFirst()){
                        psInsert = connection.prepareStatement("DELETE FROM customers WHERE phoneno = ? AND  custname = ?");
                        psInsert.setString(1, tf_phoneno.getText());
                        psInsert.setString(2, tf_custname.getText());
                        psInsert.executeUpdate();
                        // Commit the changes to the database
                        connection.commit();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Customer deleted successfully");
                        alert.show();
                        tf_custname.setText("");
                        tf_phoneno.setText("");
                        reload();
                    }else{
                        System.out.println("Customer doesn't exists");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Not found!!!");
                        alert.show();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }finally {
                    if(connection != null){
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (psCheckCustomerExists!=null){
                        try {
                            psCheckCustomerExists.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (psInsert!=null){
                        try {
                            psInsert.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (resultSet != null){
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });

    }
}
