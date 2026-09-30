package br.com.api.pessoa.entidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonAlias({"codigo_modelo", "modelo", "codModelo"})
    @Column(name = "codigo_modelo", unique = true, length = 100)
    private String codigoModelo;

    private String nome;

    private String categoria;

    private Integer quantidade = 0;

    private Double preco = 0.0;

    // Construtor padrão (obrigatório para JPA)
    public Produto() {
    }

    // Construtor completo
    public Produto(Integer id, String codigoModelo, String nome, String categoria, Integer quantidade, Double preco) {
        this.id = id;
        this.codigoModelo = codigoModelo;
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = (quantidade != null) ? quantidade : 0;
        this.preco = (preco != null) ? preco : 0.0;
    }

    // Construtor para novos cadastros (sem id)
    public Produto(String codigoModelo, String nome, String categoria, Integer quantidade, Double preco) {
        this.codigoModelo = codigoModelo;
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = (quantidade != null) ? quantidade : 0;
        this.preco = (preco != null) ? preco : 0.0;
    }

    // Getters e Setters explícitos (garantem funcionamento independente do plugin Lombok)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoModelo() {
        return codigoModelo;
    }

    public void setCodigoModelo(String codigoModelo) {
        this.codigoModelo = codigoModelo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = (quantidade != null) ? quantidade : 0;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = (preco != null) ? preco : 0.0;
    }

    /**
     * Compatibilidade com frontends que utilizam 'codigo' em vez de 'id'.
     * Ao serializar para JSON, Jackson incluirá tanto 'id' quanto 'codigo'.
     */
    @JsonProperty("codigo")
    public Integer getCodigo() {
        return this.id;
    }

    @JsonProperty("codigo")
    public void setCodigo(Integer codigo) {
        if (this.id == null && codigo != null) {
            this.id = codigo;
        }
    }

    /**
     * Compatibilidade com frontends que utilizam 'valor' em vez de 'preco'.
     */
    @JsonProperty("valor")
    public Double getValor() {
        return this.preco;
    }

    @JsonProperty("valor")
    public void setValor(Double valor) {
        if (this.preco == null || this.preco == 0.0) {
            this.preco = valor;
        }
    }

    @Override
    public String toString() {
        return "Produto [id=" + id + ", codigoModelo=" + codigoModelo + ", nome=" + nome + ", categoria=" + categoria + 
               ", quantidade=" + quantidade + ", preco=" + preco + "]";
    }
}
