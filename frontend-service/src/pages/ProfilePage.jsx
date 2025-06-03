import React, { useEffect, useState } from 'react';
import { getMe, updateMyName, getMyBookings, cancelBooking } from '../api/api';
import './ProfilePage.css';

export default function ProfilePage({ token }) {
  const [name, setName] = useState('');
  const [newName, setNewName] = useState('');
  const [message, setMessage] = useState('');
  const [bookings, setBookings] = useState([]);
  const [showCancelled, setShowCancelled] = useState(false);

  useEffect(() => {
    fetchData();
  }, [token]);

  async function fetchData() {
    try {
      const user = await getMe(token);
      setName(user.name);
      setNewName(user.name);

      const myBookings = await getMyBookings(token);
      setBookings(myBookings);
    } catch (error) {
      console.error('Ошибка при загрузке данных:', error);
    }
  }

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await updateMyName(token, newName);
      setName(newName);
      setMessage('Имя успешно обновлено');
    } catch (error) {
      console.error('Ошибка при обновлении имени:', error);
      setMessage('Ошибка при обновлении');
    }
  };

  const handleCancel = async (bookingId) => {
    try {
      await cancelBooking(token, bookingId);
      // обновим только поле cancelled локально
      setBookings(prev =>
        prev.map(b => b.id === bookingId ? { ...b, cancelled: true } : b)
      );
    } catch (error) {
      console.error('Ошибка при отмене бронирования:', error);
      alert('Не удалось отменить бронирование');
    }
  };

  const formatTime = (isoString) =>
    new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  const formatDate = (isoString) =>
    new Date(isoString).toLocaleDateString();

  const filteredBookings = showCancelled
    ? bookings
    : bookings.filter(b => !b.cancelled);

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

      <h3>Мои бронирования</h3>

      <label className="toggle-cancelled">
        <input
          type="checkbox"
          checked={showCancelled}
          onChange={(e) => setShowCancelled(e.target.checked)}
        />
        Показать отменённые
      </label>

      <ul className="booking-list">
        {filteredBookings.length === 0 ? (
          <p>Нет бронирований для отображения.</p>
        ) : (
          filteredBookings.map((booking) => {
            const isCancelled = booking.cancelled;
            const className = isCancelled ? 'booking-item cancelled' : 'booking-item';
            return (
              <li key={booking.id} className={className}>
                <strong>{booking.workspace.name}</strong> — вместимость: {booking.workspace.capacity} <br />
                Дата: {formatDate(booking.startTime)} <br />
                Время: {formatTime(booking.startTime)} — {formatTime(booking.endTime)} <br />
                {isCancelled ? (
                  <em>Бронирование отменено</em>
                ) : (
                  <button onClick={() => handleCancel(booking.id)}>
                    Отменить бронирование
                  </button>
                )}
              </li>
            );
          })
        )}
      </ul>
    </div>
  );
}
