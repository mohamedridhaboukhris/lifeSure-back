# LifeSure — Monitoring Stack

This directory contains a self-contained Docker Compose stack that runs:

| Service | URL | Description |
|---------|-----|-------------|
| **lifesure-front** | http://localhost:8080 | Angular frontend (nginx) |
| **lifesure-back** | http://localhost:8090 | Spring Boot REST API |
| **lifesure-fraud** | http://localhost:5001 | Python fraud detection |
| **Prometheus** | http://localhost:9090 | Metrics collection |
| **Grafana** | http://localhost:3000 | Dashboards (admin / admin) |
| **MySQL** | localhost:3306 | Database (internal) |







---

## Prerequisites

- Docker + Docker Compose installed
- DockerHub access to pull `hamma1925/lifesure-*` images

---

## Run the full stack

```bash
# 1. Go to the monitoring directory
cd lifeSure-back/monitoring

# 2. (First time) Login to DockerHub
docker login

# 3. Pull the latest images
docker compose pull

# 4. Start everything
docker compose up -d

# 5. Check all containers are running
docker compose ps
```

Expected output after ~30 seconds:

```
monitoring-mysql-1            Up (healthy)
monitoring-lifesure-back-1    Up
monitoring-lifesure-fraud-1   Up
monitoring-lifesure-front-1   Up
monitoring-prometheus-1       Up
monitoring-grafana-1          Up
monitoring-nginx-exporter-1   Up
```

> **Note:** `lifesure-back` starts after MySQL is healthy — it may take 20–30 seconds to appear.

---

## Verify metrics are flowing

```bash
# Spring Boot metrics endpoint
curl http://localhost:8090/actuator/prometheus | head -5

# Python fraud service metrics endpoint
curl http://localhost:5001/metrics | head -5

# nginx stub_status
curl http://localhost:8080/stub_status

# Prometheus targets (all should be "UP")
curl -s http://localhost:9090/api/v1/targets | python3 -m json.tool | grep '"health"'
```

---

## Open Grafana

1. Go to **http://localhost:3000**
2. Login: `admin` / `admin`
3. Navigate to **Dashboards → LifeSure → LifeSure Overview**

The dashboard has 4 panels:
- **Backend — HTTP Request Rate** (requests/sec by route)
- **Backend — JVM Heap Memory** (used vs max)
- **Backend — CPU Usage** (gauge)
- **Fraud Service — Request Rate** (requests/sec)

> If panels show "No data", change the time range (top right) to **Last 5 minutes**.
> Panels populate after the first API calls are made.

---

## Generate traffic to see data

```bash
# Hit the backend login endpoint
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@assurance.com","password":"admin123"}'

# Hit the fraud prediction endpoint
curl -X POST http://localhost:5001/predict \
  -H "Content-Type: application/json" \
  -d '{"montant":5000,"nb_sinistres":3,"delai":10,"type":1}'

# Hit the frontend
curl http://localhost:8080
```

After these calls, Prometheus scrapes every 15 seconds — Grafana will show the data shortly after.

---

## Stop the stack

```bash
docker compose down          # stop containers, keep data volumes
docker compose down -v       # stop and delete MySQL + Grafana data
```

---

## Environment variables (backend)

The backend requires these secrets to start. Set real values in `docker-compose.yml` for production:

| Variable | Default (dev) | Description |
|----------|--------------|-------------|
| `JWT_SECRET` | `lifesure_dev_secret_key_min32chars_ok` | JWT signing key (min 32 chars) |
| `GROQ_API_KEY` | `dummy` | Groq LLM API key for chatbot |
| `STRIPE_SECRET_KEY` | `sk_test_dummy` | Stripe secret key for payments |
| `SPRING_MAIL_USERNAME` | `noreply@lifesure.local` | Gmail address for notifications |
| `SPRING_MAIL_PASSWORD` | `dummy` | Gmail app password |
| `APP_FRONTEND_URL` | `http://localhost:8080` | Frontend URL (used in emails) |
| `FILE_UPLOAD_DIR` | `/app/uploads` | Upload directory inside container |

---

## Prometheus targets

Prometheus scrapes on a 15-second interval:

| Job | Target | Metrics path |
|-----|--------|--------------|
| `lifesure-back` | `lifesure-back:8090` | `/actuator/prometheus` |
| `lifesure-fraud` | `lifesure-fraud:5001` | `/metrics` |
| `lifesure-front-nginx` | `nginx-exporter:9113` | `/metrics` (via stub_status) |
| `prometheus` | `localhost:9090` | `/metrics` |

Check target status at: **http://localhost:9090/targets**

---

## CI/CD

Both repos have GitHub Actions pipelines that trigger on push to `main`:

1. **Test** — compile / build check
2. **Trivy scan** — security scan of the Docker image (CRITICAL + HIGH, report-only)
3. **Push** — push `:latest` and `:<git-sha>` tags to DockerHub

To update the running stack after a new push:

```bash
docker compose pull && docker compose up -d
```
