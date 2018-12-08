package acilkarnitinai;

import acilkarnitinai.calculations.AnaliciuSarasas;
import acilkarnitinai.calculations.Analite;
import acilkarnitinai.calculations.AnalitesTipas;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.log4j.Logger;

public class AnaliciuNormuController {

    public static final Logger log = Logger.getLogger(AnaliciuNormuController.class);

    @FXML
    private ListView<Analite> analiciuListView;
    @FXML
    private TextField analitesName;
    @FXML
    private TextField analitesMin;
    @FXML
    private TextField analitesMax;
    @FXML
    private ComboBox<String> analitesTipoComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    private ObservableList<String> analiciuTipai;

    public void initialize() {

        try {
            analiciuListView.getItems().setAll(AnaliciuSarasas.getInstance().getAnaliciuSarasas());
            analiciuListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            analiciuListView.getSelectionModel().selectFirst();
            analitesName.setText(analiciuListView.getItems().get(0).getAnalitesPavadinimas());
            analitesMin.setText(String.valueOf(analiciuListView.getItems().get(0).getAnalitesMin()));
            analitesMax.setText(String.valueOf(analiciuListView.getItems().get(0).getAnalitesMax()));

            analiciuTipai = FXCollections.observableArrayList();
            analiciuTipai.add("AMINORUGSTIS");
            analiciuTipai.add("ACILKARNITINAS");
            analiciuTipai.add("KITA");
            analitesTipoComboBox.setItems(analiciuTipai);
            analitesTipoComboBox.setValue(analiciuListView.getItems().get(0).getAnalitesTipas().toString());

        } catch (Exception e) {
            log.info("Klasė AnaliciuNormuController, nepavyko įkelti analites: " + e.getMessage());
            return;
        }

        analiciuListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Analite>() {
            @Override
            public void changed(ObservableValue<? extends Analite> observable, Analite oldValue, Analite newValue) {
                if (newValue != null) {
                    analitesName.setText(newValue.getAnalitesPavadinimas());
                    analitesMin.setText(String.valueOf(newValue.getAnalitesMin()));
                    analitesMax.setText(String.valueOf(newValue.getAnalitesMax()));
                    analitesTipoComboBox.setValue(newValue.getAnalitesTipas().toString());
                }
            }
        });
    }

    public boolean saveAnalite() {
        String name = analitesName.getText().trim();
        String min = analitesMin.getText().trim();
        String max = analitesMax.getText().trim();
        String tipas = analitesTipoComboBox.getValue();
        AnalitesTipas analitesTipas;
        switch (tipas) {
            case "AMINORUGSTIS":
                analitesTipas = AnalitesTipas.AMINORUGSTIS;
                break;
            case "ACILKARNITINAS":
                analitesTipas = AnalitesTipas.ACILKARNITINAS;
                break;
            default:
                analitesTipas = AnalitesTipas.KITA;
                break;
        }

        Analite newAnalite;
        try {
            newAnalite = new Analite(name, Double.valueOf(min), Double.valueOf(max), analitesTipas);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Netinkamai užpildyti laukai");
            alert.setContentText(e.getMessage());
            alert.show();
            return false;
        }

        int index = -1;
        for (Analite analite : analiciuListView.getItems()) {
            if (analite.findAnalite(newAnalite)) {
                index = analiciuListView.getItems().indexOf(analite);
                break;
            }
        }
        if (index == -1) {
            analiciuListView.getItems().add(newAnalite);
            AnaliciuSarasas.getInstance().getAnaliciuSarasas().add(newAnalite);
            return true;
        } else {
            AnaliciuSarasas.getInstance().getAnaliciuSarasas().set(index, newAnalite);
            analiciuListView.getItems().setAll(AnaliciuSarasas.getInstance().getAnaliciuSarasas());
            analiciuListView.getSelectionModel().select(newAnalite);
            return true;
        }
    }

    public void deleteAnalite() {
        Analite analite = analiciuListView.getSelectionModel().getSelectedItem();
        AnaliciuSarasas.getInstance().getAnaliciuSarasas().remove(analite);
        analiciuListView.getItems().setAll(AnaliciuSarasas.getInstance().getAnaliciuSarasas());
        analiciuListView.getSelectionModel().selectFirst();
    }
}