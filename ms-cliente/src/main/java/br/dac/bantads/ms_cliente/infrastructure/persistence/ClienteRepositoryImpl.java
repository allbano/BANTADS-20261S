package br.dac.bantads.ms_cliente.infrastructure.persistence;

import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import br.dac.bantads.ms_cliente.domain.repository.ClienteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ClienteRepositoryImpl implements ClienteRepository {

    private final SpringDataClienteRepository springDataClienteRepository;

    public ClienteRepositoryImpl(SpringDataClienteRepository springDataClienteRepository) {
        this.springDataClienteRepository = springDataClienteRepository;
    }

    @Override
    public ClienteModel save(ClienteModel cliente) {
        return springDataClienteRepository.save(cliente);
    }

    @Override
    public Optional<ClienteModel> findById(UUID uuid) {
        return springDataClienteRepository.findById(uuid);
    }

    @Override
    public Optional<ClienteModel> findByCpf(String cpf) {
        return springDataClienteRepository.findByCpf(cpf);
    }

    @Override
    public Optional<ClienteModel> findByEmail(String email) {
        return springDataClienteRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public List<ClienteModel> findAll() {
        return springDataClienteRepository.findAll();
    }

    @Override
    public void deleteById(UUID uuid) {
        springDataClienteRepository.deleteById(uuid);
    }
}
