package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import br.dac.bantads.ms_funcionario.utils.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Recarrega a base pré-cadastrada de gerentes/administrador (enunciado BANTADS).
 *
 * Os UUIDs abaixo são UUIDv7 fixos (determinísticos por serem constantes). Eles
 * DEVEM permanecer idênticos aos referenciados em {@code conta_gerente_uuid} no
 * seed do ms-conta — é o contrato de consistência cruzada do seed por UUID.
 * Persistência 100% via JPA/Hibernate (sem SQL nativo).
 */
@Service
public class RebootService {

    // ── UUIDv7 fixos dos gerentes/admin (manter idêntico no ms-conta) ──
    public static final UUID UUID_GER1 = UUID.fromString("01900000-0000-7000-8000-000000000a01"); // Geniéve
    public static final UUID UUID_GER2 = UUID.fromString("01900000-0000-7000-8000-000000000a02"); // Godophredo
    public static final UUID UUID_GER3 = UUID.fromString("01900000-0000-7000-8000-000000000a03"); // Gyândula
    public static final UUID UUID_ADM1 = UUID.fromString("01900000-0000-7000-8000-000000000a00"); // Adamântio

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public List<FuncionarioModel> reboot() {

        funcionarioRepository.deleteAllInBatch();
        String defaultHashedPassword = Security.hash("tads");

        List<FuncionarioModel> funcionarios = List.of(
            FuncionarioModel.builder()
                .uuid(UUID_GER1).cpf("98574307084").nome("Geniéve")
                .email("ger1@bantads.com.br").tipo(TipoFuncionario.GERENTE)
                .telefone("(41) 99999-1111").senha(defaultHashedPassword).build(),
            FuncionarioModel.builder()
                .uuid(UUID_GER2).cpf("64065268052").nome("Godophredo")
                .email("ger2@bantads.com.br").tipo(TipoFuncionario.GERENTE)
                .telefone("(41) 99999-2222").senha(defaultHashedPassword).build(),
            FuncionarioModel.builder()
                .uuid(UUID_GER3).cpf("23862179060").nome("Gyândula")
                .email("ger3@bantads.com.br").tipo(TipoFuncionario.GERENTE)
                .telefone("(41) 99999-3333").senha(defaultHashedPassword).build(),
            FuncionarioModel.builder()
                .uuid(UUID_ADM1).cpf("40501740066").nome("Adamântio")
                .email("adm1@bantads.com.br").tipo(TipoFuncionario.ADMINISTRADOR)
                .telefone("(41) 99999-0000").senha(defaultHashedPassword).build()
        );

        funcionarioRepository.saveAll(funcionarios);

        return funcionarios;
    }
}
