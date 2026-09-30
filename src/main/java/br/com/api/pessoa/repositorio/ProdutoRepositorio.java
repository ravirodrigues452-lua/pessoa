package br.com.api.pessoa.repositorio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.api.pessoa.entidade.Produto;

public interface ProdutoRepositorio extends JpaRepository<Produto, Integer> {

    // Busca produto exato por código de modelo
    Optional<Produto> findByCodigoModelo(String codigoModelo);

    // Verifica se já existe um produto com o código de modelo informado
    boolean existsByCodigoModelo(String codigoModelo);

    // Verifica se já existe outro produto com o mesmo código de modelo (usado na edição)
    boolean existsByCodigoModeloAndIdNot(String codigoModelo, Integer id);

    // Busca produtos pelo código de modelo (parcial / case insensitive)
    List<Produto> findByCodigoModeloContainingIgnoreCase(String codigoModelo);

    // Busca produtos pelo nome (case insensitive)
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Busca produtos pela categoria (case insensitive)
    List<Produto> findByCategoriaContainingIgnoreCase(String categoria);

    // Busca produtos com estoque baixo ou zerado (menor ou igual a quantidade passada)
    List<Produto> findByQuantidadeLessThanEqual(Integer quantidade);

    // Busca produtos disponíveis (quantidade maior que 0)
    List<Produto> findByQuantidadeGreaterThan(Integer quantidade);
}
