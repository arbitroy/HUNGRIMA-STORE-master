package com.example.hungrima_store;

import com.example.hungrima_store.model.customerModel;
import com.example.hungrima_store.model.ordersModel;
import com.example.hungrima_store.model.salesModel;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.DashedBorder;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class InvoiceController implements Initializable {
    @FXML
    private MFXComboBox tf_cname;
    @FXML
    private MFXTextField tf_id;
    @FXML
    private MFXButton bt_home;
    @FXML
    private MFXTableView tb_invoice;
    @FXML
    private MFXButton bt_search;
    @FXML
    private MFXButton bt_generate;
    @FXML
    private MFXButton bt_reload;

    static ObservableList<salesModel> record;
    static ObservableList<ordersModel> data;
    Connection connection = null;
    Connection con = null;
    PreparedStatement ps = null;
    PreparedStatement psD = null;
    ResultSet resultSet = null;
    ResultSet rs = null;
    Integer token = 0;
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
    public ObservableList<salesModel> loadData() throws SQLException {
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ps = connection.prepareStatement("SELECT * FROM sales ORDER BY s_id DESC");
        resultSet = ps.executeQuery();
        List<salesModel> sales = new ArrayList<>();
        while(resultSet.next()){
            String custname = resultSet.getString("custname");
            String mode_payment = resultSet.getString("mode_payment");
            String c_status = resultSet.getString("collection_status");
            String remarks = resultSet.getString("remarks");
            int t_amount = resultSet.getInt("t_amount");
            String sales_date = resultSet.getString("sales_date");
            Integer s_id = resultSet.getInt("s_id");
            sales.add(new salesModel(custname,mode_payment,c_status,remarks,sales_date,t_amount,s_id));
        }
        ObservableList<salesModel> p = FXCollections.observableArrayList(sales);
        return p;
    };
    public ObservableList<salesModel> searchTable(String cname) throws SQLException{
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ps = connection.prepareStatement("SELECT * FROM sales WHERE custname = ?");
        ps.setString(1, cname);
        rs = ps.executeQuery();
        List<salesModel> sales = new ArrayList<>();
        while(rs.next()){
            String custname = rs.getString("custname");
            String mode_payment = rs.getString("mode_payment");
            String c_status = rs.getString("collection_status");
            String remarks = rs.getString("remarks");
            Integer t_amount = rs.getInt("t_amount");
            String sales_date = rs.getString("sales_date");
            Integer s_id = rs.getInt("s_id");
            sales.add(new salesModel(custname,mode_payment,c_status,remarks, sales_date,t_amount,s_id));
        }
        ObservableList<salesModel> p = FXCollections.observableArrayList(sales);
        return p;
    };
    static Cell getHeaderTextCell(String textValue){
        return new Cell().add(textValue).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }
    static Cell getHeaderTextCellValue(String textValue) {
        return new Cell().add(textValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }
    static Cell getBillingandShippingCell(String textValue)
    {
        return new Cell().add(textValue).setFontSize(12f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }
    static Cell getCell10fLeft(String textValue,Boolean isBold){
        Cell myCell = new Cell().add(textValue).setFontSize(10f).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
        return isBold ?myCell.setBold():myCell;
    }

    public void reload() throws SQLException {
        record.removeAll();
        record = loadData();
        tb_invoice.setItems(record);
    }
    public void populateTable(){
        MFXTableColumn<salesModel> nameColumn = new MFXTableColumn<>("Customer Name", true, Comparator.comparing(salesModel::getCustname));
        MFXTableColumn<salesModel> paymentColumn = new MFXTableColumn<>("Mode of Payment", true, Comparator.comparing(salesModel::getModePayment));
        MFXTableColumn<salesModel> c_statusColumn = new MFXTableColumn<>("Collection Status", true, Comparator.comparing(salesModel::getCollection));
        MFXTableColumn<salesModel> remarksColumn = new MFXTableColumn<>("Remarks", true, Comparator.comparing(salesModel::getRemarks));
        MFXTableColumn<salesModel> amountColumn = new MFXTableColumn<>("Total Amount", true, Comparator.comparing(salesModel::getT_amount));
        MFXTableColumn<salesModel> dateColumn = new MFXTableColumn<>("Sales Date", true, Comparator.comparing(salesModel::getSales_date));
        MFXTableColumn<salesModel> idColumn = new MFXTableColumn<>("Sales Id", true, Comparator.comparing(salesModel::getS_id));
        idColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getS_id));
        nameColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getCustname));
        paymentColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getModePayment));
        c_statusColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getCollection));
        remarksColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getRemarks));
        amountColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getT_amount));
        dateColumn.setRowCellFactory(sales -> new MFXTableRowCell<>(salesModel::getSales_date));
        tb_invoice.getTableColumns().addAll(idColumn,nameColumn,paymentColumn,c_statusColumn,remarksColumn,amountColumn,dateColumn);
        tb_invoice.getFilters().addAll(
                new StringFilter<>("Sales Id", salesModel::getS_id),
                new StringFilter<>("Customer Name", salesModel::getCustname),
                new StringFilter<>("Mode of Payment", salesModel::getModePayment),
                new StringFilter<>("Collection Status", salesModel::getCollection),
                new StringFilter<>("Remarks", salesModel::getRemarks),
                new StringFilter<>("Total Amount", salesModel::getT_amount),
                new StringFilter<>("Sales Date", salesModel::getSales_date)
        );
        tb_invoice.setItems(record);
        tb_invoice.autosizeColumnsOnInitialization();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initializeComboC();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            record = loadData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        populateTable();
        tb_invoice.getSelectionModel().selectionProperty().addListener((MapChangeListener) e->{
            ObservableMap listValues = tb_invoice.getSelectionModel().getSelection();
            ObservableList<salesModel> namesList = FXCollections.observableArrayList(listValues.values());
            tf_id.setText(namesList.listIterator().next().getS_id());
        });
        bt_search.setOnAction(event -> {
            String name;
            if(tf_cname.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter customer to be searched");
                alert.show();
            }else{
                name = tf_cname.getText();
                record.removeAll();
                try {
                    record = searchTable(name);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                tb_invoice.setItems(record);
            }
            token = 1;
        });
        bt_reload.setOnAction(event -> {
            try {
                reload();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            tf_cname.setText("");
            tf_id.setText("");
        });
        bt_home.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "home.fxml", "Home",1286,800);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bt_generate.setOnAction(event -> {
            File file = null;
            if(tf_id.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Enter sale to be generated");
                alert.show();
            }else {
                float twocol = 285f;
                float threecol = 190f;
                float twocol150 = twocol + 150f;
                float fourCol = 142.5f;
                float twocolumnWidth[] = {twocol150, twocol};
                float threeColumnWidth[] =  {threecol, threecol, threecol};
                float fourColumnWidth[] = {fourCol, fourCol, fourCol, fourCol};
                float fullWidth[] = {twocol * 2};

                Paragraph onesp = new Paragraph("\n");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDateTime now = LocalDateTime.now();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save PDF file");
                if (token == 1) {
                    fileChooser.setInitialFileName("Invoice " + tf_cname.getText() + " " + dtf.format(now) + ".pdf");
                } else {
                    fileChooser.setInitialFileName("Invoice " + dtf.format(now) + ".pdf");
                }
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
                file = fileChooser.showSaveDialog(new Stage());
                PdfWriter pdfWriter = null;
                String path;
                if (file != null) {
                    path = file.getAbsolutePath();
                    try {
                        pdfWriter = new PdfWriter(path);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

                PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                pdfDocument.setDefaultPageSize(PageSize.A4);
                Document document = new Document(pdfDocument);

                Table table = new Table(twocolumnWidth);
                table.addCell(new Cell().add("Invoice").setFontSize(20f).setBorder(Border.NO_BORDER));
                Table nestedtable = new Table(new float[]{twocol / 2, twocol / 2});

                nestedtable.addCell(getHeaderTextCell("Date:"));
                nestedtable.addCell(getHeaderTextCellValue(dtf.format(now)));
                table.addCell(new Cell().add(nestedtable).setBorder(Border.NO_BORDER));

                Border gb = new SolidBorder(Color.GRAY, 2f);
                Table divider = new Table(fullWidth);

                divider.setBorder(gb);
                document.add(table);
                document.add(onesp);
                document.add(divider);
                document.add(onesp);
                String custname = null;
                String mode_payment = null;
                String c_status = null;
                String remarks = null;
                String sales_date = null;
                Integer s_id = null;
                Integer t_amount = null;
                try {
                    try {
                        connection = new DBConnector().getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ps = connection.prepareStatement("SELECT * FROM sales WHERE s_id = ?");
                    ps.setInt(1, Integer.parseInt(tf_id.getText()));
                    resultSet = ps.executeQuery();
                    while (resultSet.next()) {
                        custname = resultSet.getString("custname");
                        mode_payment = resultSet.getString("mode_payment");
                        c_status = resultSet.getString("collection_status");
                        remarks = resultSet.getString("remarks");
                        t_amount = resultSet.getInt("t_amount");
                        sales_date = resultSet.getString("sales_date");
                        s_id = resultSet.getInt("s_id");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Table twoColTable2 = new Table(twocolumnWidth);
                twoColTable2.addCell(getCell10fLeft("Customer Name", true));
                twoColTable2.addCell(getCell10fLeft("Mode of Payment", true));
                twoColTable2.addCell(getCell10fLeft(custname, false));
                twoColTable2.addCell(getCell10fLeft(mode_payment, false));
                document.add(twoColTable2);

                Table twoColTable3 = new Table(twocolumnWidth);
                twoColTable3.addCell(getCell10fLeft("Collection Status", true));
                twoColTable3.addCell(getCell10fLeft("Remarks", true));
                twoColTable3.addCell(getCell10fLeft(c_status, false));
                twoColTable3.addCell(getCell10fLeft(remarks, false));
                document.add(twoColTable3);

                float oneColumnWidth[] = {twocol150};

                Table oneColTable1 = new Table(oneColumnWidth);
                oneColTable1.addCell(getCell10fLeft("Sales Date", true));
                oneColTable1.addCell(getCell10fLeft(String.valueOf(sales_date), false));
                document.add(oneColTable1.setMarginBottom(10f));
                Table tableDivider2 = new Table(fullWidth);
                Border dgb = new DashedBorder(Color.GRAY, 0.5f);
                document.add(tableDivider2.setBorder(dgb));
                Paragraph prodParagragh = new Paragraph("Orders");
                document.add(prodParagragh.setBold());


                Table fourColTable = new Table(fourColumnWidth);
                fourColTable.setBackgroundColor(Color.BLACK, 0.7f);

                fourColTable.addCell(new Cell().add("Product Name").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER));
                fourColTable.addCell(new Cell().add("Quantity").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                fourColTable.addCell(new Cell().add("Price").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                fourColTable.addCell(new Cell().add("Amount").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));

                Table fourColTablecont = new Table(fourColumnWidth);

                List<ordersModel> order = new ArrayList<>();
                ObservableList<ordersModel> o;
                try {
                    psD = connection.prepareStatement("SELECT * FROM orders WHERE s_id = ?");
                    psD.setInt(1, s_id);
                    rs = psD.executeQuery();
                    while (rs.next()) {
                        String name = rs.getString("pname");
                        Integer quantity = rs.getInt("quantity");
                        Integer price = rs.getInt("price");
                        Integer amount = rs.getInt("amount");
                        order.add(new ordersModel(name, quantity, price, amount));
                    }
                    o = FXCollections.observableArrayList(order);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                List<ordersModel> orderList = o;
                for (ordersModel ord : orderList) {
                    fourColTablecont.addCell(new Cell().add(ord.getnam()).setBorder(Border.NO_BORDER).setMarginLeft(10f).setTextAlignment(TextAlignment.CENTER));
                    fourColTablecont.addCell(new Cell().add(ord.getQuant()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                    fourColTablecont.addCell(new Cell().add(ord.getPrice()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                    fourColTablecont.addCell(new Cell().add(ord.getAmount()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                }
                document.add(fourColTable);
                document.add(fourColTablecont);
                float onetwo[]= {threecol+152f, threecol*2};
                Table threeColTable4 = new Table(onetwo);
                threeColTable4.addCell(new Cell().add("").setBorder(Border.NO_BORDER));
                threeColTable4.addCell(new Cell().add(tableDivider2).setBorder(Border.NO_BORDER));
                document.add(threeColTable4);

                Table threeColTable3 = new Table(threeColumnWidth);
                threeColTable3.addCell(new Cell().add("").setBorder(Border.NO_BORDER).setMarginLeft(10f));
                threeColTable3.addCell(new Cell().add("Total").setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
                threeColTable3.addCell(new Cell().add(String.valueOf(t_amount)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setMarginRight(15f));

                document.add(threeColTable3);
                document.add(tableDivider2);
                document.add(new Paragraph("\n"));
                document.add(onesp);
                document.add(divider);
                document.close();


            }

            if(!Desktop.isDesktopSupported()){
                System.out.println("Desktop is not supported");
                return;
            }

            Desktop desktop = Desktop.getDesktop();
            if(file.exists()) {
                try {
                    desktop.open(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
