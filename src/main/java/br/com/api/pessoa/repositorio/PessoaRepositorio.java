package br.com.api.pessoa.repositorio;
import org.springframework.data.repository.CrudRepository;
import br.com.api.pessoa.entidade.Pessoa;

public interface PessoaRepositorio extends CrudRepository<Pessoa, Integer> {
     Pessoa save(Pessoa p);


}