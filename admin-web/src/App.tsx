import React, { useState } from 'react';

interface PendingListing {
  id: string;
  title: string;
  businessName: string;
  category: string;
  type: string;
  url: string;
  createdAt: string;
}

export function App() {
  const [activeTab, setActiveTab] = useState<'pending' | 'reports' | 'audit'>('pending');
  const [pendingItems, setPendingItems] = useState<PendingListing[]>([
    {
      id: 'lst_p1',
      title: 'Python Data Engineering Summer Fellowship',
      businessName: 'Alpha Cloud Tech',
      category: 'Internships and jobs',
      type: 'INTERNSHIP',
      url: 'https://alphacloudtech.example.com',
      createdAt: '2026-10-13 14:15'
    },
    {
      id: 'lst_p2',
      title: 'Shopify Store Speed Optimization Service',
      businessName: 'QuickFix Web Masters',
      category: 'Software and web development',
      type: 'SERVICE',
      url: 'https://quickfixweb.example.com',
      createdAt: '2026-10-13 12:40'
    }
  ]);

  const handleApprove = (id: string) => {
    setPendingItems(prev => prev.filter(i => i.id !== id));
    alert(`Listing ${id} approved and published to live public feed!`);
  };

  const handleReject = (id: string) => {
    const reason = prompt('Please enter mandatory rejection reason:');
    if (reason) {
      setPendingItems(prev => prev.filter(i => i.id !== id));
      alert(`Listing ${id} rejected with reason: ${reason}`);
    }
  };

  return (
    <div className="min-h-screen bg-[#070B19] text-[#F0F4FC] font-sans">
      <header className="border-b border-[#192850] bg-[#0D152D] px-8 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-lg bg-gradient-to-r from-[#0066FF] to-[#00E5FF] flex items-center justify-center font-black text-black">
            C
          </div>
          <div>
            <h1 className="text-lg font-bold">Codelligence Admin Dashboard</h1>
            <p className="text-xs text-[#00E5FF]">Codelligence Technologies Moderation Suite</p>
          </div>
        </div>
        <div className="flex items-center space-x-4">
          <span className="px-3 py-1 bg-[#192850] rounded-full text-xs font-semibold text-[#00E676] border border-[#00E676]/30">
            System Healthy • ₹0 Budget Mode
          </span>
          <span className="text-sm text-gray-400">admin@codelligence.tech</span>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-8 py-8">
        <div className="grid grid-cols-4 gap-6 mb-8">
          <div className="bg-[#0D152D] border border-[#192850] rounded-xl p-5">
            <p className="text-xs text-gray-400 uppercase font-semibold">Active Listings</p>
            <p className="text-2xl font-black text-[#00E676] mt-1">8</p>
          </div>
          <div className="bg-[#0D152D] border border-[#192850] rounded-xl p-5">
            <p className="text-xs text-gray-400 uppercase font-semibold">Pending Queue</p>
            <p className="text-2xl font-black text-[#FFB300] mt-1">{pendingItems.length}</p>
          </div>
          <div className="bg-[#0D152D] border border-[#192850] rounded-xl p-5">
            <p className="text-xs text-gray-400 uppercase font-semibold">Open Reports</p>
            <p className="text-2xl font-black text-[#FF3860] mt-1">1</p>
          </div>
          <div className="bg-[#0D152D] border border-[#192850] rounded-xl p-5">
            <p className="text-xs text-gray-400 uppercase font-semibold">Free Renewals (60d)</p>
            <p className="text-2xl font-black text-[#00E5FF] mt-1">100%</p>
          </div>
        </div>

        <div className="bg-[#0D152D] border border-[#192850] rounded-xl overflow-hidden">
          <div className="flex border-b border-[#192850] px-6">
            <button
              onClick={() => setActiveTab('pending')}
              className={`py-4 px-4 font-bold text-sm border-b-2 ${
                activeTab === 'pending' ? 'border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-gray-400'
              }`}
            >
              Pending Submissions ({pendingItems.length})
            </button>
            <button
              onClick={() => setActiveTab('reports')}
              className={`py-4 px-4 font-bold text-sm border-b-2 ${
                activeTab === 'reports' ? 'border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-gray-400'
              }`}
            >
              Abuse Reports (1)
            </button>
            <button
              onClick={() => setActiveTab('audit')}
              className={`py-4 px-4 font-bold text-sm border-b-2 ${
                activeTab === 'audit' ? 'border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-gray-400'
              }`}
            >
              Audit Trail
            </button>
          </div>

          <div className="p-6">
            {activeTab === 'pending' && (
              <div className="space-y-4">
                {pendingItems.map(item => (
                  <div key={item.id} className="p-5 bg-[#131F3F] border border-[#192850] rounded-lg flex items-center justify-between">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs font-bold text-[#00E5FF] uppercase tracking-wider">{item.type}</span>
                        <span className="text-gray-500">•</span>
                        <span className="text-xs text-gray-400">{item.category}</span>
                      </div>
                      <h3 className="text-base font-bold text-white mt-1">{item.title}</h3>
                      <p className="text-xs text-gray-300 mt-1">Business: {item.businessName} | URL: <a href={item.url} target="_blank" rel="noreferrer" className="text-[#00E5FF] underline">{item.url}</a></p>
                    </div>
                    <div className="flex space-x-3">
                      <button
                        onClick={() => handleApprove(item.id)}
                        className="px-4 py-2 bg-[#00E676] text-black font-bold text-xs rounded-lg hover:bg-[#00c853] transition"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => handleReject(item.id)}
                        className="px-4 py-2 bg-[#FF3860] text-white font-bold text-xs rounded-lg hover:bg-red-600 transition"
                      >
                        Reject
                      </button>
                    </div>
                  </div>
                ))}
                {pendingItems.length === 0 && (
                  <p className="text-center py-10 text-gray-400">All submissions have been reviewed.</p>
                )}
              </div>
            )}

            {activeTab === 'reports' && (
              <div className="p-4 bg-[#131F3F] border border-[#192850] rounded-lg">
                <p className="text-sm font-bold text-[#FF3860]">Flagged: Flexible Hot Desks in Koramangala</p>
                <p className="text-xs text-gray-300 mt-1">Reason: Outdated contact phone number.</p>
                <div className="mt-3 flex space-x-3">
                  <button className="px-3 py-1.5 bg-gray-700 text-xs font-semibold rounded">Dismiss</button>
                  <button className="px-3 py-1.5 bg-[#FF3860] text-xs font-semibold rounded text-white">Suspend Listing</button>
                </div>
              </div>
            )}

            {activeTab === 'audit' && (
              <div className="space-y-2 text-xs text-gray-300">
                <p>[2026-10-13 14:00] MODERATION_APPROVE by moderator@codelligence.tech on listing_02</p>
                <p>[2026-10-13 12:00] SYSTEM_INITIALIZE by admin@codelligence.tech</p>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
