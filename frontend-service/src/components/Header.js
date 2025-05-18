import React from 'react';
import { Link } from 'react-router-dom';

export default function Header({ isAdmin, onLogout }) {
  return (
    <header className="header">
      <nav>
        <Link to="/">Home</Link>
        <Link to="/profile">Profile</Link>
        {isAdmin && <Link to="/admin">Admin</Link>}
        <button onClick={onLogout}>Logout</button>
      </nav>
    </header>
  );
}