package acilkarnitinai.calculations;

public class Analite {

    private String analitesPavadinimas;
    private Double analitesMin;
    private Double analitesMax;
    private AnalitesTipas analitesTipas;

    public Analite(String analitesPavadinimas, Double analitesMin, Double analitesMax, AnalitesTipas analitesTipas) {
        this.analitesPavadinimas = analitesPavadinimas;
        this.analitesMin = analitesMin;
        this.analitesMax = analitesMax;
        this.analitesTipas = analitesTipas;
    }

    public String getAnalitesPavadinimas() {
        return analitesPavadinimas;
    }

    public Double getAnalitesMin() {
        return analitesMin;
    }

    public Double getAnalitesMax() {
        return analitesMax;
    }

    public AnalitesTipas getAnalitesTipas() {
        return analitesTipas;
    }

    @Override
    public String toString() {
        return analitesPavadinimas;
    }

    public boolean findAnalite(Analite otherAnalite) {
        if (this.analitesPavadinimas.equals(otherAnalite.getAnalitesPavadinimas())) {
            return true;
        } else {
            return false;
        }
    }
}