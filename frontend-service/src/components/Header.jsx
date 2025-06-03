import React from 'react';
import { Link } from 'react-router-dom';
import './Header.css';

export default function Header({ isAdmin, onLogout }) {
  return (
    <header className="header">
      <div className="left">
        <Link to="/">Главная</Link>
        <Link to="/profile">Профиль</Link>
        {isAdmin && <Link to="/admin">Админка</Link>}
      </div>
      <div className="right">
        <button className="logout-btn" onClick={onLogout}>
          Выйти
        </button>
      </div>
    </header>
  );
}
