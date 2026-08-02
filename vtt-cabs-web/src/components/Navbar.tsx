import { Link, useNavigate } from 'react-router-dom';
import { Menu, X, User, LogOut, Wallet, History, MapPin } from 'lucide-react';
import { useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import authService from '@/services/authService';

export default function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const { user, isAuthenticated, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await authService.signOut();
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Logo */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold">V</span>
              </div>
              <span className="text-xl font-bold text-gray-900">VTT CABS</span>
            </Link>
          </div>

          {/* Desktop Navigation */}
          {isAuthenticated && (
            <div className="hidden md:flex items-center gap-4">
              <Link
                to="/book"
                className="flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors"
              >
                <MapPin className="w-5 h-5" />
                <span>Book</span>
              </Link>
              <Link
                to="/history"
                className="flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors"
              >
                <History className="w-5 h-5" />
                <span>History</span>
              </Link>
              <Link
                to="/wallet"
                className="flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors"
              >
                <Wallet className="w-5 h-5" />
                <span>Wallet</span>
              </Link>

              {/* Profile Dropdown */}
              <div className="relative">
                <button
                  onClick={() => setIsProfileOpen(!isProfileOpen)}
                  className="flex items-center gap-2 p-2 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                    <User className="w-5 h-5 text-primary-600" />
                  </div>
                </button>

                {isProfileOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border py-2">
                    <div className="px-4 py-2 border-b">
                      <p className="font-medium text-gray-900">{user?.fullName}</p>
                      <p className="text-sm text-gray-500">{user?.email}</p>
                    </div>
                    <Link
                      to="/profile"
                      className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                      onClick={() => setIsProfileOpen(false)}
                    >
                      Profile
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-red-600 hover:bg-red-50 flex items-center gap-2"
                    >
                      <LogOut className="w-4 h-4" />
                      Logout
                    </button>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Mobile menu button */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="p-2 rounded-lg hover:bg-gray-100"
            >
              {isMenuOpen ? (
                <X className="w-6 h-6 text-gray-600" />
              ) : (
                <Menu className="w-6 h-6 text-gray-600" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {isMenuOpen && (
        <div className="md:hidden bg-white border-t">
          <div className="px-4 py-4 space-y-3">
            {isAuthenticated ? (
              <>
                <Link
                  to="/book"
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
                >
                  Book a Cab
                </Link>
                <Link
                  to="/history"
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
                >
                  My Rides
                </Link>
                <Link
                  to="/wallet"
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
                >
                  Wallet
                </Link>
                <Link
                  to="/profile"
                  className="block px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
                >
                  Profile
                </Link>
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg"
                >
                  Logout
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="block w-full px-4 py-2 bg-primary-600 text-white text-center rounded-lg"
              >
                Login
              </Link>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}
