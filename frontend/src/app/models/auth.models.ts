export interface AppUser {
  userId: number;
  username: string;
  email: string;
  roles: string[]; // Changed to string array to match backend
}

export interface Role {
  id: number;
  name: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface JwtResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  roles: string[];
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// Add User interface for backward compatibility
export interface User {
  userId: number;
  username: string;
  email: string;
  name?: string; // Optional name property for navbar display
  roles: string[];
}
