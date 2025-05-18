export const API_BASE = 'http://localhost:8081/api';
export const AUTH_API = 'http://localhost:8080/api/auth';

export async function login(email, password) {
  const res = await fetch(`${AUTH_API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  return res.json();
}

export async function register(email, password) {
  await fetch(`${AUTH_API}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
}

export async function getMe(token) {
  const res = await fetch(`${API_BASE}/users/me`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export async function getUserById(token, id) {
  const res = await fetch(`${API_BASE}/users/${id}`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export async function deleteWorkspace(token, workspaceId) {
  const res = await fetch(`${API_BASE}/admin/workspaces/${workspaceId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token
    }
  });

  if (!res.ok) {
    throw new Error('Failed to delete workspace');
  }
}

export async function updateUserById(token, id, updateData) {
  await fetch(`${API_BASE}/admin/users/${id}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token
    },
    body: JSON.stringify({
      name: updateData
    })
  });
}

export async function getWorkspaces(token) {
  const res = await fetch(`${API_BASE}/workspaces`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export async function getWorkspaceById(token, id) {
  const res = await fetch(`${API_BASE}/workspaces/${id}`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export const createWorkspace = async (token, name, capacity) => {
  const res = await fetch(`${API_BASE}/admin/workspaces`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token,
    },
    body: JSON.stringify({
      name: name,
      capacity: capacity
    }),
  });

  if (!res.ok) {
    const error = await res.json();
    throw new Error(error.message || 'Failed to create workspace');
  }
};
export async function bookWorkspace(token, workspaceId, start, end, date) {
    const res = await fetch(`${API_BASE}/bookings`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify({ 
        workspaceId,
        date,
        startTime: start,  
        endTime: end         
      })
    });

    if (!res.ok) {
      throw new Error('Ошибка при бронировании');
    }

    return res.json();
}

export async function getMyBookings(token) {
  const res = await fetch(`${API_BASE}/bookings`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export async function cancelBooking(token, bookingId) {
  const res = await fetch(`${API_BASE}/bookings/${bookingId}`, {
    method: 'DELETE',
    headers: { Authorization: token }
  });
  return res.ok;
}

export async function getAllBookings(token) {
  const res = await fetch(`${API_BASE}/admin/bookings`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export async function toggleWorkspaceStatus(token, workspaceId, active) {
  const res = await fetch(`${API_BASE}/admin/workspaces/${workspaceId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token,
    },
    body: JSON.stringify({ active }),
  });

  if (!res.ok) {
    const error = await res.text();
    throw new Error(error);
  }
  return res.json();
}


export async function getAllUsers(token) {
  const res = await fetch(`${API_BASE}/admin/users`, {
    headers: { Authorization: token }
  });
  return res.json();
}

export async function updateMyName(token, name) {
  const res = await fetch(`${API_BASE}/users/me`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token
    },
    body: JSON.stringify({ name })
  });

  if (!res.ok) {
    throw new Error('Ошибка при обновлении имени');
  }

  return res.json();
}

export async function getAvailableWorkspaces(token, start, end) {
  const params = new URLSearchParams({ start, end });
  const res = await fetch(`${API_BASE}/workspaces/available?${params.toString()}`, {
    headers: {
    'Content-Type': 'application/json',
    'Authorization': token
    }
  });
  return res.json();
}
