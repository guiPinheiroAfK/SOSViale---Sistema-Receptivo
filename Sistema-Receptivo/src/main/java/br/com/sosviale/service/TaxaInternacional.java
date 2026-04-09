package br.com.sosviale.service;

public enum TaxaInternacional {
    ARGENTINA(0.12),
    PARAGUAI(0.10);

    private final double taxa;

    TaxaInternacional(double taxa){
        this.taxa = taxa;
    }

    public double getTaxa(){
        return taxa;
    }

}
