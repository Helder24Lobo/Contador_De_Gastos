package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Savings;
import com.personalfinance.contador.model.SavingsMovement;
import com.personalfinance.contador.repository.SavingsDAO;
import com.personalfinance.contador.repository.SavingsMovementDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class SavingsController implements Initializable {

    // FXML Bindings - Formulario (Izquierda)
    @FXML
    private VBox paneForm;
    @FXML
    private Label lblFormTitle;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtDescription;
    @FXML
    private TextField txtTargetValue;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnSave;
    @FXML
    private HBox hbEditActions;

    // FXML Bindings - Detalle (Izquierda)
    @FXML
    private VBox paneDetail;
    @FXML
    private Label lblDetailName;
    @FXML
    private Label lblDetailDescription;
    @FXML
    private Label lblDetailTarget;
    @FXML
    private Label lblDetailSaved;
    @FXML
    private Label lblDetailRemaining;
    @FXML
    private Label lblDetailPercentage;
    @FXML
    private Label lblDetailStatus;
    @FXML
    private ProgressBar pbDetailProgress;
    @FXML
    private HBox hbSuccessAlert;
    @FXML
    private Button btnAbonar;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    // FXML Bindings - Tabla y Filtros (Derecha)
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbFilterStatus;
    @FXML
    private TableView<Savings> tblSavings;
    @FXML
    private TableColumn<Savings, Number> colId;
    @FXML
    private TableColumn<Savings, String> colName;
    @FXML
    private TableColumn<Savings, Number> colTargetValue;
    @FXML
    private TableColumn<Savings, Number> colSavedValue;
    @FXML
    private TableColumn<Savings, Number> colRemainingValue;
    @FXML
    private TableColumn<Savings, Number> colProgressPercent;
    @FXML
    private TableColumn<Savings, Number> colProgressBar;
    @FXML
    private TableColumn<Savings, String> colStatus;

    //------------------ OBJECTS -----------------------//
    private final SavingsDAO savingsDAO = new SavingsDAO();
    private final SavingsMovementDAO savingsMovementDAO = new SavingsMovementDAO();
    private Savings selectedSaving = null;
    private boolean isEditingMode = false;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private final ObservableList<Savings> savingsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar ComboBoxes
        cbStatus.setItems(FXCollections.observableArrayList("Activo", "Completado"));
        cbStatus.setValue("Activo");

        cbFilterStatus.setItems(FXCollections.observableArrayList("Todos", "Activo", "Completado"));
        cbFilterStatus.setValue("Todos");

        // Configurar columnas de la Tabla de Ahorros
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colTargetValue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTargetValue()));
        colSavedValue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSavedAmount()));
        colRemainingValue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getRemainingAmount()));
        colProgressPercent.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProgressPercentage()));
        colProgressBar.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProgressPercentage() / 100.0));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        // Formatear columnas numéricas
        colTargetValue.setCellFactory(column -> createCurrencyTableCell());
        colSavedValue.setCellFactory(column -> createCurrencyTableCell());
        colRemainingValue.setCellFactory(column -> createCurrencyTableCell());

        colProgressPercent.setCellFactory(column -> new TableCell<Savings, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f%%", item.doubleValue()));
                }
            }
        });

        // Configurar renderizado gráfico de la barra de progreso
        colProgressBar.setCellFactory(column -> new TableCell<Savings, Number>() {
            private final ProgressBar pb = new ProgressBar();
            {
                pb.setMaxWidth(Double.MAX_VALUE);
            }
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    double progress = item.doubleValue();
                    pb.setProgress(progress);
                    if (progress >= 1.0) {
                        pb.setStyle("-fx-accent: #38a169;"); // Verde cuando está completado
                    } else {
                        pb.setStyle(""); // Color por defecto
                    }
                    setGraphic(pb);
                }
            }
        });

        // Evento de selección de la tabla
        tblSavings.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && !isEditingMode) {
                selectedSaving = newSelection;
                showDetailPane(selectedSaving);
            }
        });

        dpDate.setValue(LocalDate.now());

        // Cargar datos
        loadSavingsData();
        handleClearSelection(null);
    }

    private TableCell<Savings, Number> createCurrencyTableCell() {
        return new TableCell<Savings, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item.doubleValue()));
                }
            }
        };
    }

    /**
     * Muestra el panel detallado del ahorro seleccionado.
     */
    private void showDetailPane(Savings saving) {
        paneForm.setVisible(false);
        paneForm.setManaged(false);
        paneDetail.setVisible(true);
        paneDetail.setManaged(true);

        lblDetailName.setText(saving.getName());
        lblDetailDescription.setText(saving.getDescription() == null || saving.getDescription().isEmpty() ? "Sin descripción" : saving.getDescription());
        lblDetailTarget.setText(currencyFormat.format(saving.getTargetValue()));
        lblDetailSaved.setText(currencyFormat.format(saving.getSavedAmount()));
        lblDetailRemaining.setText(currencyFormat.format(saving.getRemainingAmount()));
        lblDetailPercentage.setText(String.format("%.1f%%", saving.getProgressPercentage()));
        lblDetailStatus.setText(saving.getStatus());

        double progress = saving.getProgressPercentage() / 100.0;
        pbDetailProgress.setProgress(progress);

        if (progress >= 1.0) {
            pbDetailProgress.setStyle("-fx-accent: #38a169;");
            hbSuccessAlert.setVisible(true);
            hbSuccessAlert.setManaged(true);
            btnAbonar.setDisable(true);
        } else {
            pbDetailProgress.setStyle("");
            hbSuccessAlert.setVisible(false);
            hbSuccessAlert.setManaged(false);
            btnAbonar.setDisable(false);
        }
    }

    /**
     * Carga la lista de ahorros desde la base de datos.
     */
    private void loadSavingsData() {
        try {
            List<Savings> all = savingsDAO.findAll();
            savingsList.setAll(all);
            tblSavings.setItems(savingsList);
        } catch (SQLException e) {
            showErrorAlert("Error al cargar ahorros", e.getMessage());
        }
    }

    /**
     * Maneja la acción de guardar o actualizar una meta de ahorro.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) return;

        LocalDate date = dpDate.getValue();
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();
        double targetValue = Double.parseDouble(txtTargetValue.getText().trim());
        String status = cbStatus.getValue();

        try {
            if (selectedSaving == null) {
                // Crear meta de ahorro
                Savings newSaving = new Savings(name, description, targetValue, date, status);
                savingsDAO.insert(newSaving);
                loadSavingsData();
                handleClearSelection(null);
            } else {
                // Actualizar meta de ahorro
                selectedSaving.setDateCurrent(date);
                selectedSaving.setName(name);
                selectedSaving.setDescription(description);
                selectedSaving.setTargetValue(targetValue);
                selectedSaving.setStatus(status);

                // Autocompletar estado si sobrepasa el objetivo por edición
                if (selectedSaving.getSavedAmount() >= selectedSaving.getTargetValue() && !selectedSaving.getStatus().equals("Completado")) {
                    selectedSaving.setStatus("Completado");
                }

                savingsDAO.update(selectedSaving);
                isEditingMode = false;
                loadSavingsData();
                
                // Buscar el registro actualizado para volverlo a seleccionar
                Savings updated = null;
                for (Savings s : savingsList) {
                    if (s.getId() == selectedSaving.getId()) {
                        updated = s;
                        break;
                    }
                }
                if (updated != null) {
                    selectedSaving = updated;
                    tblSavings.getSelectionModel().select(selectedSaving);
                    showDetailPane(selectedSaving);
                } else {
                    handleClearSelection(null);
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Error al guardar ahorros", e.getMessage());
        }
    }

    /**
     * Abre el diálogo de ingreso de abonos para el ahorro seleccionado.
     */
    @FXML
    private void handleOpenAbonarDialog(ActionEvent event) {
        if (selectedSaving == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar Abono");
        dialog.setHeaderText("Registrar abono para: " + selectedSaving.getName());

        if (tblSavings.getScene() != null) {
            dialog.getDialogPane().getStylesheets().addAll(tblSavings.getScene().getStylesheets());
        }

        ButtonType saveButtonType = new ButtonType("Abonar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));

        TextField txtAbonoValor = new TextField();
        txtAbonoValor.setPromptText("Ej: 200000");
        txtAbonoValor.setPrefWidth(200);

        DatePicker dpAbonoDate = new DatePicker(LocalDate.now());
        dpAbonoDate.setPrefWidth(200);

        TextField txtAbonoObservation = new TextField();
        txtAbonoObservation.setPromptText("Opcional...");
        txtAbonoObservation.setPrefWidth(200);

        grid.add(new Label("Valor del abono:"), 0, 0);
        grid.add(txtAbonoValor, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(dpAbonoDate, 1, 1);
        grid.add(new Label("Observación:"), 0, 2);
        grid.add(txtAbonoObservation, 1, 2);

        dialog.getDialogPane().setContent(grid);

        javafx.application.Platform.runLater(txtAbonoValor::requestFocus);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        okButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String valStr = txtAbonoValor.getText().trim();
            if (valStr.isEmpty()) {
                showWarningAlert("Campo Vacío", "Por favor ingresa el valor del abono.");
                ae.consume();
                return;
            }
            try {
                double amount = Double.parseDouble(valStr);
                if (amount <= 0) {
                    showWarningAlert("Valor Inválido", "El valor del abono debe ser mayor a cero.");
                    ae.consume();
                }
            } catch (NumberFormatException e) {
                showWarningAlert("Valor Inválido", "Por favor ingresa un valor numérico válido.");
                ae.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            double amount = Double.parseDouble(txtAbonoValor.getText().trim());
            LocalDate date = dpAbonoDate.getValue();
            String observation = txtAbonoObservation.getText().trim();

            double remaining = selectedSaving.getRemainingAmount();
            if (amount > remaining) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Supera el objetivo");
                confirmAlert.setHeaderText("El abono supera el valor restante");
                confirmAlert.setContentText("El valor ingresado (" + currencyFormat.format(amount) + 
                    ") supera el valor restante de la meta (" + currencyFormat.format(remaining) + ").\n\n" +
                    "¿Deseas registrar exactamente el valor restante (" + currencyFormat.format(remaining) + ") o cancelar?");

                ButtonType registerRemainingBtn = new ButtonType("Registrar restante", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelBtn = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                confirmAlert.getButtonTypes().setAll(registerRemainingBtn, cancelBtn);

                Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == registerRemainingBtn) {
                    amount = remaining;
                } else {
                    return;
                }
            }

            try {
                // Registrar abono
                SavingsMovement movement = new SavingsMovement(selectedSaving.getId(), amount, date, observation);
                savingsMovementDAO.insert(movement);

                // Obtener dinero total actualizado
                double newSaved = savingsDAO.getSavedAmount(selectedSaving.getId());
                selectedSaving.setSavedAmount(newSaved);

                boolean metaCompletada = false;
                if (newSaved >= selectedSaving.getTargetValue()) {
                    selectedSaving.setStatus("Completado");
                    savingsDAO.updateStatus(selectedSaving.getId(), "Completado");
                    metaCompletada = true;
                }

                loadSavingsData();

                // Volver a seleccionar y recargar
                Savings reSelected = null;
                for (Savings s : savingsList) {
                    if (s.getId() == selectedSaving.getId()) {
                        reSelected = s;
                        break;
                    }
                }
                if (reSelected != null) {
                    selectedSaving = reSelected;
                    tblSavings.getSelectionModel().select(selectedSaving);
                    showDetailPane(selectedSaving);
                }

                if (metaCompletada) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("🎉 ¡Meta Alcanzada!");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("🎉 ¡Felicidades!\n\nHas completado exitosamente tu meta de ahorro \"" + selectedSaving.getName() + "\".");
                    successAlert.showAndWait();
                }
            } catch (SQLException e) {
                showErrorAlert("Error al registrar abono", e.getMessage());
            }
        }
    }

    /**
     * Abre la vista para ver el historial de movimientos de la meta seleccionada.
     */
    @FXML
    private void handleOpenHistoryDialog(ActionEvent event) {
        if (selectedSaving == null) return;

        Dialog<Void> historyDialog = new Dialog<>();
        historyDialog.setTitle("Historial de Movimientos");
        historyDialog.setHeaderText("Historial de abonos para: " + selectedSaving.getName());

        if (tblSavings.getScene() != null) {
            historyDialog.getDialogPane().getStylesheets().addAll(tblSavings.getScene().getStylesheets());
        }
        historyDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<SavingsMovement> tblMovements = new TableView<>();
        tblMovements.setPrefWidth(450);
        tblMovements.setPrefHeight(300);

        TableColumn<SavingsMovement, String> colMovDate = new TableColumn<>("Fecha");
        colMovDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        colMovDate.setPrefWidth(100);

        TableColumn<SavingsMovement, Number> colMovAmount = new TableColumn<>("Valor");
        colMovAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()));
        colMovAmount.setPrefWidth(120);
        colMovAmount.setCellFactory(column -> new TableCell<SavingsMovement, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item.doubleValue()));
                }
            }
        });

        TableColumn<SavingsMovement, String> colMovObs = new TableColumn<>("Observación");
        colMovObs.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getObservation()));
        colMovObs.setPrefWidth(210);

        tblMovements.getColumns().addAll(colMovDate, colMovAmount, colMovObs);

        try {
            List<SavingsMovement> movements = savingsMovementDAO.findBySavingId(selectedSaving.getId());
            tblMovements.setItems(FXCollections.observableArrayList(movements));
        } catch (SQLException e) {
            showErrorAlert("Error al cargar movimientos", e.getMessage());
        }

        historyDialog.getDialogPane().setContent(tblMovements);
        historyDialog.showAndWait();
    }

    /**
     * Habilita el modo de edición y abre el formulario.
     */
    @FXML
    private void handleStartEdit(ActionEvent event) {
        if (selectedSaving == null) return;

        isEditingMode = true;

        paneForm.setVisible(true);
        paneForm.setManaged(true);
        paneDetail.setVisible(false);
        paneDetail.setManaged(false);

        lblFormTitle.setText("Editar Ahorro");
        hbEditActions.setVisible(true);
        hbEditActions.setManaged(true);

        dpDate.setValue(selectedSaving.getDateCurrent());
        txtName.setText(selectedSaving.getName());
        txtDescription.setText(selectedSaving.getDescription());
        txtTargetValue.setText(String.valueOf(selectedSaving.getTargetValue()));
        cbStatus.setValue(selectedSaving.getStatus());
    }

    /**
     * Cancela la edición y vuelve a mostrar el detalle de la meta.
     */
    @FXML
    private void handleCancelEdit(ActionEvent event) {
        isEditingMode = false;
        hbEditActions.setVisible(false);
        hbEditActions.setManaged(false);
        if (selectedSaving != null) {
            showDetailPane(selectedSaving);
        } else {
            handleClearSelection(null);
        }
    }

    /**
     * Elimina el ahorro seleccionado tras confirmación.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedSaving == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que deseas eliminar la meta \"" + selectedSaving.getName() + "\" y todos sus abonos?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                savingsDAO.delete(selectedSaving.getId());
                loadSavingsData();
                handleClearSelection(null);
            } catch (SQLException e) {
                showErrorAlert("Error al eliminar ahorro", e.getMessage());
            }
        }
    }

    /**
     * Limpia la selección de la tabla y vuelve al formulario de creación.
     */
    @FXML
    private void handleClearSelection(ActionEvent event) {
        tblSavings.getSelectionModel().clearSelection();
        selectedSaving = null;
        isEditingMode = false;

        paneForm.setVisible(true);
        paneForm.setManaged(true);
        paneDetail.setVisible(false);
        paneDetail.setManaged(false);

        lblFormTitle.setText("Crear Ahorro");
        hbEditActions.setVisible(false);
        hbEditActions.setManaged(false);

        dpDate.setValue(LocalDate.now());
        txtName.clear();
        txtDescription.clear();
        txtTargetValue.clear();
        cbStatus.setValue("Activo");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        handleClearSelection(event);
    }

    /**
     * Aplica los filtros de búsqueda y estado.
     */
    @FXML
    private void applyFilters(ActionEvent event) {
        String search = txtSearch.getText();
        String status = cbFilterStatus.getValue();

        try {
            List<Savings> filtered = savingsDAO.findByFilters(null, null, status, search);
            savingsList.setAll(filtered);
            tblSavings.setItems(savingsList);
        } catch (SQLException e) {
            showErrorAlert("Error al filtrar ahorros", e.getMessage());
        }
    }

    /**
     * Restablece los filtros aplicados.
     */
    @FXML
    private void resetFilters(ActionEvent event) {
        txtSearch.clear();
        cbFilterStatus.setValue("Todos");
        loadSavingsData();
    }

    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona una fecha.");
            return false;
        }
        if (txtName.getText().trim().isEmpty()) {
            showWarningAlert("Formulario Incompleto", "Por favor ingresa el nombre de la meta.");
            return false;
        }
        try {
            double target = Double.parseDouble(txtTargetValue.getText().trim());
            if (target < 0) {
                showWarningAlert("Formulario Inválido", "El valor objetivo no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un valor objetivo numérico válido.");
            return false;
        }
        if (cbStatus.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona un estado.");
            return false;
        }
        return true;
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
