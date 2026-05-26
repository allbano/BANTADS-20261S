package br.dac.bantads.ms_funcionario.repository;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FuncionarioRepository extends JpaRepository<FuncionarioModel, UUID> {

    boolean exexistsByCpf(String cpf);

}