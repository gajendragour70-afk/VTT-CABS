import { Link } from 'react-router-dom';
import { Calendar, MapPin, Star } from 'lucide-react';

const mockHistory = [
  {
    id: '1',
    date: '2024-01-15',
    pickup: 'Sector 15, Gurugram',
    drop: 'Cyber Hub, Gurugram',
    fare: 154,
    status: 'COMPLETED',
    rating: 5,
  },
  {
    id: '2',
    date: '2024-01-14',
    pickup: 'Ambience Mall, Gurugram',
    drop: 'Rajiv Chowk, Gurugram',
    fare: 220,
    status: 'COMPLETED',
    rating: 4,
  },
  {
    id: '3',
    date: '2024-01-13',
    pickup: 'IFFCO Chowk, Gurugram',
    drop: 'HUDA City Centre',
    fare: 180,
    status: 'CANCELLED',
    rating: null,
  },
];

export default function HistoryPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Rides</h1>

      <div className="space-y-4">
        {mockHistory.map((ride) => (
          <Link key={ride.id} to={`/track/${ride.id}`} className="card p-4 hover:shadow-lg transition-shadow block">
            <div className="flex justify-between items-start mb-3">
              <div className="flex items-center gap-2 text-gray-500 text-sm">
                <Calendar className="w-4 h-4" />
                {new Date(ride.date).toLocaleDateString('en-IN', {
                  day: 'numeric',
                  month: 'short',
                  year: 'numeric',
                })}
              </div>
              <span
                className={`px-2 py-1 rounded-full text-xs font-medium ${
                  ride.status === 'COMPLETED'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-red-100 text-red-700'
                }`}
              >
                {ride.status}
              </span>
            </div>

            <div className="space-y-2 mb-3">
              <div className="flex items-start gap-3">
                <div className="mt-1">
                  <div className="w-2 h-2 bg-green-500 rounded-full" />
                </div>
                <span className="text-sm">{ride.pickup}</span>
              </div>
              <div className="flex items-start gap-3">
                <div className="mt-1">
                  <div className="w-2 h-2 bg-red-500 rounded-full" />
                </div>
                <span className="text-sm">{ride.drop}</span>
              </div>
            </div>

            <div className="flex justify-between items-center pt-3 border-t">
              <span className="font-semibold">₹{ride.fare}</span>
              {ride.rating && (
                <div className="flex items-center gap-1">
                  <Star className="w-4 h-4 text-yellow-400 fill-current" />
                  <span>{ride.rating}</span>
                </div>
              )}
            </div>
          </Link>
        ))}
      </div>

      {mockHistory.length === 0 && (
        <div className="text-center py-12">
          <MapPin className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <p className="text-gray-500">No rides yet</p>
          <Link to="/book" className="btn-primary mt-4">
            Book Your First Ride
          </Link>
        </div>
      )}
    </div>
  );
}
