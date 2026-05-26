package br.dac.bantads.ms_cliente.interfaces.rest;

import br.dac.bantads.ms_cliente.application.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

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
}
