# Chaves JWT locais para desenvolvimento

Cada desenvolvedor deve gerar seu próprio par de chaves RSA para executar o
`identity-service` localmente. Os arquivos são criados uma vez por clone/
ambiente, permanecem no computador do desenvolvedor e não devem ser enviados
para o GitHub. Não é necessário gerar novas chaves a cada inicialização do
Docker Compose.

## Pré-requisitos

- OpenSSL instalado no computador.
- O repositório externo que contém o Docker Compose local dos serviços.

Os exemplos abaixo assumem que os comandos serão executados na raiz desse
repositório externo, ao lado do arquivo Compose.

## Gerar o par de chaves

Execute este bloco uma vez:

```bash
mkdir -p secrets/identity-service

openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
  -out secrets/identity-service/jwt-private.pem

openssl pkey \
  -in secrets/identity-service/jwt-private.pem \
  -pubout \
  -out secrets/identity-service/jwt-public.pem

chmod 600 secrets/identity-service/jwt-private.pem
```

O arquivo `jwt-private.pem` é secreto. Não o compartilhe, não o adicione a
commits e não o envie em issues, mensagens ou logs. O arquivo `jwt-public.pem`
é a chave pública correspondente; ambos precisam permanecer juntos para que
este ambiente consiga assinar e validar os tokens.

## Impedir que as chaves sejam versionadas

No `.gitignore` do repositório externo do Compose, inclua:

```gitignore
/secrets/
```

Se a pasta `secrets` já tiver sido adicionada ao Git, ignorá-la não a remove do
índice. Nesse caso, remova os arquivos do índice sem apagá-los do computador:

```bash
git rm --cached secrets/identity-service/jwt-private.pem
git rm --cached secrets/identity-service/jwt-public.pem
```

Confirme que os caminhos não aparecem como arquivos rastreados antes de fazer
um commit.

## Configuração necessária do serviço

O serviço deve carregar os caminhos informados pelo ambiente, em vez de usar
chaves empacotadas no classpath. A configuração Spring deve referenciar:

```yaml
api:
  security:
    token:
      jwt:
        private-key-path: ${JWT_PRIVATE_KEY_PATH}
        public-key-path: ${JWT_PUBLIC_KEY_PATH}
```

No Compose externo, declare os arquivos como secrets e monte-os no
`identity-service`:

```yaml
secrets:
  identity-jwt-private-key:
    file: ./secrets/identity-service/jwt-private.pem
  identity-jwt-public-key:
    file: ./secrets/identity-service/jwt-public.pem

services:
  identity-service:
    secrets:
      - source: identity-jwt-private-key
        target: jwt-private.pem
      - source: identity-jwt-public-key
        target: jwt-public.pem
    environment:
      JWT_PRIVATE_KEY_PATH: file:/run/secrets/jwt-private.pem
      JWT_PUBLIC_KEY_PATH: file:/run/secrets/jwt-public.pem
```

Mantenha as configurações existentes de `build`, `ports`, `networks`,
`depends_on` e demais variáveis do serviço ao incorporar esse exemplo. O
Compose local disponibiliza os secrets em `/run/secrets/` dentro do container.

> **Importante:** neste projeto, `application.yaml` ainda pode estar apontando
> para `classpath:certs/app.key` e `classpath:certs/app.pub`. Enquanto essa
> configuração e o Compose não forem adaptados para os caminhos acima, gerar
> os arquivos não basta para o serviço usá-los.

## Executar e compartilhar o ambiente

Depois de configurar o serviço e o Compose:

```bash
docker compose up -d --build identity-service
```

Cada desenvolvedor gera seu próprio par local. Tokens emitidos por uma
instalação não serão aceitos por outra instalação que use uma chave pública
diferente. Isso é esperado para desenvolvimento independente. Não regenere as
chaves enquanto quiser manter válidos os tokens já emitidos naquele ambiente.

## Se a chave antiga já foi publicada

Não reutilize a chave privada anteriormente armazenada no projeto. Considere-a
comprometida e use o novo par gerado por este tutorial. Remover o arquivo em um
novo commit não apaga sua cópia do histórico Git; para um repositório de
portfólio, avalie também remover a chave do histórico antes de publicar ou
continuar compartilhando o repositório.
