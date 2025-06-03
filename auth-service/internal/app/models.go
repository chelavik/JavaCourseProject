package app

// Ответ для валидации токена
type ValidateResponse struct {
    Valid     bool   `json:"valid" example:"true"`
    UserID    string `json:"user_id" example:"550e8400-e29b-41d4-a716-446655440000"`
    ExpiresAt int64  `json:"expires_at" example:"1735689600"`
}

// Стандартный ответ ошибки
type ErrorResponse struct {
    Error string `json:"error" example:"invalid token"`
    Code  int    `json:"code" example:"401"`
}

// RegisterRequest - модель для запроса регистрации
type RegisterRequest struct {
    Email    string `json:"email" example:"user@example.com"`
    Password string `json:"password" example:"strongPassword123!"`
}

// LoginRequest - модель для запроса входа
type LoginRequest struct {
    Email    string `json:"email" example:"user@example.com"`
    Password string `json:"password" example:"strongPassword123!"`
}

// LoginResponse - модель ответа при успешном входе
type LoginResponse struct {
    Token string `json:"token" example:"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."`
}
