// GiftVault Pro V7.0 PC Suite — Core State & Controller

const NETWORK_TIERS = {
  RUPAY: ["Select", "Platinum", "Classic", "EKAA"],
  VISA: ["Infinite", "Signature", "Platinum", "Gold", "Classic"],
  MASTERCARD: ["World Elite", "World", "Platinum / Titanium", "Classic"],
  AMEX: ["Platinum", "Select", "Reward", "Classic"],
  DINERS: ["Black", "Club", "Select", "Classic"]
};

const BRAND_CATEGORIES = {
  "myntra": "Fashion",
  "ajio": "Fashion",
  "trends": "Fashion",
  "zara": "Fashion",
  "amazon prime": "OTT & Streaming",
  "prime": "OTT & Streaming",
  "hotstar": "OTT & Streaming",
  "jiohotstar": "OTT & Streaming",
  "netflix": "OTT & Streaming",
  "apollo": "Pharmacy",
  "apollo pharmacy": "Pharmacy",
  "netmeds": "Pharmacy",
  "1mg": "Pharmacy",
  "blinkit": "Groceries",
  "zepto": "Groceries",
  "bigbasket": "Groceries",
  "swiggy": "Food Delivery",
  "zomato": "Food Delivery",
  "dominos": "Food Delivery",
  "polar bear": "Food Delivery",
  "croma": "Electronics",
  "reliance digital": "Electronics",
  "kalyan": "Jewellery",
  "kalyan jewellers": "Jewellery",
  "lakme": "Spa & Salon",
  "cult": "Fitness",
  "cult.fit": "Fitness",
  "bookmyshow": "Entertainment",
  "google play": "Entertainment",
  "playstore": "Entertainment",
  "gplay": "Entertainment",
  "district": "Food & Dining",
  "uber": "Travel",
  "gaana": "Entertainment",
  "makemytrip": "Travel",
  "decathlon": "Sports",
  "amazon pay": "Shopping",
  "flipkart": "Shopping",
  "hp pay": "Payments"
};

// Initial state from localStorage or defaults
let state = {
  bankCards: JSON.parse(localStorage.getItem('gv_bank_cards')) || [
    {
      id: "card_1",
      cardNickname: "PNB Select Imperial",
      bankName: "PNB",
      network: "RUPAY",
      tier: "Select",
      cardNumber: "6071520012345678",
      cvv: "891",
      expiryMonth: "09",
      expiryYear: "28",
      cardholderName: "Asthik Sharma",
      isPrimary: true,
      quarterlySpendTarget: 50000,
      currentQuarterSpend: 18500
    }
  ],
  vouchers: JSON.parse(localStorage.getItem('gv_vouchers')) || [
    {
      id: "vouch_1",
      merchantName: "Apollo Pharmacy",
      category: "Pharmacy",
      cardNumber: "APOLLO987654321012",
      pin: "4321",
      balance: 1000,
      initialValue: 1000,
      expiryDate: "2026-12-31",
      linkedCardId: "card_1"
    },
    {
      id: "vouch_2",
      merchantName: "Myntra",
      category: "Fashion",
      cardNumber: "MYNT889977665544",
      pin: "9012",
      balance: 1500,
      initialValue: 1500,
      expiryDate: "2026-11-15",
      linkedCardId: "card_1"
    }
  ],
  claims: JSON.parse(localStorage.getItem('gv_claims')) || [
    {
      id: "claim_1",
      orderId: "TRZ-2026-90412",
      merchantName: "Apollo Pharmacy",
      amount: 1000,
      bankCardId: "card_1",
      status: "RESOLVED",
      notes: "Auto-ingested and verified."
    }
  ],
  revealedCards: new Set()
};

function saveState() {
  localStorage.setItem('gv_bank_cards', JSON.stringify(state.bankCards));
  localStorage.setItem('gv_vouchers', JSON.stringify(state.vouchers));
  localStorage.setItem('gv_claims', JSON.stringify(state.claims));
  renderAll();
}

