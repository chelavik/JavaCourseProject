import React, { useEffect, useState, useCallback } from 'react';
import './AdminPage.css';
import {
  getAllBookings,
  getAllUsers,
  getWorkspaces,
  toggleWorkspaceStatus,
  updateUserById,
  createWorkspace,
  cancelBooking,
} from '../api/api';

export default function AdminPage({ token }) {
  const [bookings, setBookings] = useState([]);
  const [users, setUsers] = useState([]);
  const [workspaces, setWorkspaces] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [newWorkspace, setNewWorkspace] = useState({ name: '', capacity: '' });

  const loadData = useCallback(() => {
    getAllBookings(token).then(setBookings);
    getAllUsers(token).then(setUsers);
    getWorkspaces(token).then(setWorkspaces);
  }, [token]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleUserNameChange = async (id, name) => {
    const newName = prompt('Enter new name', name);
    if (newName) {
      await updateUserById(token, id, newName);
      loadData();
    }
  };

  const handleCreateWorkspace = async () => {
    if (!newWorkspace.name || !newWorkspace.capacity) return;
    await createWorkspace(token, newWorkspace.name, Number(newWorkspace.capacity));
    setShowModal(false);
    setNewWorkspace({ name: '', capacity: '' });
    loadData();
  };

  const handleCancelBooking = async (id) => {
    await cancelBooking(token, id);
    loadData();
  };

  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    const pad = (n) => n.toString().padStart(2, '0');
    return `${pad(date.getDate())}.${pad(date.getMonth() + 1)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  };
  const formatTime = (dateStr) => {
    const date = new Date(dateStr);
    const pad = (n) => n.toString().padStart(2, '0');
    return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  };

  return (
    <div className="admin-container">
      <h2>Admin Panel</h2>

      <section>
        <h3>Users</h3>
        <div className="card-grid">
          {users.map(u => (
            <div className="card" key={u.id}>
              <p><strong>{u.name}</strong></p>
              <p>{u.email}</p>
              <button onClick={() => handleUserNameChange(u.id, u.name)}>Edit</button>
            </div>
          ))}
        </div>
      </section>

      <section>
        <h3>Workspaces</h3>
        <button onClick={() => setShowModal(true)}>Create Workspace</button>
        <div className="card-grid">
          {workspaces.map(w => (
            <div className="card" key={w.id}>
              <p><strong>{w.name}</strong> ({w.capacity} seats)</p>
              <p>Status: {w.active ? 'Active' : 'Disabled'}</p>
              <button onClick={() => toggleWorkspaceStatus(token, w.id, !w.active).then(loadData)}>
              Toggle Status
              </button>
            </div>
          ))}
        </div>
      </section>

      <section>
        <h3>Bookings</h3>
        <div className="card-grid">
          {bookings.filter(b => !b.cancelled).map(b => (
            <div className="card" key={b.id}>
              <p><strong>Workspace:</strong> {b.workspace.name}</p>
              <p><strong>User:</strong> {b.user.name}</p>
              <p><strong>Time:</strong> {formatDate(b.startTime)} - {formatTime(b.endTime)}</p>
              <button onClick={() => handleCancelBooking(b.id)}>Cancel Booking</button>
            </div>
          ))}
        </div>
      </section>

      {showModal && (
        <div className="modal-backdrop">
          <div className="modal">
            <h3>Create Workspace</h3>
            <input
              type="text"
              placeholder="Name"
              value={newWorkspace.name}
              onChange={(e) => setNewWorkspace({ ...newWorkspace, name: e.target.value })}
            />
            <input
              type="number"
              placeholder="Capacity"
              value={newWorkspace.capacity}
              onChange={(e) => setNewWorkspace({ ...newWorkspace, capacity: e.target.value })}
            />
            <div className="modal-buttons">
              <button onClick={handleCreateWorkspace}>Create</button>
              <button onClick={() => setShowModal(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
