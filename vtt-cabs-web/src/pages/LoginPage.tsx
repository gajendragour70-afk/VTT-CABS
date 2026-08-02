import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { supabase, getCustomerProfile } from '../lib/supabase';
import { Mail, Lock, Eye, EyeOff, AlertCircle, User } from 'lucide-react';

export default function LoginPage() {
  const navigate = useNavigate();
  const [loginType, setLoginType] = useState<'customer' | 'driver'>('customer');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password,
      });

      if (error) {
        setError(error.message);
      } else if (data.user) {
        // Check if user exists in customers or drivers table
        try {
          const customer = await getCustomerProfile(data.user.id);
          if (customer) {
            setSuccess('Login successful! Redirecting...');
            localStorage.setItem('userType', 'customer');
            localStorage.setItem('customerId', customer.id);
            setTimeout(() => navigate('/'), 1000);
          }
        } catch {
          // Not a customer, check driver
          const { data: driverData } = await supabase
            .from('drivers')
            .select('*')
            .eq('auth_user_id', data.user.id)
            .single();
          
          if (driverData) {
            setSuccess('Login successful! Redirecting...');
            localStorage.setItem('userType', 'driver');
            localStorage.setItem('driverId', driverData.id);
            setTimeout(() => navigate('/driver-dashboard'), 1000);
          } else {
            // Create customer profile if not exists
            const { data: newCustomer, error: createError } = await supabase
              .from('customers')
              .insert({
                auth_user_id: data.user.id,
                email: data.user.email,
                full_name: data.user.user_metadata?.full_name || 'User',
                phone: data.user.phone || '',
                wallet_balance: 0,
                total_rides: 0,
                is_active: true,
              })
              .select()
              .single();
            
            if (!createError && newCustomer) {
              setSuccess('Login successful! Redirecting...');
              localStorage.setItem('userType', 'customer');
              localStorage.setItem('customerId', newCustomer.id);
              setTimeout(() => navigate('/'), 1000);
            } else {
              setSuccess('Login successful! Redirecting...');
              setTimeout(() => navigate('/'), 1000);
            }
          }
        }
      }
    } catch (err: any) {
      setError(err.message || 'An unexpected error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-blue-800 to-blue-700 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2">
            <div className="w-12 h-12 bg-white rounded-xl flex items-center justify-center">
              <span className="text-blue-600 text-2xl font-bold">V</span>
            </div>
            <span className="text-white text-3xl font-bold">VTT CABS</span>
          </Link>
        </div>

        {/* Login Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-2xl font-bold text-gray-900 text-center mb-2">Welcome Back</h2>
          <p className="text-gray-600 text-center mb-6">Sign in to continue</p>

          {/* Login Type Toggle */}
          <div className="flex gap-2 mb-6">
            <button
              type="button"
              onClick={() => setLoginType('customer')}
              className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
                loginType === 'customer'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Customer
            </button>
            <button
              type="button"
              onClick={() => setLoginType('driver')}
              className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
                loginType === 'driver'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              Driver
            </button>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2 text-red-600">
              <AlertCircle className="w-5 h-5 flex-shrink-0" />
              <span className="text-sm">{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2 text-green-600">
              <span className="text-sm">{success}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email"
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  className="w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Signing in...' : `Sign In as ${loginType === 'customer' ? 'Customer' : 'Driver'}`}
            </button>
          </form>

          <p className="mt-6 text-center text-gray-600">
            Don't have an account?{' '}
            <Link to="/register" className="text-blue-600 font-semibold hover:underline">
              Sign Up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
