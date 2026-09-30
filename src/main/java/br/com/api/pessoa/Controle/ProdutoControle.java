package br.com.api.pessoa.Controle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.api.pessoa.entidade.Produto;
import br.com.api.pessoa.repositorio.ProdutoRepositorio;

@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {
        RequestMethod.GET, 
        RequestMethod.POST, 
        RequestMethod.PUT, 
        RequestMethod.DELETE, 
        RequestMethod.OPTIONS, 
        RequestMethod.PATCH
    }
)
@RestController
public class ProdutoControle {

    private final ProdutoRepositorio repositorio;

    public ProdutoControle(ProdutoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Rota de diagnóstico / status do estoque.
     * Retorna se a API está online, o total de tipos de produtos cadastrados e a soma total de unidades em estoque.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        List<Produto> todos = repositorio.findAll();
        long totalTipos = todos.size();
        long totalUnidades = todos.stream().mapToLong(p -> p.getQuantidade() != null ? p.getQuantidade() : 0).sum();
        long estoqueCritico = todos.stream().filter(p -> p.getQuantidade() != null && p.getQuantidade() <= 5).count();

        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("mensagem", "API Spring Boot de Controle de Estoque rodando perfeitamente!");
        info.put("totalProdutosCadastrados", totalTipos);
        info.put("totalItensEmEstoque", totalUnidades);
        info.put("produtosComEstoqueBaixo", estoqueCritico);
        return ResponseEntity.ok(info);
    }

    /**
     * Rota para Selecionar / Listar todos os produtos do estoque.
     * Mantém compatibilidade com '/', '/selecionar', '/produtos' e '/estoque' para o frontend.
     */
    @GetMapping({"/", "/selecionar", "/produtos", "/estoque"})
    public ResponseEntity<List<Produto>> selecionar() {
        List<Produto> lista = repositorio.findAll();
        return ResponseEntity.ok(lista);
    }

    /**
     * Rota para buscar um produto por ID ou código.
     */
    @GetMapping({"/selecionar/{id}", "/produtos/{id}", "/{id}"})
    public ResponseEntity<Produto> selecionarPorId(@PathVariable Integer id) {
        return repositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Rota para buscar produto pelo Código de Modelo (identificador único de estoque).
     */
    @GetMapping({"/modelo/{codigoModelo}", "/produtos/modelo/{codigoModelo}"})
    public ResponseEntity<?> buscarPorCodigoModelo(@PathVariable String codigoModelo) {
        return repositorio.findByCodigoModelo(codigoModelo.trim())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Rota para filtrar produtos com estoque baixo ou zerado.
     * Exemplo: GET /produtos/alerta?limite=5
     */
    @GetMapping({"/produtos/alerta", "/estoque/alerta"})
    public ResponseEntity<List<Produto>> listarEstoqueBaixo(@RequestParam(defaultValue = "5") Integer limite) {
        List<Produto> lista = repositorio.findByQuantidadeLessThanEqual(limite);
        return ResponseEntity.ok(lista);
    }

    /**
     * Rota para Cadastrar novo produto no estoque.
     * Suporta '/cadastrar', '/produtos', '/estoque' e POST em '/'.
     * Retorna HTTP 201 Created com o produto persistido e o ID gerado.
     */
    @PostMapping({"/cadastrar", "/produtos", "/estoque", "/"})
    public ResponseEntity<?> cadastrar(@RequestBody Produto p) {
        if (p.getCodigoModelo() == null || p.getCodigoModelo().trim().isEmpty()) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "O código de modelo do produto é obrigatório.");
            return ResponseEntity.badRequest().body(erro);
        }

        String codModelo = p.getCodigoModelo().trim();
        p.setCodigoModelo(codModelo);

        // Valida se o código de modelo já está em uso no banco
        if (repositorio.existsByCodigoModelo(codModelo)) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Já existe um produto cadastrado com o código de modelo: " + codModelo);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
        }

