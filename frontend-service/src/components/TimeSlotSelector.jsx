import React, { useEffect, useState } from 'react';
import './TimeSlotSelector.css';

const startHour = 10;
const slots = Array.from({ length: 25 }, (_, i) => {
  const hour = Math.floor(startHour + i * 0.5);
  const minute = i % 2 === 0 ? '00' : '30';
  return `${hour.toString().padStart(2, '0')}:${minute}`;
});

export default function TimeSlotSelector({ token, onConfirm, mode = 'book' }) {
  const [selectedDate, setSelectedDate] = useState('');
  const [availableSlots, setAvailableSlots] = useState([]);
  const [selectedSlots, setSelectedSlots] = useState([]);

  

  useEffect(() => {
    setSelectedSlots([]);
    if (mode === 'filter') {
      setAvailableSlots(slots); // Разрешаем все кнопки
    }
    // В режиме "book" проверка доступных слотов остаётся на внешнем уровне или по API
  }, [selectedDate, mode]);

  const handleSlotClick = (time) => {
    if (selectedSlots.includes(time)) {
      // Снимаем выбор
      const newSelection = selectedSlots.filter(t => t !== time);
      setSelectedSlots(newSelection);
      return;
    }

    const slotIndex = slots.indexOf(time);

    if (selectedSlots.length === 0) {
      setSelectedSlots([time]);
      return;
    }

    const lastSelectedIndex = slots.indexOf(selectedSlots[selectedSlots.length - 1]);

    if (slotIndex === lastSelectedIndex + 1 && selectedSlots.length < 8) {
      setSelectedSlots([...selectedSlots, time]);
    }
  };


  const handleConfirm = () => {
    if (selectedSlots.length === 0 || !selectedDate) return;

    const start = `${selectedDate}T${selectedSlots[0]}:00`;

    const [endHour, endMinute] = selectedSlots[selectedSlots.length - 1].split(':').map(Number);

    let newHour = endHour;
    let newMinute = endMinute + 30;
    if (newMinute === 60) {
      newMinute = 0;
      newHour += 1;
    }

    const formattedEnd = `${selectedDate}T${newHour.toString().padStart(2, '0')}:${newMinute.toString().padStart(2, '0')}:00`;

    onConfirm({ start, end: formattedEnd });
};



  return (
    <div className="timeslot-selector">
      <h4>Выберите дату и время:</h4>
      <input
        type="date"
        value={selectedDate}
        onChange={(e) => setSelectedDate(e.target.value)}
      />

      <div className="slot-buttons">
        {slots.map((time) => {
          const isAvailable = mode === 'filter' || availableSlots.includes(time);
          const isSelected = selectedSlots.includes(time);

          return (
            <button
              key={time}
              disabled={!isAvailable}
              className={`slot-btn ${isSelected ? 'selected' : ''}`}
              onClick={() => handleSlotClick(time)}
            >
              {time}
            </button>
          );
        })}
      </div>

      <button
        onClick={handleConfirm}
        disabled={selectedSlots.length === 0}
        className="confirm-btn"
      >
        {mode === 'filter' ? 'Показать коворкинги' : 'Подтвердить бронь'}
      </button>
    </div>
  );
}
