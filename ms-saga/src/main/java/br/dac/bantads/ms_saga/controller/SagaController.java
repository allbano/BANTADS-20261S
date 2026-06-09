package br.dac.bantads.ms_saga.controller;

import br.dac.bantads.ms_saga.dto.AutocadastroRequestDTO;
import br.dac.bantads.ms_saga.dto.SagaIniciadaResponseDTO;
import br.dac.bantads.ms_saga.orchestrator.AutocadastroSagaOrchestrator;
import br.dac.bantads.ms_saga.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/saga")
@CrossOrigin
@RequiredArgsConstructor
public class SagaController {

    private final AutocadastroSagaOrchestrator orchestrator;

    /**
     * Inicia a saga de autocadastro de um novo cliente.
     * O orquestrador coordena ms-cliente → ms-conta → ms-auth com compensação automática.
     */
    @PostMapping("/autocadastro")
    ResponseEntity<SagaIniciadaResponseDTO> iniciarAutocadastro(
            @RequestBody AutocadastroRequestDTO request) {
        UUID sagaId = orchestrator.iniciarSaga(request);
        return ResponseEntity.accepted()
                .body(new SagaIniciadaResponseDTO(sagaId, "INICIADA"));
    }

    /**
     * Consulta o status atual de uma saga em andamento.
     */
    @GetMapping("/autocadastro/{sagaId}")
    ResponseEntity<Map<String, String>> consultarStatus(@PathVariable UUID sagaId) {
        SagaStatus status = orchestrator.consultarStatus(sagaId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("sagaId", sagaId.toString(), "status", status.name()));
    }
}
