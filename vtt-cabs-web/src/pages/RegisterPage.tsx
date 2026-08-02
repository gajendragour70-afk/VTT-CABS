import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, User, Phone, ArrowRight, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import authService from '@/services/authService';

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.fullName || !formData.email || !formData.phone) {
      toast.error('Please fill all fields');
      return;
    }
    
    setIsLoading(true);
    
    try {
      const result = await authService.signUp(formData);
      
      if (result.success) {
        toast.success('Account created! Please login.');
        navigate('/login');
      } else {
        toast.error(result.error || 'Registration failed');
      }
    } catch (error) {
      toast.error('An error occurred');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <div className="lg:hidden flex items-center gap-2 mb-8">
        <div className="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center">
          <span className="text-white font-bold text-xl">V</span>
        </div>
        <span className="text-2xl font-bold text-gray-900">VTT CABS</span>
      </div>

      <h1 className="text-3xl font-bold text-gray-900 mb-2">Create Account</h1>
      <p className="text-gray-600 mb-8">Sign up to start booking rides</p>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Full Name
          </label>
          <div className="relative">
            <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              value={formData.fullName}
              onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              placeholder="Enter your name"
              className="input pl-10"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Email Address
          </label>
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="Enter your email"
              className="input pl-10"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Phone Number
          </label>
          <div className="relative">
            <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="tel"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              placeholder="Enter phone number"
              className="input pl-10"
              required
            />
          </div>
        </div>

        <button type="submit" disabled={isLoading} className="btn-primary w-full py-3">
          {isLoading ? (
            <Loader2 className="w-5 h-5 animate-spin" />
          ) : (
            <>
              Create Account
              <ArrowRight className="w-5 h-5 ml-2" />
            </>
          )}
        </button>
      </form>

      <p className="mt-8 text-center text-gray-600">
        Already have an account?{' '}
        <Link to="/login" className="text-primary-600 hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
