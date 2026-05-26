package br.dac.bantads.ms_funcionario.dto;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioResponseDTO() {

    private String cpf;
    private String nome;
    private String email;
    private TipoFuncionario tipo;

    public FuncionarioResponseDTO (FuncionarioModel funcionarioModel){
        this.cpf = funcionarioModel.getCpf();
        this.nome = funcionarioModel.getNome();
        this.email = funcionarioModel.getEmail();
        this.tipo = funcionarioModel.getTipo();
    }
}
