# LLM Gateway

A unified API gateway for multiple LLM providers (OpenAI, Anthropic, Ollama) with caching, rate limiting, and cost tracking.

## Features

- **Multi-provider support**: OpenAI, Anthropic, Ollama (local)
- **Request caching**: Save costs on repeated prompts via Redis
- **Rate limiting**: Per-user/API key rate limits
- **Cost tracking**: Monitor usage and costs across providers
- **Virtual threads**: Java 21 Project Loom for high concurrency

## Tech Stack

- Java 21
- Spring Boot 3.2+
- PostgreSQL 16
- Redis 7
- Docker Compose

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Run locally

1. Start dependencies:
```bash
docker-compose up -d
```

2. Set environment variables:
```bash
export OPENAI_API_KEY=your-key
export ANTHROPIC_API_KEY=your-key
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

4. Test health endpoint:
```bash
curl http://localhost:8080/api/v1/health
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Health check |
| POST | `/api/v1/keys` | Create API key |
| GET | `/api/v1/keys` | List API keys |
| DELETE | `/api/v1/keys/{id}` | Deactivate API key |
| POST | `/api/v1/chat` | Chat completion |
| GET | `/api/v1/usage` | Usage statistics |

## Usage Example

### Create an API key
```bash
curl -X POST http://localhost:8080/api/v1/keys \
  -H "Content-Type: application/json" \
  -d '{"name": "my-key", "owner": "user@example.com"}'
```

### Send a chat request
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -H "X-API-Key: llmgw_your_key_here" \
  -d '{
    "provider": "openai",
    "model": "gpt-4o-mini",
    "messages": [
      {"role": "user", "content": "Hello!"}
    ]
  }'
```

### Check usage
```bash
curl http://localhost:8080/api/v1/usage \
  -H "X-API-Key: llmgw_your_key_here"
```

## License

MIT
