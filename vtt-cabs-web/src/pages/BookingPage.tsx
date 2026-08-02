import { MapPin, Calendar, Clock, Car } from 'lucide-react';

export default function BookingPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Map Placeholder */}
      <div className="h-64 bg-gray-200 flex items-center justify-center">
        <div className="text-center">
          <MapPin className="w-12 h-12 text-gray-400 mx-auto mb-2" />
          <p className="text-gray-500">Map Loading...</p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-3 gap-8">
          {/* Booking Form */}
          <div className="lg:col-span-2 space-y-6">
            <div className="card p-6">
              <h2 className="text-xl font-semibold mb-4">Where to?</h2>
              
              <div className="space-y-4">
                <div className="flex items-center gap-4">
                  <div className="w-3 h-3 bg-green-500 rounded-full" />
                  <input
                    type="text"
                    placeholder="Pickup location"
                    className="input flex-1"
                  />
                </div>
                
                <div className="flex items-center gap-4">
                  <div className="w-3 h-3 bg-red-500 rounded-full" />
                  <input
                    type="text"
                    placeholder="Drop location"
                    className="input flex-1"
                  />
                </div>
              </div>
            </div>

            {/* Vehicle Selection */}
            <div className="card p-6">
              <h3 className="font-semibold mb-4">Select Vehicle</h3>
              <div className="grid grid-cols-2 gap-4">
                {[
                  { type: 'HATCHBACK', icon: '🚗', name: 'Hatchback', price: '₹80' },
                  { type: 'SEDAN', icon: '🚙', name: 'Sedan', price: '₹100' },
                  { type: 'SUV', icon: '🚘', name: 'SUV', price: '₹150' },
                  { type: 'AUTO', icon: '🛺', name: 'Auto', price: '₹50' },
                ].map((vehicle) => (
                  <div
                    key={vehicle.type}
                    className="p-4 border-2 border-gray-200 rounded-xl hover:border-primary-500 cursor-pointer transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <span className="text-2xl">{vehicle.icon}</span>
                        <div>
                          <p className="font-medium">{vehicle.name}</p>
                          <p className="text-sm text-gray-500">4 seats</p>
                        </div>
                      </div>
                      <span className="font-semibold">{vehicle.price}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Fare Summary */}
          <div className="card p-6 h-fit">
            <h3 className="font-semibold mb-4">Trip Details</h3>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Base Fare</span>
                <span>₹80</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Distance Charge</span>
                <span>₹120</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Time Charge</span>
                <span>₹30</span>
              </div>
              <div className="border-t pt-3 flex justify-between font-semibold">
                <span>Total</span>
                <span>₹230</span>
              </div>
            </div>
            <button className="btn-primary w-full mt-6">
              Confirm Booking
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