function renderAll() {
  renderDashboard();
  renderBankCards();
  renderVouchersView();
  renderClaimsView();
  renderRuPayView();
  populateCardLinkDropdown();
  if (window.lucide) lucide.createIcons();
}

function switchTab(tabId) {
  const views = ['dashboard', 'cards', 'vouchers', 'rupay', 'claims'];
  views.forEach(v => {
    const el = document.getElementById(`view-${v}`);
    const btn = document.getElementById(`tab-btn-${v}`);
    if (v === tabId) {
      el.classList.remove('hidden');
      btn.className = "tab-btn px-4 py-2 rounded-lg text-xs font-semibold flex items-center space-x-2 transition-all bg-indigo-600 text-white shadow-md";
    } else {
      el.classList.add('hidden');
      btn.className = "tab-btn px-4 py-2 rounded-lg text-xs font-semibold flex items-center space-x-2 transition-all text-slate-400 hover:text-white";
    }
  });
  if (window.lucide) lucide.createIcons();
}

// ── Dashboard Renderer ──
function renderDashboard() {
  const totalBalance = state.vouchers
    .filter(v => v.category !== 'OTT & Streaming')
    .reduce((acc, v) => acc + (parseFloat(v.balance) || 0), 0);
  document.getElementById('stat-total-balance').textContent = `₹${totalBalance.toLocaleString('en-IN')}`;
  document.getElementById('stat-card-count').innerHTML = `<span>${state.vouchers.length} Active Vouchers (excluding OTT from balance)</span>`;

  document.getElementById('stat-bank-cards-count').textContent = state.bankCards.length;
  const primaryCard = state.bankCards.find(c => c.isPrimary) || state.bankCards[0];
  document.getElementById('stat-primary-card-name').textContent = primaryCard ? `Primary: ${primaryCard.cardNickname}` : "Primary: None Set";

  const totalQuarterSpend = state.bankCards.reduce((acc, c) => acc + (parseFloat(c.currentQuarterSpend) || 0), 0);
  const totalQuarterTarget = state.bankCards.reduce((acc, c) => acc + (parseFloat(c.quarterlySpendTarget) || 50000), 0);
  document.getElementById('stat-quarterly-spend').textContent = `₹${totalQuarterSpend.toLocaleString('en-IN')}`;
  const spendPct = totalQuarterTarget > 0 ? Math.min(100, Math.round((totalQuarterSpend / totalQuarterTarget) * 100)) : 0;
  document.getElementById('stat-spend-bar').style.width = `${spendPct}%`;
  document.getElementById('stat-spend-target-text').textContent = `Target: ₹${totalQuarterTarget.toLocaleString('en-IN')} (${spendPct}%)`;

  // Expiring count (next 30 days)
  const now = new Date();
  const in30Days = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);
  const expiring = state.vouchers.filter(v => {
    if (!v.expiryDate) return false;
    const d = new Date(v.expiryDate);
    return d >= now && d <= in30Days;
  });
  document.getElementById('stat-expiring-count').textContent = expiring.length;

  // Bank carousel in Dashboard
  const carouselEl = document.getElementById('dashboard-bank-carousel');
  if (state.bankCards.length === 0) {
    carouselEl.innerHTML = `<div class="col-span-3 p-6 rounded-2xl bg-slate-900 border border-slate-800 text-center text-xs text-slate-500">No bank cards added yet. Click "+ Add Card" above.</div>`;
  } else {
    carouselEl.innerHTML = state.bankCards.map(c => `
      <div class="p-4 rounded-2xl bg-gradient-to-br from-slate-900 via-slate-900 to-indigo-950/40 border ${c.isPrimary ? 'border-indigo-500/50' : 'border-slate-800'} shadow-lg space-y-3">
        <div class="flex justify-between items-start">
          <div class="flex items-center space-x-2">
            <span class="px-2 py-0.5 rounded-md bg-indigo-500 text-[10px] font-black text-white">${c.bankName}</span>
            <span class="text-xs font-bold text-white">${c.cardNickname}</span>
          </div>
          <span class="text-[10px] font-black text-amber-400 uppercase">${c.network} ${c.tier}</span>
        </div>
        <p class="font-mono text-sm tracking-wider text-slate-300">•••• •••• •••• ${c.cardNumber ? c.cardNumber.slice(-4) : '----'}</p>
        <div class="flex justify-between items-center text-[10px] text-slate-400 pt-2 border-t border-slate-800">
          <span>Spend: ₹${c.currentQuarterSpend || 0} / ₹${c.quarterlySpendTarget || 50000}</span>
          <span class="font-bold text-indigo-400">${c.quarterlySpendTarget > 0 ? Math.round(((c.currentQuarterSpend || 0) / c.quarterlySpendTarget) * 100) : 0}%</span>
        </div>
      </div>
    `).join('');
  }

  // Dashboard Recent Vouchers
  const vouchListEl = document.getElementById('dashboard-vouchers-list');
  if (state.vouchers.length === 0) {
    vouchListEl.innerHTML = `<div class="col-span-2 p-6 rounded-2xl bg-slate-900 border border-slate-800 text-center text-xs text-slate-500">No active gift vouchers. Click "+ Add Voucher" to store a voucher.</div>`;
  } else {
    vouchListEl.innerHTML = state.vouchers.slice(0, 4).map(v => `
      <div class="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex justify-between items-center hover:border-slate-700 transition">
        <div class="space-y-1">
          <div class="flex items-center space-x-2">
            <span class="text-xs font-bold text-white">${v.merchantName}</span>
            <span class="px-2 py-0.5 rounded-full bg-slate-800 text-[10px] font-semibold text-slate-400">${v.category || 'Others'}</span>
          </div>
          <p class="font-mono text-xs text-slate-500 tracking-wider">•••• ${v.cardNumber.slice(-4)}</p>
        </div>
        <div class="text-right space-y-1">
          <p class="text-sm font-black text-indigo-400">₹${v.balance}</p>
          <button onclick="openCashierModal('${v.id}')" class="px-2.5 py-1 rounded-lg bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-400 text-[10px] font-bold border border-indigo-500/20 transition">
            Cashier Mode
          </button>
        </div>
      </div>
    `).join('');
  }

  // Category Breakdown
  const catContainer = document.getElementById('category-breakdown-container');
  const catGroups = {};
  state.vouchers.forEach(v => {
    const cat = v.category || 'Others';
    catGroups[cat] = (catGroups[cat] || 0) + (parseFloat(v.balance) || 0);
  });
  const catEntries = Object.entries(catGroups).sort((a, b) => b[1] - a[1]);
  if (catEntries.length === 0) {
    catContainer.innerHTML = `<p class="text-xs text-slate-500 text-center">No category data yet.</p>`;
  } else {
    catContainer.innerHTML = catEntries.map(([cat, bal]) => {
      const pct = totalBalance > 0 ? Math.round((bal / totalBalance) * 100) : 0;
      return `
        <div class="space-y-1">
          <div class="flex justify-between text-xs">
            <span class="font-semibold text-slate-300">${cat}</span>
            <span class="font-bold text-white">₹${bal.toLocaleString('en-IN')} (${pct}%)</span>
          </div>
          <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div class="bg-indigo-500 h-full rounded-full" style="width: ${pct}%"></div>
          </div>
        </div>
      `;
    }).join('');
  }
}

