package com.sifap.beneficiary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Embedded address value object. */
@Embeddable
public class Address {

    @Column(name = "addr_logradouro", length = 120)   private String logradouro;
    @Column(name = "addr_numero", length = 20)        private String numero;
    @Column(name = "addr_complemento", length = 60)   private String complemento;
    @Column(name = "addr_bairro", length = 80)        private String bairro;
    @Column(name = "addr_municipio", length = 80)     private String municipio;
    @Column(name = "addr_uf", length = 2)             private String uf;
    @Column(name = "addr_cep", length = 8)            private String cep;

    protected Address() {}

    public Address(String logradouro, String numero, String complemento,
                   String bairro, String municipio, String uf, String cep) {
        this.logradouro  = logradouro;
        this.numero      = numero;
        this.complemento = complemento;
        this.bairro      = bairro;
        this.municipio   = municipio;
        this.uf          = uf;
        this.cep         = cep;
    }

    public String getLogradouro()  { return logradouro; }
    public String getNumero()      { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro()      { return bairro; }
    public String getMunicipio()   { return municipio; }
    public String getUf()          { return uf; }
    public String getCep()         { return cep; }
}
