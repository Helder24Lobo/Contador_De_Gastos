package com.personalfinance.contador.controller;

import com.personalfinance.contador.model.Savings;
import com.personalfinance.contador.repository.SavingsDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SavingsController implements Initializable {

    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField txtDescription;
    @FXML
    private ComboBox<String> cbPriority;
    @FXML
    private TextField txtValor;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnClear;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbFilterPriority;
    @FXML
    private TableView<Savings> tblSavings;
    @FXML
    private TableColumn<Savings, Number> colId;
    @FXML
    private TableColumn<Savings, String> colDate;
    @FXML
    private TableColumn<Savings, String> colDescription;
    @FXML
    private TableColumn<Savings, String> colPriority;
    @FXML
    private TableColumn<Savings, Number> colValue;

    //------------------ OBJECTS-----------------------//
    private final SavingsDAO savingsDAO = new SavingsDAO();
    private Savings selectedSaving = null;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    //------------------ VARIABLES-----------------------//
    private final ObservableList<Savings> savingsList = FXCollections.observableArrayList();
    private final String[] incomeTypes = {
            "Alta", "Media", "Baja"
    };


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize ComboBoxes
        cbPriority.setItems(FXCollections.observableArrayList(incomeTypes));

        ObservableList<String> filterTypes = FXCollections.observableArrayList("Todos");
        filterTypes.addAll(incomeTypes);
        cbFilterPriority.setItems(filterTypes);
        cbFilterPriority.setValue("Todos");

        // Configure Table Columns
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateCurrent().toString()));
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        colPriority.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPriority()));
        colValue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()));

        colValue.setCellFactory(column -> new TableCell<Savings, Number>() {
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

        // Table selection event
        tblSavings.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedSaving = newSelection;
                populateForm(selectedSaving);
            }
        });

        dpDate.setValue(LocalDate.now());

        loadSavingsData();
    }

    /**
     * Maneja el evento de guardar un nuevo ahorro o actualizar uno existente.
     * Valida el formulario, extrae los datos de los campos, y luego inserta o actualiza
     * el registro en la base de datos según corresponda.
     *
     * @param event El evento de acción generado por el botón de guardar
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) return;

        LocalDate date = dpDate.getValue();
        String description = txtDescription.getText().trim();
        String priority = cbPriority.getValue();
        double amount = Double.parseDouble(txtValor.getText().trim());

        try {
            if (selectedSaving == null) {
                Savings newSaving = new Savings(date, description, priority, amount);
                savingsDAO.insert(newSaving);
            } else {
                selectedSaving.setDateCurrent(date);
                selectedSaving.setDescription(description);
                selectedSaving.setPriority(priority);
                selectedSaving.setAmount(amount);
                savingsDAO.update(selectedSaving);
            }

            loadSavingsData();
            handleClear(null);
        } catch (SQLException e) {
            showErrorAlert("Error al guardar ahorros", e.getMessage());
        }
    }

    /**
     * Valida que todos los campos del formulario contengan datos válidos.
     * Verifica que la fecha, descripción y prioridad no estén vacíos, y que el valor
     * sea un número válido y no negativo.
     *
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validateForm() {
        if (dpDate.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona una fecha.");
            return false;
        }
        if (txtDescription.getText().trim().isEmpty()) {
            showWarningAlert("Formulario Incompleto", "Por favor ingresa una descripción.");
            return false;
        }
        if (cbPriority.getValue() == null) {
            showWarningAlert("Formulario Incompleto", "Por favor selecciona la prioridad de ahorro.");
            return false;
        }
        try {
            double amount = Double.parseDouble(txtValor.getText().trim());
            if (amount < 0) {
                showWarningAlert("Formulario Inválido", "El valor del ahorro no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Formulario Inválido", "Por favor ingresa un valor numérico válido.");
            return false;
        }
        return true;
    }

    /**
     * Carga la lista de ahorros desde la base de datos y actualiza la tabla de visualización.
     * En caso de error, muestra un diálogo de error al usuario.
     *
     * @throws SQLException Si ocurre un error al acceder a la base de datos
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
     * Limpia todos los campos del formulario y restaura los valores por defecto.
     * Limpia la selección de la tabla y oculta el botón de eliminar.
     *
     * @param event El evento de acción generado por el botón limpiar
     */
    @FXML
    private void handleClear(ActionEvent event) {
        dpDate.setValue(LocalDate.now());
        txtDescription.clear();
        cbPriority.setValue(null);
        txtValor.clear();
        selectedSaving = null;
        btnDelete.setVisible(false);
        tblSavings.getSelectionModel().clearSelection();
    }

    /**
     * Muestra un diálogo de alerta de error al usuario con el título y contenido especificados.
     *
     * @param title   El título del diálogo de error
     * @param content El contenido o mensaje de error a mostrar
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de alerta de advertencia al usuario con el título y contenido especificados.
     *
     * @param title   El título del diálogo de advertencia
     * @param content El contenido o mensaje de advertencia a mostrar
     */
    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