// ── Bank Cards Management ──
function renderBankCards() {
  const container = document.getElementById('bank-cards-grid');
  if (state.bankCards.length === 0) {
    container.innerHTML = `<div class="col-span-3 p-12 text-center text-slate-500">No bank cards registered. Add your first card above!</div>`;
    return;
  }

  container.innerHTML = state.bankCards.map(c => {
    const isRevealed = state.revealedCards.has(c.id);
    const displayNum = isRevealed ? (c.cardNumber || '').replace(/(\d{4})/g, '$1 ').trim() : `•••• •••• •••• ${(c.cardNumber || '').slice(-4)}`;
    const displayExpiry = isRevealed ? `${c.expiryMonth || '••'}/${c.expiryYear || '••'}` : `••/••`;
    const displayCvv = isRevealed ? (c.cvv || '•••') : `•••`;
    const progress = c.quarterlySpendTarget > 0 ? Math.min(100, Math.round(((c.currentQuarterSpend || 0) / c.quarterlySpendTarget) * 100)) : 0;

    return `
      <div class="rounded-3xl bg-slate-900 border ${c.isPrimary ? 'border-indigo-500/60' : 'border-slate-800'} p-6 shadow-xl space-y-4 relative overflow-hidden flex flex-col justify-between">
        <!-- Top row -->
        <div class="flex justify-between items-start">
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center font-black text-xs text-white shadow-md">
              ${c.bankName.slice(0, 3).toUpperCase()}
            </div>
            <div>
              <div class="flex items-center space-x-2">
                <h4 class="font-bold text-white">${c.cardNickname}</h4>
                ${c.isPrimary ? '<span class="px-2 py-0.5 text-[9px] font-black bg-indigo-500/20 text-indigo-400 rounded-md border border-indigo-500/30">PRIMARY</span>' : ''}
              </div>
              <p class="text-xs text-amber-400 font-semibold">${c.network} ${c.tier}</p>
            </div>
          </div>

          <div class="flex items-center space-x-1">
            <button onclick="toggleCardReveal('${c.id}')" title="Reveal Details" class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white">
              <i data-lucide="${isRevealed ? 'eye-off' : 'eye'}" class="w-4 h-4"></i>
            </button>
            <button onclick="editCard('${c.id}')" title="Edit" class="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white">
              <i data-lucide="edit-3" class="w-4 h-4"></i>
            </button>
            <button onclick="deleteCard('${c.id}')" title="Delete" class="p-1.5 rounded-lg hover:bg-slate-800 text-rose-400 hover:text-rose-300">
              <i data-lucide="trash-2" class="w-4 h-4"></i>
            </button>
          </div>
        </div>

        <!-- Card Face Visual -->
        <div class="p-4 rounded-2xl bg-gradient-to-tr from-slate-950 via-slate-900 to-indigo-950 border border-slate-800/80 space-y-3 shadow-inner">
          <div class="flex justify-between items-center">
            <p class="font-mono text-sm tracking-widest text-white font-bold">${displayNum}</p>
            <button onclick="navigator.clipboard.writeText('${c.cardNumber}'); alert('Card number copied!')" class="text-slate-400 hover:text-white">
              <i data-lucide="copy" class="w-3.5 h-3.5"></i>
            </button>
          </div>

          <div class="flex justify-between items-end text-xs">
            <div>
              <p class="text-[9px] uppercase tracking-wider text-slate-500">Cardholder</p>
              <p class="font-bold text-slate-200 uppercase">${c.cardholderName || 'VALUED CARDHOLDER'}</p>
            </div>
            <div class="flex space-x-3 text-right">
              <div>
                <p class="text-[9px] uppercase tracking-wider text-slate-500">Expires</p>
                <p class="font-mono font-bold text-white">${displayExpiry}</p>
              </div>
              <div>
                <p class="text-[9px] uppercase tracking-wider text-amber-500">CVV</p>
                <p class="font-mono font-bold text-amber-400">${displayCvv}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Quarterly Milestone -->
        <div class="space-y-1 pt-1">
          <div class="flex justify-between text-[11px]">
            <span class="text-slate-400">Quarterly Target: ₹${c.currentQuarterSpend || 0} / ₹${c.quarterlySpendTarget || 50000}</span>
            <span class="font-bold text-indigo-400">${progress}%</span>
          </div>
          <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div class="bg-indigo-500 h-full rounded-full" style="width: ${progress}%"></div>
          </div>
        </div>

        ${!c.isPrimary ? `
          <button onclick="setPrimaryCard('${c.id}')" class="w-full py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-indigo-400 text-xs font-bold transition">
            Set as Primary Profile
          </button>
        ` : ''}
      </div>
    `;
  }).join('');
}

