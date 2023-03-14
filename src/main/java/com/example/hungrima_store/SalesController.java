package com.example.hungrima_store;

import com.example.hungrima_store.model.billModel;
import com.example.hungrima_store.model.customerModel;
import com.example.hungrima_store.model.productsModel;


import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Comparator.*;

public class SalesController implements Initializable {
    ObservableList<String> modeOfPaymentList = FXCollections.observableArrayList("Mpesa","Cash");
    @FXML
    private MFXComboBox mpcombo;
    @FXML
    private MFXComboBox tf_pname;
    @FXML
    private MFXComboBox tf_cname;
    @FXML
    private MFXTextField tf_total;
    @FXML
    private MFXTextField tf_cstatus;
    @FXML
    private MFXTextField tf_quantity;
    @FXML
    private MFXTextField tf_price;
    @FXML
    private MFXTextField tf_remarks;
    @FXML
    private MFXDatePicker tf_sdate;
    @FXML
    private MFXTableView tb_bill;
    @FXML
    private MFXButton bt_add;
    @FXML
    private MFXButton bt_remove;
    @FXML
    private MFXButton bt_home;
    @FXML
    private MFXButton bt_msale;
    @FXML
    private MFXButton bt_csale;
    private ObservableList<billModel> record;
    //Ensure bill list is global to avoid reinitializing it every time loadData function is called
    private List<billModel> bill = new ArrayList<>();
    //holds selected value from table to be deleted
    ObservableList<billModel> selected;
    //total amount variable
    Integer t_amount = 0;
    int newQ = 0;