/**
 * Aplica los filtros indicados en la interfaz (texto de búsqueda y tipo/prioridad)
 * y actualiza la lista y la tabla con los resultados filtrados.
 *
 * Comportamiento:
 * - Lee el texto de búsqueda desde `txtSearch` y el tipo/prioridad desde `cbFilterPriority`.
 * - Llama a `savingsDAO.findByFilters(...)` pasando null para los rangos de fecha (no se filtra por fechas
 *   en esta implementación), y pasando `type` y `search` como criterios.
 * - Reemplaza el contenido de `savingsList` con los resultados devueltos y vuelve a asignar
 *   la lista a `tblSavings` para refrescar la vista.
 * - Si ocurre una excepción de SQL, muestra una alerta de error con `showErrorAlert`.
 *
 * Notas:
 * - `type` o `search` pueden ser null o vacíos: el DAO debe manejar esos casos (ej. no aplicar filtro).
 *
 * @param event evento JavaFX que disparó la acción (puede ser null si se llama programáticamente)
 */
    private void applyFilters(ActionEvent event) {
        String search = txtSearch.getText();
        String type = cbFilterPriority.getValue();

        try {
            List<Savings> filtered = savingsDAO.findByFilters(null, null, type, search);
            savingsList.setAll(filtered);
            tblSavings.setItems(savingsList);
        } catch (SQLException e) {
            showErrorAlert("Error al filtrar ahorros", e.getMessage());
        }
    }

    @FXML
/**
 * Restablece los filtros de búsqueda a sus valores por defecto y recarga todos los registros.
 *
 * Comportamiento:
 * - Limpia el campo de búsqueda (`txtSearch`).
 * - Establece el ComboBox de filtro (`cbFilterPriority`) al valor "Todos".
 * - Llama a `loadSavingsData()` para recargar todos los ahorros desde la base de datos.
 *
 * @param event evento JavaFX que disparó la acción (puede ser null si se llama programáticamente)
 */
    private void resetFilters(ActionEvent event) {
        txtSearch.clear();
        cbFilterPriority.setValue("Todos");
        loadSavingsData();
    }

    /**
     * Rellena el formulario del detalle con los datos del objeto `Savings` seleccionado.
     * <p>
     * Comportamiento:
     * - Pone la fecha en `dpDate`.
     * - Rellena la descripción en `txtDescription`.
     * - Selecciona la prioridad en `cbPriority`.
     * - Pone el valor/importe en `txtValor` (usa el valor sin formatear; si se desea formato,
     * modificar según `currencyFormat` o similar).
     * - Hace visible el botón de eliminar `btnDelete` para permitir borrar el registro.
     *
     * @param saving Objeto `Savings` cuyo contenido se va a mostrar en el formulario. No debe ser null.
     */
    private void populateForm(Savings saving) {
        dpDate.setValue(saving.getDateCurrent());
        txtDescription.setText(saving.getDescription());
        cbPriority.setValue(saving.getPriority());
        txtValor.setText(String.valueOf(saving.getAmount()));
        btnDelete.setVisible(true);
    }
}