// ── Vouchers View ──
function renderVouchersView() {
  const container = document.getElementById('vouchers-grid');
  const search = (document.getElementById('voucher-search-input')?.value || '').toLowerCase();
  const catFilter = document.getElementById('voucher-category-filter')?.value || 'ALL';

  const filtered = state.vouchers.filter(v => {
    const matchSearch = v.merchantName.toLowerCase().includes(search) || v.cardNumber.toLowerCase().includes(search);
    const matchCat = catFilter === 'ALL' || v.category === catFilter;
    return matchSearch && matchCat;
  });

  if (filtered.length === 0) {
    container.innerHTML = `<div class="col-span-3 p-12 text-center text-slate-500">No vouchers match your search criteria.</div>`;
    return;
  }

  container.innerHTML = filtered.map(v => {
    const linkedCard = state.bankCards.find(c => c.id === v.linkedCardId);
    return `
      <div class="p-5 rounded-3xl bg-slate-900 border border-slate-800 shadow-xl space-y-4 hover:border-slate-700 transition flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <div class="flex items-center space-x-2">
              <h4 class="font-black text-white text-base">${v.merchantName}</h4>
              <span class="px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 text-[10px] font-bold">${v.category || 'Others'}</span>
            </div>
            ${linkedCard ? `<p class="text-[10px] text-amber-400 font-semibold mt-0.5">Linked: ${linkedCard.cardNickname}</p>` : ''}
          </div>
          <span class="text-xl font-black text-indigo-400">₹${v.balance}</span>
        </div>

        <!-- Voucher Code Box -->
        <div class="p-3.5 rounded-2xl bg-slate-950 border border-slate-800 space-y-1.5 font-mono">
          <div class="flex justify-between items-center text-xs">
            <span class="text-slate-400">Code:</span>
            <span class="font-bold text-white tracking-wider">${v.cardNumber}</span>
          </div>
          ${v.pin ? `
            <div class="flex justify-between items-center text-xs pt-1 border-t border-slate-800/80">
              <span class="text-slate-400">PIN:</span>
              <span class="font-bold text-amber-400">${v.pin}</span>
            </div>
          ` : ''}
          ${v.expiryDate ? `
            <div class="flex justify-between items-center text-[10px] pt-1 text-slate-500">
              <span>Expiry:</span>
              <span>${v.expiryDate}</span>
            </div>
          ` : ''}
        </div>

        <div class="grid grid-cols-2 gap-2 pt-1">
          <button onclick="openCashierModal('${v.id}')" class="py-2 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold flex items-center justify-center space-x-1.5 shadow-lg shadow-indigo-600/20 transition">
            <i data-lucide="store" class="w-3.5 h-3.5"></i>
            <span>Cashier Mode</span>
          </button>
          <button onclick="deleteVoucher('${v.id}')" class="py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-bold flex items-center justify-center space-x-1.5 transition">
            <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
            <span>Delete</span>
          </button>
        </div>
      </div>
    `;
  }).join('');
  if (window.lucide) lucide.createIcons();
}

