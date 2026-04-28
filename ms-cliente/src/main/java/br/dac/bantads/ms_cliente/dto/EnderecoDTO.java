package br.dac.bantads.ms_cliente.dto;

/**
 * DTO para transferência de dados de endereço do cliente.
 * Utilizado nos requests/responses de autocadastro (R1) e alteração de perfil (R4).
 */
public record EnderecoDTO(
        String logradouro,
        String numero,
        String complemento,
        String cep,
        String cidade,
        String estado
) {}
