package com.example.hungrima_store;

import com.example.hungrima_store.model.productsModel;
import com.example.hungrima_store.model.supplyModel;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class SupplyController implements Initializable {
    @FXML
    private MFXButton bt_home;
    @FXML
    private MFXButton bt_add;
    @FXML
    private MFXButton bt_delete;
    @FXML
    private MFXButton bt_update;
    @FXML
    private MFXTableView tb_supply;
    @FXML
    private MFXComboBox tf_pname;
    @FXML
    private MFXTextField tf_supplyid;
    @FXML
    private MFXTextField tf_quantity;
    @FXML
    private MFXTextField tf_cost;
    @FXML
    private MFXDatePicker tf_supplydate;
    static ObservableList<supplyModel> record;
    Connection connection = null;
    Connection con = null;
    PreparedStatement psInsert = null;
    PreparedStatement psCheckSupplyExists = null;
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    ResultSet rs = null;
    int newQ = 0;
    Long id = null;
    public void initializeCombo() throws SQLException {
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
    public ObservableList<supplyModel> loadTable() throws SQLException {
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        psCheckSupplyExists = connection.prepareStatement("SELECT * FROM supply ORDER BY supply_id DESC");
        resultSet = psCheckSupplyExists.executeQuery();
        List<supplyModel> supply= new ArrayList<>();
        while(resultSet.next()){
            String supplyDate = resultSet.getString("supply_date");
            String cost = String.valueOf(resultSet.getInt("cost"));
            String quantity = String.valueOf(resultSet.getInt("quantity"));
            String p_name = resultSet.getString("p_name");
            String supply_id = resultSet.getString("supply_id");
            supply.add(new supplyModel(supplyDate,cost,quantity,p_name,supply_id));
        }
        ObservableList<supplyModel> p = FXCollections.observableArrayList(supply);
        connection.close();
        psCheckSupplyExists.close();
        resultSet.close();
        return p;
    }
    public void populateTable(){
        MFXTableColumn<supplyModel> idColumn = new MFXTableColumn<>("Supply ID", true, Comparator.comparing(supplyModel::getSupply_id));
        MFXTableColumn<supplyModel> nameColumn = new MFXTableColumn<>("Product Name", true, Comparator.comparing(supplyModel::getP_name));
        MFXTableColumn<supplyModel> quantityColumn= new MFXTableColumn<>("Quantity", true, Comparator.comparing(supplyModel::getQuantity));
        MFXTableColumn<supplyModel> costColumn= new MFXTableColumn<>("Cost", true, Comparator.comparing(supplyModel::getCost));
        MFXTableColumn<supplyModel> dateColumn= new MFXTableColumn<>("Supply Date", true, Comparator.comparing(supplyModel::getSupply_date));
        idColumn.setRowCellFactory(supply -> new MFXTableRowCell<>(supplyModel::getSupply_id));
        nameColumn.setRowCellFactory(supply -> new MFXTableRowCell<>(supplyModel::getP_name));
        quantityColumn.setRowCellFactory(supply -> new MFXTableRowCell<>(supplyModel::getQuantity));
        costColumn.setRowCellFactory(supply -> new MFXTableRowCell<>(supplyModel::getCost));
        dateColumn.setRowCellFactory(supply -> new MFXTableRowCell<>(supplyModel::getSupply_date));
        tb_supply.getTableColumns().addAll(idColumn,nameColumn,quantityColumn,costColumn,dateColumn);
        tb_supply.getFilters().addAll(
                new StringFilter<>("Supply Id", supplyModel::getSupply_id),
                new StringFilter<>("Product Name", supplyModel::getP_name),
                new StringFilter<>("Quantity", supplyModel::getQuantity),
                new StringFilter<>("Cost", supplyModel::getCost),
                new StringFilter<>("Supply Date", supplyModel::getSupply_date)
        );
        tb_supply.setItems(record);
        tb_supply.autosizeColumnsOnInitialization();
    }
    public void sum(Integer quantity, String name) throws SQLException {
        try {
            try {
                connection = new DBConnector().getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ps = connection.prepareStatement("SELECT quantity FROM products WHERE pname = ?");
            ps.setString(1, name);
            resultSet = ps.executeQuery();

            Integer oldQ = null;
            while (resultSet.next()) {
                oldQ = resultSet.getInt("quantity");
            }
            newQ = quantity + oldQ;
            if(newQ <= 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("The current quantity will be less than 0. Please rework the update with whats currently in stock.");
                alert.show();
            }else {
                psInsert = connection.prepareStatement("UPDATE products SET quantity = ? WHERE pname = ?");
                psInsert.setInt(1, newQ);
                psInsert.setString(2, name);
                psInsert.executeUpdate();

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            connection.close();
            ps.close();
            psInsert.close();
            resultSet.close();
        }
    }
    public void reload() throws SQLException {
        record.removeAll();
        record = loadTable();
        tb_supply.setItems(record);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            record = loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            initializeCombo();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        populateTable();
        tb_supply.getSelectionModel().selectionProperty().addListener((MapChangeListener) e->{
            ObservableMap listValues = tb_supply.getSelectionModel().getSelection();
            ObservableList<supplyModel> namesList = FXCollections.observableArrayList(listValues.values());
            tf_supplyid.setText(namesList.listIterator().next().getSupply_id());
            tf_pname.setText(namesList.listIterator().next().getP_name());
            tf_quantity.setText(namesList.listIterator().next().getQuantity());
            tf_cost.setText(namesList.listIterator().next().getCost());
            tf_supplydate.setText(namesList.listIterator().next().getSupply_date());
        });
        bt_home.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "home.fxml", "Home",1286,800);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bt_add.setOnAction(event ->{
            if(tf_pname.getText().isEmpty() && tf_cost.getText().isEmpty() && tf_quantity.getText().isEmpty() && tf_supplydate.getText().isEmpty()){
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
                    psInsert = connection.prepareStatement("INSERT INTO supply(p_name, quantity, cost, supply_date) VALUES (?, ?, ?, ?)", psInsert.RETURN_GENERATED_KEYS);
                    psInsert.setString(1, tf_pname.getText());
                    psInsert.setInt(2, Integer.parseInt(tf_quantity.getText()));
                    psInsert.setInt(3, Integer.parseInt(tf_cost.getText()));
                    psInsert.setString(4, String.valueOf(Date.valueOf(tf_supplydate.getValue())));


                    psInsert.executeUpdate();

                    // this only works if you add ", psInsert.RETURN_GENERATED_KEYS" to the end of the sql statement especially in postgres
                    rs = psInsert.getGeneratedKeys();
                    while (rs.next()){
                        id = rs.getLong(1);
                    }
                    //sum(Integer.valueOf(tf_quantity.getText()), tf_pname.getText());
                    DBUtils.track(event, tf_pname.getText(), String.valueOf(Date.valueOf(tf_supplydate.getValue())),"supply", Integer.parseInt(tf_quantity.getText()), Math.toIntExact(id));
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Supplies added successfully");
                    alert.show();
                    tf_pname.setText("");
                    tf_quantity.setText("");
                    tf_cost.setText("");
                    tf_supplydate.setText("");
                    tb_supply.update();
                    reload();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        psInsert.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
        bt_delete.setOnAction(event -> {
            if(tf_supplyid.getText().isEmpty()||tf_pname.getText().isEmpty()){
                  Alert alert = new Alert(Alert.AlertType.ERROR);
                  alert.setContentText("Click on the supply to be deleted");
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
                    ps = connection.prepareStatement("DELETE FROM supply WHERE supply_id = ?");
                    ps.setInt(1, Integer.parseInt(tf_supplyid.getText()));
                    ps.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                    DBUtils.deleteTrack(event, Integer.parseInt(tf_supplyid.getText()), "supply", tf_pname.getText());
                    //sum(-1*Integer.parseInt(tf_quantity.getText()),tf_pname.getText());

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Supply deleted successfully");
                    alert.show();
                    reload();
                    connection.close();
                    ps.close();
                    tf_pname.setText("");
                    tf_quantity.setText("");
                    tf_cost.setText("");
                    tf_supplydate.setText("");
                    tf_supplyid.setText("");

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        bt_update.setOnAction(event -> {
            if (tf_supplyid.getText().isEmpty() || tf_pname.getText().isEmpty() && tf_cost.getText().isEmpty() && tf_quantity.getText().isEmpty() && tf_supplydate.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Click on the supply to be deleted");
                alert.show();
            } else {
                try {
                    try {
                        connection = new DBConnector().getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    psCheckSupplyExists = connection.prepareStatement("SELECT * FROM supply WHERE supply_id = ?");
                    psCheckSupplyExists.setInt(1, Integer.parseInt(tf_supplyid.getText()));
                    resultSet = psCheckSupplyExists.executeQuery();

                    if(resultSet.next()){
                        int difference = resultSet.getInt("quantity");
                        difference = Integer.parseInt(tf_quantity.getText()) - difference;
                        //sum(difference, tf_pname.getText());
                    }
                    try {
                        con = new DBConnector().getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ps = con.prepareStatement("UPDATE supply SET p_name = ?, quantity = ?, cost = ?, supply_date = ? WHERE supply_id = ?");
                    ps.setInt(5, Integer.parseInt(tf_supplyid.getText()));
                    ps.setString(1, tf_pname.getText());
                    ps.setInt(2, Integer.parseInt(tf_quantity.getText()));
                    ps.setInt(3, Integer.parseInt(tf_cost.getText()));
                    ps.setString(4, tf_supplydate.getText());
                    ps.executeUpdate();
                    // Commit the changes to the database
                    connection.commit();
                    DBUtils.updatetrack(event, tf_quantity.getText(),Integer.parseInt(tf_quantity.getText()),Integer.parseInt(tf_supplyid.getText()), "supply");
                    tf_pname.setText("");
                    tf_quantity.setText("");
                    tf_cost.setText("");
                    tf_supplydate.setText("");
                    tf_supplyid.setText("");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Supply updated successfully");
                    alert.show();
                    reload();
                    connection.close();
                    ps.close();
                    psCheckSupplyExists.close();
                    resultSet.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }
}
