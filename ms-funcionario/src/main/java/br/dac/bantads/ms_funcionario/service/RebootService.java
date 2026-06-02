package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import br.dac.bantads.ms_funcionario.utils.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RebootService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<FuncionarioModel> reboot() {

        funcionarioRepository.deleteAll();
        String defaultHashedPassword = Security.hash("tads");
        List<FuncionarioModel> funcionarios = List.of(
            new FuncionarioModel(null, "98574307084", "Geniéve", "ger1@bantads.com.br", TipoFuncionario.GERENTE, "(41) 99999-1111", defaultHashedPassword),
            new FuncionarioModel(null, "64065268052", "Godophredo", "ger2@bantads.com.br", TipoFuncionario.GERENTE, "(41) 99999-2222", defaultHashedPassword),
            new FuncionarioModel(null, "23862179060", "Gyândula", "ger3@bantads.com.br", TipoFuncionario.GERENTE, "(41) 99999-3333", defaultHashedPassword),
            new FuncionarioModel(null, "40501740066", "Adamântio",  "adm1@bantads.com.br", TipoFuncionario.ADMINISTRADOR, "(41) 99999-0000", defaultHashedPassword)
        );

        funcionarioRepository.saveAll(funcionarios);

        return funcionarios;

    }
}