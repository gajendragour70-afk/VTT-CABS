import { useParams, Link } from 'react-router-dom';
import { MapPin, Phone, MessageSquare } from 'lucide-react';

export default function TrackRidePage() {
  const { bookingId } = useParams();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Map Placeholder */}
      <div className="h-96 bg-gray-200 flex items-center justify-center relative">
        <div className="text-center">
          <MapPin className="w-12 h-12 text-gray-400 mx-auto mb-2" />
          <p className="text-gray-500">Tracking your ride...</p>
        </div>
        
        {/* Driver Info Overlay */}
        <div className="absolute bottom-4 left-4 right-4 bg-white rounded-xl shadow-lg p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                <span className="text-lg font-semibold">RS</span>
              </div>
              <div>
                <p className="font-semibold">Rajesh S.</p>
                <p className="text-sm text-gray-500">Maruti Swift • DL 01 AB 1234</p>
              </div>
            </div>
            <div className="flex gap-2">
              <button className="p-3 bg-gray-100 rounded-full hover:bg-gray-200">
                <Phone className="w-5 h-5" />
              </button>
              <button className="p-3 bg-gray-100 rounded-full hover:bg-gray-200">
                <MessageSquare className="w-5 h-5" />
              </button>
            </div>
          </div>
          <div className="mt-3 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-yellow-500">★★★★★</span>
              <span className="text-sm text-gray-500">4.8</span>
            </div>
            <span className="text-sm text-primary-600 font-medium">OTP: 1234</span>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-3 gap-8">
          {/* Trip Details */}
          <div className="lg:col-span-2 space-y-6">
            <div className="card p-6">
              <h2 className="text-lg font-semibold mb-4">Trip Details</h2>
              <div className="space-y-4">
                <div className="flex items-start gap-4">
                  <div className="mt-1">
                    <div className="w-3 h-3 bg-green-500 rounded-full" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Pickup</p>
                    <p className="font-medium">Sector 15, Gurugram</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="mt-1">
                    <div className="w-3 h-3 bg-red-500 rounded-full" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Drop</p>
                    <p className="font-medium">Cyber Hub, Gurugram</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="card p-6">
              <h2 className="text-lg font-semibold mb-4">Fare Details</h2>
              <div className="space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Base Fare</span>
                  <span>₹80</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Distance (5.2 km)</span>
                  <span>₹62</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Time (12 min)</span>
                  <span>₹12</span>
                </div>
                <div className="border-t pt-3 flex justify-between font-semibold">
                  <span>Total</span>
                  <span>₹154</span>
                </div>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-4">
            <Link to="/history" className="btn-secondary w-full">
              View Ride History
            </Link>
            <button className="btn-outline w-full">
              Report an Issue
            </button>
            <button className="btn bg-red-600 text-white hover:bg-red-700 w-full">
              Emergency SOS
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
