package auth

import (
	"context"
	"encoding/json"
	"net/http"
	"strings"
)

func AuthMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        // Swagger UI отправляет "BearerToken" вместо "Bearer"
        authHeader := r.Header.Get("Authorization")
        if authHeader == "" {
            // Для Swagger UI пробуем получить из query параметра
            authHeader = "Bearer " + r.URL.Query().Get("token")
            if authHeader == "Bearer " {
                respondWithError(w, http.StatusUnauthorized, "authorization header required")
                return
            }
        }

        tokenParts := strings.Split(authHeader, " ")
        if len(tokenParts) != 2 || tokenParts[0] != "Bearer" {
            respondWithError(w, http.StatusUnauthorized, "invalid authorization header format")
            return
        }

        claims, err := ParseToken(tokenParts[1])
        if err != nil {
            respondWithError(w, http.StatusUnauthorized, "invalid token: "+err.Error())
            return
        }

        ctx := context.WithValue(r.Context(), "jwt_claims", claims)
        next.ServeHTTP(w, r.WithContext(ctx))
    })
}

func respondWithError(w http.ResponseWriter, code int, message string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(map[string]interface{}{
		"error": message,
		"valid": false,
	})
}
