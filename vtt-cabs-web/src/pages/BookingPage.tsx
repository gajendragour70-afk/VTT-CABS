import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabase';
import { MapPin, Calendar, Clock, Car, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';

const vehicleTypes = [
  { id: 'sedan', name: 'Sedan', seats: '4 Seats', pricePerKm: 10, image: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=200&h=120&fit=crop' },
  { id: 'hatchback', name: 'Hatchback', seats: '4 Seats', pricePerKm: 8, image: 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=200&h=120&fit=crop' },
  { id: 'suv', name: 'SUV', seats: '7 Seats', pricePerKm: 15, image: 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=200&h=120&fit=crop' },
  { id: 'luxury', name: 'Luxury', seats: '4 Seats', pricePerKm: 25, image: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=200&h=120&fit=crop' },
];

export default function BookingPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);
  const [customerId, setCustomerId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    pickupAddress: '',
    dropAddress: '',
    pickupDate: '',
    pickupTime: '',
    vehicleType: 'sedan',
    bookingType: 'local',
    customerNotes: '',
  });

  useEffect(() => {
    checkUser();
  }, []);

  const checkUser = async () => {
    try {
      const { data: { user } } = await supabase.auth.getUser();
      if (!user) {
        navigate('/login');
        return;
      }
      setUser(user);
      
      const customerIdFromStorage = localStorage.getItem('customerId');
      if (customerIdFromStorage) {
        setCustomerId(customerIdFromStorage);
      } else {
        const { data: customer } = await supabase
          .from('customers')
          .select('id')
          .eq('auth_user_id', user.id)
          .single();
        if (customer) {
          setCustomerId(customer.id);
          localStorage.setItem('customerId', customer.id);
        }
      }
    } catch (err) {
      console.error('Error checking user:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      if (!customerId) {
        setError('Customer profile not found. Please login again.');
        return;
      }

      const selectedVehicle = vehicleTypes.find(v => v.id === formData.vehicleType);
      const baseFare = selectedVehicle?.pricePerKm || 10;
      const estimatedDistance = 10;
      const distanceFare = baseFare * estimatedDistance;
      const totalFare = distanceFare + 50;
      const bookingNumber = `VTT${Date.now()}`;

      const tripDateTime = `${formData.pickupDate}T${formData.pickupTime}:00`;

      const { data, error: insertError } = await supabase
        .from('bookings')
        .insert({
          booking_number: bookingNumber,
          customer_id: customerId,
          booking_type: formData.bookingType,
          pickup_address: formData.pickupAddress,
          pickup_latitude: 0,
          pickup_longitude: 0,
          drop_address: formData.dropAddress,
          drop_latitude: 0,
          drop_longitude: 0,
          trip_date: tripDateTime,
          trip_time: formData.pickupTime,
          base_fare: 50,
          distance_fare: distanceFare,
          time_fare: 0,
          surge_multiplier: 1.0,
          coupon_discount: 0,
          toll_charges: 0,
          total_fare: totalFare,
          driver_earnings: totalFare * 0.8,
          platform_fee: totalFare * 0.2,
          status: 'requested',
          customer_notes: formData.customerNotes,
        })
        .select()
        .single();

      if (insertError) {
        console.error('Booking error:', insertError);
        setError('Failed to create booking: ' + insertError.message);
      } else {
        setSuccess(true);
        setTimeout(() => navigate('/history'), 2000);
      }
    } catch (err: any) {
      console.error('Submit error:', err);
      setError(err.message || 'An unexpected error occurred');
    } finally {
      setSubmitting(false);
    }
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

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle className="w-8 h-8 text-green-600" />
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Booking Confirmed!</h2>
          <p className="text-gray-600 mb-6">Your ride has been booked. Redirecting...</p>
          <Loader2 className="w-6 h-6 animate-spin text-blue-600 mx-auto" />
        </div>
      </div>
    );
  }

  const selectedVehicle = vehicleTypes.find(v => v.id === formData.vehicleType);
  const estimatedPrice = ((selectedVehicle?.pricePerKm || 10) * 10) + 50;

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
              <span className="text-sm text-gray-600 hidden sm:block">{user?.email}</span>
              <button onClick={handleLogout} className="px-4 py-2 text-sm text-gray-700 hover:text-red-600">
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Book Your Ride</h1>
          <p className="text-gray-600 mt-1">Fill in the details to book your cab</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-2 text-red-600">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
            <div>
              <p className="font-medium">Booking Failed</p>
              <p className="text-sm">{error}</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg p-6 md:p-8">
          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">Select Vehicle</label>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {vehicleTypes.map((vehicle) => (
                  <label key={vehicle.id} className={`relative cursor-pointer rounded-xl border-2 p-3 transition-all ${formData.vehicleType === vehicle.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-200'}`}>
                    <input type="radio" name="vehicleType" value={vehicle.id} checked={formData.vehicleType === vehicle.id} onChange={handleChange} className="sr-only" />
                    <img src={vehicle.image} alt={vehicle.name} className="w-full h-20 object-cover rounded-lg mb-2" />
                    <div className="text-center">
                      <p className="font-semibold text-gray-900">{vehicle.name}</p>
                      <p className="text-xs text-gray-500">{vehicle.seats}</p>
                      <p className="text-sm font-bold text-blue-600">₹{vehicle.pricePerKm}/km</p>
                    </div>
                    {formData.vehicleType === vehicle.id && (
                      <div className="absolute top-2 right-2 w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center">
                        <CheckCircle className="w-3 h-3 text-white" />
                      </div>
                    )}
                  </label>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Booking Type</label>
              <select name="bookingType" value={formData.bookingType} onChange={handleChange} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                <option value="local">Local Ride</option>
                <option value="rental">Rental</option>
                <option value="airport">Airport</option>
                <option value="outstation">Outstation</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Location</label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-green-500" />
                <input type="text" name="pickupAddress" value={formData.pickupAddress} onChange={handleChange} placeholder="Enter pickup address" className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" required />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Drop Location</label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-red-500" />
                <input type="text" name="dropAddress" value={formData.dropAddress} onChange={handleChange} placeholder="Enter drop address" className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" required />
              </div>
            </div>

            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Date</label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input type="date" name="pickupDate" value={formData.pickupDate} onChange={handleChange} min={new Date().toISOString().split('T')[0]} className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" required />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Time</label>
                <div className="relative">
                  <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input type="time" name="pickupTime" value={formData.pickupTime} onChange={handleChange} className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500" required />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Notes (Optional)</label>
              <textarea name="customerNotes" value={formData.customerNotes} onChange={handleChange} placeholder="Any special requests..." rows={3} className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 resize-none" />
            </div>

            <div className="bg-blue-50 rounded-xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <Car className="w-5 h-5 text-blue-600" />
                <span className="font-medium text-blue-900">Estimated Price</span>
              </div>
              <p className="text-2xl font-bold text-blue-600">₹{estimatedPrice}+</p>
              <p className="text-sm text-blue-700">Based on minimum 10km distance</p>
            </div>

            <button type="submit" disabled={submitting} className="w-full py-4 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center justify-center gap-2">
              {submitting ? <><Loader2 className="w-5 h-5 animate-spin" /> Processing...</> : <><CheckCircle className="w-5 h-5" /> Confirm Booking</>}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
