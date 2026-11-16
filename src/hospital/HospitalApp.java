package hospital;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HospitalApp extends Application {

    // ---------- Database settings ----------
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "root";
    private static final String PASSWORD = "class123";

    // ---------- Form controls ----------
    private TextField tfId = new TextField();
    private TextField tfName = new TextField();
    private TextField tfAge = new TextField();
    private ComboBox<String> cbGender = new ComboBox<>();
    private TextField tfPhone = new TextField();
    private TextField tfDiagnosis = new TextField();
    private DatePicker dpAdmission = new DatePicker();

    private Button btnAdd = new Button("Add Patient");
    private Button btnUpdate = new Button("Update");
    private Button btnDelete = new Button("Delete");
    private Button btnClear = new Button("Clear");

    // ---------- Records controls ----------
    private TextField tfSearch = new TextField();
    private Button btnSearch = new Button("Search");
    private Button btnShowAll = new Button("Show All");
    private TableView<Patient> table = new TableView<>();

    // ---------- Status ----------
    private Label lblStatus = new Label("Ready.");

    @Override
    public void start(Stage stage) {
        // Top: title and subtitle
        Label title = new Label("Hospital Management System");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label subtitle = new Label("Patient Registration");
        VBox top = new VBox(5, title, subtitle);
        top.setPadding(new Insets(10));

        // Left: patient form
        tfId.setEditable(false);
        cbGender.setItems(FXCollections.observableArrayList(
                "Female", "Male", "Non-binary", "Prefer not to say"));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Patient ID:"), tfId);
        form.addRow(1, new Label("Full Name:"), tfName);
        form.addRow(2, new Label("Age:"), tfAge);
        form.addRow(3, new Label("Gender:"), cbGender);
        form.addRow(4, new Label("Phone:"), tfPhone);
        form.addRow(5, new Label("Diagnosis:"), tfDiagnosis);
        form.addRow(6, new Label("Admission Date:"), dpAdmission);

        HBox buttonRow1 = new HBox(10, btnAdd, btnUpdate);
        HBox buttonRow2 = new HBox(10, btnDelete, btnClear);

        VBox leftPane = new VBox(10, new Label("Patient Details"), form, buttonRow1, buttonRow2);
        leftPane.setPadding(new Insets(10));

        // Right: search bar and table
        HBox searchRow = new HBox(10, tfSearch, btnSearch, btnShowAll);
        tfSearch.setPromptText("Search by name or phone");
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        setupTable();
        VBox rightPane = new VBox(10, new Label("Patient Records"), searchRow, table);
        rightPane.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox center = new HBox(10, leftPane, rightPane);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        // Bottom: status message
        HBox bottom = new HBox(lblStatus);
        bottom.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(center);
        root.setBottom(bottom);

        // Button actions
        btnAdd.setOnAction(e -> addPatient());
        btnUpdate.setOnAction(e -> updatePatient());
        btnDelete.setOnAction(e -> deletePatient());
        btnClear.setOnAction(e -> clearForm());
        btnSearch.setOnAction(e -> searchPatients());
        btnShowAll.setOnAction(e -> showAllPatients());

        // Clicking a row fills the form
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });

        stage.setTitle("Hospital Management System");
        stage.setScene(new Scene(root, 1100, 550));
        stage.show();

        showAllPatients();
    }

    private void setupTable() {
        TableColumn<Patient, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Patient, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Patient, Integer> colAge = new TableColumn<>("Age");
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<Patient, String> colGender = new TableColumn<>("Gender");
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Patient, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Patient, String> colDiagnosis = new TableColumn<>("Diagnosis");
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        TableColumn<Patient, LocalDate> colDate = new TableColumn<>("Admission Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));

        table.getColumns().addAll(colId, colName, colAge, colGender, colPhone, colDiagnosis, colDate);
    }

    // ================= CREATE =================
    private void addPatient() {
    		if (!tfId.getText().isEmpty()) {
            lblStatus.setText("Click Clear before adding a new patient.");
            return;
        }

        String error = validateForm();
        if (error != null) {
            lblStatus.setText(error);
            return;
        }

        String sql = "INSERT INTO patients (name, age, gender, phone, diagnosis, admission_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setInt(2, Integer.parseInt(tfAge.getText().trim()));
            ps.setString(3, cbGender.getValue());
            ps.setString(4, tfPhone.getText().trim());
            ps.setString(5, tfDiagnosis.getText().trim());
            ps.setDate(6, Date.valueOf(dpAdmission.getValue()));
            ps.executeUpdate();

            lblStatus.setText("Patient added successfully.");
            clearForm();
            showAllPatients();
        } catch (SQLException ex) {
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    // ================= READ =================
    private void showAllPatients() {
        String sql = "SELECT * FROM patients ORDER BY patient_id";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(makePatient(rs));
            }
            table.getItems().setAll(list);
            lblStatus.setText("Showing " + list.size() + " patient(s).");
        } catch (SQLException ex) {
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    // ================= UPDATE =================
    private void updatePatient() {
        if (tfId.getText().isEmpty()) {
            lblStatus.setText("Select a patient from the table first.");
            return;
        }
        String error = validateForm();
        if (error != null) {
            lblStatus.setText(error);
            return;
        }

        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, "
                + "diagnosis = ?, admission_date = ? WHERE patient_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tfName.getText().trim());
            ps.setInt(2, Integer.parseInt(tfAge.getText().trim()));
            ps.setString(3, cbGender.getValue());
            ps.setString(4, tfPhone.getText().trim());
            ps.setString(5, tfDiagnosis.getText().trim());
            ps.setDate(6, Date.valueOf(dpAdmission.getValue()));
            ps.setInt(7, Integer.parseInt(tfId.getText()));
            ps.executeUpdate();

            lblStatus.setText("Patient updated successfully.");
            clearForm();
            showAllPatients();
        } catch (SQLException ex) {
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    // ================= DELETE =================
    private void deletePatient() {
        if (tfId.getText().isEmpty()) {
            lblStatus.setText("Select a patient from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this patient?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
            lblStatus.setText("Delete cancelled.");
            return;
        }

        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(tfId.getText()));
            ps.executeUpdate();

            lblStatus.setText("Patient deleted successfully.");
            clearForm();
            showAllPatients();
        } catch (SQLException ex) {
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    // ================= SEARCH =================
    private void searchPatients() {
        String keyword = tfSearch.getText().trim();
        if (keyword.isEmpty()) {
            showAllPatients();
            return;
        }

        String sql = "SELECT * FROM patients WHERE name LIKE ? OR phone LIKE ?";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(makePatient(rs));
                }
            }
            table.getItems().setAll(list);
            lblStatus.setText(list.size() + " patient(s) found.");
        } catch (SQLException ex) {
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    // ================= VALIDATION =================
    private String validateForm() {
        if (tfName.getText().isBlank()) {
            return "Full name is required.";
        }
        if (tfAge.getText().isBlank()) {
            return "Age is required.";
        }
        try {
            int age = Integer.parseInt(tfAge.getText().trim());
            if (age < 0 || age > 125) {
                return "Age must be between 0 and 125.";
            }
        } catch (NumberFormatException ex) {
            return "Age must be a whole number.";
        }
        if (cbGender.getValue() == null) {
            return "Please select a gender.";
        }
        if (tfPhone.getText().isBlank()) {
            return "Phone is required.";
        }
        if (!tfPhone.getText().trim().matches("^\\+?[0-9()\\-\\s]{7,20}$")) {
            return "Phone number format is invalid.";
        }
        if (tfDiagnosis.getText().isBlank()) {
            return "Diagnosis is required.";
        }
        if (dpAdmission.getValue() == null) {
            return "Admission date is required.";
        }
        if (dpAdmission.getValue().isAfter(LocalDate.now())) {
            return "Admission date cannot be in the future.";
        }
        return null; // no errors
    }

    // ================= HELPERS =================
    private Patient makePatient(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("patient_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("diagnosis"),
                rs.getDate("admission_date").toLocalDate());
    }

    private void fillForm(Patient p) {
        tfId.setText(String.valueOf(p.getId()));
        tfName.setText(p.getName());
        tfAge.setText(String.valueOf(p.getAge()));
        cbGender.setValue(p.getGender());
        tfPhone.setText(p.getPhone());
        tfDiagnosis.setText(p.getDiagnosis());
        dpAdmission.setValue(p.getAdmissionDate());
    }

    private void clearForm() {
        tfId.clear();
        tfName.clear();
        tfAge.clear();
        cbGender.setValue(null);
        tfPhone.clear();
        tfDiagnosis.clear();
        dpAdmission.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}