import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabase';
import { Calendar, Clock, MapPin, User, Car, Loader2 } from 'lucide-react';

export default function HistoryPage() {
  const navigate = useNavigate();
  const [bookings, setBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      const { data: { user } } = await supabase.auth.getUser();
      if (!user) {
        navigate('/login');
        return;
      }

      const customerId = localStorage.getItem('customerId');
      let query = supabase.from('bookings').select('*').order('created_at', { ascending: false });
      
      if (customerId) {
        query = query.eq('customer_id', customerId);
      }
      
      const { data, error } = await query;
      if (error) {
        console.error('Error fetching bookings:', error);
      } else {
        setBookings(data || []);
      }
    } catch (err) {
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'payment_completed':
      case 'completed': return 'bg-green-100 text-green-800';
      case 'requested': return 'bg-yellow-100 text-yellow-800';
      case 'cancelled': return 'bg-red-100 text-red-800';
      case 'trip_started': return 'bg-blue-100 text-blue-800';
      case 'confirmed':
      case 'driver_assigned': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatStatus = (status: string) => {
    return status.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
  };

  const handleLogout = async () => {
    await supabase.auth.signOut();
    localStorage.clear();
    navigate('/login');
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
              <button onClick={() => navigate('/')} className="px-4 py-2 text-sm text-blue-600 hover:text-blue-800 font-medium">
                Book New Ride
              </button>
              <button onClick={handleLogout} className="px-4 py-2 text-sm text-gray-700 hover:text-red-600">
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Booking History</h1>

        {bookings.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <Car className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-gray-900 mb-2">No Bookings Yet</h2>
            <p className="text-gray-600 mb-6">You haven't made any bookings yet.</p>
            <button onClick={() => navigate('/')} className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700">
              Book Your First Ride
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            {bookings.map((booking) => (
              <div key={booking.id} className="bg-white rounded-2xl shadow-lg p-6">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <p className="font-semibold text-gray-900">{booking.booking_number}</p>
                    <p className="text-sm text-gray-500">
                      {new Date(booking.trip_date).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })} at {booking.trip_time}
                    </p>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(booking.status)}`}>
                    {formatStatus(booking.status)}
                  </span>
                </div>
                <div className="space-y-3">
                  <div className="flex items-start gap-3">
                    <div className="w-3 h-3 rounded-full bg-green-500 mt-1.5"></div>
                    <div>
                      <p className="text-sm text-gray-500">Pickup</p>
                      <p className="font-medium text-gray-900">{booking.pickup_address}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3">
                    <div className="w-3 h-3 rounded-full bg-red-500 mt-1.5"></div>
                    <div>
                      <p className="text-sm text-gray-500">Drop</p>
                      <p className="font-medium text-gray-900">{booking.drop_address || 'To be determined'}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4 pt-3 border-t">
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <Car className="w-4 h-4" />
                      <span>{booking.booking_type.charAt(0).toUpperCase() + booking.booking_type.slice(1)}</span>
                    </div>
                    <div className="font-semibold text-blue-600">₹{booking.total_fare}</div>
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
