package acilkarnitinai.calculations;

import javafx.collections.FXCollections;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Pacientas {

    public static final Logger log = Logger.getLogger(Pacientas.class);

    private String fileName;
    private String sampleName;
    private Map<String, Double> rezultatai;
    private ArrayList<String> analites;

    public Pacientas(ArrayList<String> duomenys, ArrayList<String> antrastes) {

        if (duomenys.size() < 7 || duomenys.get(2).equals("QC Low") || duomenys.get(2).equals("QC High")) {
            return;
        }

        this.fileName = duomenys.get(0);
        this.sampleName = duomenys.get(2);

        Double[] reiksmes = new Double[duomenys.size() - 6];

        for (int i = 0; i < reiksmes.length; i++) {
            if (duomenys.get(i + 6).equals("") || duomenys.get(i + 6).equals("\t")) {
                reiksmes[i] = 0.0;
            } else {
                try {
                    reiksmes[i] = Double.valueOf(duomenys.get(i + 6));
                } catch (Exception e) {
                    log.info("Klasė Pacientas, klaida paverčiant duomenis į Paciento objektus: " + e.getMessage());
                    return;
                }
            }
        }
        this.rezultatai = new LinkedHashMap<>();
        this.analites = new ArrayList<>();
        for (int i = 0; i < reiksmes.length; i++) {
            rezultatai.put(antrastes.get(i + 6), reiksmes[i]);
            analites.add(antrastes.get(i + 6));
        }
    }

    @Override
    public String toString() {
        return sampleName;
    }

    public Map<String, Double> getRezultatai() {
        return FXCollections.observableMap(rezultatai);
    }

    public ArrayList<String> getAnalites() {
        return analites;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSampleName() {
        return sampleName;
    }
}