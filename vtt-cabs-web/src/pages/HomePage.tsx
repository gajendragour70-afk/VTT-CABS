import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

// Contact info - VTT CABS
const PHONE_1 = '+919893988127';
const PHONE_2 = '+9111791022';
const EMAIL = 'vttcabs2708@gmail.com';
const INSTAGRAM_URL = 'https://instagram.com/vtt27.08';

// SVG Icons
const PhoneIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
  </svg>
);

const WhatsAppIcon = () => (
  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
    <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"/>
  </svg>
);

const ShieldIcon = () => (
  <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
  </svg>
);

const UserIcon = () => (
  <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
  </svg>
);

const ClockIcon = () => (
  <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const CarIcon = () => (
  <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 17a2 2 0 11-4 0 2 2 0 014 0zM19 17a2 2 0 11-4 0 2 2 0 014 0z" />
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0" />
  </svg>
);

const MapPinIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
  </svg>
);

const CheckIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
  </svg>
);

const StarIcon = ({ filled }: { filled: boolean }) => (
  <svg className="w-5 h-5" fill={filled ? "currentColor" : "none"} stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
  </svg>
);

// Fleet Data
const fleetData = [
  {
    name: 'Maruti Ertiga',
    seats: '6+1 Seater',
    description: 'Spacious, Premium, Ideal for Family & Group Travel',
    nonAcPrice: '₹13/km',
    acPrice: '₹14/km',
    image: 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=400&h=280&fit=crop'
  },
  {
    name: 'Aura Prime',
    seats: '4+1 Seater',
    description: 'Smart Sedan with Exclusive Premium Feel',
    nonAcPrice: '₹10/km',
    acPrice: '₹11/km',
    image: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400&h=280&fit=crop'
  },
  {
    name: 'Maruti Swift',
    seats: '4+1 Seater',
    description: 'Sporty & Modern, Ideal for Small Groups',
    nonAcPrice: '₹8/km',
    acPrice: '₹9/km',
    image: 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=400&h=280&fit=crop'
  },
  {
    name: 'Maruti WagonR',
    seats: '4+1 Seater',
    description: 'Compact, Efficient, Reliable',
    nonAcPrice: '₹8/km',
    acPrice: '₹9/km',
    image: 'https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=400&h=280&fit=crop'
  },
  {
    name: 'Alto K10',
    seats: '4+1 Seater',
    description: 'Best-in-Class Economical Travel',
    nonAcPrice: '₹7/km',
    acPrice: '₹8/km',
    image: 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=400&h=280&fit=crop'
  }
];

