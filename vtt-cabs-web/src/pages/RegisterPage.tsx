import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabase';
import { Mail, Lock, Eye, EyeOff, User, Phone, AlertCircle, CheckCircle } from 'lucide-react';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [registerType, setRegisterType] = useState<'customer' | 'driver'>('customer');
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);

    try {
      const { data: authData, error: authError } = await supabase.auth.signUp({
        email: formData.email,
        password: formData.password,
        options: {
          data: {
            full_name: formData.fullName,
            phone: formData.phone,
          },
        },
      });

      if (authError) {
        setError(authError.message);
      } else if (authData.user) {
        if (registerType === 'customer') {
          const { error: customerError } = await supabase.from('customers').insert({
            auth_user_id: authData.user.id,
            email: formData.email,
            full_name: formData.fullName,
            phone: formData.phone,
            wallet_balance: 0,
            total_rides: 0,
            is_active: true,
          });
          if (customerError) console.error('Customer error:', customerError);
        } else {
          const { error: driverError } = await supabase.from('drivers').insert({
            auth_user_id: authData.user.id,
            email: formData.email,
            full_name: formData.fullName,
            phone: formData.phone,
            status: 'draft',
            is_online: false,
            is_available: false,
            is_active: true,
            total_trips: 0,
            average_rating: 0,
            wallet_balance: 0,
            pending_payout: 0,
          });
          if (driverError) console.error('Driver error:', driverError);
        }

        setSuccess('Account created! Check email to verify.');
        setTimeout(() => navigate('/login'), 3000);
      }
    } catch (err) {
      setError('An unexpected error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-blue-800 to-blue-700 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2">
            <div className="w-12 h-12 bg-white rounded-xl flex items-center justify-center">
              <span className="text-blue-600 text-2xl font-bold">V</span>
            </div>
            <span className="text-white text-3xl font-bold">VTT CABS</span>
          </Link>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-2xl font-bold text-gray-900 text-center mb-2">Create Account</h2>
          <p className="text-gray-600 text-center mb-6">Sign up to get started</p>

          <div className="flex gap-2 mb-6">
            <button
              type="button"
              onClick={() => setRegisterType('customer')}
              className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
                registerType === 'customer' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Customer
            </button>
            <button
              type="button"
              onClick={() => setRegisterType('driver')}
              className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
                registerType === 'driver' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Driver
            </button>
          </div>

          {registerType === 'driver' && (
            <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-800 text-sm">
              Driver accounts require verification before activation.
            </div>
          )}

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-600">
              <AlertCircle className="w-5 h-5 flex-shrink-0" />
              <span className="text-sm">{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2 text-green-600">
              <CheckCircle className="w-5 h-5 flex-shrink-0" />
              <span className="text-sm">{success}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} placeholder="Enter your full name" className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" required />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input type="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter your email" className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" required />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input type="tel" name="phone" value={formData.phone} onChange={handleChange} placeholder="+91 98765 43210" className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" required />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input type={showPassword ? 'text' : 'password'} name="password" value={formData.password} onChange={handleChange} placeholder="Create password" className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" required minLength={6} />
                <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600">
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input type={showPassword ? 'text' : 'password'} name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} placeholder="Confirm password" className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" required />
              </div>
            </div>

            <button type="submit" disabled={loading} className="w-full py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50">
              {loading ? 'Creating Account...' : `Create ${registerType === 'customer' ? 'Customer' : 'Driver'} Account`}
            </button>
          </form>

          <p className="mt-6 text-center text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 font-semibold hover:underline">Sign In</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
