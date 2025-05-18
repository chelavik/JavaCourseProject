import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getWorkspaceById, book } from '../api/api';
import TimeSlotSelector from '../components/TimeSlotSelector';

export default function WorkspaceDetailsPage({ token }) {
  const { id } = useParams();
  const [workspace, setWorkspace] = useState(null);

  useEffect(() => {
    getWorkspaceById(token, id).then(setWorkspace);
  }, [id, token]);

  const handleBook = async (time) => {
    const result = await book(token, id, time);
    alert(result.message || 'Booked!');
  };

  if (!workspace) return <div>Loading...</div>;

  return (
    <div>
      <h2>{workspace.name}</h2>
      <p>{workspace.description}</p>
      <TimeSlotSelector onSelect={handleBook} />
    </div>
  );
}