// ── RuPay Auto-Redeem Center ──
function renderRuPayView() {
  const cardBox = document.getElementById('rupay-active-card-box');
  const activeCard = state.bankCards.find(c => c.network === 'RUPAY') || state.bankCards[0];

  if (!activeCard) {
    cardBox.innerHTML = `<p class="text-xs text-slate-500">No RuPay card found. Add a RuPay Select card to enable 1-tap autofill.</p>`;
    return;
  }

  cardBox.innerHTML = `
    <div class="p-4 rounded-2xl bg-gradient-to-br from-indigo-900 to-slate-950 border border-indigo-500/40 space-y-2">
      <div class="flex justify-between items-center">
        <span class="text-xs font-bold text-white">${activeCard.cardNickname}</span>
        <span class="text-[10px] font-black text-amber-400">${activeCard.tier}</span>
      </div>
      <p class="font-mono text-sm tracking-wider text-slate-200">${activeCard.cardNumber}</p>
      <div class="flex justify-between text-xs text-slate-400">
        <span>Exp: ${activeCard.expiryMonth}/${activeCard.expiryYear}</span>
        <span>CVV: ${activeCard.cvv}</span>
      </div>
    </div>
  `;

  document.getElementById('rupay-btn-lastfour').textContent = `•••• ${activeCard.cardNumber.slice(-4)}`;
  document.getElementById('rupay-btn-expiry').textContent = `${activeCard.expiryMonth}/${activeCard.expiryYear}`;
  document.getElementById('rupay-btn-cvv').textContent = activeCard.cvv;
}

