package com.example.hungrima_store;

import com.example.hungrima_store.model.transModel;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import io.github.palexdev.materialfx.filter.StringFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Comparator.comparing;

public class TransactionsController implements Initializable {
    @FXML
    private MFXButton bt_reload;
    @FXML
    private MFXButton bt_home;
    @FXML
    private MFXButton bt_search;
    @FXML
    private MFXButton bt_print;
    @FXML
    private MFXTableView tb_transactions;
    @FXML
    private MFXTextField tf_pname;
    @FXML
    private MFXTextField tf_cs;

    private ObservableList<transModel> record;
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;


    Integer token = 0;

    public ObservableList<transModel> searchTable(String pname) throws SQLException{
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ps = connection.prepareStatement("SELECT * FROM transactions WHERE pname = ?");
        ps.setString(1, pname);
        rs = ps.executeQuery();
        List<transModel> track= new ArrayList<>();
        while(rs.next()){
            String pName = rs.getString("pname");
            String date = rs.getString("date");
            String t_type = rs.getString("t_type");
            Integer quantity = rs.getInt("quantity");
            Integer s_count = rs.getInt("s_count");
            track.add(new transModel(pName,date,t_type, quantity,s_count));
        }
        ObservableList<transModel> t = FXCollections.observableArrayList(track);
        return t;
    };
    public ObservableList<transModel> loadTable() throws SQLException {
        try {
            connection = new DBConnector().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ps = connection.prepareStatement("SELECT * FROM transactions");
        rs = ps.executeQuery();
        List<transModel> track= new ArrayList<>();
        while(rs.next()){
            String pName = rs.getString("pname");
            String date = rs.getString("date");
            String t_type = rs.getString("t_type");
            Integer quantity = rs.getInt("quantity");
            Integer s_count = rs.getInt("s_count");
            track.add(new transModel(pName,date,t_type, quantity,s_count));
        }
        connection.close();
        ps.close();
        rs.close();
        ObservableList<transModel> t = FXCollections.observableArrayList(track);
        return t;
    };
    static Cell getHeaderTextCell(String textValue){
        return new Cell().add(textValue).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }
    static Cell getHeaderTextCellValue(String textValue) {
        return new Cell().add(textValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }
    public void populateTable(){
        MFXTableColumn<transModel> nameColumn = new MFXTableColumn<>("Product Name", true, comparing(transModel::getPname));
        MFXTableColumn<transModel> dateColumn= new MFXTableColumn<>("Date", true, comparing(transModel::getDate));
        MFXTableColumn<transModel> typeColumn= new MFXTableColumn<>("Transaction type", true, comparing(transModel::getT_type));
        MFXTableColumn<transModel> quantityColumn= new MFXTableColumn<>("Quantity", true, comparing(transModel::getQuantity));
        MFXTableColumn<transModel> stockColumn = new MFXTableColumn<>("Stock count", true, comparing(transModel::getS_count));

        nameColumn.setRowCellFactory(tt -> new MFXTableRowCell<>(transModel::getPname));
        quantityColumn.setRowCellFactory(tt -> new MFXTableRowCell<>(transModel::getQuantity));
        dateColumn.setRowCellFactory(tt -> new MFXTableRowCell<>(transModel::getDate));
        typeColumn.setRowCellFactory(tt -> new MFXTableRowCell<>(transModel::getT_type));
        stockColumn.setRowCellFactory(tt -> new MFXTableRowCell<>(transModel::getS_count));

        tb_transactions.getTableColumns().addAll(nameColumn,dateColumn,typeColumn,quantityColumn,stockColumn);
        tb_transactions.getFilters().addAll(
                new StringFilter<>("Product Name", transModel::getPname),
                new StringFilter<>("Date", transModel::getDate),
                new StringFilter<>("Transaction Type", transModel::getT_type),
                new StringFilter<>("Quantity", transModel::getQuantity),
                new StringFilter<>("Stock count", transModel::getS_count)
        );
        tb_transactions.setItems(record);
        tb_transactions.autosizeColumnsOnInitialization();
    }
    public void reload() throws SQLException {
        record.removeAll();
        record = loadTable();
        tb_transactions.setItems(record);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            record = loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        populateTable();
        bt_reload.setOnAction(event -> {
            try {
                reload();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            tf_pname.setText("");
            tf_cs.setText("");

        });

        bt_search.setOnAction(event -> {
            String name;
            Integer current;
            current = 0;

           if(tf_pname.getText().isEmpty()){
               Alert alert = new Alert(Alert.AlertType.ERROR);
               alert.setContentText("Enter product to be searched");
               alert.show();
           }else {
               name = tf_pname.getText();
               record.removeAll();
               try {
                   record = searchTable(name);
               } catch (SQLException e) {
                   throw new RuntimeException(e);
               }
               tb_transactions.setItems(record);
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
                   rs = ps.executeQuery();
                   while (rs.next()) {
                       current = rs.getInt("quantity");
                   }
                   tf_cs.setText(String.valueOf(current));
                   connection.close();
                   ps.close();
                   rs.close();
               } catch (SQLException e) {
                   throw new RuntimeException(e);
               }
               token = 1;
           }
        });
        bt_home.setOnAction(event -> {
            try {
                DBUtils.changeScene(event, "home.fxml", "Home",1286,800);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bt_print.setOnAction(event -> {
            File file = null;
            float twocol = 285f;
            float twocol150 = twocol + 150f;
            float fiveCol = 114f;
            float twocolumnWidth[] = {twocol150, twocol};
            float fiveColumnWidth[] = {fiveCol, fiveCol, fiveCol, fiveCol, fiveCol};
            float fullWidth[] = {twocol * 2};

            Paragraph onesp = new Paragraph("\n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime now = LocalDateTime.now();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF file");
            fileChooser.setInitialFileName("Transactions report " + tf_pname.getText() + " " + dtf.format(now) + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
            file = fileChooser.showSaveDialog(new Stage());
            PdfWriter pdfWriter = null;
            if (file != null) {
                String path = file.getAbsolutePath();
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
            table.addCell(new Cell().add("Transactions").setFontSize(20f).setBorder(Border.NO_BORDER));
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

            Table fiveColTable = new Table(fiveColumnWidth);
            fiveColTable.setBackgroundColor(Color.BLACK, 0.7f);

            fiveColTable.addCell(new Cell().add("Product Name").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER));
            fiveColTable.addCell(new Cell().add("Date").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
            fiveColTable.addCell(new Cell().add("Transaction type").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
            fiveColTable.addCell(new Cell().add("Quantity").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
            fiveColTable.addCell(new Cell().add("Stock Count").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));

            Table fiveColTablecont = new Table(fiveColumnWidth);
            List<transModel> transList = record;
            for (transModel trans : transList) {
                fiveColTablecont.addCell(new Cell().add(trans.getPname()).setBorder(Border.NO_BORDER).setMarginLeft(10f).setTextAlignment(TextAlignment.CENTER));
                fiveColTablecont.addCell(new Cell().add(trans.getDate()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                fiveColTablecont.addCell(new Cell().add(trans.getT_type()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                fiveColTablecont.addCell(new Cell().add(trans.getQuantity()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                fiveColTablecont.addCell(new Cell().add(trans.getS_count()).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).setMarginRight(10f));
            }
            document.add(fiveColTable);
            document.add(fiveColTablecont);

            document.add(onesp);
            document.add(divider);
            document.add(onesp);


            document.close();
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
