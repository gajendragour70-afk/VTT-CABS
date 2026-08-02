import { Link } from 'react-router-dom';
import { Car, Shield, Clock, CreditCard, Star, MapPin } from 'lucide-react';

export default function HomePage() {
  return (
    <div className="flex flex-col">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-primary-600 to-primary-800 text-white py-20 lg:py-32">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div>
              <h1 className="text-4xl lg:text-6xl font-bold mb-6">
                Book your ride with VTT CABS
              </h1>
              <p className="text-xl text-white/80 mb-8">
                Fast, safe, and affordable cab service. Download our app or book directly from here.
              </p>
              <div className="flex flex-wrap gap-4">
                <Link to="/register" className="btn bg-white text-primary-600 hover:bg-gray-100 px-8 py-4 text-lg">
                  Get Started
                </Link>
                <Link to="/book" className="btn border-2 border-white text-white hover:bg-white/10 px-8 py-4 text-lg">
                  Book Now
                </Link>
              </div>
            </div>
            
            <div className="hidden lg:block">
              <img
                src="https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=600&h=400&fit=crop"
                alt="VTT CABS"
                className="rounded-2xl shadow-2xl"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-center mb-12">Why Choose VTT CABS?</h2>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            <div className="text-center p-6 rounded-xl hover:bg-gray-50 transition-colors">
              <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Car className="w-8 h-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Wide Range</h3>
              <p className="text-gray-600">Choose from Hatchback, Sedan, SUV, and Auto</p>
            </div>
            
            <div className="text-center p-6 rounded-xl hover:bg-gray-50 transition-colors">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Shield className="w-8 h-8 text-green-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Safe Rides</h3>
              <p className="text-gray-600">Verified drivers and real-time tracking</p>
            </div>
            
            <div className="text-center p-6 rounded-xl hover:bg-gray-50 transition-colors">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Clock className="w-8 h-8 text-blue-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">24/7 Available</h3>
              <p className="text-gray-600">Round the clock service for your needs</p>
            </div>
            
            <div className="text-center p-6 rounded-xl hover:bg-gray-50 transition-colors">
              <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CreditCard className="w-8 h-8 text-purple-600" />
              </div>
              <h3 className="text-xl font-semibold mb-2">Easy Payment</h3>
              <p className="text-gray-600">Cash, card, or UPI - you choose</p>
            </div>
          </div>
        </div>
      </section>

      {/* Vehicle Types */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-center mb-12">Our Vehicles</h2>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="card-hover p-6">
              <div className="text-4xl mb-4">🚗</div>
              <h3 className="text-xl font-semibold mb-2">Hatchback</h3>
              <p className="text-gray-600 mb-4">Affordable rides for daily commute</p>
              <div className="flex items-center justify-between text-sm text-gray-500">
                <span className="flex items-center gap-1">
                  <MapPin className="w-4 h-4" /> From ₹80
                </span>
                <span>4 seats</span>
              </div>
            </div>
            
            <div className="card-hover p-6">
              <div className="text-4xl mb-4">🚙</div>
              <h3 className="text-xl font-semibold mb-2">Sedan</h3>
              <p className="text-gray-600 mb-4">Comfortable rides for city travel</p>
              <div className="flex items-center justify-between text-sm text-gray-500">
                <span className="flex items-center gap-1">
                  <MapPin className="w-4 h-4" /> From ₹100
                </span>
                <span>4 seats</span>
              </div>
            </div>
            
            <div className="card-hover p-6">
              <div className="text-4xl mb-4">🚘</div>
              <h3 className="text-xl font-semibold mb-2">SUV</h3>
              <p className="text-gray-600 mb-4">Premium rides for group travel</p>
              <div className="flex items-center justify-between text-sm text-gray-500">
                <span className="flex items-center gap-1">
                  <MapPin className="w-4 h-4" /> From ₹150
                </span>
                <span>6 seats</span>
              </div>
            </div>
            
            <div className="card-hover p-6">
              <div className="text-4xl mb-4">🛺</div>
              <h3 className="text-xl font-semibold mb-2">Auto</h3>
              <p className="text-gray-600 mb-4">Budget-friendly three-wheeler</p>
              <div className="flex items-center justify-between text-sm text-gray-500">
                <span className="flex items-center gap-1">
                  <MapPin className="w-4 h-4" /> From ₹50
                </span>
                <span>3 seats</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-center mb-12">What Our Customers Say</h2>
          
          <div className="grid md:grid-cols-3 gap-8">
            <div className="p-6 bg-gray-50 rounded-xl">
              <div className="flex items-center gap-1 text-yellow-400 mb-4">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-5 h-5 fill-current" />
                ))}
              </div>
              <p className="text-gray-600 mb-4">
                "Excellent service! The driver was very professional and the ride was comfortable."
              </p>
              <div className="font-medium">- Rahul S.</div>
            </div>
            
            <div className="p-6 bg-gray-50 rounded-xl">
              <div className="flex items-center gap-1 text-yellow-400 mb-4">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-5 h-5 fill-current" />
                ))}
              </div>
              <p className="text-gray-600 mb-4">
                "Best cab service in the city. Always on time and very affordable."
              </p>
              <div className="font-medium">- Priya M.</div>
            </div>
            
            <div className="p-6 bg-gray-50 rounded-xl">
              <div className="flex items-center gap-1 text-yellow-400 mb-4">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} className="w-5 h-5 fill-current" />
                ))}
              </div>
              <p className="text-gray-600 mb-4">
                "Love the real-time tracking feature. Makes me feel safe during rides."
              </p>
              <div className="font-medium">- Ankit K.</div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-primary-600 text-white">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-4">Ready to ride?</h2>
          <p className="text-xl text-white/80 mb-8">
            Download our app or book directly from our website
          </p>
          <Link to="/book" className="btn bg-white text-primary-600 hover:bg-gray-100 px-8 py-4 text-lg">
            Book Your First Ride
          </Link>
        </div>
      </section>
    </div>
  );
}
