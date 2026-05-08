# BFF — Backend for Frontend

A Kotlin/Spring Boot service acting as a Backend-for-Frontend (BFF) layer between frontend clients and downstream product/pricing microservices.

- [Assumptions](ASSUMPTIONS.md)

---

## Setup and Run

### Local (Gradle)

Start the WireMock stubs first:

```bash
docker compose up catalog-service pricing-service
```

Then run the application:

```bash
./gradlew bootRun
```

The service starts on `http://localhost:8080`.

### Docker (full stack)

```bash
docker compose up --build
```

This starts the BFF, catalog WireMock, and pricing WireMock together.

---

## Not implemented features
1. For pricing client failures no degraded service info is passed to the frontend explicitly (price is set to `null`)

--- 

## Downstream services
The downstream services are mocked using WireMock, with predefined responses for the catalog and pricing endpoints. The catalog service returns a static list of products, while the pricing service returns prices based on product IDs. Both services can be started via Docker Compose.

---
## Endpoint Summary

The endpoints that return product data require the `X-Client-Type` header (`web` or `mobile`). Requests without this header return 404.

### `/results` query parameters

Spring Data's `Pageable` convention is used — parameters are passed as `page`, `size`, and `sort`.

| Parameter | Type | Default | Notes |
|-----------|------|---------|-------|
| `search` | string | — | Case-insensitive substring match on title |
| `type` | string (repeatable) | — | OR within group, AND across groups. Supported values: `jean`, `jacket`, `skirt`, `blouse`, `scarf` |
| `color` | string (repeatable) | — | Supported values: `green`, `red`, `blue`, `yellow` |
| `size` | string (repeatable) | — | Supported values: `small`, `medium`, `large` |
| `sort` | string | — | `price` or `price,desc`; any other value → no sorting |
| `page` | int | `0` | 0-based |
| `size` | int | `20` | Maximum 100 |

Filter and sort values are case-insensitive. Unsupported values for `type`, `color`, and `size` return 400.

**Sort examples:**
- `?sort=price` — ascending (Spring default when direction is omitted)
- `?sort=price,desc` — descending
- `?sort=price,asc` — ascending (explicit)

**Full URL example:**
```bash
curl "http://localhost:8080/results?color=red&color=green&sort=price,desc&page=0&pageSize=20" \ 
    -H "X-Client-Type: web"

```

### Client shaping

**Web** (`X-Client-Type: web`):
```json
{
  "id": "p-101",
  "title": "Green Linen Blouse",
  "description": "Lightweight blouse with relaxed fit.",
  "type": "blouse",
  "color": "green",
  "size": "medium",
  "imageUrl": "/images/green-blouse.jpg",
  "price": 59.90
}
```

**Mobile** (`X-Client-Type: mobile`):
```json
{
  "id": "p-101",
  "title": "Green Linen Blouse",
  "thumbnailUrl": "/images/green-blouse.jpg",
  "price": 59.90
}
```

`price` is `null` when the pricing service is unavailable (degraded response).

---

## Pagination Model

Spring Data's `Pageable` is used throughout. Pages are **0-based** (default page 0, default size 20, maximum size 100). The response follows Spring's `Page<T>` structure:

```json
{
  "content": [...],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 106,
    "totalPages": 6
  }
}
```

I used the framework's default pagination model for simplicity and consistency. 


## Concurrency Model

For `/products/{id}`, catalog and pricing are called **in parallel** using Kotlin coroutines:


For `/results`, calls are **sequential by design**: pricing is only called after catalog returns and filtering is applied, so only the matched product IDs are sent to the pricing service — avoiding fetching prices for the entire catalog on every request.

---

## Partial Failure Behaviour

| Downstream | Failure behaviour |
|------------|-----------------|
| **Catalog** | Request fails → 503. No catalog data = no response possible. |
| **Pricing** | Degraded — products are returned with `price: null`. The client detects missing prices by checking for a `null` price field. |

The circuit breaker (Resilience4j) wraps both Feign clients. If pricing failures exceed the threshold, subsequent calls are short-circuited immediately rather than waiting for a timeout. The degradation behaviour (`price: null`) is the same whether the circuit is open or the HTTP call fails.

---

## Downstream Clients

Both downstream services use **Spring Cloud OpenFeign** declarative clients. Timeouts are configured per client in `application.yaml`

---

## Catalog Caching

`GET /products` responses from the catalog service are cached in-process using **Caffeine** (TTL 5 minutes, max 1 entry). This means the catalog is fetched once on the first request and served from memory for subsequent requests within the TTL window.

---

## Unsupported Sort Values

If `sort` is set to any value other than `price`, no sorting is applied and results are returned in catalog order. This is documented behaviour: only price-based sorting is currently implemented.

---

## Unsupported Filter Values

Unknown filter values (e.g. `color=purple`) return 400. Only the values returned by `GET /filters` are accepted.


## Localisation

Labels are loaded from classpath `messages_*.properties` files via Spring's `MessageSource`. 

---

## Circuit Breaker

Resilience4j wraps both Feign clients via `spring.cloud.openfeign.circuitbreaker.enabled=true`. The default circuit breaker configuration (sliding window 10, failure threshold 50%, wait 20 s, 3 half-open probes)
