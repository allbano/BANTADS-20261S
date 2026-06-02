package br.dac.bantads.ms_funcionario.repository;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import java.util.Optional;
import java.util.List;

public interface FuncionarioRepository extends JpaRepository<FuncionarioModel, UUID> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Optional<FuncionarioModel> findByCpf(String cpf);

    Optional<FuncionarioModel> findByEmail(String email);

    List<FuncionarioModel> findByTipo(TipoFuncionario tipo);

}