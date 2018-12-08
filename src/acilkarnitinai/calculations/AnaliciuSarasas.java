package acilkarnitinai.calculations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AnaliciuSarasas {

    public static final Logger log = Logger.getLogger(AnaliciuSarasas.class);

    private static AnaliciuSarasas instance = new AnaliciuSarasas();
    private static String fileName = "Normos.txt";
    private ObservableList<Analite> analiciuSarasas;

    private AnaliciuSarasas() {
    }

    public static AnaliciuSarasas getInstance() {
        return instance;
    }

    public void nuskaitytiAnaliciuSarasa() {

        analiciuSarasas = FXCollections.observableArrayList();
        Path path;
        BufferedReader br = null;
        String input;

        try {
            path = Paths.get(fileName);
            br = Files.newBufferedReader(path);
            while ((input = br.readLine()) != null) {
                String[] lineSplit = input.split(",");
                AnalitesTipas tipas = null;
                if (lineSplit[3].trim().equals(AnalitesTipas.AMINORUGSTIS.toString())) {
                    tipas = AnalitesTipas.AMINORUGSTIS;
                } else if (lineSplit[3].trim().equals(AnalitesTipas.ACILKARNITINAS.toString())) {
                    tipas = AnalitesTipas.ACILKARNITINAS;
                } else {
                    tipas = AnalitesTipas.KITA;
                }
                Analite analite = new Analite(lineSplit[0].trim(), Double.valueOf(lineSplit[1].trim()), Double.valueOf(lineSplit[2].trim()), tipas);
                analiciuSarasas.add(analite);
            }
        } catch (Exception e) {
            log.info("Klasė Analičių sąrašas, klaida nuskaitant analites iš .txt failo: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText("Įvyko klaida");
            alert.setContentText(e.getMessage());
            alert.show();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.info("Klasė Analičių sąrašas, klaida nuskaitant analites iš .txt failo: " + e.getMessage());
            }

        }
    }

    public void issaugotiAnaliciuSarasa(ObservableList<Analite> newAnaliciuSarasas) {

        Path path;
        BufferedWriter bw = null;

        try {
            path = Paths.get(fileName);
            bw = Files.newBufferedWriter(path);
            Iterator<Analite> iter = newAnaliciuSarasas.iterator();
            while (iter.hasNext()) {
                Analite item = iter.next();
                bw.write(String.format("%s, %s, %s, %s",
                        item.getAnalitesPavadinimas(),
                        String.valueOf(item.getAnalitesMin()),
                        String.valueOf(item.getAnalitesMax()),
                        item.getAnalitesTipas()
                ));
                bw.newLine();
            }
        } catch (Exception e) {
            log.info("Klasė Analičių sąrašas, klaida išsaugas analites į .txt failą: " + e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                log.info("Klasė Analičių sąrašas, klaida išsaugas analites į .txt failą: " + e.getMessage());
            }
        }
    }

    public Map<String, Double[]> normosSkaiciavimams(ObservableList<Analite> analites) {

        Map<String, Double[]> normuMap = new HashMap<>();

        for (Analite analite : analites) {
            Double[] normos = {analite.getAnalitesMin(), analite.getAnalitesMax()};
            normuMap.put(analite.getAnalitesPavadinimas(), normos);
        }
        return normuMap;
    }

    public ObservableList<Analite> getAnaliciuSarasas() {
        return analiciuSarasas;
    }

}