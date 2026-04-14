package br.dac.bantads.ms_auth.infrastructure.persistence.scylladb;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Optional;

public interface SpringDataUserAccountRepository extends CassandraRepository<UserAccountRow, String> {
    Optional<UserAccountRow> findByEmail(String email);
}
