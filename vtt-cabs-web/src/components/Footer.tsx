// Contact info - VTT CABS
const PHONE_1 = '+919893988127';
const PHONE_2 = '+9111791022';
const EMAIL = 'vttcabs2708@gmail.com';
const INSTAGRAM_URL = 'https://instagram.com/vtt27.08';

export default function Footer() {
  const currentYear = new Date().getFullYear();

  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <footer className="bg-gray-900 text-white">
      {/* Main Footer */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12">
            {/* Brand */}
            <div className="lg:col-span-1">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-14 h-14 bg-gradient-to-br from-blue-600 to-orange-500 rounded-xl flex items-center justify-center shadow-lg">
                  <span className="text-white font-black text-2xl">V</span>
                </div>
                <div>
                  <span className="text-2xl font-black">VTT CABS</span>
                  <p className="text-xs text-gray-400">Vallabhi Tour and Travels</p>
                </div>
              </div>
              <p className="text-orange-400 font-semibold italic mb-4 text-lg">"Aapki Yatra, Humara Waada"</p>
              <p className="text-gray-400 mb-6">
                Bhopal (M.P.) & All India Service Available | 24x7 Available | Safe & Secure Journey
              </p>
              <div className="flex gap-3">
                <a href={`tel:${PHONE_1}`} className="w-11 h-11 bg-gray-800 rounded-full flex items-center justify-center hover:bg-blue-600 transition-all">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                </a>
                <a href={INSTAGRAM_URL} target="_blank" rel="noopener noreferrer" className="w-11 h-11 bg-gray-800 rounded-full flex items-center justify-center hover:bg-gradient-to-r hover:from-purple-600 hover:to-pink-500 transition-all">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324zM12 16a4 4 0 110-8 4 4 0 010 8zm6.406-11.845a1.44 1.44 0 100 2.881 1.44 1.44 0 000-2.881z"/>
                  </svg>
                </a>
                <a href={`mailto:${EMAIL}`} className="w-11 h-11 bg-gray-800 rounded-full flex items-center justify-center hover:bg-green-600 transition-all">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </a>
              </div>
            </div>

            {/* Quick Links */}
            <div>
              <h3 className="text-lg font-bold mb-6 text-white">Quick Links</h3>
              <ul className="space-y-3">
                <li><button onClick={() => scrollToSection('fleet')} className="text-gray-400 hover:text-orange-400 transition-colors">Our Fleet</button></li>
                <li><button onClick={() => scrollToSection('packages')} className="text-gray-400 hover:text-orange-400 transition-colors">Tour Packages</button></li>
                <li><button onClick={() => scrollToSection('rates')} className="text-gray-400 hover:text-orange-400 transition-colors">Cab Rates</button></li>
                <li><button onClick={() => scrollToSection('contact')} className="text-gray-400 hover:text-orange-400 transition-colors">Contact Us</button></li>
              </ul>
            </div>

            {/* Contact */}
            <div>
              <h3 className="text-lg font-bold mb-6 text-white">Contact Us</h3>
              <ul className="space-y-4">
                <li className="flex items-start gap-3">
                  <span className="text-orange-400 mt-1">📞</span>
                  <div>
                    <p className="text-white font-semibold">{PHONE_1}</p>
                    <p className="text-white font-semibold">{PHONE_2}</p>
                  </div>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-orange-400">✉️</span>
                  <p className="text-white">{EMAIL}</p>
                </li>
                <li className="flex items-start gap-3">
                  <span className="text-orange-400">📍</span>
                  <p className="text-white">Bhopal (M.P.)</p>
                </li>
              </ul>
            </div>

            {/* Legal */}
            <div>
              <h3 className="text-lg font-bold mb-6 text-white">Legal</h3>
              <ul className="space-y-3">
                <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">Privacy Policy</a></li>
                <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">Terms of Service</a></li>
                <li><a href="#" className="text-gray-400 hover:text-orange-400 transition-colors">Refund Policy</a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="border-t border-gray-800 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-gray-400 text-sm text-center md:text-left">
              © {currentYear} VTT CABS - Vallabhi Tour and Travels Bhopal. All rights reserved.
            </p>
            <p className="text-gray-500 text-sm">
              Safe • Reliable • Comfortable
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
