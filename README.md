# OrbitCommerce

Plataforma de e-commerce distribuída, orientada a eventos, construída como projeto de portfólio técnico.

> Este repositório contém a especificação de arquitetura completa e a infraestrutura local, além da implementação incremental dos serviços. Não há fluxo de fork — este é o projeto final, desenvolvido diretamente aqui.

## O que tem aqui

```
orbitcommerce/
├── docs/architecture/          ← Documento de Arquitetura de Software (SAD) — fonte da verdade
├── docker-compose.yml          ← Infraestrutura local (Postgres, Redis, Kafka, RabbitMQ, observabilidade)
├── infra/                      ← Configuração de Postgres (init de databases), Prometheus, OTel Collector
├── services/                   ← Um serviço por pasta — implementação incremental (ver roadmap abaixo)
└── LICENSE
```

## Stack

Java 25 (LTS) · Spring Boot 4 · Kotlin · Quarkus · Coroutines · Apache Kafka · RabbitMQ · PostgreSQL ·
Redis · Docker · AWS (EKS) · GitHub Actions · JUnit 5 · Mockito · Testcontainers · Prometheus ·
Grafana · OpenTelemetry · Jaeger

## Status de implementação

| Serviço | Stack | Porta local | Status |
|---|---|---|---|
| API Gateway | Java · Spring Cloud Gateway | 8080 | Não iniciado |
| Identity Service | Java · Spring Boot | 8081 | Em andamento |
| Catalog Service | Java · Spring Boot | 8082 | Em andamento |
| Order Service (Saga Orchestrator) | Java · Spring Boot | 8083 | Não iniciado |
| Inventory Service | Kotlin · Quarkus | 8084 | Não iniciado |
| Payment Service | Java · Spring Boot | 8085 | Não iniciado |
| Shipping Service | Kotlin · Quarkus | 8086 | Não iniciado |
| Notification Service | Kotlin · Quarkus | 8087 | Não iniciado |

## Rodando localmente

1. Leia o documento de arquitetura completo em `docs/architecture/`.
2. Suba a infraestrutura local:
   ```bash
   docker-compose up -d postgres redis kafka rabbitmq
   ```
3. Suba os serviços já implementados (ver tabela de status acima) com `docker-compose up -d --build <nome-do-serviço>`.

## Licença

MIT — veja [LICENSE](./LICENSE).