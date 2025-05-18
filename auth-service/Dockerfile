FROM golang:1.23-alpine AS builder

WORKDIR /app


COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -o auth-service ./cmd/server/main.go

FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/auth-service .
EXPOSE 8080
CMD ./auth-service 0.0.0.0:8080 ${DB_DSN}