package br.com.api.pessoa.Controle;

import org.springframework.web.bind.annotation.RestController;

import br.com.api.pessoa.entidade.Pessoa;
import br.com.api.pessoa.repositorio.PessoaRepositorio;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@CrossOrigin(value = "*")
@RestController
public class PessoaControle {

    // Referenciar repositório
    private final PessoaRepositorio repositorio;

    PessoaControle(PessoaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    //Rota para retornar um Hello World;
    @GetMapping("/")
    public String helloWorld() {
        return "Hello World!";
    }
    
    //Rota para cadastrar
    @PostMapping("/cadastrar")
    public Pessoa cadsatrar(@RequestBody Pessoa p) {
      return repositorio.save(p);
    }

    //Rota para Selecionar
    @GetMapping ("/selecionar")
    public Iterable<Pessoa> selecionar(){
          return repositorio.findAll();
    }

    //Rota para Alterar 
    @PutMapping("/alterar/{id}")
    public Pessoa alterar(@PathVariable Integer id, @RequestBody Pessoa p) {
        p.setId(id);
        return repositorio.save(p);
    }

    //Rota para remover
    @DeleteMapping("/remover/{id}")
    public void remover(@PathVariable Integer id){
        repositorio.deleteById(id);
    } 
}
