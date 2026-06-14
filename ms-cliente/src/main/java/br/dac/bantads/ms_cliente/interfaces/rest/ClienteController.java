package br.dac.bantads.ms_cliente.interfaces.rest;

import br.dac.bantads.ms_cliente.application.dto.ClienteRequestDTO;
import br.dac.bantads.ms_cliente.application.dto.ClienteResponseDTO;
import br.dac.bantads.ms_cliente.application.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // --- Endpoints Existentes mantidos ---

    @GetMapping
    public ResponseEntity<?> getClientes(@RequestParam(value = "filtro", required = false) String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return ResponseEntity.ok(clienteService.getClientesComConta());
        }

        switch (filtro) {
            case "para_aprovar":
                return ResponseEntity.ok(clienteService.getClientesParaAprovar());
            case "melhores_clientes":
                return ResponseEntity.ok(clienteService.getMelhoresClientes());
            case "adm_relatorio_clientes":
                return ResponseEntity.ok(clienteService.getClientesComConta());
            default:
                return ResponseEntity.badRequest().body("Filtro inválido. Valores válidos: para_aprovar, melhores_clientes, adm_relatorio_clientes");
        }
    }

    // --- Novos Endpoints Modernizados ---

    @GetMapping("/list")
    public ResponseEntity<List<ClienteResponseDTO>> listAll() {
        try {
            return ResponseEntity.ok(clienteService.listAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ClienteResponseDTO> getCliente(@PathVariable UUID uuid) {
        try {
            return clienteService.getById(uuid)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/por-cpf/{cpf}")
    public ResponseEntity<ClienteResponseDTO> getClientePorCpf(@PathVariable String cpf) {
        try {
            return clienteService.getByCpf(cpf)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/por-email/{email}")
    public ResponseEntity<ClienteResponseDTO> getClientePorEmail(@PathVariable String email) {
        try {
            return clienteService.getByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<ClienteResponseDTO> cadastro(@RequestBody ClienteRequestDTO request) {
        try {
            ClienteResponseDTO created = clienteService.cadastro(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** R11 — Rejeitar cliente (direto, não-SAGA): marca inativo e publica notificação. */
    @PostMapping("/{cpf}/rejeitar")
    public ResponseEntity<Map<String, Object>> rejeitar(@PathVariable String cpf,
                                                        @RequestBody(required = false) Map<String, Object> body) {
        String motivo = body != null && body.get("motivo") != null ? String.valueOf(body.get("motivo")) : "";
        try {
            clienteService.rejeitar(cpf, motivo);
            return ResponseEntity.ok(Map.of("status", "rejeitado", "cpf", cpf));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable UUID uuid, @RequestBody ClienteRequestDTO request) {
        try {
            ClienteResponseDTO updated = clienteService.update(uuid, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
