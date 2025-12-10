let plans = [];
let currentBill = 0;

async function fetchPlans() {
  try {
    const r = await fetch('/api/plans');
    plans = await r.json();
    updatePlansDisplay();
    updatePlanSelects();
  } catch (e) {
    console.error('Error loading plans:', e);
  }
}

function updatePlansDisplay() {
  const html = plans.map(p => `<div style="margin: 10px 0; padding: 10px; background: white; border-radius: 5px; border-left: 4px solid #667eea;"><strong>${p.name}</strong><br>Monthly: $${p.monthlyFee} | Usage: $${p.ratePerMb}/MB</div>`).join('');
  document.getElementById('plansDisplay').innerHTML = html || '<p>No plans</p>';
}

function updatePlanSelects() {
  const select = document.getElementById('assignPlanId');
  select.innerHTML = '<option value="">-- Select a Plan --</option>' + plans.map(p => `<option value="${p.id}">${p.name} - $${p.monthlyFee}</option>`).join('');
}

async function registerCustomer() {
  const name = document.getElementById('regName').value.trim();
  const email = document.getElementById('regEmail').value.trim();
  const phone = document.getElementById('regPhone').value.trim();
  
  if (!name || !email || !phone) {
    showMessage('regMsg', 'Please fill all fields', 'error');
    return;
  }

  try {
    const r = await fetch('/api/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, phone })
    });
    const data = await r.json();
    showMessage('regMsg', `Success! Customer ID: ${data.id}\nEmail sent to ${email}`, 'success');
    document.getElementById('regName').value = '';
    document.getElementById('regEmail').value = '';
    document.getElementById('regPhone').value = '';
    loadCustomers();
  } catch (e) {
    showMessage('regMsg', 'Error: ' + e.message, 'error');
  }
}

async function assignPlan() {
  const customerId = document.getElementById('assignCustId').value;
  const planId = document.getElementById('assignPlanId').value;
  
  if (!customerId || !planId) {
    showMessage('assignMsg', 'Select customer and plan', 'error');
    return;
  }

  try {
    const r = await fetch('/api/assign', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), planId: parseInt(planId) })
    });
    await r.json();
    showMessage('assignMsg', 'Plan assigned!', 'success');
    document.getElementById('assignCustId').value = '';
    document.getElementById('assignPlanId').value = '';
    loadCustomers();
  } catch (e) {
    showMessage('assignMsg', 'Error: ' + e.message, 'error');
  }
}

async function logUsage() {
  const customerId = document.getElementById('usageCustId').value;
  const deviceName = document.getElementById('usageDevice').value.trim();
  const mbUsed = document.getElementById('usageMb').value;
  
  if (!customerId || !deviceName || !mbUsed) {
    showMessage('usageMsg', 'Fill all fields', 'error');
    return;
  }

  try {
    const r = await fetch('/api/usage', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), deviceName, mbUsed: parseFloat(mbUsed) })
    });
    await r.json();
    showMessage('usageMsg', `Logged ${mbUsed}MB for ${deviceName}`, 'success');
    document.getElementById('usageCustId').value = '';
    document.getElementById('usageDevice').value = '';
    document.getElementById('usageMb').value = '';
  } catch (e) {
    showMessage('usageMsg', 'Error: ' + e.message, 'error');
  }
}

async function raiseTicket() {
  const customerId = document.getElementById('ticketCustId').value;
  const title = document.getElementById('ticketTitle').value.trim();
  const description = document.getElementById('ticketDesc').value.trim();
  
  if (!customerId || !title || !description) {
    showMessage('ticketMsg', 'Fill all fields', 'error');
    return;
  }

  try {
    const r = await fetch('/api/tickets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), title, description })
    });
    await r.json();
    showMessage('ticketMsg', 'Ticket created!', 'success');
    document.getElementById('ticketCustId').value = '';
    document.getElementById('ticketTitle').value = '';
    document.getElementById('ticketDesc').value = '';
  } catch (e) {
    showMessage('ticketMsg', 'Error: ' + e.message, 'error');
  }
}

