package br.com.sosviale.service;

// taxas aplicadas sobre o valor base em transfers internacionais
// nome beeeeem autoexplicativo
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