function copyActiveCardField(field) {
  const activeCard = state.bankCards.find(c => c.network === 'RUPAY') || state.bankCards[0];
  if (!activeCard) return;

  if (field === 'number') {
    navigator.clipboard.writeText(activeCard.cardNumber);
    alert('RuPay Card Number copied to clipboard!');
  } else if (field === 'expiry') {
    navigator.clipboard.writeText(`${activeCard.expiryMonth}/${activeCard.expiryYear}`);
    alert('Expiry copied to clipboard!');
  } else if (field === 'cvv') {
    navigator.clipboard.writeText(activeCard.cvv);
    alert('CVV copied to clipboard!');
  }
}

function parseAndIngestText() {
  const text = document.getElementById('rupay-paste-input').value.trim();
  if (!text) {
    alert('Please paste text first!');
    return;
  }

  // Regex extraction for merchant, 16-char code, PIN, and amount
  let merchant = "RuPay Voucher";
  for (const [kw, cat] of Object.entries(BRAND_CATEGORIES)) {
    if (text.toLowerCase().includes(kw)) {
      merchant = kw.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
      break;
    }
  }

  const codeMatch = text.match(/\b([A-Z0-9]{10,20})\b/);
  const pinMatch = text.match(/(?:pin|passcode|code)[\s:]*([0-9]{3,8})/i);
  const amountMatch = text.match(/(?:rs\.?|inr|₹)[\s]*([0-9]+)/i);

  const newVoucher = {
    id: `vouch_${Date.now()}`,
    merchantName: merchant,
    category: BRAND_CATEGORIES[merchant.toLowerCase()] || "Others",
    cardNumber: codeMatch ? codeMatch[1] : `RUPAY-${Date.now().toString().slice(-8)}`,
    pin: pinMatch ? pinMatch[1] : "",
    balance: amountMatch ? parseFloat(amountMatch[1]) : 500,
    initialValue: amountMatch ? parseFloat(amountMatch[1]) : 500,
    expiryDate: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    linkedCardId: state.bankCards[0]?.id || null
  };

  state.vouchers.unshift(newVoucher);
  saveState();
  document.getElementById('rupay-paste-input').value = "";
  alert(`Successfully auto-ingested ₹${newVoucher.balance} ${newVoucher.merchantName} voucher into Vault!`);
  switchTab('vouchers');
}

