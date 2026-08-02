import { Wallet as WalletIcon, Plus, Minus, ArrowUpRight, ArrowDownLeft } from 'lucide-react';

export default function WalletPage() {
  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Wallet</h1>

      {/* Balance Card */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-2xl p-6 mb-6">
        <p className="text-white/80 mb-1">Available Balance</p>
        <h2 className="text-4xl font-bold mb-4">₹ 250</h2>
        <div className="flex gap-3">
          <button className="btn bg-white text-primary-600 hover:bg-gray-100 flex items-center gap-2">
            <Plus className="w-4 h-4" />
            Add Money
          </button>
          <button className="btn bg-white/20 text-white hover:bg-white/30 flex items-center gap-2">
            <ArrowUpRight className="w-4 h-4" />
            Withdraw
          </button>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <button className="card p-4 flex items-center gap-3 hover:shadow-md transition-shadow">
          <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
            <ArrowDownLeft className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-left">
            <p className="font-medium">Add Money</p>
            <p className="text-sm text-gray-500">From bank/UPI</p>
          </div>
        </button>
        <button className="card p-4 flex items-center gap-3 hover:shadow-md transition-shadow">
          <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
            <ArrowUpRight className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-left">
            <p className="font-medium">Withdraw</p>
            <p className="text-sm text-gray-500">To bank</p>
          </div>
        </button>
      </div>

      {/* Transaction History */}
      <div className="card">
        <div className="p-4 border-b">
          <h3 className="font-semibold">Transaction History</h3>
        </div>
        <div className="divide-y">
          {[
            { type: 'credit', desc: 'Ride Payment Refund', amount: 50, date: '15 Jan 2024' },
            { type: 'debit', desc: 'Ride Booking', amount: -154, date: '14 Jan 2024' },
            { type: 'credit', desc: 'Welcome Bonus', amount: 100, date: '10 Jan 2024' },
            { type: 'debit', desc: 'Ride Booking', amount: -80, date: '08 Jan 2024' },
          ].map((tx, idx) => (
            <div key={idx} className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center ${
                    tx.type === 'credit' ? 'bg-green-100' : 'bg-red-100'
                  }`}
                >
                  {tx.type === 'credit' ? (
                    <ArrowDownLeft className="w-5 h-5 text-green-600" />
                  ) : (
                    <ArrowUpRight className="w-5 h-5 text-red-600" />
                  )}
                </div>
                <div>
                  <p className="font-medium">{tx.desc}</p>
                  <p className="text-sm text-gray-500">{tx.date}</p>
                </div>
              </div>
              <span
                className={`font-semibold ${
                  tx.type === 'credit' ? 'text-green-600' : 'text-red-600'
                }`}
              >
                {tx.type === 'credit' ? '+' : ''}₹{Math.abs(tx.amount)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
