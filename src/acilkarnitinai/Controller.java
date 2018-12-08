package acilkarnitinai;

import acilkarnitinai.calculations.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    public static final Logger log = Logger.getLogger(Controller.class);

    @FXML
    private RadioButton buttonVisos;
    @FXML
    private RadioButton buttonAmino;
    @FXML
    private RadioButton buttonAcil;
    @FXML
    private ListView<Pacientas> pacientuListView;
    @FXML
    private GridPane pacientuRezultatai;
    @FXML
    private TextField fileName;
    @FXML
    private Label pavadinimas;
    @FXML
    private ToggleGroup analiciuToggleGroup;
    @FXML
    private BorderPane pagrindinisLangas;

    private Map<String, Double[]> normuMap;
    private ObservableList<Pacientas> pacientuDuomenys;
    private Predicate<Analite> tikAcilkarnitinai;
    private Predicate<Analite> tikAminorugstys;
    private Predicate<Analite> visosAnalites;
    private FilteredList<Analite> filteredList;

    public void initialize() {

        Image image1 = new Image("file:VNT_Logo.png", 70, 70, true, false);
        pavadinimas.setGraphic(new ImageView(image1));

        filteredList = new FilteredList<Analite>(AnaliciuSarasas.getInstance().getAnaliciuSarasas(), visosAnalites);

        pacientuListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pacientas>() {
            @Override
            public void changed(ObservableValue<? extends Pacientas> observable, Pacientas oldValue, Pacientas newValue) {
                if (newValue != null) {
                    paspaustiAntPaciento();
                }
            }
        });

        analiciuToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                RadioButton rb = (RadioButton) newValue;
                String pasirinkimas = rb.getId();
                if (pacientuListView.getItems() != null) {
                    switch (pasirinkimas) {
                        case "buttonVisos":
                            filteredList = new FilteredList<Analite>(AnaliciuSarasas.getInstance().getAnaliciuSarasas(), visosAnalites);
                            paspaustiAntPaciento();
                            break;
                        case "buttonAmino":
                            filteredList = new FilteredList<Analite>(AnaliciuSarasas.getInstance().getAnaliciuSarasas(), tikAminorugstys);
                            paspaustiAntPaciento();
                            break;
                        case "buttonAcil":
                            filteredList = new FilteredList<Analite>(AnaliciuSarasas.getInstance().getAnaliciuSarasas(), tikAcilkarnitinai);
                            paspaustiAntPaciento();
                            break;
                    }
                }
            }
        });

        tikAcilkarnitinai = new Predicate<Analite>() {
            @Override
            public boolean test(Analite analite) {
                return analite.getAnalitesTipas() == AnalitesTipas.ACILKARNITINAS;
            }
        };

        tikAminorugstys = new Predicate<Analite>() {
            @Override
            public boolean test(Analite analite) {
                return analite.getAnalitesTipas() == AnalitesTipas.AMINORUGSTIS;
            }
        };

        visosAnalites = new Predicate<Analite>() {
            @Override
            public boolean test(Analite analite) {
                return true;
            }
        };
    }

    @FXML
    public void ikeltiPacientus() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pasirinkite duomenų failą");
        File pasirinktasFailas = fileChooser.showOpenDialog(null);
        if (pasirinktasFailas == null) {
            return;
        }
        if (!pasirinktasFailas.toString().endsWith(".csv")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Netinkamas failo formatas!");
            alert.setContentText("Ši programa gali nuskaityti tik atitinkamai suformatuotus *.csv failus, gautus iš ChemoView programos.");
            alert.show();
            return;
        }
        fileName.setText(pasirinktasFailas.toString());

        try {
            pacientuDuomenys = Nuskaitymas.duomenuSutvarkymas(Nuskaitymas.nuskaityti(pasirinktasFailas));
            pacientuListView.getItems().setAll(pacientuDuomenys);
            pacientuListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            pacientuListView.getSelectionModel().selectFirst();
        } catch (Exception e) {
            log.info("Klaida: " + e.getMessage());
        }
    }

    @FXML
    public void paspaustiAntPaciento() {

        Pacientas paspaustasPacientas = pacientuListView.getSelectionModel().getSelectedItem();

        if (paspaustasPacientas == null) {
            return;
        }
        pacientuRezultatai.getChildren().clear();
        pacientuRezultatai.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        pacientuRezultatai.setHgap(10);
        pacientuRezultatai.setVgap(10);

        int kiekAnaliciu = paspaustasPacientas.getAnalites().size();

        int rowIndex = 0;
        int columnIndex = 0;

        for (int i = 0; i < kiekAnaliciu; i++) {

            String currentAnalite = paspaustasPacientas.getAnalites().get(i);
            normuMap = AnaliciuSarasas.getInstance().normosSkaiciavimams(filteredList);

            if (!normuMap.containsKey(currentAnalite)) {
                continue;
            }

            Double currentAnaliteMin = normuMap.get(currentAnalite)[0];
            Double currentAnaliteMax = normuMap.get(currentAnalite)[1];
            Double pastaustoPacientoRezultatas = paspaustasPacientas.getRezultatai().get(currentAnalite);

            Canvas canvas = new Canvas(120, 40);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            Label label = new Label(currentAnalite + " : " + pastaustoPacientoRezultatas);
            label.setFont(new Font("Times New Roman", 13));
            label.setPrefWidth(120);

            double faktorius;

            if (pastaustoPacientoRezultatas > currentAnaliteMax) {
                faktorius = (currentAnaliteMax - currentAnaliteMin) / (pastaustoPacientoRezultatas - currentAnaliteMin);
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 10, 100 * faktorius, 10);
                gc.setFill(Color.RED);
                gc.fillRect(100 - 2, 10, 5, 10);
            } else if (pastaustoPacientoRezultatas < currentAnaliteMin) {
                faktorius = (currentAnaliteMax - currentAnaliteMin) / (currentAnaliteMax - pastaustoPacientoRezultatas);
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(100 * (1 - faktorius), 10, 100 * faktorius, 10);
                gc.setFill(Color.RED);
                gc.fillRect(0, 10, 5, 10);
            } else {
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 10, 100, 10);
                gc.setFill(Color.GREEN);
                double start = (pastaustoPacientoRezultatas - currentAnaliteMin) / (currentAnaliteMax - currentAnaliteMin);
                gc.fillRect(((start * 100) - 2), 10, 5, 10);
            }
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Times New Roman", 10));
            gc.fillText(currentAnaliteMin + " - " + currentAnaliteMax, 30, 30);

            pacientuRezultatai.add(label, (2 * columnIndex), rowIndex);
            pacientuRezultatai.add(canvas, (1 + 2 * columnIndex), rowIndex);

            if (columnIndex == 2) {
                columnIndex = 0;
                rowIndex++;
            } else {
                columnIndex++;
            }
        }
    }

    @FXML
    public void suformuotiLentele() {

        Pacientas pacientas = pacientuListView.getSelectionModel().getSelectedItem();

        if (pacientas == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Nepasirinktas pacientas!");
            alert.setContentText("Pagrindinio lango kairėje pusėje esančiame sąraše paspauskite ant paciento, kuriam norite suformuoti rezultatų lentelę");
            alert.show();
        } else {
            Stage rezultatuLenteleStage = new Stage();
            rezultatuLenteleStage.setTitle("Rezultatų lentelė");
            GridPane rezultatuLentelesGridPane = new GridPane();
            ScrollPane rezultatuLentelesScrollPane = new ScrollPane(rezultatuLentelesGridPane);
            rezultatuLentelesScrollPane.setPannable(true);
            rezultatuLentelesScrollPane.setFitToHeight(true);
            rezultatuLentelesScrollPane.setFitToWidth(true);
            rezultatuLenteleStage.setScene(new Scene(rezultatuLentelesScrollPane, 530, 600));
            rezultatuLenteleStage.show();
            rezultatuLentelesGridPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
            rezultatuLentelesGridPane.setAlignment(Pos.CENTER);
            rezultatuLentelesGridPane.setVgap(5);
            rezultatuLentelesGridPane.setHgap(15);

            Label label00 = new Label("Failo pavadinimas: " + pacientas.getFileName());
            label00.setWrapText(true);
            label00.setFont(new Font("Times New Roman", 12));
            Label label01 = new Label("Pacientas: " + pacientas.getSampleName());
            label01.setWrapText(true);
            label01.setFont(new Font("Times New Roman", 12));
            Separator separator = new Separator();

            rezultatuLentelesGridPane.add(label00, 0, 0, 5, 1);
            rezultatuLentelesGridPane.add(label01, 0, 1, 5, 1);
            rezultatuLentelesGridPane.add(separator, 0, 2, 5, 1);

            for (int j = 0; j < 5; j++) {
                Label labelA = new Label();
                String pavadinimas = "";
                switch (j) {
                    case 0:
                        pavadinimas = "Analitė";
                        break;
                    case 1:
                        pavadinimas = "Rezultatas (μM)";
                        break;
                    case 2:
                        pavadinimas = "Min riba (μM)";
                        break;
                    case 3:
                        pavadinimas = "Max riba (μM)";
                        break;
                    case 4:
                        pavadinimas = "Vertinimas";
                        break;
                }
                labelA.setText(pavadinimas);
                labelA.setFont(new Font("Times New Roman bold", 12));
                labelA.setAlignment(Pos.CENTER);
                labelA.setPrefWidth(100);
                rezultatuLentelesGridPane.add(labelA, j, 3);
            }

            int i = 4;

            for (Map.Entry<String, Double> rez : pacientas.getRezultatai().entrySet()) {
                if (normuMap.get(rez.getKey()) == null) {
                    continue;
                }
                Label label1 = new Label(rez.getKey());
                setLabel(label1);
                Label label2 = new Label(String.valueOf(rez.getValue()));
                setLabel(label2);
                Label label3 = new Label(String.valueOf(normuMap.get(rez.getKey())[0]));
                setLabel(label3);
                Label label4 = new Label(String.valueOf(normuMap.get(rez.getKey())[1]));
                setLabel(label4);
                Label label5 = new Label();
                if (rez.getValue() < normuMap.get(rez.getKey())[1] && rez.getValue() > normuMap.get(rez.getKey())[0]) {
                    label5.setText("Passed");
                } else {
                    label5.setText("Outlier");
                    label5.setTextFill(Color.RED);
                }
                setLabel(label5);
                rezultatuLentelesGridPane.addRow(i++, label1, label2, label3, label4, label5);
            }
            separator = new Separator();
            rezultatuLentelesGridPane.add(separator, 0, ++i, 5, 1);
            rezultatuLentelesGridPane.setPadding(new Insets(7, 5, 7, 5));
        }
    }

    private void setLabel(Label label) {
        label.setFont(new Font("Times New Roman", 12));
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(100);
    }

    @FXML
    public void redaguotiAnalites() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(pagrindinisLangas.getScene().getWindow());
        dialog.setTitle("Redaguoti analičių normas");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("analiciuNormos.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Nepavyko atidaryti analičių lango");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        Optional<ButtonType> result = dialog.showAndWait();

        while (true) {
            if (result.isPresent() && result.get() == ButtonType.OK) {
                AnaliciuSarasas.getInstance().issaugotiAnaliciuSarasa(AnaliciuSarasas.getInstance().getAnaliciuSarasas());
                AnaliciuSarasas.getInstance().nuskaitytiAnaliciuSarasa();
                initialize();
                pacientuListView.getSelectionModel().selectFirst();
                paspaustiAntPaciento();
                break;
            } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                AnaliciuSarasas.getInstance().nuskaitytiAnaliciuSarasa();
                initialize();
                pacientuListView.getSelectionModel().selectFirst();
                break;
            } else if (result.isPresent() && result.get() == ButtonType.APPLY) {
                AnaliciuSarasas.getInstance().issaugotiAnaliciuSarasa(AnaliciuSarasas.getInstance().getAnaliciuSarasas());
                AnaliciuSarasas.getInstance().nuskaitytiAnaliciuSarasa();
                result = dialog.showAndWait();
            }
        }
    }
}
