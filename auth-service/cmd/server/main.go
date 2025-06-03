package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"auth-service/internal/app"
)

func main() {
	if len(os.Args) < 3 {
		log.Fatal("required: auth-service <addr> <db_dsn>")
	}

	addr := os.Args[1]
	dbDSN := os.Args[2]

	server, err := app.NewServer(addr, dbDSN)
	if err != nil {
		log.Fatalf("Failed to create server: %v", err)
	}

	serverErr := make(chan error, 1)
	go func() {
		log.Printf("Server starting on %s", addr)
		serverErr <- server.Start()
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, os.Interrupt, syscall.SIGTERM)

	select {
	case err := <-serverErr:
		if err != nil && err != http.ErrServerClosed {
			log.Fatalf("Server error: %v", err)
		}
	case sig := <-quit:
		log.Printf("Received signal: %v", sig)
		log.Println("Shutting down server gracefully...")

		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()

		if err := server.Stop(ctx); err != nil {
			log.Printf("Graceful shutdown failed: %v", err)
			os.Exit(1)
		}
		log.Println("Server stopped gracefully")
	}
}