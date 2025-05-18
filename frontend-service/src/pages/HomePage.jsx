import React, { useState } from 'react';
import WorkspaceCard from '../components/WorkspaceCard';
import TimeSlotSelector from '../components/TimeSlotSelector';
import { getAvailableWorkspaces, bookWorkspace } from '../api/api';
import './HomePage.css';

export default function HomePage({ token }) {
  const [availableWorkspaces, setAvailableWorkspaces] = useState([]);
  const [filterApplied, setFilterApplied] = useState(false);
  const [selectedTime, setSelectedTime] = useState(null);

  const handleTimeSelect = async ({ start, end }) => {
    const date = start.split('T')[0]; // извлекаем дату
    try {
      const data = await getAvailableWorkspaces(token, start, end);
      setAvailableWorkspaces(data);
      setFilterApplied(true);
      setSelectedTime({ start, end, date }); // сохраняем date
    } catch (e) {
      console.error('Ошибка при получении доступных коворкингов:', e);
      alert('Не удалось загрузить список доступных коворкингов.');
    }
};


  const handleBook = async (workspaceId) => {
    if (!selectedTime) return;
    const { start, end, date } = selectedTime;
    try {
      await bookWorkspace(token, workspaceId, start, end, date);
      alert('Бронирование успешно!');
    } catch (e) {
      console.error('Ошибка при бронировании:', e);
      alert('Не удалось забронировать коворкинг.');
    }
  };


  return (
    <div className="home-container">
      <h1 className="page-title">Найти доступный коворкинг</h1>

      <TimeSlotSelector token={token} mode="filter" onConfirm={handleTimeSelect} />

      {filterApplied && (
        <div className="workspace-list">
          {availableWorkspaces.length > 0 ? (
            availableWorkspaces.map(ws => (
              <WorkspaceCard key={ws.id} workspace={ws} onBook={handleBook} />
            ))
          ) : (
            <p className="empty-message">Нет доступных коворкингов на это время.</p>
          )}
        </div>
      )}
    </div>
  );
}