    Connection connection = null;
    PreparedStatement psInsert = null;
    PreparedStatement ps = null;
    PreparedStatement psD = null;
    ResultSet resultSet = null;
    ResultSet rs = null;
    Long id = null;
    //gets data from textfields and stores it in record list
    public ObservableList<billModel> loadData() {
        String pname = tf_pname.getText();
        Integer quantity = Integer.valueOf(tf_quantity.getText());
        Integer price = Integer.valueOf(tf_price.getText());
        Integer amount = quantity * price;
        t_amount = t_amount + amount;
        try {
            try {
                connection = new DBConnector().getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ps = connection.prepareStatement("SELECT quantity FROM products WHERE pname = ?");
            ps.setString(1, pname);
            resultSet = ps.executeQuery();
            Integer oldQ = null;
            while (resultSet.next()) {
                oldQ = resultSet.getInt("quantity");
            }
            int newQ = oldQ - quantity;
            if(newQ <= 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(pname +" out of stock. Please restock.");
                alert.show();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        bill.add(new billModel(pname, quantity, price, amount));
        ObservableList<billModel> b = FXCollections.observableArrayList(bill);
        return b;
    }


    public void populateTable(){
        MFXTableColumn<billModel> nameColumn = new MFXTableColumn<>("Product Name", true, comparing(billModel::getPname));
        MFXTableColumn<billModel> quantityColumn= new MFXTableColumn<>("Quantity", true, comparing(billModel::getQuantity));
        MFXTableColumn<billModel> priceColumn= new MFXTableColumn<>("Price", true, comparing(billModel::getPrice));
        MFXTableColumn<billModel> amountColumn= new MFXTableColumn<>("Amount", true, comparing(billModel::getAmount));

        nameColumn.setRowCellFactory(bill -> new MFXTableRowCell<>(billModel::getPname));
        quantityColumn.setRowCellFactory(bill -> new MFXTableRowCell<>(billModel::getQuantity));
        priceColumn.setRowCellFactory(bill -> new MFXTableRowCell<>(billModel::getPrice));
        amountColumn.setRowCellFactory(bill -> new MFXTableRowCell<>(billModel::getAmount));
        tb_bill.getTableColumns().addAll(nameColumn,quantityColumn,priceColumn,amountColumn);
        tb_bill.getFilters().addAll(
                new StringFilter<>("Product Name", billModel::getPname),
                new StringFilter<>("Quantity", billModel::getQuantity),
                new StringFilter<>("Price", billModel::getPrice),
                new StringFilter<>("Supply Date", billModel::getAmount)
        );
        tb_bill.setItems(record);
        tb_bill.autosizeColumnsOnInitialization();
    }

    private void initialize() {
        mpcombo.setValue("Cash");
        mpcombo.setItems(modeOfPaymentList);
    }
    public void initializeComboP() throws SQLException {
        try {
            try {
                connection = new DBConnector().getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ps = connection.prepareStatement("SELECT pname FROM products");
            rs = ps.executeQuery();
            List<productsModel> productList = new ArrayList<>();
            while (rs.next()) {
                productList.add(new productsModel(rs.getString("pname")));
            }
            ObservableList<productsModel> p = FXCollections.observableArrayList(productList);
            StringConverter<productsModel> converter = FunctionalStringConverter.to(products -> (products == null)?"": products.getPname());
            tf_pname.setConverter(converter);
            tf_pname.setItems(p);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            connection.close();
            rs.close();
            ps.close();
        }

    }

    public void initializeComboC() throws SQLException {
        try {
            try {
                connection = new DBConnector().getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ps = connection.prepareStatement("SELECT custname FROM customers");
            rs = ps.executeQuery();
            List<customerModel> customerModelListList = new ArrayList<>();
            while (rs.next()) {
                customerModelListList.add(new customerModel(rs.getString("custname")));
            }
            ObservableList<customerModel> p = FXCollections.observableArrayList(customerModelListList);
            StringConverter<customerModel> converter = FunctionalStringConverter.to(customer -> (customer == null)?"": customer.getCustname());
            tf_cname.setConverter(converter);
            tf_cname.setItems(p);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            connection.close();
            rs.close();
            ps.close();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initialize();
        populateTable();
        try {
            initializeComboP();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            initializeComboC();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        tb_bill.getSelectionModel().selectionProperty().addListener((MapChangeListener) e->{
            ObservableMap listValues = tb_bill.getSelectionModel().getSelection();
            selected = FXCollections.observableArrayList(listValues.values());
        });
        bt_home.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "home.fxml", "Home",1286,800);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bt_add.setOnAction(event -> {
            if(tf_pname.getText().isEmpty()||tf_quantity.getText().isEmpty()||tf_price.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Fill in the fields");
                alert.show();
            }else {
                record = loadData();
                tb_bill.setItems(record);
                tf_total.setText(String.valueOf(t_amount));
                tf_pname.setText("");
                tf_quantity.setText("");
                tf_price.setText("");
            }
        });
        bt_remove.setOnAction(event -> {
            if(selected == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Select item to be removed");
                alert.show();
            }else{
                bill.remove(selected.listIterator().next());
                Integer del = Integer.valueOf(selected.listIterator().next().getAmount());
                tb_bill.getItems().remove(selected.listIterator().next());
                t_amount = t_amount - del;
                tf_total.setText(String.valueOf(t_amount));
                tb_bill.setItems(record);
            }
        });
        bt_msale.setOnAction(event -> {
            if(tf_cname.getText().isEmpty()||tf_total.getText().isEmpty()||mpcombo.getText().isEmpty()||tf_sdate.getText().isEmpty()){
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
                    connection.setAutoCommit(false);
                    psInsert = connection.prepareStatement("INSERT INTO sales(custname, mode_payment, collection_status, remarks, t_amount , sales_date) VALUES (?,?,?,?,?,?)", psInsert.RETURN_GENERATED_KEYS);
                    psInsert.setString(1, tf_cname.getText());
                    psInsert.setString(2, mpcombo.getText());
                    psInsert.setString(3, tf_cstatus.getText());
                    psInsert.setString(4, tf_remarks.getText());
                    psInsert.setString(6, String.valueOf(Date.valueOf(tf_sdate.getValue())));
                    psInsert.setInt(5, Integer.parseInt(tf_total.getText()));
                    psInsert.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                    // this only works if you add ", psInsert.RETURN_GENERATED_KEYS" to the end of the sql statement
                    rs = psInsert.getGeneratedKeys();
                    while (rs.next()){
                        id = rs.getLong(1);
                    }
                    ps = connection.prepareStatement("INSERT INTO orders(pname, price, amount, quantity,s_id) VALUES(?,?,?,?,?)");
                    for (int i = 0; i < bill.size(); i++){
                        ps.setString(1, bill.get(i).getPname());
                        ps.setInt(2, Integer.parseInt(bill.get(i).getPrice()));
                        ps.setInt(3, Integer.parseInt(bill.get(i).getAmount()));
                        ps.setInt(4, Integer.parseInt(bill.get(i).getQuantity()));
                        ps.setInt(5, Math.toIntExact(id));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    connection.commit();
                    for (int i = 0; i < bill.size(); i++) {
                        DBUtils.track(event, bill.get(i).getPname(),String.valueOf(Date.valueOf(tf_sdate.getValue())), "sale", Integer.parseInt(bill.get(i).getQuantity()), Math.toIntExact(id));
                    }
                    // Commit the changes to the database
                    connection.commit();
                    bill.clear();
                    tb_bill.getItems().clear();
                    tf_sdate.setText("");
                    tf_cname.setText("");
                    tf_remarks.setText("");
                    mpcombo.setText("");
                    tf_cstatus.setText("");
                    tf_total.setText("");
                    connection.close();
                    ps.close();
                    psInsert.close();
                    rs.close();
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setContentText("Sale succesful");
                    alert.show();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        bt_csale.setOnAction(event -> {
            bill.clear();
            tb_bill.getItems().clear();
            tf_sdate.setText("");
            tf_cname.setText("");
            tf_remarks.setText("");
            mpcombo.setText("");
            tf_cstatus.setText("");
            tf_total.setText("");
        });

    }

}
