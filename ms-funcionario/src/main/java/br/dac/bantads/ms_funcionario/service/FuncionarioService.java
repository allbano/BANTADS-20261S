package br.dac.bantads.ms_funcionario.service;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.FuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.UpdateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.repository.FuncionarioRepository;
import br.dac.bantads.ms_funcionario.utils.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository repository;

    public List<FuncionarioModel> listAll() {
        return repository.findAll();
    }

    public List<FuncionarioModel> listByTipo(TipoFuncionario tipo) {
        return repository.findByTipo(tipo);
    }

    public Optional<FuncionarioModel> getByUuid(UUID uuid) {
        return repository.findById(uuid);
    }

    public Optional<FuncionarioModel> getByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    public Optional<FuncionarioModel> getByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Transactional
    public FuncionarioModel create(FuncionarioRequestDTO dto) {
        if (repository.existsByCpf(dto.cpf())) {
            throw new IllegalArgumentException("Já existe um funcionário com este CPF");
        }
        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Já existe um funcionário com este Email");
        }

        FuncionarioModel funcionario = FuncionarioModel.builder()
                .cpf(dto.cpf())
                .nome(dto.nome())
                .email(dto.email())
                .tipo(dto.tipo())
                .build();

        return repository.save(funcionario);
    }

    @Transactional
    public FuncionarioModel update(UUID uuid, UpdateFuncionarioRequestDTO dto) {
        FuncionarioModel funcionario = repository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTipo(dto.getTipo());

        return repository.save(funcionario);
    }

    @Transactional
    public void delete(UUID uuid) {
        if (!repository.existsById(uuid)) {
            throw new IllegalArgumentException("Funcionário não encontrado");
        }
        repository.deleteById(uuid);
    }

    @Transactional
    public FuncionarioModel saveOrUpdate(FuncionarioRequestDTO dto) {
        Optional<FuncionarioModel> existingOpt = repository.findByCpf(dto.cpf());
        FuncionarioModel funcionario;
        if (existingOpt.isPresent()) {
            funcionario = existingOpt.get();
            funcionario.setNome(dto.nome());
            funcionario.setEmail(dto.email());
            funcionario.setTipo(dto.tipo());
        } else {
            funcionario = FuncionarioModel.builder()
                    .cpf(dto.cpf())
                    .nome(dto.nome())
                    .email(dto.email())
                    .tipo(dto.tipo())
                    .build();
        }
        return repository.save(funcionario);
    }

    @Transactional
    public FuncionarioModel saveFuncionario(FuncionarioModel funcionario) {
        return repository.save(funcionario);
    }
}
