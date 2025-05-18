import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate, useNavigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import HomePage from './pages/HomePage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import WorkspaceDetailsPage from './pages/WorkspaceDetailsPage';
import Header from './components/Header';
import { getMe } from './api/api';
import './styles.css';

function ProtectedRoute({ isAllowed, redirectTo = '/login', children }) {
  return isAllowed ? children : <Navigate to={redirectTo} replace />;
}

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(!!token);

  useEffect(() => {
    if (!token) {
      setIsAdmin(false);
      setLoading(false);
      return;
    }
    async function fetchUser() {
      try {
        setLoading(true);
        const user = await getMe(token);
        setIsAdmin(user.role === 'ADMIN');
      } catch (e) {
        console.error('Ошибка при получении пользователя', e);
        handleLogout();
      } finally {
        setLoading(false);
      }
    }
    fetchUser();
  }, [token]);

  const handleLogin = async (newToken) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
    const user = await getMe(newToken);
    setIsAdmin(user.role === 'ADMIN');
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setIsAdmin(false);
  };

  if (loading) return <p>Загрузка...</p>;

  return (
    <Router>
      {token && <Header isAdmin={isAdmin} onLogout={handleLogout} />}
      <Routes>
        {!token && (
          <>
            <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
            <Route path="/register" element={<RegisterPage onRegister={handleLogin} />} />
            <Route path="*" element={<Navigate to="/login" />} />
          </>
        )}
        {token && (
          <>
            <Route path="/" element={<HomePage token={token} />} />
            <Route path="/profile" element={<ProfilePage token={token} />} />
            <Route
              path="/admin"
              element={
                <ProtectedRoute isAllowed={isAdmin}>
                  <AdminPage token={token} />
                </ProtectedRoute>
              }
            />
            <Route path="/workspace/:id" element={<WorkspaceDetailsPage token={token} />} />
            <Route path="*" element={<p>404 — Страница не найдена</p>} />
          </>
        )}
      </Routes>
    </Router>
  );
}
