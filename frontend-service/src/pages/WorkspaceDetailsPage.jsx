import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getWorkspaceById, bookWorkspace } from '../api/api';
import TimeSlotSelector from '../components/TimeSlotSelector';
import './WorkspaceDetailsPage.css';

export default function WorkspaceDetailsPage({ token }) {
  const { id } = useParams();
  const [workspace, setWorkspace] = useState(null);

  useEffect(() => {
    getWorkspaceById(token, id).then(setWorkspace);
  }, [id, token]);

  const handleBook = async ({ start, end }) => {
    try {
      const result = await bookWorkspace(token, id, { start, end });
      alert(result.message || 'Бронь успешно создана!');
    } catch (e) {
      alert('Ошибка бронирования');
      console.error(e);
    }
  };

  if (!workspace) return <div className="workspace-loading">Загрузка...</div>;

  return (
    <div className="workspace-details">
      <h2 className="workspace-title">{workspace.name}</h2>
      <p className="workspace-description">{workspace.description}</p>
      <TimeSlotSelector workspaceId={parseInt(id)} onConfirm={handleBook} mode="book" />
    </div>
  );
}
