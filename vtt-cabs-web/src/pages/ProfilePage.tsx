import { useAuthStore } from '@/stores/authStore';
import { User, Mail, Phone, Star, LogOut } from 'lucide-react';

export default function ProfilePage() {
  const { user, logout } = useAuthStore();

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Profile</h1>

      <div className="card p-6">
        {/* Profile Header */}
        <div className="flex items-center gap-4 mb-6 pb-6 border-b">
          <div className="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center">
            <span className="text-3xl font-semibold text-primary-600">
              {user?.fullName?.charAt(0) || 'U'}
            </span>
          </div>
          <div>
            <h2 className="text-xl font-semibold">{user?.fullName || 'User'}</h2>
            <p className="text-gray-500">Customer</p>
            <div className="flex items-center gap-1 mt-1">
              <Star className="w-4 h-4 text-yellow-400 fill-current" />
              <span className="text-sm">4.8 • 25 rides</span>
            </div>
          </div>
        </div>

        {/* Profile Fields */}
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
              <User className="w-5 h-5 text-gray-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Full Name</p>
              <p className="font-medium">{user?.fullName || 'Not set'}</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
              <Mail className="w-5 h-5 text-gray-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Email</p>
              <p className="font-medium">{user?.email || 'Not set'}</p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
              <Phone className="w-5 h-5 text-gray-500" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Phone</p>
              <p className="font-medium">{user?.phone || 'Not set'}</p>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="mt-6 pt-6 border-t space-y-3">
          <button className="btn-secondary w-full">Edit Profile</button>
          <button className="btn-outline w-full">Change Password</button>
          <button className="btn-outline w-full text-red-600 border-red-600 hover:bg-red-50">
            Delete Account
          </button>
          <button
            onClick={logout}
            className="btn w-full text-red-600 hover:bg-red-50 flex items-center justify-center gap-2"
          >
            <LogOut className="w-4 h-4" />
            Logout
          </button>
        </div>
      </div>
    </div>
  );
}
