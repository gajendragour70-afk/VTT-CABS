import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, ArrowRight, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/stores/authStore';
import authService from '@/services/authService';

export default function LoginPage() {
  const [step, setStep] = useState<'email' | 'otp'>('email');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuthStore();
  const navigate = useNavigate();

  const handleSendOTP = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !email.includes('@')) {
      toast.error('Please enter a valid email');
      return;
    }
    
    setIsLoading(true);
    
    try {
      const result = await authService.sendOTP(email);
      
      if (result.success) {
        toast.success('OTP sent to your email!');
        setStep('otp');
      } else {
        toast.error(result.error || 'Failed to send OTP');
      }
    } catch (error) {
      toast.error('An error occurred');
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyOTP = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!otp || otp.length !== 6) {
      toast.error('Please enter a valid 6-digit OTP');
      return;
    }
    
    setIsLoading(true);
    
    try {
      const result = await authService.verifyOTP(email, otp);
      
      if (result.success && result.user) {
        login(result.user, 'demo-token');
        toast.success('Welcome back!');
        navigate('/');
      } else {
        toast.error(result.error || 'Invalid OTP');
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

      <h1 className="text-3xl font-bold text-gray-900 mb-2">
        {step === 'email' ? 'Welcome back' : 'Enter OTP'}
      </h1>
      <p className="text-gray-600 mb-8">
        {step === 'email'
          ? 'Enter your email to continue'
          : `We sent a code to ${email}`}
      </p>

      {step === 'email' ? (
        <form onSubmit={handleSendOTP} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Email Address
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                className="input pl-10"
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="btn-primary w-full py-3"
          >
            {isLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <>
                Continue
                <ArrowRight className="w-5 h-5 ml-2" />
              </>
            )}
          </button>
        </form>
      ) : (
        <form onSubmit={handleVerifyOTP} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              OTP (Demo: 123456)
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="Enter 6-digit OTP"
                className="input pl-10 text-center text-2xl tracking-widest"
                maxLength={6}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="btn-primary w-full py-3"
          >
            {isLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              'Verify & Login'
            )}
          </button>

          <button
            type="button"
            onClick={() => setStep('email')}
            className="btn-secondary w-full"
          >
            Change Email
          </button>
        </form>
      )}

      <p className="mt-8 text-center text-gray-600">
        Don't have an account?{' '}
        <Link to="/register" className="text-primary-600 hover:underline">
          Sign up
        </Link>
      </p>
    </div>
  );
}
