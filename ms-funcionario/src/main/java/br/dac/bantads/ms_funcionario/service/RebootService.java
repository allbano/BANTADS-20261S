package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RebootService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<FuncionarioModel> reboot() {

        funcionarioRepository.deleteAll();
        List<FuncionarioModel> funcionarios = List.of(
            new FuncionarioModel(null, "98574307084", "Geniéve", "ger1@bantads.com.br", TipoFuncionario.GERENTE),
            new FuncionarioModel(null, "64065268052", "Godophredo", "ger2@bantads.com.br", TipoFuncionario.GERENTE),
            new FuncionarioModel(null, "23862179060", "Gyândula", "ger3@bantads.com.br", TipoFuncionario.GERENTE),
            new FuncionarioModel(null, "40501740066", "Adamântio",  "adm1@bantads.com.br", TipoFuncionario.ADMINISTRADOR)
        );

        funcionarioRepository.saveAll(funcionarios);

        return funcionarios;

    }
}