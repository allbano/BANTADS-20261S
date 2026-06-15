package br.dac.bantads.ms_cliente.domain.repository;

import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    ClienteModel save(ClienteModel cliente);
    Optional<ClienteModel> findById(UUID uuid);
    Optional<ClienteModel> findByCpf(String cpf);
    Optional<ClienteModel> findByEmail(String email);
    List<ClienteModel> findAll();
    void deleteById(UUID uuid);
}
