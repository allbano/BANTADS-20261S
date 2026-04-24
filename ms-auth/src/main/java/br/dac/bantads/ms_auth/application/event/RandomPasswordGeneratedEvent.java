package br.dac.bantads.ms_auth.application.event;

public record RandomPasswordGeneratedEvent(String email, String password) {
}
