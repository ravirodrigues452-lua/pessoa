package br.com.api.pessoa.config;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Intercepta exceções e erros da API para retornar JSON amigável,
 * evitando que o frontend React receba páginas HTML de erro do Spring (Whitelabel).
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Requisição Inválida");
        corpo.put("mensagem", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> tratarViolacaoIntegridade(org.springframework.dao.DataIntegrityViolationException ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", HttpStatus.CONFLICT.value());
        corpo.put("erro", "Conflito de Dados / Duplicidade");
        corpo.put("mensagem", "Já existe um registro com este Código de Modelo ou violação de unicidade no banco de dados.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarExcecaoGeral(Exception ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        corpo.put("erro", "Erro interno do servidor");
        corpo.put("mensagem", ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro inesperado.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }
}
