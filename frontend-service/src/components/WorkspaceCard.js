import React from 'react';
import { Link } from 'react-router-dom';

export default function WorkspaceCard({ workspace }) {
  return (
    <div className="workspace-card">
      <h3>{workspace.name}</h3>
      <p>{workspace.description}</p>
      <p>Status: {workspace.active ? 'Active' : 'Disabled'}</p>
      <Link to={`/workspace/${workspace.id}`}>View Details</Link>
    </div>
  );
}