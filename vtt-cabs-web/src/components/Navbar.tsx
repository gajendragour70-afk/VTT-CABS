import { Menu, X } from 'lucide-react';

interface NavbarProps {
  scrolled: boolean;
  mobileMenuOpen: boolean;
  setMobileMenuOpen: (open: boolean) => void;
  scrollToSection: (id: string) => void;
}

const navLinks = [
  { label: 'About', id: 'about' },
  { label: 'Services', id: 'services' },
  { label: 'Fleet', id: 'fleet' },
  { label: 'Why Us', id: 'why-us' },
  { label: 'Reviews', id: 'reviews' },
  { label: 'Contact', id: 'contact' },
];

export default function Navbar({ scrolled, mobileMenuOpen, setMobileMenuOpen, scrollToSection }: NavbarProps) {
  const openApp = () => {
    window.open('https://play.google.com/store/apps', '_blank');
  };

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
      scrolled ? 'bg-white shadow-lg' : 'bg-transparent'
    }`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          {/* Logo */}
          <div className="flex items-center">
            <button 
              onClick={() => scrollToSection('')}
              className="flex items-center gap-2"
            >
              <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                scrolled ? 'bg-blue-600' : 'bg-white'
              }`}>
                <span className={`text-lg font-bold ${scrolled ? 'text-white' : 'text-blue-600'}`}>V</span>
              </div>
              <span className={`text-2xl font-bold ${scrolled ? 'text-gray-900' : 'text-white'}`}>
                VTT CABS
              </span>
            </button>
          </div>

          {/* Desktop Navigation */}
          <div className="hidden lg:flex items-center gap-8">
            {navLinks.map((link) => (
              <button
                key={link.id}
                onClick={() => scrollToSection(link.id)}
                className={`font-medium transition-colors ${
                  scrolled 
                    ? 'text-gray-600 hover:text-blue-600' 
                    : 'text-white/90 hover:text-white'
                }`}
              >
                {link.label}
              </button>
            ))}
            <button
              onClick={openApp}
              className={`px-6 py-2 rounded-full font-semibold transition-all transform hover:scale-105 ${
                scrolled 
                  ? 'bg-blue-600 text-white hover:bg-blue-700' 
                  : 'bg-white text-blue-900 hover:bg-blue-50'
              }`}
            >
              Download App
            </button>
          </div>

          {/* Mobile menu button */}
          <div className="lg:hidden flex items-center gap-4">
            <button
              onClick={openApp}
              className={`px-4 py-2 rounded-full font-medium text-sm ${
                scrolled 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-white text-blue-900'
              }`}
            >
              App
            </button>
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className={`p-2 rounded-lg ${
                scrolled ? 'text-gray-600' : 'text-white'
              }`}
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
        <div className="lg:hidden bg-white border-t shadow-lg">
          <div className="px-4 py-4 space-y-2">
            {navLinks.map((link) => (
              <button
                key={link.id}
                onClick={() => scrollToSection(link.id)}
                className="block w-full text-left px-4 py-3 text-gray-700 hover:bg-blue-50 rounded-lg font-medium transition-colors"
              >
                {link.label}
              </button>
            ))}
            <button
              onClick={openApp}
              className="block w-full px-4 py-3 bg-blue-600 text-white text-center rounded-lg font-semibold mt-4"
            >
              Download App
            </button>
          </div>
        </div>
      )}
    </nav>
  );
}