        if (p.getNome() == null || p.getNome().trim().isEmpty()) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "O nome do produto é obrigatório.");
            return ResponseEntity.badRequest().body(erro);
        }

        // Garante valores padrão caso não sejam informados
        if (p.getQuantidade() == null || p.getQuantidade() < 0) {
            p.setQuantidade(0);
        }
        if (p.getPreco() == null || p.getPreco() < 0.0) {
            p.setPreco(0.0);
        }
        if (p.getCategoria() == null || p.getCategoria().trim().isEmpty()) {
            p.setCategoria("Geral");
        }

        // Garante geração de novo ID
        p.setId(null);
        Produto salvo = repositorio.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    /**
     * Rota para Alterar dados do produto com ID na URL (/alterar/{id}, /produtos/{id}, /{id}).
     */
    @PutMapping({"/alterar/{id}", "/produtos/{id}", "/{id}"})
    public ResponseEntity<?> alterarComId(@PathVariable Integer id, @RequestBody Produto p) {
        Produto existente = repositorio.findById(id).orElse(null);
        if (existente == null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Produto com ID " + id + " não encontrado para alteração.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }

        if (p.getCodigoModelo() != null && !p.getCodigoModelo().trim().isEmpty()) {
            String codModelo = p.getCodigoModelo().trim();
            if (repositorio.existsByCodigoModeloAndIdNot(codModelo, id)) {
                Map<String, String> erro = new HashMap<>();
                erro.put("mensagem", "Já existe outro produto cadastrado com o código de modelo: " + codModelo);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
            }
            p.setCodigoModelo(codModelo);
        } else {
            p.setCodigoModelo(existente.getCodigoModelo());
        }

        p.setId(id);
        if (p.getQuantidade() == null || p.getQuantidade() < 0) {
            p.setQuantidade(0);
        }
        if (p.getPreco() == null || p.getPreco() < 0.0) {
            p.setPreco(0.0);
        }

        Produto atualizado = repositorio.save(p);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Rota para Alterar dados quando o ID vem dentro do corpo JSON (/alterar, /produtos, /estoque).
     * Essencial para formulários React que enviam o objeto completo no body sem ID na URL.
     */
    @PutMapping({"/alterar", "/produtos", "/estoque"})
    public ResponseEntity<?> alterarComBody(@RequestBody Produto p) {
        Integer id = p.getId() != null ? p.getId() : p.getCodigo();

        if (id == null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "O campo 'id' ou 'codigo' é obrigatório no corpo da requisição.");
            return ResponseEntity.badRequest().body(erro);
        }

        Produto existente = repositorio.findById(id).orElse(null);
        if (existente == null) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Produto com ID " + id + " não encontrado para alteração.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }

        if (p.getCodigoModelo() != null && !p.getCodigoModelo().trim().isEmpty()) {
            String codModelo = p.getCodigoModelo().trim();
            if (repositorio.existsByCodigoModeloAndIdNot(codModelo, id)) {
                Map<String, String> erro = new HashMap<>();
                erro.put("mensagem", "Já existe outro produto cadastrado com o código de modelo: " + codModelo);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
            }
            p.setCodigoModelo(codModelo);
        } else {
            p.setCodigoModelo(existente.getCodigoModelo());
        }

        p.setId(id);
        if (p.getQuantidade() == null || p.getQuantidade() < 0) {
            p.setQuantidade(0);
        }
        if (p.getPreco() == null || p.getPreco() < 0.0) {
            p.setPreco(0.0);
        }

        Produto atualizado = repositorio.save(p);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Movimentação de Estoque: ENTRADA de mercadoria (soma ao estoque existente).
     * Suporta receber a quantidade via JSON: {"quantidade": 10} ou query param: ?quantidade=10.
     */
    @PostMapping({"/produtos/{id}/entrada", "/estoque/{id}/entrada"})
    @PatchMapping({"/produtos/{id}/entrada", "/estoque/{id}/entrada"})
    public ResponseEntity<?> entradaEstoque(@PathVariable Integer id, 
                                            @RequestParam(required = false) Integer quantidade,
                                            @RequestBody(required = false) Map<String, Object> body) {
        return repositorio.findById(id).map(produto -> {
            Integer qtdAdicionar = extrairQuantidade(quantidade, body);
            if (qtdAdicionar <= 0) {
                Map<String, String> erro = new HashMap<>();
                erro.put("mensagem", "A quantidade de entrada deve ser maior que zero.");
                return ResponseEntity.badRequest().body((Object) erro);
            }

            int atual = produto.getQuantidade() != null ? produto.getQuantidade() : 0;
            produto.setQuantidade(atual + qtdAdicionar);
            Produto atualizado = repositorio.save(produto);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("status", "sucesso");
            resposta.put("mensagem", "Entrada realizada com sucesso!");
            resposta.put("quantidadeAdicionada", qtdAdicionar);
            resposta.put("saldoAtual", produto.getQuantidade());
            resposta.put("produto", atualizado);
            return ResponseEntity.ok((Object) resposta);
        }).orElseGet(() -> {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Produto com ID " + id + " não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        });
    }

    /**
     * Movimentação de Estoque: SAÍDA / BAIXA de mercadoria (subtrai do estoque com validação de saldo).
     * Impede que o estoque fique negativo!
     */
    @PostMapping({"/produtos/{id}/saida", "/estoque/{id}/saida"})
    @PatchMapping({"/produtos/{id}/saida", "/estoque/{id}/saida"})
    public ResponseEntity<?> saidaEstoque(@PathVariable Integer id, 
                                          @RequestParam(required = false) Integer quantidade,
                                          @RequestBody(required = false) Map<String, Object> body) {
        return repositorio.findById(id).map(produto -> {
            Integer qtdRetirar = extrairQuantidade(quantidade, body);
            if (qtdRetirar <= 0) {
                Map<String, String> erro = new HashMap<>();
                erro.put("mensagem", "A quantidade de saída deve ser maior que zero.");
                return ResponseEntity.badRequest().body((Object) erro);
            }

            int atual = produto.getQuantidade() != null ? produto.getQuantidade() : 0;
            if (atual < qtdRetirar) {
                Map<String, Object> erro = new HashMap<>();
                erro.put("status", "erro");
                erro.put("mensagem", "Estoque insuficiente para realizar a saída.");
                erro.put("saldoDisponivel", atual);
                erro.put("quantidadeTentada", qtdRetirar);
                return ResponseEntity.badRequest().body((Object) erro);
            }

            produto.setQuantidade(atual - qtdRetirar);
            Produto atualizado = repositorio.save(produto);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("status", "sucesso");
            resposta.put("mensagem", "Saída realizada com sucesso!");
            resposta.put("quantidadeRetirada", qtdRetirar);
            resposta.put("saldoAtual", produto.getQuantidade());
            resposta.put("produto", atualizado);
            return ResponseEntity.ok((Object) resposta);
        }).orElseGet(() -> {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Produto com ID " + id + " não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        });
    }

    /**
     * Rota para Remover produto do estoque.
     * Retorna JSON para evitar que o React lance 'SyntaxError: Unexpected end of JSON input'.
     */
    @DeleteMapping({"/remover/{id}", "/produtos/{id}", "/{id}"})
    public ResponseEntity<?> remover(@PathVariable Integer id) {
        if (!repositorio.existsById(id)) {
            Map<String, String> erro = new HashMap<>();
            erro.put("mensagem", "Produto com ID " + id + " não encontrado para exclusão.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }

        repositorio.deleteById(id);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", "sucesso");
        resposta.put("mensagem", "Produto removido do estoque com sucesso!");
        resposta.put("id", id);
        return ResponseEntity.ok(resposta);
    }

    private Integer extrairQuantidade(Integer paramQtd, Map<String, Object> body) {
        if (paramQtd != null) {
            return paramQtd;
        }
        if (body != null && body.containsKey("quantidade")) {
            Object val = body.get("quantidade");
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            try {
                return Integer.parseInt(val.toString());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    public ProdutoRepositorio getRepositorio() {
        return repositorio;
    }
}