// Popular Destinations
const destinations = [
  { name: 'Sanchi Stupa', description: 'UNESCO World Heritage Site', image: 'https://images.unsplash.com/photo-1585135497273-1a86b09fe70e?w=400&h=300&fit=crop' },
  { name: 'Udayagiri Caves', description: 'Ancient Rock-Cut Caves', image: 'https://images.unsplash.com/photo-1590077428593-a55bb07c4665?w=400&h=300&fit=crop' },
  { name: 'Bhimbetka Rocks', description: 'Prehistoric Cave Paintings', image: 'https://images.unsplash.com/photo-1609766856923-7e0a0c06584d?w=400&h=300&fit=crop' },
  { name: 'Bhojpur Temple', description: 'Ancient Shiva Temple', image: 'https://images.unsplash.com/photo-1548013146-72479768bada?w=400&h=300&fit=crop' },
  { name: 'Raisen Fort', description: 'Historic Hill Fortress', image: 'https://images.unsplash.com/photo-1590077428593-a55bb07c4665?w=400&h=300&fit=crop' },
  { name: 'Bhopal Lake', description: 'City of Lakes View', image: 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=400&h=300&fit=crop' }
];

// Tour Packages Data
const tourPackages = [
  { id: 1, name: 'Raisen Fort → Sanchi → Airport', distance: '150 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 2, name: 'Bhojpur → Bhimbetka → Airport', distance: '130 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 3, name: 'Sanchi → Udayagiri → Airport', distance: '150 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 4, name: 'Raisen Fort → Bhojpur → Airport', distance: '140 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 5, name: 'Bhimbetka → Bhojpur → Airport', distance: '120 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 6, name: 'Salkanpur → Airport', distance: '160 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 7, name: 'Narmadapuram → Bhimbetka → Airport', distance: '210 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 8, name: 'Vidisha → Udayagiri → Sanchi → Airport', distance: '180 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 9, name: 'Delawadi Jungle → Kerwa Dam → Airport', distance: '110 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' },
  { id: 10, name: 'Bhopal City Tour (Full Day) + Airport Drop', distance: '100 KM', auraNonAC: '₹2,500', auraAC: '₹2,750', ertigaNonAC: '₹3,250', ertigaAC: '₹3,500' }
];

// Reviews
const reviews = [
  { name: 'Rahul Sharma', text: 'Excellent service! Professional drivers and comfortable rides.', rating: 5 },
  { name: 'Priya Patel', text: 'Best cab service in Bhopal. Always on time.', rating: 5 },
  { name: 'Amit Kumar', text: 'Affordable prices and great vehicles. Highly recommended!', rating: 4 },
  { name: 'Sneha Reddy', text: 'Safe and reliable. Love the tour packages.', rating: 5 }
];

function HomePage() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollToSection = (id: string) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
    setMobileMenuOpen(false);
  };

  const callVTT = () => {
    window.location.href = `tel:${PHONE_1}`;
  };

  const callVTT2 = () => {
    window.location.href = `tel:${PHONE_2}`;
  };

  const whatsappVTT = () => {
    window.open(`https://wa.me/91${PHONE_1.replace('+91', '')}?text=Hello%20VTT%20CABS%2C%20I%20want%20to%20book%20a%20cab`, '_blank');
  };

  return (
    <div className="min-h-screen bg-white">
      <Navbar
        scrolled={scrolled}
        mobileMenuOpen={mobileMenuOpen}
        setMobileMenuOpen={setMobileMenuOpen}
        scrollToSection={scrollToSection}
      />

      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-900 via-blue-800 to-blue-900 overflow-hidden">
        <div className="absolute inset-0 bg-black/40"></div>
        <div className="absolute inset-0">
          <img
            src="https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=1920&h=1080&fit=crop"
            alt="VTT CABS"
            className="w-full h-full object-cover opacity-30"
          />
        </div>
        
        {/* Decorative elements */}
        <div className="absolute top-20 left-10 w-72 h-72 bg-orange-500/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-20 right-10 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl"></div>
        
        <div className="relative z-10 text-center px-4 max-w-5xl mx-auto py-20">
          {/* Brand Badge */}
          <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm rounded-full px-5 py-2 mb-8 border border-white/20">
            <span className="text-white text-sm font-medium">🚗 Vallabhi Tour and Travels</span>
          </div>
          
          <h1 className="text-5xl md:text-7xl lg:text-8xl font-black text-white mb-4 tracking-tight">
            VTT CABS
          </h1>
          
          <div className="inline-block bg-gradient-to-r from-orange-500 to-orange-600 text-white px-8 py-3 rounded-full text-xl font-bold mb-6 shadow-xl">
            "Aapki Yatra, Humara Waada"
          </div>
          
          <div className="flex flex-wrap justify-center gap-3 text-lg text-blue-100 mb-8">
            <span className="flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full">
              <span className="text-green-400">✓</span> Safe
            </span>
            <span className="flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full">
              <span className="text-green-400">✓</span> Reliable
            </span>
            <span className="flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full">
              <span className="text-green-400">✓</span> Comfortable
            </span>
          </div>
          
          <p className="text-xl md:text-2xl text-blue-100 mb-4">
            Bhopal & All India Service Available
          </p>
          
          <p className="text-lg text-blue-200 mb-10">
            24x7 Available | Professional Drivers | Clean & Comfortable Cars
          </p>
          
          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-12">
            <Link
              to="/book"
              className="w-full sm:w-auto px-10 py-4 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-bold rounded-full hover:from-orange-600 hover:to-orange-700 transition-all transform hover:scale-105 shadow-xl text-lg"
            >
              🚖 Book Your Ride Now
            </Link>
            <button
              onClick={whatsappVTT}
              className="w-full sm:w-auto px-10 py-4 bg-green-500 text-white font-bold rounded-full hover:bg-green-600 transition-all transform hover:scale-105 shadow-xl flex items-center justify-center gap-2 text-lg"
            >
              <WhatsAppIcon />
              WhatsApp Us
            </button>
          </div>
          
          {/* Quick Contact */}
          <div className="flex flex-wrap justify-center gap-4">
            <button
              onClick={callVTT}
              className="flex items-center gap-2 bg-white/10 backdrop-blur-sm text-white px-6 py-3 rounded-full hover:bg-white/20 transition-all border border-white/20"
            >
              <PhoneIcon />
              <span>{PHONE_1}</span>
            </button>
            <button
              onClick={callVTT2}
              className="flex items-center gap-2 bg-white/10 backdrop-blur-sm text-white px-6 py-3 rounded-full hover:bg-white/20 transition-all border border-white/20"
            >
              <PhoneIcon />
              <span>{PHONE_2}</span>
            </button>
          </div>
        </div>
        
        {/* Scroll indicator */}
        <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 animate-bounce">
          <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
          </svg>
        </div>
      </section>

      {/* Our Promise Section */}
      <section className="py-20 bg-gradient-to-b from-white to-blue-50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-600 font-bold text-sm uppercase tracking-wider">Our Commitment</span>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mt-2">Our Promise to You</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-blue-600 to-orange-500 mx-auto mt-4 rounded-full"></div>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Card 1 */}
            <div className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-100 group hover:border-orange-200">
              <div className="w-20 h-20 bg-gradient-to-br from-blue-100 to-blue-200 rounded-2xl flex items-center justify-center mb-6 group-hover:from-orange-100 group-hover:to-orange-200 transition-all">
                <ShieldIcon />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Safe & Secure Journey</h3>
              <p className="text-gray-600">Your safety is our priority. All vehicles are regularly maintained and equipped with safety features.</p>
            </div>
            
            {/* Card 2 */}
            <div className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-100 group hover:border-orange-200">
              <div className="w-20 h-20 bg-gradient-to-br from-blue-100 to-blue-200 rounded-2xl flex items-center justify-center mb-6 group-hover:from-orange-100 group-hover:to-orange-200 transition-all">
                <UserIcon />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Professional Drivers</h3>
              <p className="text-gray-600">Experienced, trained, and verified drivers who know the routes well and prioritize your comfort.</p>
            </div>
            
            {/* Card 3 */}
            <div className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-100 group hover:border-orange-200">
              <div className="w-20 h-20 bg-gradient-to-br from-blue-100 to-blue-200 rounded-2xl flex items-center justify-center mb-6 group-hover:from-orange-100 group-hover:to-orange-200 transition-all">
                <ClockIcon />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">24x7 Available</h3>
              <p className="text-gray-600">Round the clock service available. Day or night, we're always ready to serve you.</p>
            </div>
            
            {/* Card 4 */}
            <div className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-100 group hover:border-orange-200">
              <div className="w-20 h-20 bg-gradient-to-br from-blue-100 to-blue-200 rounded-2xl flex items-center justify-center mb-6 group-hover:from-orange-100 group-hover:to-orange-200 transition-all">
                <CarIcon />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-3">Clean & Comfortable</h3>
              <p className="text-gray-600">Well-maintained, hygienic vehicles with AC for a pleasant and comfortable travel experience.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Fleet Section */}
      <section id="fleet" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-600 font-bold text-sm uppercase tracking-wider">Our Vehicles</span>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mt-2">Premium Fleet</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-blue-600 to-orange-500 mx-auto mt-4 rounded-full"></div>
            <p className="text-gray-600 mt-4 max-w-2xl mx-auto">Choose from our wide range of well-maintained vehicles for your travel needs</p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
            {fleetData.map((vehicle, index) => (
              <div key={index} className="bg-white rounded-2xl overflow-hidden shadow-lg hover:shadow-xl transition-all duration-300 border border-gray-100 hover:border-orange-300 group">
                <div className="relative overflow-hidden h-40">
                  <img
                    src={vehicle.image}
                    alt={vehicle.name}
                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                  />
                  <div className="absolute top-3 right-3 bg-gradient-to-r from-orange-500 to-orange-600 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg">
                    {vehicle.seats}
                  </div>
                </div>
                <div className="p-5">
                  <h3 className="text-lg font-bold text-gray-900 mb-1">{vehicle.name}</h3>
                  <p className="text-sm text-gray-500 mb-3">{vehicle.description}</p>
                  <div className="space-y-1">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">Non AC:</span>
                      <span className="font-bold text-blue-600">{vehicle.nonAcPrice}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-500">AC:</span>
                      <span className="font-bold text-orange-600">{vehicle.acPrice}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Rates Section */}
      <section id="rates" className="py-20 bg-gradient-to-br from-blue-900 via-blue-800 to-blue-900">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-400 font-bold text-sm uppercase tracking-wider">Transparent Pricing</span>
            <h2 className="text-4xl md:text-5xl font-black text-white mt-2">Our Rates</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-orange-500 to-orange-400 mx-auto mt-4 rounded-full"></div>
          </div>
          
          <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto mb-12">
            {/* Ertiga Rates */}
            <div className="bg-white rounded-2xl p-8 shadow-2xl">
              <div className="flex items-center gap-4 mb-6">
                <div className="w-16 h-16 bg-gradient-to-br from-blue-100 to-blue-200 rounded-xl flex items-center justify-center">
                  <span className="text-2xl">🚗</span>
                </div>
                <div>
                  <h3 className="text-2xl font-bold text-gray-900">Ertiga</h3>
                  <p className="text-gray-500">6+1 Seater</p>
                </div>
              </div>
              <div className="space-y-4">
                <div className="flex justify-between items-center p-4 bg-blue-50 rounded-xl">
                  <span className="font-medium text-gray-700">Non AC</span>
                  <span className="text-2xl font-bold text-blue-600">₹13/km</span>
                </div>
                <div className="flex justify-between items-center p-4 bg-orange-50 rounded-xl">
                  <span className="font-medium text-gray-700">AC</span>
                  <span className="text-2xl font-bold text-orange-600">₹14/km</span>
                </div>
              </div>
            </div>
            
            {/* Aura Rates */}
            <div className="bg-white rounded-2xl p-8 shadow-2xl">
              <div className="flex items-center gap-4 mb-6">
                <div className="w-16 h-16 bg-gradient-to-br from-blue-100 to-blue-200 rounded-xl flex items-center justify-center">
                  <span className="text-2xl">🚙</span>
                </div>
                <div>
                  <h3 className="text-2xl font-bold text-gray-900">Aura</h3>
                  <p className="text-gray-500">4+1 Seater</p>
                </div>
              </div>
              <div className="space-y-4">
                <div className="flex justify-between items-center p-4 bg-blue-50 rounded-xl">
                  <span className="font-medium text-gray-700">Non AC</span>
                  <span className="text-2xl font-bold text-blue-600">₹10/km</span>
                </div>
                <div className="flex justify-between items-center p-4 bg-orange-50 rounded-xl">
                  <span className="font-medium text-gray-700">AC</span>
                  <span className="text-2xl font-bold text-orange-600">₹11/km</span>
                </div>
              </div>
            </div>
          </div>
          
          {/* Important Info */}
          <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-8 max-w-4xl mx-auto">
            <h3 className="text-xl font-bold text-white mb-6 text-center">Important Information</h3>
            <div className="grid md:grid-cols-2 gap-6 text-white">
              <div className="bg-white/10 rounded-xl p-5">
                <p className="font-semibold text-orange-400 mb-2">Minimum Billing</p>
                <p className="text-lg">250 KM / 8 Hours</p>
                <p className="text-sm text-blue-200">(Whichever is earlier)</p>
              </div>
              <div className="bg-white/10 rounded-xl p-5">
                <p className="font-semibold text-orange-400 mb-2">Extra Hour Charges</p>
                <p className="text-lg">₹150/hour</p>
              </div>
              <div className="bg-white/10 rounded-xl p-5 md:col-span-2">
                <p className="font-semibold text-orange-400 mb-2">Additional Charges</p>
                <p className="text-sm text-blue-200">Toll, Parking and Entry Tickets are Extra</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Popular Destinations */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-600 font-bold text-sm uppercase tracking-wider">Explore</span>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mt-2">Popular Destinations</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-blue-600 to-orange-500 mx-auto mt-4 rounded-full"></div>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {destinations.map((dest, index) => (
              <div key={index} className="group relative overflow-hidden rounded-2xl h-64 shadow-lg">
                <img
                  src={dest.image}
                  alt={dest.name}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent"></div>
                <div className="absolute bottom-0 left-0 right-0 p-6">
                  <h3 className="text-xl font-bold text-white mb-1">{dest.name}</h3>
                  <p className="text-sm text-gray-300">{dest.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Tour Packages Section */}
      <section id="packages" className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-600 font-bold text-sm uppercase tracking-wider">Special Offers</span>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mt-2">One-Day Tour Packages</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-blue-600 to-orange-500 mx-auto mt-4 rounded-full"></div>
            <p className="text-gray-600 mt-4">All packages include airport drop | Toll, parking extra</p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {tourPackages.map((pkg) => (
              <div key={pkg.id} className="bg-white rounded-2xl shadow-lg overflow-hidden border border-gray-100 hover:border-orange-300 hover:shadow-xl transition-all duration-300">
                <div className="bg-gradient-to-r from-blue-600 to-blue-700 p-5">
                  <div className="flex items-center justify-between">
                    <span className="bg-white/20 text-white text-xs px-3 py-1 rounded-full font-medium">Package {pkg.id}</span>
                    <span className="flex items-center gap-1 text-white text-sm">
                      <MapPinIcon />
                      ~{pkg.distance}
                    </span>
                  </div>
                  <h3 className="text-lg font-bold text-white mt-3">Bhopal → {pkg.name}</h3>
                </div>
                <div className="p-5">
                  <div className="grid grid-cols-2 gap-3 mb-4">
                    <div className="text-center p-3 bg-blue-50 rounded-lg">
                      <p className="text-xs text-gray-500 mb-1">Aura Non AC</p>
                      <p className="font-bold text-blue-600">{pkg.auraNonAC}</p>
                    </div>
                    <div className="text-center p-3 bg-orange-50 rounded-lg">
                      <p className="text-xs text-gray-500 mb-1">Aura AC</p>
                      <p className="font-bold text-orange-600">{pkg.auraAC}</p>
                    </div>
                    <div className="text-center p-3 bg-blue-50 rounded-lg">
                      <p className="text-xs text-gray-500 mb-1">Ertiga Non AC</p>
                      <p className="font-bold text-blue-600">{pkg.ertigaNonAC}</p>
                    </div>
                    <div className="text-center p-3 bg-orange-50 rounded-lg">
                      <p className="text-xs text-gray-500 mb-1">Ertiga AC</p>
                      <p className="font-bold text-orange-600">{pkg.ertigaAC}</p>
                    </div>
                  </div>
                  <Link
                    to="/book"
                    className="block w-full py-3 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-bold rounded-lg hover:from-orange-600 hover:to-orange-700 transition-all text-center"
                  >
                    Book This Package
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section id="reviews" className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-600 font-bold text-sm uppercase tracking-wider">Testimonials</span>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mt-2">What Our Customers Say</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-blue-600 to-orange-500 mx-auto mt-4 rounded-full"></div>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {reviews.map((review, index) => (
              <div key={index} className="bg-white p-6 rounded-2xl shadow-lg">
                <div className="flex items-center mb-4">
                  {[...Array(5)].map((_, i) => (
                    <StarIcon key={i} filled={i < review.rating} />
                  ))}
                </div>
                <p className="text-gray-600 mb-4 italic">"{review.text}"</p>
                <div className="flex items-center">
                  <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-orange-500 rounded-full flex items-center justify-center text-white font-bold text-lg">
                    {review.name.charAt(0)}
                  </div>
                  <span className="ml-3 font-semibold text-gray-900">{review.name}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Contact Section */}
      <section id="contact" className="py-20 bg-gradient-to-br from-blue-900 to-blue-800">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center mb-12">
            <span className="text-orange-400 font-bold text-sm uppercase tracking-wider">Get In Touch</span>
            <h2 className="text-4xl md:text-5xl font-black text-white mt-2">Contact Us</h2>
            <div className="w-24 h-1 bg-gradient-to-r from-orange-500 to-orange-400 mx-auto mt-4 rounded-full"></div>
            <p className="text-blue-200 mt-4">We're here to help you 24/7</p>
          </div>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl mx-auto mb-8">
            <button
              onClick={callVTT}
              className="bg-white rounded-2xl p-6 text-center hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl"
            >
              <div className="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <PhoneIcon />
              </div>
              <p className="font-bold text-gray-900 text-lg">{PHONE_1}</p>
              <p className="text-sm text-gray-500 mt-1">Call Now</p>
            </button>

            <button
              onClick={callVTT2}
              className="bg-white rounded-2xl p-6 text-center hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl"
            >
              <div className="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <PhoneIcon />
              </div>
              <p className="font-bold text-gray-900 text-lg">{PHONE_2}</p>
              <p className="text-sm text-gray-500 mt-1">Call Now</p>
            </button>

            <button
              onClick={whatsappVTT}
              className="bg-white rounded-2xl p-6 text-center hover:bg-green-50 transition-all shadow-lg hover:shadow-xl"
            >
              <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <WhatsAppIcon />
              </div>
              <p className="font-bold text-gray-900 text-lg">WhatsApp</p>
              <p className="text-sm text-gray-500 mt-1">Message Us</p>
            </button>

            <a
              href={INSTAGRAM_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="bg-white rounded-2xl p-6 text-center hover:bg-pink-50 transition-all shadow-lg hover:shadow-xl block"
            >
              <div className="w-16 h-16 bg-pink-100 text-pink-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 0C8.741 0 8.333.014 7.053.072 2.695.272.273 2.69.073 7.052.014 8.333 0 8.741 0 12c0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98C8.333 23.986 8.741 24 12 24c3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98C15.668.014 15.259 0 12 0zm0 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324zM12 16a4 4 0 110-8 4 4 0 010 8zm6.406-11.845a1.44 1.44 0 100 2.881 1.44 1.44 0 000-2.881z"/>
                </svg>
              </div>
              <p className="font-bold text-gray-900 text-lg">@Vtt27.08</p>
              <p className="text-sm text-gray-500 mt-1">Instagram</p>
            </a>
          </div>
          
          {/* Email */}
          <div className="max-w-md mx-auto">
            <a
              href={`mailto:${EMAIL}?subject=Booking Enquiry&body=Hello, I want to book a cab.`}
              className="block w-full bg-white rounded-2xl p-6 text-center hover:bg-blue-50 transition-all shadow-lg hover:shadow-xl"
            >
              <div className="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <p className="font-bold text-gray-900 text-lg">{EMAIL}</p>
              <p className="text-sm text-gray-500 mt-1">Email Us</p>
            </a>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-gradient-to-r from-orange-500 to-orange-600">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl md:text-4xl font-black text-white mb-4">Ready to Book Your Ride?</h2>
          <p className="text-xl text-orange-100 mb-8">Contact us now or book directly!</p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/book"
              className="px-10 py-4 bg-white text-orange-600 font-bold rounded-full hover:bg-orange-50 transition-all transform hover:scale-105 shadow-xl text-lg"
            >
              🚖 Book Now
            </Link>
            <button
              onClick={callVTT}
              className="px-10 py-4 bg-blue-900 text-white font-bold rounded-full hover:bg-blue-800 transition-all transform hover:scale-105 shadow-xl text-lg"
            >
              📞 Call: {PHONE_1}
            </button>
          </div>
        </div>
      </section>

      <Footer />

      {/* Floating Action Buttons */}
      <div className="fixed bottom-6 right-6 flex flex-col gap-3 z-50">
        <button
          onClick={whatsappVTT}
          className="w-14 h-14 bg-green-500 text-white rounded-full shadow-lg hover:bg-green-600 transition-all transform hover:scale-110 flex items-center justify-center"
          aria-label="WhatsApp"
        >
          <WhatsAppIcon />
        </button>
        <button
          onClick={callVTT}
          className="w-14 h-14 bg-gradient-to-r from-blue-600 to-orange-500 text-white rounded-full shadow-lg hover:from-blue-700 hover:to-orange-600 transition-all transform hover:scale-110 flex items-center justify-center"
          aria-label="Call"
        >
          <PhoneIcon />
        </button>
      </div>
    </div>
  );
}

export default HomePage;
