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
  const html = plans.map(p => `<div style="margin: 10px 0; padding: 10px; background: white; border-radius: 5px; border-left: 4px solid #667eea;">
    <strong>${p.name}</strong><br>
    📅 Monthly: $${p.monthlyFee} | 📊 Usage: $${p.ratePerMb}/MB
  </div>`).join('');
  document.getElementById('plansDisplay').innerHTML = html || '<p>No plans available</p>';
}

function updatePlanSelects() {
  const select = document.getElementById('assignPlanId');
  select.innerHTML = '<option value="">-- Select a Plan --</option>' + plans.map(p => `<option value="${p.id}">${p.name} - $${p.monthlyFee}/month</option>`).join('');
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
    showMessage('regMsg', `✅ Customer registered!\n\nID: ${data.id}\n📧 Welcome email sent to ${email}`, 'success');
    document.getElementById('regName').value = '';
    document.getElementById('regEmail').value = '';
    document.getElementById('regPhone').value = '';
    loadCustomers();
  } catch (e) {
    showMessage('regMsg', 'Error registering customer: ' + e.message, 'error');
  }
}

async function assignPlan() {
  const customerId = document.getElementById('assignCustId').value;
  const planId = document.getElementById('assignPlanId').value;
  
  if (!customerId || !planId) {
    showMessage('assignMsg', 'Please select customer and plan', 'error');
    return;
  }

  try {
    const r = await fetch('/api/assign', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), planId: parseInt(planId) })
    });
    await r.json();
    showMessage('assignMsg', '✓ Plan assigned successfully', 'success');
    document.getElementById('assignCustId').value = '';
    document.getElementById('assignPlanId').value = '';
    loadCustomers();
  } catch (e) {
    showMessage('assignMsg', 'Error assigning plan: ' + e.message, 'error');
  }
}

async function logUsage() {
  const customerId = document.getElementById('usageCustId').value;
  const deviceName = document.getElementById('usageDevice').value.trim();
  const mbUsed = document.getElementById('usageMb').value;
  
  if (!customerId || !deviceName || !mbUsed) {
    showMessage('usageMsg', 'Please fill all fields', 'error');
    return;
  }

  try {
    const r = await fetch('/api/usage', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), deviceName, mbUsed: parseFloat(mbUsed) })
    });
    await r.json();
    showMessage('usageMsg', `✓ Logged ${mbUsed} MB for ${deviceName}`, 'success');
    document.getElementById('usageCustId').value = '';
    document.getElementById('usageDevice').value = '';
    document.getElementById('usageMb').value = '';
  } catch (e) {
    showMessage('usageMsg', 'Error logging usage: ' + e.message, 'error');
  }
}

async function raiseTicket() {
  const customerId = document.getElementById('ticketCustId').value;
  const title = document.getElementById('ticketTitle').value.trim();
  const description = document.getElementById('ticketDesc').value.trim();
  
  if (!customerId || !title || !description) {
    showMessage('ticketMsg', 'Please fill all fields', 'error');
    return;
  }

  try {
    const r = await fetch('/api/tickets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), title, description })
    });
    await r.json();
    showMessage('ticketMsg', '✓ Ticket created successfully', 'success');
    document.getElementById('ticketCustId').value = '';
    document.getElementById('ticketTitle').value = '';
    document.getElementById('ticketDesc').value = '';
  } catch (e) {
    showMessage('ticketMsg', 'Error creating ticket: ' + e.message, 'error');
  }
}

async function generateBill() {
  const customerId = document.getElementById('billCustId').value;
  
  if (!customerId) {
    showMessage('billMsg', 'Please enter customer ID', 'error');
    return;
  }

  try {
    const r = await fetch(`/api/bill/${customerId}`);
    const data = await r.json();
    currentBill = data.bill;
    showMessage('billMsg', `💵 Total Amount Due: $${data.bill.toFixed(2)}`, 'info');
    document.getElementById('payBtn').style.display = 'block';
  } catch (e) {
    showMessage('billMsg', 'Error generating bill: ' + e.message, 'error');
  }
}

async function processPayment() {
  const customerId = document.getElementById('billCustId').value;
  
  if (!customerId || currentBill === 0) {
    showMessage('billMsg', 'Please generate bill first', 'error');
    return;
  }

  try {
    const r = await fetch('/api/payment', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerId: parseInt(customerId), amount: currentBill })
    });
    const data = await r.json();
    showMessage('billMsg', `✅ ${data.message}\n\n📧 Receipt has been sent to your email!\n\n💳 Payment of $${currentBill.toFixed(2)} processed successfully!`, 'success');
    document.getElementById('payBtn').style.display = 'none';
    document.getElementById('billCustId').value = '';
    currentBill = 0;
  } catch (e) {
    showMessage('billMsg', 'Payment failed: ' + e.message, 'error');
  }

async function viewCustomer() {
  const customerId = document.getElementById('viewCustId').value;
  
  if (!customerId) {
    showMessage('viewMsg', 'Please enter customer ID', 'error');
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

    let html = `<strong>${customer.name}</strong><br>
      Email: ${customer.email}<br>
      Phone: ${customer.phone}<br>
      Plan ID: ${customer.planId || 'No plan assigned'}<br>`;
    
    const usageR = await fetch(`/api/usage/${customerId}`);
    const usage = await usageR.json();
    if (usage && usage.length > 0) {
      html += `<br><strong>Recent Usage:</strong><br>`;
      usage.slice(-5).forEach(u => {
        html += `${u.deviceName}: ${u.mbUsed}MB on ${u.dateTime}<br>`;
      });
    }
    
    document.getElementById('viewMsg').innerHTML = html;
  } catch (e) {
    showMessage('viewMsg', 'Error loading customer: ' + e.message, 'error');
  }
}

async function loadCustomers() {
  try {
    const r = await fetch('/api/customers');
    const customers = await r.json();
    document.getElementById('totalCustomers').innerText = customers.length;

    if (customers.length === 0) {
      document.getElementById('customersList').innerHTML = '<p style="color: #999;">No customers registered yet</p>';
      return;
    }

    let html = '<table><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Plan</th></tr>';
    customers.forEach(c => {
      const plan = plans.find(p => p.id === c.planId);
      html += `<tr>
        <td>${c.id}</td>
        <td>${c.name}</td>
        <td>${c.email}</td>
        <td>${c.phone}</td>
        <td>${plan ? plan.name : 'None'}</td>
      </tr>`;
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
  
  // Refresh customer list every 10 seconds
  setInterval(loadCustomers, 10000);
});
    document.getElementById('billResult').innerText = JSON.stringify(data,null,2);
  });

  window.loadCustomers = async function(){
    const r = await fetch('/api/customers');
    const data = await r.json();
    document.getElementById('customers').innerText = JSON.stringify(data,null,2);
  }

  loadCustomers();
});
