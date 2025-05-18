import React, { useEffect, useState } from 'react';
import { getWorkspaces } from '../api/api';
import WorkspaceCard from '../components/WorkspaceCard';

export default function HomePage({ token }) {
  const [workspaces, setWorkspaces] = useState([]);

  useEffect(() => {
    getWorkspaces(token).then(setWorkspaces);
  }, [token]);

  return (
    <div>
      <h1>Workspaces</h1>
      <div className="workspace-list">
        {workspaces.map(ws => <WorkspaceCard key={ws.id} workspace={ws} />)}
      </div>
    </div>
  );
}