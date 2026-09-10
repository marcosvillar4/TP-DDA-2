import { mockUsers } from './mockAuth';

// Mocked version of authApi.js to test roles without backend

export async function loginRequest({ email, password }) {
  // Simulate network delay
  await new Promise(resolve => setTimeout(resolve, 800));

  const user = mockUsers.find(u => u.email === email && u.password === password);

  if (!user) {
    throw new Error("Correo o contraseña incorrectos.");
  }

  // Return exactly what the useLogin hook expects
  return {
    id: user.id,
    username: user.username,
    rol: user.rol,
    entidadId: user.entidadId,
    token: `fake-jwt-token-for-${user.id}`
  };
}

export async function registerRequest({ username, email, password, rol }) {
  await new Promise(resolve => setTimeout(resolve, 800));
  
  const existing = mockUsers.find(u => u.email === email);
  if (existing) {
    throw new Error("El correo ya está registrado.");
  }

  return {
    message: "Registro exitoso simulado."
  };
}