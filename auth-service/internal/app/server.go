// @title Auth Service API
// @version 1.0
// @description API для аутентификации и управления пользователями
// @host localhost:8080

package app

import (
	"auth-service/internal/auth"
	"auth-service/internal/storage"
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"

	_ "auth-service/docs"

	"github.com/jackc/pgx/v5/pgxpool"
	httpSwagger "github.com/swaggo/http-swagger"
	"golang.org/x/crypto/bcrypt"
)

type Server struct {
	addr        string
	db          *pgxpool.Pool
	server      *http.Server
	authservice *auth.AuthService
}

// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
// @description Введите "Bearer <JWT-токен>" для авторизации
func NewServer(addr, dbDSN string) (*Server, error) {
	db, err := pgxpool.New(context.Background(), dbDSN)
	if err != nil {
		return nil, fmt.Errorf("failed to create db pool: %w", err)
	}

	if err := db.Ping(context.Background()); err != nil {
		return nil, fmt.Errorf("failed to ping database: %w", err)
	}

	if err := runMigrations(db); err != nil {
		return nil, fmt.Errorf("failed to run migrations: %w", err)
	}

	userRepo := storage.NewPostgresUserRepository(db)
	authService := auth.NewAuthService(userRepo)

	srv := &Server{
		addr:        addr,
		db:          db,
		authservice: authService,
	}

	srv.server = &http.Server{
		Addr:    addr,
		Handler: srv.routes(),
	}

	return srv, nil
}

func (s *Server) routes() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("/health", s.healthHandler)
	mux.HandleFunc("/api/auth/register", s.registerHandler)
	mux.HandleFunc("/api/auth/login", s.loginHandler)
	mux.Handle("/api/auth/validate", auth.AuthMiddleware(http.HandlerFunc(s.validateHandler)))

	// Swagger UI
	mux.HandleFunc("/swagger/", httpSwagger.WrapHandler)

	return corsMiddleware(mux)
}

// @Summary Проверка здоровья сервиса
// @Tags service
// @Success 200 {object} map[string]string "Статус сервиса"
// @Router /health [get]
func (s *Server) healthHandler(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(`{"status":"OK"}`))
}

func (s *Server) Start() error {
	return s.server.ListenAndServe()
}

func (s *Server) Stop(ctx context.Context) error {
	if err := s.server.Shutdown(ctx); err != nil {
		return err
	}
	s.db.Close()
	return nil
}

func runMigrations(db *pgxpool.Pool) error {
	adminPassword, _ := bcrypt.GenerateFromPassword([]byte("admin123"), bcrypt.DefaultCost)
	db.Exec(context.Background(), `
		CREATE TABLE IF NOT EXISTS users (
			id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
			email VARCHAR(255) UNIQUE NOT NULL,
			password_hash VARCHAR(255) NOT NULL
		);
	`)

	_, err := db.Exec(context.Background(), `
        INSERT INTO users (id, email, password_hash)
        VALUES ($1, $2, $3)
        ON CONFLICT (email) DO NOTHING;
    `, "3f9f0c0e-45a3-4b79-8a67-62b4b3a8f0c9", "admin@example.com", string(adminPassword))
	return err
}

