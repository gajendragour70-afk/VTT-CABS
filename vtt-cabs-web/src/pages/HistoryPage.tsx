import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase, Booking } from '../lib/supabase';
import { Calendar, Clock, MapPin, Car, Loader2, Plus } from 'lucide-react';

const statusColors: Record<string, string> = {
  pending: 'bg-yellow-100 text-yellow-800',
  confirmed: 'bg-blue-100 text-blue-800',
  in_progress: 'bg-purple-100 text-purple-800',
  completed: 'bg-green-100 text-green-800',
  cancelled: 'bg-red-100 text-red-800',
};

const vehicleImages: Record<string, string> = {
  sedan: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=100&h=60&fit=crop',
  ertiga: 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=100&h=60&fit=crop',
  suv: 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=100&h=60&fit=crop',
  tempo: 'https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=100&h=60&fit=crop',
};

export default function HistoryPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    checkUserAndFetchBookings();
  }, []);

  const checkUserAndFetchBookings = async () => {
    try {
      const { data: { user } } = await supabase.auth.getUser();
      if (!user) {
        navigate('/login');
        return;
      }
      setUser(user);
      await fetchBookings(user.id);
    } catch (err) {
      console.error('Error:', err);
      setError('Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  const fetchBookings = async (userId: string) => {
    try {
      const { data, error: fetchError } = await supabase
        .from('bookings')
        .select('*')
        .eq('user_id', userId)
        .order('created_at', { ascending: false });

      if (fetchError) {
        console.error('Fetch error:', fetchError);
        setError('Failed to fetch bookings');
      } else {
        setBookings(data || []);
      }
    } catch (err) {
      console.error('Error fetching bookings:', err);
      setError('Failed to fetch bookings');
    }
  };

  const handleLogout = async () => {
    await supabase.auth.signOut();
    navigate('/login');
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold">V</span>
              </div>
              <span className="text-xl font-bold text-gray-900">VTT CABS</span>
            </div>
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/book')}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span className="hidden sm:inline">New Booking</span>
              </button>
              <span className="text-sm text-gray-600 hidden sm:block">{user?.email}</span>
              <button
                onClick={handleLogout}
                className="px-4 py-2 text-sm text-gray-700 hover:text-red-600 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">My Bookings</h1>
            <p className="text-gray-600 mt-1">View and manage your ride history</p>
          </div>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
            {error}
          </div>
        )}

        {bookings.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Car className="w-10 h-10 text-gray-400" />
            </div>
            <h2 className="text-xl font-semibold text-gray-900 mb-2">No Bookings Yet</h2>
            <p className="text-gray-600 mb-6">You haven't made any bookings yet. Book your first ride!</p>
            <button
              onClick={() => navigate('/book')}
              className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 transition-colors"
            >
              Book Your First Ride
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {bookings.map((booking) => (
              <div key={booking.id} className="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow">
                <div className="p-4 md:p-6">
                  <div className="flex flex-col md:flex-row md:items-center gap-4">
                    {/* Vehicle Image */}
                    <div className="flex-shrink-0">
                      <img
                        src={vehicleImages[booking.vehicle_type] || vehicleImages.sedan}
                        alt={booking.vehicle_type}
                        className="w-full md:w-32 h-20 object-cover rounded-lg"
                      />
                    </div>

                    {/* Booking Details */}
                    <div className="flex-1 min-w-0">
                      <div className="flex flex-wrap items-center gap-2 mb-2">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium capitalize ${statusColors[booking.status]}`}>
                          {booking.status}
                        </span>
                        <span className="text-sm text-gray-500">
                          #{booking.id.slice(0, 8)}
                        </span>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
                        <div className="flex items-start gap-2">
                          <MapPin className="w-4 h-4 text-green-500 mt-0.5" />
                          <div>
                            <p className="text-gray-500 text-xs">Pickup</p>
                            <p className="font-medium text-gray-900 truncate">{booking.pickup_location}</p>
                          </div>
                        </div>
                        <div className="flex items-start gap-2">
                          <MapPin className="w-4 h-4 text-red-500 mt-0.5" />
                          <div>
                            <p className="text-gray-500 text-xs">Drop</p>
                            <p className="font-medium text-gray-900 truncate">{booking.drop_location}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4 text-blue-500" />
                          <span className="text-gray-700">{formatDate(booking.pickup_date)}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Clock className="w-4 h-4 text-blue-500" />
                          <span className="text-gray-700">{booking.pickup_time}</span>
                        </div>
                      </div>
                    </div>

                    {/* Price */}
                    <div className="flex md:flex-col items-center md:items-end gap-4 md:gap-1">
                      <div className="text-right">
                        <p className="text-2xl font-bold text-gray-900">₹{booking.estimated_price || booking.final_price || 0}</p>
                        <p className="text-sm text-gray-500 capitalize">{booking.vehicle_type}</p>
                      </div>
                    </div>
                  </div>

                  {/* Date info */}
                  <div className="mt-4 pt-4 border-t border-gray-100 text-xs text-gray-500">
                    Booked on {formatDate(booking.created_at)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
