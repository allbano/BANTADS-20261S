package br.dac.bantads.ms_funcionario.controller;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.FuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.FuncionarioResponseDTO;
import br.dac.bantads.ms_funcionario.dto.GerenteDTO;
import br.dac.bantads.ms_funcionario.dto.UpdateFuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.service.FuncionarioService;
import br.dac.bantads.ms_funcionario.service.RebootService;
import br.dac.bantads.ms_funcionario.utils.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MS Gerente (subdomínio de gerentes/funcionários). Expõe as consultas de
 * gerentes (R19 listar, detalhe por cpf/uuid) usadas pela API Composition do
 * gateway e o reboot. As mutações do CRUD — inserir (R17), alterar (R20) e
 * remover (R18) — são executadas pelos passos de SAGA (mensageria).
 */
@CrossOrigin
@RestController
public class FuncionarioController {

    @Autowired
    private FuncionarioService service;

    @Autowired
    private RebootService rebootService;

    // ─────────────────────────────────────────────────────────────────────────
    // ROTAS DE FUNCIONÁRIO (Padrão Novo)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/funcionarios")
    public ResponseEntity<List<FuncionarioResponseDTO>> listFuncionarios() {
        List<FuncionarioModel> list = service.listAll();
        List<FuncionarioResponseDTO> response = list.stream()
                .map(FuncionarioResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/funcionarios/{uuid}")
    public ResponseEntity<FuncionarioResponseDTO> getFuncionario(@PathVariable UUID uuid) {
        return service.getByUuid(uuid)
                .map(f -> ResponseEntity.ok(new FuncionarioResponseDTO(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/funcionarios/por-cpf/{cpf}")
    public ResponseEntity<FuncionarioResponseDTO> getFuncionarioPorCpf(@PathVariable String cpf) {
        return service.getByCpf(cpf)
                .map(f -> ResponseEntity.ok(new FuncionarioResponseDTO(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/funcionarios/por-email/{email}")
    public ResponseEntity<FuncionarioResponseDTO> getFuncionarioPorEmail(@PathVariable String email) {
        return service.getByEmail(email)
                .map(f -> ResponseEntity.ok(new FuncionarioResponseDTO(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/funcionarios")
    public ResponseEntity<FuncionarioResponseDTO> createFuncionario(@RequestBody FuncionarioRequestDTO dto) {
        try {
            FuncionarioModel f = service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FuncionarioResponseDTO(f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/funcionarios/{uuid}")
    public ResponseEntity<FuncionarioResponseDTO> updateFuncionario(@PathVariable UUID uuid, @RequestBody UpdateFuncionarioRequestDTO dto) {
        try {
            FuncionarioModel f = service.update(uuid, dto);
            return ResponseEntity.ok(new FuncionarioResponseDTO(f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/funcionarios/{uuid}")
    public ResponseEntity<Void> deleteFuncionario(@PathVariable UUID uuid) {
        try {
            service.delete(uuid);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/reboot")
    public ResponseEntity<List<FuncionarioResponseDTO>> reboot() {
        List<FuncionarioModel> list = rebootService.reboot();
        List<FuncionarioResponseDTO> response = list.stream()
                .map(FuncionarioResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ROTAS DE GERENTE (Compatibilidade com Legado e API Gateway /gerentes/)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping({"/gerentes", "/gerentes/", "/gerente/list"})
    public ResponseEntity<List<GerenteDTO>> listGerentes() {
        List<FuncionarioModel> gerentes = service.listByTipo(TipoFuncionario.GERENTE);
        List<GerenteDTO> response = gerentes.stream()
                .map(this::mapToGerenteDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/gerentes/{uuid}", "/gerente/{uuid}"})
    public ResponseEntity<GerenteDTO> getGerente(@PathVariable String uuid) {
        try {
            UUID parsedUuid = UUID.fromString(uuid);
            return service.getByUuid(parsedUuid)
                    .map(g -> ResponseEntity.ok(mapToGerenteDTO(g)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping({"/gerentes/por-cpf/{cpf}", "/gerente/por-cpf/{cpf}"})
    public ResponseEntity<GerenteDTO> getGerentePorCpf(@PathVariable String cpf) {
        return service.getByCpf(cpf)
                .filter(f -> f.getTipo() == TipoFuncionario.GERENTE)
                .map(g -> ResponseEntity.ok(mapToGerenteDTO(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping({"/gerentes/por-email/{email}", "/gerente/por-email/{email}"})
    public ResponseEntity<GerenteDTO> getGerentePorEmail(@PathVariable String email) {
        return service.getByEmail(email)
                .filter(f -> f.getTipo() == TipoFuncionario.GERENTE)
                .map(g -> ResponseEntity.ok(mapToGerenteDTO(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping({"/gerentes/novo", "/gerente/novo", "/gerentes"})
    public ResponseEntity<GerenteDTO> cadastroGerente(@RequestBody GerenteDTO dto) {
        try {
            if (service.getByCpf(dto.getCpf()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            FuncionarioModel g = FuncionarioModel.builder()
                    .cpf(dto.getCpf())
                    .nome(dto.getNome())
                    .email(dto.getEmail())
                    .telefone(dto.getTelefone())
                    .senha(Security.hash(dto.getSenha()))
                    .tipo(TipoFuncionario.GERENTE)
                    .build();

            FuncionarioModel saved = service.saveFuncionario(g);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToGerenteDTO(saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping({"/gerentes/{uuid}", "/gerente/{uuid}"})
    public ResponseEntity<GerenteDTO> updateGerente(@PathVariable String uuid, @RequestBody GerenteDTO dto) {
        try {
            UUID parsedUuid = UUID.fromString(uuid);
            Optional<FuncionarioModel> opt = service.getByUuid(parsedUuid);
            if (opt.isPresent()) {
                FuncionarioModel g = opt.get();
                g.setNome(dto.getNome());
                g.setEmail(dto.getEmail());
                g.setTelefone(dto.getTelefone());
                if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                    g.setSenha(Security.hash(dto.getSenha()));
                }
                FuncionarioModel saved = service.saveFuncionario(g);
                return ResponseEntity.ok(mapToGerenteDTO(saved));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping({"/gerentes/{uuid}", "/gerente/{uuid}"})
    public ResponseEntity<GerenteDTO> deleteGerente(@PathVariable String uuid) {
        try {
            UUID parsedUuid = UUID.fromString(uuid);
            Optional<FuncionarioModel> opt = service.getByUuid(parsedUuid);
            if (opt.isPresent()) {
                FuncionarioModel g = opt.get();
                service.delete(parsedUuid);
                return ResponseEntity.ok(mapToGerenteDTO(g));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper para mapear FuncionarioModel para GerenteDTO
    private GerenteDTO mapToGerenteDTO(FuncionarioModel m) {
        if (m == null) return null;
        return GerenteDTO.builder()
                .id(m.getUuid() != null ? m.getUuid().toString() : null)
                .nome(m.getNome())
                .email(m.getEmail())
                .senha(m.getSenha())
                .cpf(m.getCpf())
                .telefone(m.getTelefone())
                .cargo(m.getTipo() != null ? m.getTipo().name() : null)
                .build();
    }
}