async function generateBill() {
  const customerId = document.getElementById('billCustId').value;
  
  if (!customerId) {
    showMessage('billMsg', 'Enter customer ID', 'error');
    return;
  }

  try {
    const r = await fetch(`/api/bill/${customerId}`);
    const data = await r.json();
    currentBill = data.bill;
    showMessage('billMsg', `Amount Due: $${data.bill.toFixed(2)}`, 'info');
    document.getElementById('payBtn').style.display = 'block';
  } catch (e) {
    showMessage('billMsg', 'Error: ' + e.message, 'error');
  }
}

async function processPayment() {
  const customerId = document.getElementById('billCustId').value;
  
  if (!customerId || currentBill === 0) {
    showMessage('billMsg', 'Generate bill first', 'error');
    return;
  }

  try {
    // Call Stripe Checkout API
    const r = await fetch('/api/checkout', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId) })
    });
    const data = await r.json();
    
    if (data.sessionId && !data.sessionId.startsWith('demo-')) {
      // Real Stripe session - redirect to Stripe Checkout
      const stripe = Stripe('pk_test_51234567890123456789012345');  // Placeholder - will be handled by server
      stripe.redirectToCheckout({ sessionId: data.sessionId }).catch(e => {
        showMessage('billMsg', 'Error: ' + e.message, 'error');
      });
    } else {
      // Demo mode - simulate payment
      showMessage('billMsg', `Demo Payment Processed!\nAmount: $${currentBill.toFixed(2)}\n(Set STRIPE_SECRET_KEY to use real Stripe)`, 'success');
      document.getElementById('payBtn').style.display = 'none';
      document.getElementById('billCustId').value = '';
      currentBill = 0;
    }
  } catch (e) {
    showMessage('billMsg', 'Payment error: ' + e.message, 'error');
  }
}

async function viewCustomer() {
  const customerId = document.getElementById('viewCustId').value;
  
  if (!customerId) {
    showMessage('viewMsg', 'Enter customer ID', 'error');
    return;
  }

  try {
    const r = await fetch(`/api/customers`);
    const customers = await r.json();
    const customer = customers.find(c => c.id == customerId);
    
    if (!customer) {
      showMessage('viewMsg', 'Customer not found', 'error');
      return;
    }

    let html = `Name: ${customer.name}<br>Email: ${customer.email}<br>Phone: ${customer.phone}<br>Plan: ${customer.planId || 'None'}`;
    
    try {
      const usageR = await fetch(`/api/usage/${customerId}`);
      const usage = await usageR.json();
      if (usage && usage.length > 0) {
        html += `<br><br>Recent Usage:<br>`;
        usage.slice(-3).forEach(u => {
          html += `${u.deviceName}: ${u.mbUsed}MB<br>`;
        });
      }
    } catch (e) {}
    
    document.getElementById('viewMsg').innerHTML = html;
  } catch (e) {
    showMessage('viewMsg', 'Error: ' + e.message, 'error');
  }
}

async function loadCustomers() {
  try {
    const r = await fetch('/api/customers');
    const customers = await r.json();
    document.getElementById('totalCustomers').innerText = customers.length;

    if (customers.length === 0) {
      document.getElementById('customersList').innerHTML = '<p>No customers</p>';
      return;
    }

    let html = '<table><tr><th>ID</th><th>Name</th><th>Email</th><th>Plan</th></tr>';
    customers.forEach(c => {
      const plan = plans.find(p => p.id === c.planId);
      html += `<tr><td>${c.id}</td><td>${c.name}</td><td>${c.email}</td><td>${plan ? plan.name : 'None'}</td></tr>`;
    });
    html += '</table>';
    document.getElementById('customersList').innerHTML = html;
  } catch (e) {
    console.error('Error loading customers:', e);
  }
}

function showMessage(elementId, message, type) {
  const elem = document.getElementById(elementId);
  elem.className = `message ${type}`;
  elem.innerText = message;
  setTimeout(() => {
    elem.innerText = '';
    elem.className = '';
  }, 5000);
}

document.addEventListener('DOMContentLoaded', async () => {
  await fetchPlans();
  await loadCustomers();
  setInterval(loadCustomers, 10000);
});
