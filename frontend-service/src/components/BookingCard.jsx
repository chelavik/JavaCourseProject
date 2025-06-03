import React from 'react';

export default function BookingCard({ booking, onCancel }) {
  return (
    <div className="booking-card">
      <p>Workspace ID: {booking.workspaceId}</p>
      <p>Time: {booking.slotTime}</p>
      <button onClick={() => onCancel(booking.id)}>Cancel</button>
    </div>
  );
}