import { Link } from 'react-router-dom';
import { Menu, X } from 'lucide-react';

interface NavbarProps {
  scrolled: boolean;
  mobileMenuOpen: boolean;
  setMobileMenuOpen: (open: boolean) => void;
  scrollToSection: (id: string) => void;
}

const PHONE_1 = '+919893988127';

const navLinks = [
  { label: 'Home', id: '' },
  { label: 'Fleet', id: 'fleet' },
  { label: 'Packages', id: 'packages' },
  { label: 'Rates', id: 'rates' },
  { label: 'Contact', id: 'contact' },
];

export default function Navbar({ scrolled, mobileMenuOpen, setMobileMenuOpen, scrollToSection }: NavbarProps) {
  const callVTT = () => {
    window.location.href = `tel:${PHONE_1}`;
  };

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
      scrolled ? 'bg-white shadow-lg' : 'bg-blue-900/90 backdrop-blur-sm'
    }`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          {/* Logo */}
          <div className="flex items-center">
            <button 
              onClick={() => scrollToSection('')}
              className="flex items-center gap-3"
            >
              <div className="w-12 h-12 bg-gradient-to-br from-blue-600 to-orange-500 rounded-xl flex items-center justify-center shadow-lg">
                <span className="text-white font-black text-xl">V</span>
              </div>
              <div>
                <span className={`text-2xl font-black ${scrolled ? 'text-gray-900' : 'text-white'}`}>
                  VTT CABS
                </span>
                <p className={`text-xs ${scrolled ? 'text-gray-500' : 'text-blue-200'}`}>
                  Vallabhi Tour and Travels
                </p>
              </div>
            </button>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden lg:flex items-center gap-6">
            {navLinks.map((link) => (
              <button
                key={link.id}
                onClick={() => scrollToSection(link.id)}
                className={`font-semibold transition-colors ${
                  scrolled 
                    ? 'text-gray-600 hover:text-blue-600' 
                    : 'text-white/90 hover:text-white'
                }`}
              >
                {link.label}
              </button>
            ))}
            
            {/* Call Button */}
            <button
              onClick={callVTT}
              className={`flex items-center gap-2 px-4 py-2 rounded-full font-semibold transition-all ${
                scrolled 
                  ? 'bg-blue-100 text-blue-600 hover:bg-blue-200' 
                  : 'bg-white/20 text-white hover:bg-white/30'
              }`}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
              <span className="hidden xl:inline">Call Now</span>
            </button>
            
            <Link
              to="/book"
              className="px-6 py-2.5 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-bold rounded-full hover:from-orange-600 hover:to-orange-700 transition-all transform hover:scale-105 shadow-lg"
            >
              Book Now
            </Link>
            <Link
              to="/login"
              className={`px-6 py-2.5 rounded-full font-semibold transition-all border-2 ${
                scrolled 
                  ? 'border-blue-600 text-blue-600 hover:bg-blue-50' 
                  : 'border-white text-white hover:bg-white/10'
              }`}
            >
              Login
            </Link>
          </div>

          {/* Mobile menu button */}
          <div className="lg:hidden flex items-center gap-2">
            <Link
              to="/book"
              className="px-4 py-2 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-bold rounded-full text-sm shadow-lg"
            >
              Book
            </Link>
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className={`p-2 rounded-lg ${scrolled ? 'text-gray-600 bg-gray-100' : 'text-white bg-white/20'}`}
            >
              {mobileMenuOpen ? (
                <X className="w-6 h-6" />
              ) : (
                <Menu className="w-6 h-6" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="lg:hidden bg-white border-t shadow-xl">
          <div className="px-4 py-4 space-y-2">
            {navLinks.map((link) => (
              <button
                key={link.id}
                onClick={() => scrollToSection(link.id)}
                className="block w-full text-left px-4 py-3 text-gray-700 hover:bg-blue-50 rounded-lg font-semibold transition-colors"
              >
                {link.label}
              </button>
            ))}
            <div className="pt-4 space-y-2">
              <button
                onClick={callVTT}
                className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-blue-100 text-blue-600 rounded-lg font-semibold"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                Call Now
              </button>
              <Link
                to="/book"
                className="block w-full px-4 py-3 bg-gradient-to-r from-orange-500 to-orange-600 text-white text-center rounded-lg font-bold"
              >
                Book Your Ride
              </Link>
              <Link
                to="/login"
                className="block w-full px-4 py-3 border-2 border-blue-600 text-blue-600 text-center rounded-lg font-semibold"
              >
                Login / Register
              </Link>
            </div>
          </div>
        </div>
      )}
    </nav>
  );
}
