import { Routes, Route, Navigate } from 'react-router-dom';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BookingPage from './pages/BookingPage';
import HistoryPage from './pages/HistoryPage';

// Customer-only website: legacy driver URLs are redirected to the customer login page.
const DRIVER_LEGACY_PATHS = [
  '/driver',
  '/driver-login',
  '/driver/login',
  '/driver/signup',
  '/driver/register',
  '/driver/dashboard',
  '/driver-dashboard',
  '/driver-portal',
  '/driver-portal/dashboard',
];

function App() {
  return (
    <Routes>
      {/* Public Routes (customer only) */}
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected Routes */}
      <Route path="/book" element={<BookingPage />} />
      <Route path="/history" element={<HistoryPage />} />

      {/* Redirect any legacy driver URL to the customer login page */}
      {DRIVER_LEGACY_PATHS.map((p) => (
        <Route key={p} path={p} element={<Navigate to="/login" replace />} />
      ))}

      {/* Catch-all: any other unknown URL goes to the homepage */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
