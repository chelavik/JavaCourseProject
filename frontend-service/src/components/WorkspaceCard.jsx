import React from 'react';
import './WorkspaceCard.css';

export default function WorkspaceCard({ workspace, onBook }) {
  return (
    <div className="workspace-card">
      <h3>{workspace.name}</h3>
      <p><strong>Описание:</strong> {workspace.description}</p>
      <p><strong>Вместимость:</strong> {workspace.capacity} человек</p>
      <button className="book-btn" onClick={() => onBook(workspace.id)}>
        Забронировать
      </button>
    </div>
  );
}
