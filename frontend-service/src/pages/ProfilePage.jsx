import React, { useEffect, useState } from 'react';
import { getMe, updateMe } from '../api/api';
import './ProfilePage.css';

export default function ProfilePage({ token }) {
  const [name, setName] = useState('');
  const [newName, setNewName] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    async function fetchUser() {
      try {
        const user = await getMe(token);
        setName(user.name);
        setNewName(user.name);
      } catch (error) {
        console.error('Ошибка при загрузке профиля:', error);
      }
    }
    fetchUser();
  }, [token]);

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await updateMe(token, { name: newName });
      setName(newName);
      setMessage('Имя успешно обновлено');
    } catch (error) {
      console.error('Ошибка при обновлении имени:', error);
      setMessage('Ошибка при обновлении');
    }
  };

  return (
    <div className="profile-page">
      <h2>Мой профиль</h2>
      <form onSubmit={handleUpdate} className="profile-form">
        <label>
          Имя:
          <input
            type="text"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            minLength={3}
            maxLength={50}
            required
          />
        </label>
        <button type="submit">Сохранить</button>
        {message && <p className="message">{message}</p>}
      </form>
    </div>
  );
}

