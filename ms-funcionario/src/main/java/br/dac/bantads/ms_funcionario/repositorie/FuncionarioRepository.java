package br.dac.bantads.ms_funcionario.repositorie;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID


public interface FuncionarioRepository extends JpaRepository<FuncionarioModel, UUID >{

}