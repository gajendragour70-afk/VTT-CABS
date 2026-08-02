import { Outlet, Link } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <div className="min-h-screen flex">
      {/* Left Side - Branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary-600 to-primary-800 p-12 flex-col justify-between">
        <div>
          <Link to="/" className="flex items-center gap-2">
            <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
              <span className="text-primary-600 font-bold text-xl">V</span>
            </div>
            <span className="text-white text-2xl font-bold">VTT CABS</span>
          </Link>
        </div>
        
        <div className="text-white">
          <h1 className="text-4xl font-bold mb-4">
            Your trusted cab booking partner
          </h1>
          <p className="text-xl text-white/80 mb-8">
            Book cabs easily, track in real-time, and enjoy safe rides with VTT CABS.
          </p>
          
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center">
                <span className="text-xl">🚗</span>
              </div>
              <span>Wide range of vehicles</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center">
                <span className="text-xl">📍</span>
              </div>
              <span>Real-time GPS tracking</span>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center">
                <span className="text-xl">🔒</span>
              </div>
              <span>Safe and secure rides</span>
            </div>
          </div>
        </div>
        
        <p className="text-white/60 text-sm">
          © 2024 VTT CABS. All rights reserved.
        </p>
      </div>
      
      {/* Right Side - Form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
