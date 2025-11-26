# Sitka Satellite Image Generator

Serviço Spring Boot para gerar imagens de satélite usando Google Maps API e enviar via WATI.

## 🎯 Funcionalidades

- ✅ Gera imagens de satélite de endereços usando Google Maps API
- ✅ Envia imagens via WATI WhatsApp API
- ✅ Suporta múltiplas requisições simultâneas
- ✅ Limpeza automática de arquivos após envio
- ✅ Logging detalhado de todas as operações
- ✅ Health check endpoint

## 🚀 Arquitetura

```
POST /analise-imagemdesatelite
    ↓
1. Gerar imagem de satélite (Google Maps API)
    ↓
2. Salvar imagem em arquivo
    ↓
3. Enviar via WATI (/api/v1/sendSessionFile/{watiPhoneId})
    ↓
4. Deletar arquivo local
    ↓
Resposta JSON com status
```

## 📋 Requisitos

- Java 17+
- Maven 3.9+
- Docker (para deploy)

## 🔧 Configuração

### Variáveis de Ambiente

```bash
# Google Maps API
GOOGLE_API_KEY=sua_chave_google_aqui

# WATI
WATI_BASE_URL=https://live.wati.io/1047617
WATI_API_TOKEN=seu_token_wati_aqui
WATI_PHONE_ID=5511989838304  # Seu número do WATI

# Aplicação
PORT=9000
SPRING_PROFILES_ACTIVE=prod
```

## 💻 Desenvolvimento Local

```bash
# Clone o repositório
git clone https://github.com/Sitkadi/sitka-satellite-image-generator-java.git
cd sitka-satellite-image-generator-java

# Configure o .env
cp .env.example .env
# Edite .env com suas credenciais

# Execute com Maven
mvn spring-boot:run

# Ou compile e execute
mvn clean package
java -jar target/sitka-satellite-image-generator-1.0.0.jar
```

## 🐳 Docker

```bash
# Build
docker build -t sitka-satellite-image-generator .

# Run
docker run -p 9000:9000 \
  -e GOOGLE_API_KEY=sua_chave \
  -e WATI_API_TOKEN=seu_token \
  -e WATI_PHONE_ID=seu_numero \
  sitka-satellite-image-generator
```

## 📡 API Endpoints

### POST /analise-imagemdesatelite

Gera e envia imagem de satélite.

**Request:**
```json
{
  "telefone": "5511976169677",
  "endereco": "Av. Dr. Guilherme Dumont Vilares, 2000, São Paulo, SP"
}
```

**Response (Sucesso):**
```json
{
  "ok": true,
  "result": "success",
  "mensagem_imagemdesatelite": "Imagem de satélite enviada com sucesso!",
  "imagemdesatelite_url": "/root/sitka-temp/imagens/..."
}
```

**Response (Erro):**
```json
{
  "ok": false,
  "result": "error",
  "mensagem_imagemdesatelite": "Descrição do erro"
}
```

### GET /analise-imagemdesatelite/health

Health check do serviço.

**Response:**
```json
{
  "status": "UP",
  "service": "Sitka Satellite Image Generator"
}
```

## 🔑 Fluxo de Envio WATI

1. **Gerar imagem** via Google Maps Static API
2. **Salvar** em `/root/sitka-temp/imagens/{endereco}/`
3. **Enviar** para WATI usando:
   - URL: `https://live.wati.io/1047617/api/v1/sendSessionFile/{watiPhoneId}`
   - Header: `Authorization: Bearer {watiApiToken}`
   - Body (multipart):
     - `media`: arquivo PNG
     - `recipient`: número do destinatário
     - `caption`: descrição da imagem
4. **Deletar** arquivo local após sucesso

## 📊 Logs

Todos os eventos são registrados com timestamp:

```
[2025-11-26 22:25:35] [INFO] ========================================================
[2025-11-26 22:25:35] [INFO] NOVA REQUISIÇÃO
[2025-11-26 22:25:35] [INFO] Telefone: 5511976169677
[2025-11-26 22:25:35] [INFO] Endereço: Av. Dr. Guilherme Dumont Vilares, 2000, São Paulo, SP
[2025-11-26 22:25:35] [INFO] ========================================================
[2025-11-26 22:25:35] [INFO] Gerando imagem para: Av. Dr. Guilherme Dumont Vilares, 2000, São Paulo, SP
[2025-11-26 22:25:42] [INFO] ✓ Imagem gerada com sucesso! Dimensões: 600x600
[2025-11-26 22:25:43] [INFO] ✓ Imagem salva em: /root/sitka-temp/imagens/...
[2025-11-26 22:25:43] [INFO] ========================================================
[2025-11-26 22:25:43] [INFO] ENVIANDO IMAGEM PARA WATI
[2025-11-26 22:25:43] [INFO] ========================================================
[2025-11-26 22:25:43] [INFO] Telefone (destinatário): 5511976169677
[2025-11-26 22:25:43] [INFO] WATI Phone ID (remetente): 5511989838304
[2025-11-26 22:25:43] [INFO] ✓ Imagem enviada com sucesso!
[2025-11-26 22:25:43] [INFO] ========================================================
[2025-11-26 22:25:43] [INFO] ✓ PROCESSO CONCLUÍDO COM SUCESSO
[2025-11-26 22:25:43] [INFO] ========================================================
```

## 🚀 Deploy no Render

1. Faça push para GitHub
2. Conecte o repositório no Render
3. Configure as variáveis de ambiente
4. Deploy automático será acionado

## 📝 Licença

MIT

## 👨‍💻 Autor

Sitka Desenvolvimento
