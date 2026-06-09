package br.dac.bantads.ms_cliente.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id; // Para compatibilidade legada
    private UUID uuid; // Formato UUID moderno
    private String nome;
    private String email;
    private String senha;
    private String cpf;
    private String telefone;
    private BigDecimal salario;

    // Campos de endereço do formato legado
    private String rua;
    private Integer numero;
    private String complemento;

    // Campo de endereço do formato moderno
    private String endereco;

    private String cep;

    // Cidade e Estado (podem ser passados como Integer ou String)
    private Object cidade;
    private Object estado;

    private String cargo;
    private boolean ativo;

    // Preenchido pelo ms-saga quando o fluxo é orquestrado; null para fluxo legado
    private String sagaId;

    public UUID getResolvedUuid() {
        if (uuid != null) {
            return uuid;
        }
        if (id != null) {
            // Conversão determinística de Long para UUID para compatibilidade
            return new UUID(0L, id);
        }
        return null;
    }

    public String getResolvedEndereco() {
        if (endereco != null && !endereco.isBlank()) {
            return endereco;
        }
        if (rua != null) {
            StringBuilder sb = new StringBuilder(rua);
            if (numero != null) {
                sb.append(", ").append(numero);
            }
            if (complemento != null && !complemento.isBlank()) {
                sb.append(" - ").append(complemento);
            }
            return sb.toString();
        }
        return null;
    }

    public String getCidadeAsString() {
        if (cidade == null) {
            return null;
        }
        return String.valueOf(cidade);
    }

    public String getEstadoAsString() {
        if (estado == null) {
            return null;
        }
        return String.valueOf(estado);
    }
}
