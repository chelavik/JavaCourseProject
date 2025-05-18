import React from 'react';

export default function TimeSlotSelector({ onSelect }) {
  const startHour = 10;
  const slots = Array.from({ length: 25 }, (_, i) => {
    const hour = Math.floor(startHour + i * 0.5);
    const minute = i % 2 === 0 ? '00' : '30';
    return `${hour.toString().padStart(2, '0')}:${minute}`;
  });

  return (
    <div className="timeslot-selector">
      <h4>Select a time slot:</h4>
      {slots.map(time => (
        <button key={time} onClick={() => onSelect(time)} style={{ margin: '5px' }}>
          {time}
        </button>
      ))}
    </div>
  );
}