// @Summary Регистрация нового пользователя
// @Tags Authentication
// @Accept json
// @Produce json
// @Param request body RegisterRequest true "Данные для регистрации"
// @Success 201 "Пользователь успешно зарегистрирован"
// @Failure 400 {object} ErrorResponse "Некорректные данные"
// @Failure 409 {object} ErrorResponse "Пользователь уже существует"
// @Failure 500 {object} ErrorResponse "Ошибка сервера"
// @Router /api/auth/register [post]
func (s *Server) registerHandler(w http.ResponseWriter, r *http.Request) {
	if r.Header.Get("Content-Type") != "application/json" {
		http.Error(w, `{"error":"invalid content type","code":400}`, http.StatusBadRequest)
		return
	}

	var req RegisterRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error":"invalid request body","code":400}`, http.StatusBadRequest)
		return
	}

	if req.Email == "" || req.Password == "" {
		http.Error(w, `{"error":"email and password are required","code":400}`, http.StatusBadRequest)
		return
	}

	userID, err := s.authservice.Register(r.Context(), req.Email, req.Password)
	if err != nil {
		code := http.StatusInternalServerError
		if errors.Is(err, auth.ErrUserExists) {
			code = http.StatusConflict
		}
		http.Error(w, fmt.Sprintf(`{"error":"%v","code":%d}`, err.Error(), code), code)
		return
	}

	secret := "my-secure-token"
	go func() {
		payload := map[string]string{
			"id":    userID.String(),
			"email": req.Email,
		}
		body, _ := json.Marshal(payload)

		reqJava, err := http.NewRequest("POST", "http://main-service:8081/internal/users", bytes.NewBuffer(body))
		if err != nil {
			log.Println("createUserFromExternal request error:", err)
			return
		}
		reqJava.Header.Set("Content-Type", "application/json")
		reqJava.Header.Set("Authorization", "InternalSecret "+secret)

		resp, err := http.DefaultClient.Do(reqJava)
		if err != nil {
			log.Println("Error sending request to Java service:", err)
			return
		}
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusCreated {
			log.Println("Java service responded with", resp.StatusCode, ". Error info:", resp.Status)
			return
		}
	}()

	w.WriteHeader(http.StatusCreated)
}

// @Summary Аутентификация пользователя
// @Tags Authentication
// @Accept json
// @Produce json
// @Param request body LoginRequest true "Данные для входа"
// @Success 200 {object} LoginResponse "Успешный вход"
// @Failure 400 {object} ErrorResponse "Некорректные данные"
// @Failure 401 {object} ErrorResponse "Неверные учетные данные"
// @Failure 500 {object} ErrorResponse "Ошибка сервера"
// @Router /api/auth/login [post]
func (s *Server) loginHandler(w http.ResponseWriter, r *http.Request) {
	if r.Header.Get("Content-Type") != "application/json" {
		http.Error(w, `{"error":"invalid content type","code":400}`, http.StatusBadRequest)
		return
	}

	var req LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, `{"error":"invalid request body","code":400}`, http.StatusBadRequest)
		return
	}

	if req.Email == "" || req.Password == "" {
		http.Error(w, `{"error":"email and password are required","code":400}`, http.StatusBadRequest)
		return
	}

	token, err := s.authservice.Login(r.Context(), req.Email, req.Password)
	if err != nil {
		code := http.StatusInternalServerError
		if errors.Is(err, auth.ErrInvalidCredentials) {
			code = http.StatusUnauthorized
		}
		http.Error(w, fmt.Sprintf(`{"error":"%v","code":%d}`, err.Error(), code), code)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(LoginResponse{Token: token})
}

// @Summary Проверка валидности JWT токена
// @Description Проверяет действительность токена и возвращает информацию о пользователе
// @Tags Authentication
// @Security BearerAuth
// @Produce json
// @Success 200 {object} ValidateResponse
// @Failure 401 {object} ErrorResponse
// @Router /api/auth/validate [get]
func (s *Server) validateHandler(w http.ResponseWriter, r *http.Request) {
	claims, ok := r.Context().Value("jwt_claims").(*auth.Claims)
	if !ok {
		http.Error(w, `{"error":"invalid token claims","code":401}`, http.StatusUnauthorized)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"valid":      true,
		"user_id":    claims.UserID,
		"expires_at": claims.ExpiresAt.Unix(),
	})
}

func corsMiddleware(next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Access-Control-Allow-Origin", "http://localhost:3000")
        w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
        w.Header().Set("Access-Control-Allow-Credentials", "true")

        if r.Method == http.MethodOptions {
            w.WriteHeader(http.StatusOK)
            return
        }

        next.ServeHTTP(w, r)
    })
}