// ── Claims / Disputes ──
function renderClaimsView() {
  const container = document.getElementById('claims-table-container');
  if (state.claims.length === 0) {
    container.innerHTML = `<div class="p-8 text-center text-xs text-slate-500">No logged claims or disputes.</div>`;
    return;
  }

  container.innerHTML = `
    <table class="w-full text-left text-xs text-slate-300">
      <thead class="bg-slate-950 text-slate-400 uppercase text-[10px] font-bold border-b border-slate-800">
        <tr>
          <th class="p-3.5">Order ID</th>
          <th class="p-3.5">Merchant</th>
          <th class="p-3.5">Amount</th>
          <th class="p-3.5">Status</th>
          <th class="p-3.5 text-right">Action</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-800">
        ${state.claims.map(c => `
          <tr class="hover:bg-slate-800/40 transition">
            <td class="p-3.5 font-mono font-bold text-white">${c.orderId}</td>
            <td class="p-3.5 font-semibold">${c.merchantName}</td>
            <td class="p-3.5 font-bold text-indigo-400">₹${c.amount}</td>
            <td class="p-3.5">
              <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${c.status === 'RESOLVED' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-rose-500/20 text-rose-400 border border-rose-500/30'}">
                ${c.status}
              </span>
            </td>
            <td class="p-3.5 text-right">
              <button onclick="generateEscalationEmail('${c.id}')" class="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-[10px]">
                Escalate via Email
              </button>
            </td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

function generateEscalationEmail(claimId) {
  const claim = state.claims.find(c => c.id === claimId);
  if (!claim) return;
  const subject = encodeURIComponent(`Escalation: Truztee Voucher Issue [Order #${claim.orderId}]`);
  const body = encodeURIComponent(`Dear RuPay / Truztee Support Team,\n\nI am writing to escalate an unresolved issue with my RuPay Select voucher redemption.\n\nOrder ID: ${claim.orderId}\nMerchant: ${claim.merchantName}\nAmount: ₹${claim.amount}\nIssue Details: ${claim.notes || 'Voucher code not received or showing invalid at register.'}\n\nPlease verify and release the voucher or refund at the earliest.\n\nThank you.`);
  window.open(`mailto:rupay@truztee.com?subject=${subject}&body=${body}`, '_blank');
}

// ── Modals & Actions ──
function openCashierModal(voucherId) {
  const v = state.vouchers.find(x => x.id === voucherId);
  if (!v) return;

  document.getElementById('cashier-merchant').textContent = v.merchantName;
  document.getElementById('cashier-code-display').textContent = (v.cardNumber || '').replace(/(.{4})/g, '$1 ').trim();
  document.getElementById('cashier-pin-display').textContent = v.pin || 'N/A';
  document.getElementById('cashier-balance-display').textContent = `₹${v.balance}`;

  // Render Barcode
  try {
    JsBarcode("#cashier-barcode-svg", v.cardNumber, {
      format: "CODE128",
      lineColor: "#000000",
      width: 2,
      height: 70,
      displayValue: false
    });
  } catch (e) {
    console.error("Barcode error:", e);
  }

  document.getElementById('cashier-modal').classList.remove('hidden');
}

function closeCashierModal() {
  document.getElementById('cashier-modal').classList.add('hidden');
}

function copyCashierCode() {
  const text = document.getElementById('cashier-code-display').textContent.replace(/\s+/g, '');
  navigator.clipboard.writeText(text);
  alert('Voucher Code copied to clipboard!');
}

function openAddCardModal() {
  document.getElementById('card-form-id').value = "";
  document.getElementById('card-form').reset();
  document.getElementById('card-modal-title').textContent = "Add Bank Card Details";
  updateTierOptions();
  document.getElementById('card-modal').classList.remove('hidden');
}

function closeCardModal() {
  document.getElementById('card-modal').classList.add('hidden');
}

function updateTierOptions() {
  const network = document.getElementById('card-form-network').value;
  const tierSelect = document.getElementById('card-form-tier');
  const tiers = NETWORK_TIERS[network] || ["Classic", "Platinum", "Select"];
  tierSelect.innerHTML = tiers.map(t => `<option value="${t}">${t}</option>`).join('');
}

function saveBankCard(e) {
  e.preventDefault();
  const id = document.getElementById('card-form-id').value || `card_${Date.now()}`;
  const nickname = document.getElementById('card-form-nickname').value;
  const bank = document.getElementById('card-form-bank').value;
  const network = document.getElementById('card-form-network').value;
  const tier = document.getElementById('card-form-tier').value;
  const number = document.getElementById('card-form-number').value.replace(/\s+/g, '');
  const month = document.getElementById('card-form-month').value;
  const year = document.getElementById('card-form-year').value;
  const cvv = document.getElementById('card-form-cvv').value;
  const holder = document.getElementById('card-form-holder').value;
  const target = parseFloat(document.getElementById('card-form-target').value) || 50000;
  const spend = parseFloat(document.getElementById('card-form-spend').value) || 0;
  const isPrimary = document.getElementById('card-form-primary').checked;

  if (isPrimary) {
    state.bankCards.forEach(c => c.isPrimary = false);
  }

  const existingIdx = state.bankCards.findIndex(c => c.id === id);
  const cardObj = {
    id,
    cardNickname: nickname,
    bankName: bank,
    network,
    tier,
    cardNumber: number,
    cvv,
    expiryMonth: month,
    expiryYear: year,
    cardholderName: holder,
    isPrimary,
    quarterlySpendTarget: target,
    currentQuarterSpend: spend
  };

  if (existingIdx >= 0) {
    state.bankCards[existingIdx] = cardObj;
  } else {
    state.bankCards.push(cardObj);
  }

  saveState();
  closeCardModal();
}

function toggleCardReveal(cardId) {
  if (state.revealedCards.has(cardId)) {
    state.revealedCards.delete(cardId);
  } else {
    state.revealedCards.add(cardId);
  }
  renderBankCards();
  if (window.lucide) lucide.createIcons();
}

function deleteCard(cardId) {
  if (confirm("Are you sure you want to delete this bank card profile?")) {
    state.bankCards = state.bankCards.filter(c => c.id !== cardId);
    saveState();
  }
}

function setPrimaryCard(cardId) {
  state.bankCards.forEach(c => c.isPrimary = (c.id === cardId));
  saveState();
}

function openAddVoucherModal() {
  document.getElementById('voucher-form-id').value = "";
  document.getElementById('voucher-form').reset();
  document.getElementById('voucher-modal-title').textContent = "Add Gift Voucher";
  populateCardLinkDropdown();
  document.getElementById('voucher-modal').classList.remove('hidden');
}

function closeVoucherModal() {
  document.getElementById('voucher-modal').classList.add('hidden');
}

function populateCardLinkDropdown() {
  const select = document.getElementById('voucher-form-cardlink');
  if (!select) return;
  select.innerHTML = '<option value="">None (Standalone)</option>' +
    state.bankCards.map(c => `<option value="${c.id}">${c.cardNickname} (${c.bankName})</option>`).join('');
}

function autoMapCategory() {
  const merchant = document.getElementById('voucher-form-merchant').value.toLowerCase();
  for (const [kw, cat] of Object.entries(BRAND_CATEGORIES)) {
    if (merchant.includes(kw)) {
      document.getElementById('voucher-form-category').value = cat;
      break;
    }
  }
}

function saveVoucher(e) {
  e.preventDefault();
  const id = document.getElementById('voucher-form-id').value || `vouch_${Date.now()}`;
  const merchant = document.getElementById('voucher-form-merchant').value;
  const category = document.getElementById('voucher-form-category').value;
  const cardlink = document.getElementById('voucher-form-cardlink').value;
  const number = document.getElementById('voucher-form-number').value.trim();
  const pin = document.getElementById('voucher-form-pin').value.trim();
  const balance = parseFloat(document.getElementById('voucher-form-balance').value) || 0;
  const expiry = document.getElementById('voucher-form-expiry').value;

  const vouchObj = {
    id,
    merchantName: merchant,
    category,
    cardNumber: number,
    pin,
    balance,
    initialValue: balance,
    expiryDate: expiry,
    linkedCardId: cardlink || null
  };

  state.vouchers.unshift(vouchObj);
  saveState();
  closeVoucherModal();
}

function deleteVoucher(voucherId) {
  if (confirm("Delete this voucher?")) {
    state.vouchers = state.vouchers.filter(v => v.id !== voucherId);
    saveState();
  }
}

function exportBackup() {
  const data = JSON.stringify(state, null, 2);
  const blob = new Blob([data], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `GiftVault_Backup_${new Date().toISOString().split('T')[0]}.giftvault`;
  a.click();
  URL.revokeObjectURL(url);
}

// Initialize on Load
document.addEventListener("DOMContentLoaded", () => {
  renderAll();
});
