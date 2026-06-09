package br.dac.bantads.ms_saga.saga;

import java.math.BigDecimal;
import java.util.UUID;

public class SagaInstance {

    private final UUID sagaId;
    private SagaStatus status;

    // Dados do cliente necessários para cada passo
    private final String nome;
    private final String email;
    private final String senha;
    private final String cpf;
    private final String telefone;
    private final BigDecimal salario;
    private final String endereco;
    private final String cep;
    private final String cidade;
    private final String estado;

    // Preenchido após o passo 1 (ms-cliente responde com o UUID gerado)
    private UUID uuidCliente;

    public SagaInstance(UUID sagaId, String nome, String email, String senha, String cpf,
                        String telefone, BigDecimal salario, String endereco, String cep,
                        String cidade, String estado) {
        this.sagaId  = sagaId;
        this.nome    = nome;
        this.email   = email;
        this.senha   = senha;
        this.cpf     = cpf;
        this.telefone = telefone;
        this.salario  = salario;
        this.endereco = endereco;
        this.cep      = cep;
        this.cidade   = cidade;
        this.estado   = estado;
        this.status   = SagaStatus.INICIADA;
    }

    public UUID getSagaId()     { return sagaId; }
    public SagaStatus getStatus() { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }

    public String getNome()     { return nome; }
    public String getEmail()    { return email; }
    public String getSenha()    { return senha; }
    public String getCpf()      { return cpf; }
    public String getTelefone() { return telefone; }
    public BigDecimal getSalario() { return salario; }
    public String getEndereco() { return endereco; }
    public String getCep()      { return cep; }
    public String getCidade()   { return cidade; }
    public String getEstado()   { return estado; }

    public UUID getUuidCliente() { return uuidCliente; }
    public void setUuidCliente(UUID uuidCliente) { this.uuidCliente = uuidCliente; }
}
