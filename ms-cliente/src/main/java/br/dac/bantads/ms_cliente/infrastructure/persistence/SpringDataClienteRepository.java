package br.dac.bantads.ms_cliente.infrastructure.persistence;

import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataClienteRepository extends JpaRepository<ClienteModel, UUID> {
    Optional<ClienteModel> findByCpf(String cpf);
}
