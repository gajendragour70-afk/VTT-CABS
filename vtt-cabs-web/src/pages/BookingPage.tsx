import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase, Booking } from '../lib/supabase';
import { MapPin, Calendar, Clock, Car, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';

const vehicleTypes = [
  { id: 'sedan', name: 'Sedan', seats: '4 Seats', price: 10, image: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=200&h=120&fit=crop' },
  { id: 'ertiga', name: 'Ertiga', seats: '7 Seats', price: 14, image: 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=200&h=120&fit=crop' },
  { id: 'suv', name: 'SUV', seats: '7 Seats', price: 18, image: 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=200&h=120&fit=crop' },
  { id: 'tempo', name: 'Tempo Traveller', seats: '12 Seats', price: 25, image: 'https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=200&h=120&fit=crop' },
];

export default function BookingPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    pickupLocation: '',
    dropLocation: '',
    pickupDate: '',
    pickupTime: '',
    vehicleType: 'sedan',
    tripType: 'oneway',
    notes: '',
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
    } catch (err) {
      console.error('Error checking user:', err);
      navigate('/login');
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
      const { data: { user } } = await supabase.auth.getUser();
      if (!user) {
        navigate('/login');
        return;
      }

      const selectedVehicle = vehicleTypes.find(v => v.id === formData.vehicleType);
      const basePrice = selectedVehicle?.price || 10;
      const estimatedPrice = basePrice * 10; // Estimate 10km minimum

      const booking: Omit<Booking, 'id' | 'created_at' | 'updated_at'> = {
        user_id: user.id,
        pickup_location: formData.pickupLocation,
        drop_location: formData.dropLocation,
        pickup_date: formData.pickupDate,
        pickup_time: formData.pickupTime,
        vehicle_type: formData.vehicleType as Booking['vehicle_type'],
        trip_type: formData.tripType as Booking['trip_type'],
        status: 'pending',
        estimated_price: estimatedPrice,
        estimated_distance: 10,
        notes: formData.notes || '',
      };

      const { data, error: insertError } = await supabase
        .from('bookings')
        .insert(booking)
        .select()
        .single();

      if (insertError) {
        console.error('Booking error:', insertError);
        setError('Failed to create booking. Table may not exist. Please run the SQL schema first.');
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
          <p className="text-gray-600 mb-6">Your ride has been booked successfully. Redirecting to history...</p>
          <Loader2 className="w-6 h-6 animate-spin text-blue-600 mx-auto" />
        </div>
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
              <p className="text-sm mt-1">Make sure you have run the SQL schema in Supabase SQL Editor.</p>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg p-6 md:p-8">
          <div className="space-y-6">
            {/* Vehicle Type Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">Select Vehicle</label>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {vehicleTypes.map((vehicle) => (
                  <label
                    key={vehicle.id}
                    className={`relative cursor-pointer rounded-xl border-2 p-3 transition-all ${
                      formData.vehicleType === vehicle.id
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-blue-200'
                    }`}
                  >
                    <input
                      type="radio"
                      name="vehicleType"
                      value={vehicle.id}
                      checked={formData.vehicleType === vehicle.id}
                      onChange={handleChange}
                      className="sr-only"
                    />
                    <img
                      src={vehicle.image}
                      alt={vehicle.name}
                      className="w-full h-20 object-cover rounded-lg mb-2"
                    />
                    <div className="text-center">
                      <p className="font-semibold text-gray-900">{vehicle.name}</p>
                      <p className="text-xs text-gray-500">{vehicle.seats}</p>
                      <p className="text-sm font-bold text-blue-600">₹{vehicle.price}/km</p>
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

            {/* Trip Type */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">Trip Type</label>
              <div className="flex gap-4">
                <label className={`flex-1 cursor-pointer rounded-xl border-2 p-4 transition-all ${
                  formData.tripType === 'oneway'
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-200'
                }`}>
                  <input
                    type="radio"
                    name="tripType"
                    value="oneway"
                    checked={formData.tripType === 'oneway'}
                    onChange={handleChange}
                    className="sr-only"
                  />
                  <div className="text-center">
                    <p className="font-semibold text-gray-900">One Way</p>
                    <p className="text-xs text-gray-500">Point to point</p>
                  </div>
                </label>
                <label className={`flex-1 cursor-pointer rounded-xl border-2 p-4 transition-all ${
                  formData.tripType === 'roundtrip'
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-200'
                }`}>
                  <input
                    type="radio"
                    name="tripType"
                    value="roundtrip"
                    checked={formData.tripType === 'roundtrip'}
                    onChange={handleChange}
                    className="sr-only"
                  />
                  <div className="text-center">
                    <p className="font-semibold text-gray-900">Round Trip</p>
                    <p className="text-xs text-gray-500">Return journey included</p>
                  </div>
                </label>
              </div>
            </div>

            {/* Pickup Location */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Location</label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-green-500" />
                <input
                  type="text"
                  name="pickupLocation"
                  value={formData.pickupLocation}
                  onChange={handleChange}
                  placeholder="Enter pickup address"
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>
            </div>

            {/* Drop Location */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Drop Location</label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-red-500" />
                <input
                  type="text"
                  name="dropLocation"
                  value={formData.dropLocation}
                  onChange={handleChange}
                  placeholder="Enter drop address"
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>
            </div>

            {/* Date and Time */}
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Date</label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="date"
                    name="pickupDate"
                    value={formData.pickupDate}
                    onChange={handleChange}
                    min={new Date().toISOString().split('T')[0]}
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Time</label>
                <div className="relative">
                  <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="time"
                    name="pickupTime"
                    value={formData.pickupTime}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
              </div>
            </div>

            {/* Notes */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Additional Notes (Optional)</label>
              <textarea
                name="notes"
                value={formData.notes}
                onChange={handleChange}
                placeholder="Any special requests or instructions..."
                rows={3}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              />
            </div>

            {/* Estimated Price */}
            <div className="bg-blue-50 rounded-xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <Car className="w-5 h-5 text-blue-600" />
                <span className="font-medium text-blue-900">Estimated Price</span>
              </div>
              <p className="text-2xl font-bold text-blue-600">
                ₹{(vehicleTypes.find(v => v.id === formData.vehicleType)?.price || 10) * 10}+
              </p>
              <p className="text-sm text-blue-700">Based on minimum 10km distance</p>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={submitting}
              className="w-full py-4 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {submitting ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Processing...
                </>
              ) : (
                <>
                  <CheckCircle className="w-5 h-5" />
                  Confirm Booking
                </>
              )}
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
