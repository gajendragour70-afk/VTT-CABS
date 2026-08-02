# VTT CABS - Customer Web App

Modern React web application for VTT CABS cab booking platform.

## 🚀 Features

- **Authentication**: Email OTP login
- **Booking**: Easy cab booking with multiple vehicle options
- **Real-time Tracking**: Track your ride in real-time
- **Payment**: Multiple payment options (Cash, Online, Wallet)
- **History**: Complete ride history with ratings
- **Profile**: Manage your profile and settings
- **Wallet**: Add money, withdraw, transaction history

## 🛠️ Tech Stack

- **Framework**: React 18 + Vite
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **Routing**: React Router DOM v6
- **Backend**: Supabase
- **Maps**: Google Maps API
- **HTTP Client**: Axios
- **Query**: TanStack Query
- **Icons**: Lucide React
- **Animations**: Framer Motion

## 📁 Project Structure

```
src/
├── components/          # Reusable components
│   ├── layouts/        # Layout components
│   └── ui/             # UI components
├── pages/              # Page components
├── hooks/              # Custom React hooks
├── services/           # API services
├── stores/             # Zustand stores
├── types/              # TypeScript types
├── utils/              # Utility functions
└── assets/             # Static assets
```

## 🚀 Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn
- Supabase account

### Installation

1. Clone the repository
2. Install dependencies:
   ```bash
   npm install
   ```

3. Create environment file:
   ```bash
   cp .env.example .env
   ```

4. Update `.env` with your Supabase credentials:
   ```
   VITE_SUPABASE_URL=your-supabase-url
   VITE_SUPABASE_ANON_KEY=your-anon-key
   VITE_GOOGLE_MAPS_API_KEY=your-google-maps-key
   ```

5. Start development server:
   ```bash
   npm run dev
   ```

### Build

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## 📝 Pages

- `/` - Home page
- `/login` - Login with OTP
- `/register` - Create new account
- `/book` - Book a cab
- `/track/:id` - Track ride
- `/history` - Ride history
- `/profile` - User profile
- `/wallet` - Wallet & transactions

## 🔐 Authentication

The app uses email OTP authentication via Supabase:

1. Enter email to receive OTP
2. Enter 6-digit OTP (Demo: `123456`)
3. Login successful

## 📱 Responsive Design

- Mobile-first design
- Works on desktop and mobile devices
- Optimized for booking flow

## 🔧 Development

### Code Style

- TypeScript for type safety
- Tailwind CSS for styling
- Component-based architecture

### Testing

```bash
npm run lint
```

## 📄 License

MIT License
