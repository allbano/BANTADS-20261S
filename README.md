# BANTADS - Internet Banking do TADS

Sistema de Internet Banking desenvolvido para a disciplina DS152 - Desenvolvimento de Aplicações Corporativas (UFPR - TADS).

O BANTADS é um sistema bancário com três perfis de acesso (Cliente, Gerente e Administrador), construído sobre uma arquitetura de microsserviços com comunicação assíncrona via mensageria.

---

## Sumário

- [Arquitetura](#arquitetura)
- [Equipe](#equipe)

---

## Arquitetura

O sistema segue uma arquitetura de microsserviços com os seguintes componentes:

```
                                    ┌─────────────────┐
                                    │    RabbitMQ      │
                                    │  (mensageria)    │
                                    └────────┬────────┘
                                             │
┌──────────┐    HTTP     ┌──────────────┐    │    ┌──────────────┐
│          │────────────>│              │────────>│  MS Cliente   │──> PostgreSQL (DB_Clientes)
│ Frontend │             │  API Gateway │────────>│  MS Conta     │──> PostgreSQL (DB_Conta - schema leitura | escrita)
│ (SPA)    │<────────────│  (Node.js)   │────────>│  MS Gerente   │──> PostgreSQL (DB_gerente)
│          │             │              │────────>│  MS Auth      │──> MongoDB (db_auth)
└──────────┘             └──────────────┘    │    └──────────────┘
                                             │
                                    ┌────────┴────────-┐
                                    │ SAGA Orquestrador│
                                    │  (coordenação)   │
                                    └─────────────────-┘
```

O frontend se comunica **exclusivamente** com o API Gateway via HTTP-REST. Os microsserviços se comunicam entre si via RabbitMQ. Transações distribuídas são coordenadas pelo orquestrador de SAGAs.

---

## Equipe

Trabalho desenvolvido para a disciplina DS152 - DAC, UFPR - TADS, 2026/1.

| Responsabilidades                                  | Membros                      |
|----------------------------------------------------|------------------------------|
| Frontend (Angular + TypeScript) + API Gateway      |Albano, Dyego e Matheus       | 
| MS Auth + MS Gerente + DevOps                      |Albano, Dyego e Matheus       |
| MS Cliente + MS Conta (CQRS) + SAGA Orquestrador   |Albano, Dyego e Matheus       | 

