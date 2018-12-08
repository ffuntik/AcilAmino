package acilkarnitinai.calculations;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Nuskaitymas {

    public static final Logger log = Logger.getLogger(Nuskaitymas.class);

    private static ObservableList<Pacientas> pacientuObserListas;

    public static String nuskaityti(File file) {

        Scanner scanner = null;
        StringBuilder duomenys = new StringBuilder();
        try {
            scanner = new Scanner(new FileReader(file.toString()));
            while (scanner.hasNextLine()) {
                duomenys.append(scanner.nextLine());
                duomenys.append("\n");
            }
        } catch (Exception e) {
            log.info("Klasė Nuskaitymas, klaida nuskaitant duomenis iš .csv failo: " + e.toString());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return duomenys.toString();
    }

    public static ObservableList<Pacientas> duomenuSutvarkymas(String duomenys) {

        String[] splitDuomenys = duomenys.split("\n");
        ArrayList<String> antrastes = new ArrayList<>();

        pacientuObserListas = FXCollections.observableArrayList();

        for (int i = 2; i < splitDuomenys.length; i++) {
            ArrayList<String> atskiriDuomenys = new ArrayList<>();
            String stringCopy = splitDuomenys[i];
            while (!stringCopy.isEmpty()) {
                if (stringCopy.indexOf(",") < stringCopy.indexOf("\"") || !stringCopy.contains("\"")) {
                    atskiriDuomenys.add(stringCopy.substring(0, stringCopy.indexOf(",")).trim());
                    stringCopy = stringCopy.substring(stringCopy.indexOf(",") + 1);
                } else {
                    // Čia tas atvėjis, jei kažkokia informacija yra kabutėse, pvz., kaip mėginio pavadinimas
                    // ir reikia paimti visą frazę, esančia kabutėse, neatsižvelgiant į tai, kad jos viduje yra kablelių
                    String pattern = "(\")(.*?)(\")"; // lazy quantifier
                    Pattern groupPattern = Pattern.compile(pattern);
                    Matcher groupMatcher = groupPattern.matcher(stringCopy);
                    if (groupMatcher.find()) {
                        atskiriDuomenys.add(groupMatcher.group(2).trim());
                        stringCopy = stringCopy.substring(stringCopy.indexOf("\"") + 1);
                        stringCopy = stringCopy.substring(stringCopy.indexOf("\"") + 2);
                    }
                }
            }
            if (i == 2) {
                antrastes = new ArrayList<>(atskiriDuomenys);
                i = 4;
                continue;
            }
            Pacientas pacientas = new Pacientas(atskiriDuomenys, antrastes);
            if (pacientas.getSampleName() != null) {
                pacientuObserListas.add(pacientas);
            }
        }
        return pacientuObserListas;
    }
}