package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.model.cud.ContaModel;
import br.dac.bantads.ms_conta.service.RebootService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class RebootController {

    private final RebootService rebootService;

    @PostMapping("/reboot")
    public ResponseEntity<Map<String, Object>> reboot() {
        List<ContaModel> contas = rebootService.reboot();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "servico", "ms-conta",
                "contas", contas.size(),
                "movimentacoes", 15
        ));
    }
}
