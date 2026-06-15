package br.dac.bantads.ms_cliente.interfaces.rest;

import br.dac.bantads.ms_cliente.application.service.RebootService;
import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint de recarga da base pré-cadastrada (POST /reboot na raiz).
 * Acionado pelo fan-out de /reboot do API Gateway.
 */
@CrossOrigin
@RestController
public class RebootController {

    private final RebootService rebootService;

    public RebootController(RebootService rebootService) {
        this.rebootService = rebootService;
    }

    @PostMapping("/reboot")
    public ResponseEntity<Map<String, Object>> reboot() {
        List<ClienteModel> clientes = rebootService.reboot();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "servico", "ms-cliente",
                "clientes", clientes.size()
        ));
    }
}
