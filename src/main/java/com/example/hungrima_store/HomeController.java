package com.example.hungrima_store;

import com.example.hungrima_store.model.productsModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private MFXButton bt_sales;
    @FXML
    private MFXButton bt_supply;
    @FXML
    private MFXButton bt_transactions;
    @FXML
    private MFXButton bt_product;
    @FXML
    private MFXButton bt_customer;
    @FXML
    private MFXButton bt_logout;
    @FXML
    private MFXButton bt_exit;
    @FXML
    private BorderPane borderPane;
    @FXML
    private MFXComboBox tf_pname;
    @FXML
    private MFXButton bt_search;
    @FXML
    private MFXTableView tb_inventory;
    @FXML
    private MFXButton bt_invoice;
    Connection connection = null;
    PreparedStatement psInsert = null;
    PreparedStatement ps = null;
    PreparedStatement psD = null;
    ResultSet resultSet = null;
    ResultSet rs = null;
    static ObservableList<productsModel> record;
    String pname;
    public void loadUI(String ui){
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource((ui+".fxml")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        borderPane.setCenter(root);

    }
    public ObservableList<productsModel> searchTable(String pname) throws SQLException{

        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }        ps = connection.prepareStatement("SELECT * FROM products WHERE pname = ?");
        ps.setString(1, pname);
        rs = ps.executeQuery();
        List<productsModel> products= new ArrayList<>();
        while(rs.next()){
            String pName = rs.getString("pname");
            String quantity = String.valueOf(rs.getInt("quantity"));
            String p_id = String.valueOf(rs.getInt("p_id"));
            products.add(new productsModel(pName,quantity,p_id));
        }
        ObservableList<productsModel> t = FXCollections.observableArrayList(products);
        return t;
    };
    public ObservableList<productsModel> loadTable() throws SQLException {
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        psD = connection.prepareStatement("SELECT * FROM products");
        resultSet = psD.executeQuery();
        List<productsModel> products= new ArrayList<>();
        while(resultSet.next()){
            String pName = resultSet.getString("pname");
            String quantity = String.valueOf(resultSet.getInt("quantity"));
            String p_id = String.valueOf(resultSet.getInt("p_id"));
            products.add(new productsModel(pName,quantity,p_id));
        }
        ObservableList<productsModel> p = FXCollections.observableArrayList(products);
        return p;
    };
    public void populateTable(){
        MFXTableColumn<productsModel> nameColumn = new MFXTableColumn<>("Product Name", true, Comparator.comparing(productsModel::getPname));
        MFXTableColumn<productsModel> priceColumn = new MFXTableColumn<>("Quantity", true, Comparator.comparing(productsModel::getPrice));
        MFXTableColumn<productsModel> idColumn = new MFXTableColumn<>("Product Id", true, Comparator.comparing(productsModel::getP_id));
        idColumn.setRowCellFactory(product -> new MFXTableRowCell<>(productsModel::getP_id));
        nameColumn.setRowCellFactory(product -> new MFXTableRowCell<>(productsModel::getPname));
        priceColumn.setRowCellFactory(product -> new MFXTableRowCell<>(productsModel::getPrice));
        tb_inventory.getTableColumns().addAll(idColumn,nameColumn,priceColumn);
        tb_inventory.getFilters().addAll(
                new StringFilter<>("Product Id", productsModel::getP_id),
                new StringFilter<>("Product Name", productsModel::getPname),
                new StringFilter<>("Quantity", productsModel::getPrice)
        );
        tb_inventory.setItems(record);
        tb_inventory.autosizeColumnsOnInitialization();
    }
    public void reload() throws SQLException {
        record.removeAll();
        record = loadTable();
        tb_inventory.setItems(record);
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
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            record = loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        populateTable();

        try {
            initializeComboP();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        bt_sales.setOnAction(event -> {

           loadUI("Sales_form");
        });

        bt_supply.setOnAction(event -> {

            loadUI("Supply_form");
        });

        bt_transactions.setOnAction(event -> {

            loadUI("transactions");
        });

        bt_product.setOnAction(event -> {

            loadUI("product_form");
        });

        bt_customer.setOnAction(event -> {

            loadUI("customer");
        });
        bt_invoice.setOnAction(event -> {
            loadUI("invoice");
        });
        bt_search.setOnAction(event -> {
            pname = tf_pname.getText();
            record.removeAll();
            try {
                record = searchTable(pname);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            tb_inventory.setItems(record);
        });
        bt_logout.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "Login.fxml","Login", 600, 400);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bt_exit.setOnAction(event -> {
            Stage stage = (Stage) bt_logout.getScene().getWindow();
            stage.close();
        });

    }
}
