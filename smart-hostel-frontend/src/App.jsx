import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Activity, AlertCircle, ArrowDownRight, ArrowRight, ArrowUpRight, BedDouble,
  Bell, Building2, CalendarDays, Check, CheckCircle2, ChevronDown, ClipboardList,
  Clock3, CreditCard, DoorOpen, Download, FileText, Home, LogOut, Menu, MessageSquare,
  Plus, RefreshCw, Search, Settings2, ShieldCheck, Sparkles, Utensils, Users,
  UserRound, UserRoundCheck, Wallet, Wrench, X, Zap,
} from 'lucide-react';
import { api, get, post, put } from './api';

const DEMO_ACCOUNTS = [
  { username: 'warden1', role: 'WARDEN', label: 'Warden' },
  { username: 'student1', role: 'STUDENT', label: 'Student' },
  { username: 'staff1', role: 'MAINTENANCE_STAFF', label: 'Maintenance staff' },
  { username: 'acc1', role: 'ACCOUNTANT', label: 'Accountant' },
];
const today = () => { const d = new Date(); const offset = d.getTimezoneOffset(); return new Date(d.getTime() - offset * 60000).toISOString().slice(0, 10); };
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(value || 0));
const nice = (value = '') => String(value).replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
const dateText = (value) => value ? new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
const timeText = (value) => value ? new Date(value).toLocaleString('en-IN', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '—';
const roleName = (role) => ({ WARDEN: 'Warden', STUDENT: 'Student', MAINTENANCE_STAFF: 'Maintenance staff', ACCOUNTANT: 'Accountant' }[role] || nice(role));
const initials = (name = 'Hostel User') => name.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase();

function statusClass(value = '') {
  const text = String(value).toLowerCase();
  if (['paid', 'resolved', 'verified', 'active', 'in_progress'].includes(text)) return 'success';
  if (['critical', 'emergency', 'overdue'].includes(text)) return 'danger';
  if (['assigned', 'partial', 'high', 'pending'].includes(text)) return 'warning';
  return 'neutral';
}

function Status({ value }) {
  return <span className={`status status-${statusClass(value)}`}><i />{nice(value || 'Unknown')}</span>;
}

function IconBox({ icon: Icon, tone = 'blue' }) {
  return <span className={`icon-box tone-${tone}`}><Icon size={19} strokeWidth={2.1} /></span>;
}

function StatCard({ label, value, note, icon: Icon, tone = 'blue', trend }) {
  return <article className="stat-card">
    <div className="stat-top"><span>{label}</span><IconBox icon={Icon} tone={tone} /></div>
    <div className="stat-value">{value}</div>
    <div className="stat-foot">{trend && <span className="trend"><ArrowUpRight size={13} />{trend}</span>}<span>{note}</span></div>
  </article>;
}

function EmptyState({ icon: Icon = ClipboardList, title = 'Nothing to show yet', detail = 'Records will appear here when they are added.' }) {
  return <div className="empty-state"><span><Icon size={25} /></span><strong>{title}</strong><p>{detail}</p></div>;
}

function PageTitle({ eyebrow, title, subtitle, action }) {
  return <div className="page-title-row"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{subtitle}</p></div>{action}</div>;
}

function Panel({ title, subtitle, action, children, className = '' }) {
  return <section className={`panel ${className}`}><div className="panel-head"><div><h3>{title}</h3>{subtitle && <p>{subtitle}</p>}</div>{action}</div>{children}</section>;
}

function DataTable({ columns, rows, emptyTitle, emptyDetail, rowKey = 'id' }) {
  if (!rows?.length) return <EmptyState title={emptyTitle} detail={emptyDetail} />;
  return <div className="table-wrap"><table><thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}</tr></thead><tbody>
    {rows.map((row, index) => <tr key={row[rowKey] ?? index}>{columns.map((column) => <td key={column.key}>{column.render ? column.render(row) : (row[column.key] ?? '—')}</td>)}</tr>)}
  </tbody></table></div>;
}

function Modal({ config, onClose, busy }) {
  const [values, setValues] = useState(() => Object.fromEntries(config.fields.map((field) => [field.name, field.defaultValue ?? ''])));
  const [formError, setFormError] = useState('');
  const update = (name, value) => setValues((current) => ({ ...current, [name]: value }));
  const submit = async (event) => {
    event.preventDefault(); setFormError('');
    try { await config.onSubmit(values); } catch (error) { setFormError(error.message || 'Unable to complete this action.'); }
  };
  return <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
    <div className="modal-card"><div className="modal-head"><div><span className="eyebrow">HOSTEL MANAGEMENT</span><h2>{config.title}</h2></div><button className="icon-button" onClick={onClose} aria-label="Close"><X size={19} /></button></div>
      <form onSubmit={submit} className="modal-form">
        {config.fields.map((field) => <label className={`field ${field.wide ? 'field-wide' : ''}`} key={field.name}><span>{field.label}{field.required !== false && <b> *</b>}</span>
          {field.type === 'select' ? <select required={field.required !== false} value={values[field.name]} onChange={(event) => update(field.name, event.target.value)}><option value="">Select {field.label.toLowerCase()}</option>{field.options?.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select>
            : field.type === 'textarea' ? <textarea required={field.required !== false} rows={field.rows || 4} placeholder={field.placeholder} value={values[field.name]} onChange={(event) => update(field.name, event.target.value)} />
              : <input required={field.required !== false} type={field.type || 'text'} min={field.min} max={field.max} step={field.step} placeholder={field.placeholder} value={values[field.name]} onChange={(event) => update(field.name, event.target.value)} />}
        </label>)}
        {formError && <div className="form-error"><AlertCircle size={16} />{formError}</div>}
        <div className="modal-actions"><button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button><button className="btn btn-primary" type="submit" disabled={busy}>{busy ? 'Saving…' : (config.submitLabel || 'Save changes')}<ArrowRight size={16} /></button></div>
      </form>
    </div>
  </div>;
}

const NAV = {
  WARDEN: [
    { id: 'overview', label: 'Overview', icon: Home, group: 'WORKSPACE' },
    { id: 'rooms', label: 'Room management', icon: BedDouble },
    { id: 'complaints', label: 'Complaints', icon: MessageSquare },
    { id: 'visitors', label: 'Visitors', icon: UserRoundCheck },
    { id: 'students', label: 'Students', icon: Users, group: 'PEOPLE' },
    { id: 'staff', label: 'Staff directory', icon: Wrench },
    { id: 'notices', label: 'Notice board', icon: FileText, group: 'HOSTEL' },
    { id: 'mess', label: 'Mess & dining', icon: Utensils },
  ],
  STUDENT: [
    { id: 'overview', label: 'My overview', icon: Home, group: 'MY HOSTEL' },
    { id: 'rooms', label: 'My room', icon: BedDouble },
    { id: 'complaints', label: 'My complaints', icon: MessageSquare },
    { id: 'visitors', label: 'My visitors', icon: UserRoundCheck },
    { id: 'fees', label: 'My fees', icon: Wallet, group: 'SERVICES' },
    { id: 'notices', label: 'Notice board', icon: FileText },
    { id: 'mess', label: 'Mess & dining', icon: Utensils },
    { id: 'notifications', label: 'Notifications', icon: Bell },
  ],
  MAINTENANCE_STAFF: [
    { id: 'overview', label: 'My overview', icon: Home, group: 'WORKSPACE' },
    { id: 'complaints', label: 'Assigned tasks', icon: Wrench },
    { id: 'notifications', label: 'Notifications', icon: Bell },
  ],
  ACCOUNTANT: [
    { id: 'overview', label: 'Overview', icon: Home, group: 'WORKSPACE' },
    { id: 'fees', label: 'Fee management', icon: Wallet },
  ],
};

function App() {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem('haven-user') || 'null'); } catch { return null; }
  });
  const [page, setPage] = useState('overview');
  const [data, setData] = useState({});
  const [loading, setLoading] = useState(false);
  const [loginBusy, setLoginBusy] = useState(false);
  const [actionBusy, setActionBusy] = useState(false);
  const [toast, setToast] = useState(null);
  const [modal, setModal] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [search, setSearch] = useState('');
  const [mobileNav, setMobileNav] = useState(false);

  const notify = useCallback((message, type = 'success') => {
    setToast({ message, type });
    window.clearTimeout(notify.timer);
    notify.timer = window.setTimeout(() => setToast(null), 3800);
  }, []);

  const fetchResources = useCallback(async (currentUser) => {
    const role = currentUser.role;
    const jobs = {};
    const add = (key, path) => { jobs[key] = get(path, currentUser); };
    if (role === 'WARDEN') {
      add('rooms', '/rooms'); add('allocations', '/rooms/allocations'); add('complaints', '/complaints');
      add('visitors', '/visitors'); add('notices', '/notices/all'); add('students', '/users/students');
      add('staff', '/users/staff'); add('menu', `/mess/menu?date=${today()}`); add('feedback', '/mess/feedback');
    } else if (role === 'STUDENT') {
      add('room', '/rooms/my'); add('complaints', '/complaints/my'); add('visitors', '/visitors/my');
      add('fees', '/fees/my'); add('notices', '/notices'); add('menu', `/mess/menu?date=${today()}`);
      add('notifications', '/notifications'); add('preferences', '/mess/preferences/my');
    } else if (role === 'MAINTENANCE_STAFF') {
      add('complaints', '/complaints/assigned'); add('notifications', '/notifications');
    } else if (role === 'ACCOUNTANT') {
      add('fees', '/fees');
    }
    const keys = Object.keys(jobs);
    const results = await Promise.allSettled(Object.values(jobs));
    const next = {};
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') next[keys[index]] = result.value;
      else next[`${keys[index]}Error`] = result.reason?.message || 'Could not load this section';
    });
    return next;
  }, []);

  useEffect(() => {
    if (!user) return;
    let alive = true;
    setLoading(true);
    fetchResources(user).then((next) => {
      if (alive) setData(next);
    }).finally(() => { if (alive) setLoading(false); });
    return () => { alive = false; };
  }, [user, refreshKey, fetchResources]);

  const refresh = () => setRefreshKey((current) => current + 1);
  const perform = async (callback, successMessage) => {
    setActionBusy(true);
    try {
      await callback();
      setModal(null);
      notify(successMessage || 'Changes saved successfully.');
      refresh();
    } catch (error) {
      notify(error.message || 'Something went wrong.', 'error');
      throw error;
    } finally { setActionBusy(false); }
  };

  const login = async (username, password) => {
    setLoginBusy(true);
    try {
      const result = await post('/auth/login', { username, password });
      const nextUser = { ...result, username };
      sessionStorage.setItem('haven-user', JSON.stringify(nextUser));
      setUser(nextUser); setPage('overview'); setData({});
    } catch (error) { notify(error.message || 'Login failed. Check your credentials.', 'error'); }
    finally { setLoginBusy(false); }
  };

  const register = async (values) => {
    await post('/auth/register', values);
    notify(`Account created. A welcome email has been sent to ${values.email}`);
  };

  const payOnline = async (fee) => {
    try {
      if (!window.Razorpay) {
        await new Promise((resolve, reject) => {
          const script = document.createElement('script');
          script.src = 'https://checkout.razorpay.com/v1/checkout.js';
          script.onload = resolve; script.onerror = () => reject(new Error('Unable to load Razorpay Checkout.'));
          document.body.appendChild(script);
        });
      }
      const order = await post(`/fees/${fee.id}/online/order`, {}, user);
      const checkout = new window.Razorpay({
        key: order.keyId, amount: order.amount, currency: order.currency, name: 'Sunrise Hostel',
        description: fee.description, order_id: order.orderId,
        prefill: { name: user.fullName }, theme: { color: '#315f87' },
        handler: async (response) => {
          try {
            const result = await post(`/fees/${fee.id}/online/verify`, {
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature,
            }, user);
            notify('Online payment successful.'); refresh();
            if (result.paymentId) {
              const receipt = await get(`/fees/payments/${result.paymentId}/receipt`, user);
              setModal({ title: 'Payment receipt', receipt });
            }
          } catch (error) { notify(error.message || 'Payment verification failed.', 'error'); }
        },
        modal: { ondismiss: () => notify('Payment was cancelled.', 'error') },
      });
      checkout.on('payment.failed', (response) => notify(response.error?.description || 'Payment failed.', 'error'));
      checkout.open();
    } catch (error) { notify(error.message || 'Unable to start online payment.', 'error'); }
  };

  const logout = () => {
    sessionStorage.removeItem('haven-user'); setUser(null); setData({}); setPage('overview'); setModal(null);
  };

  const navItems = NAV[user?.role] || [];
  const currentTitle = navItems.find((item) => item.id === page)?.label || 'Overview';
  const filteredNav = navItems.filter((item) => item.label.toLowerCase().includes(search.toLowerCase()));
  const openModal = (title, fields, onSubmit, submitLabel) => setModal({ title, fields, onSubmit, submitLabel });

  const students = data.students || [];
  const staff = data.staff || [];
  const rooms = data.rooms || [];
  const complaints = data.complaints || [];
  const notices = data.notices || [];
  const visitors = data.visitors || [];
  const fees = data.fees || [];
  const menu = data.menu || [];

  const openComplaint = () => openModal('Raise a complaint', [
    { name: 'category', label: 'Category', type: 'select', options: ['ELECTRICAL', 'PLUMBING', 'CLEANING', 'INTERNET'].map((x) => ({ value: x, label: nice(x) })) },
    { name: 'description', label: 'Describe the issue', type: 'textarea', wide: true, placeholder: 'Tell us what happened and where…' },
    { name: 'strategy', label: 'Priority calculation', type: 'select', required: false, defaultValue: 'CATEGORY', options: [{ value: 'CATEGORY', label: 'Based on category' }, { value: 'KEYWORD', label: 'Based on description keywords' }] },
  ], (values) => perform(() => post('/complaints', values, user), 'Complaint submitted successfully.'), 'Submit complaint');

  const openAllocation = () => openModal('Allocate a room', [
    { name: 'studentId', label: 'Student', type: 'select', options: students.map((s) => ({ value: s.id, label: `${s.fullName} · ${s.username}` })) },
    { name: 'roomId', label: 'Available room', type: 'select', options: rooms.filter((r) => Number(r.availableBeds) > 0).map((r) => ({ value: r.id, label: `Room ${r.roomNumber} · ${r.availableBeds} bed(s) free` })) },
  ], (values) => perform(() => post('/rooms/allocate', { studentId: Number(values.studentId), roomId: Number(values.roomId) }, user), 'Room allocated successfully.'), 'Allocate room');

  const openChangeRoom = (studentId) => openModal('Change student room', [
    { name: 'studentId', label: 'Student ID', type: 'number', defaultValue: studentId },
    { name: 'newRoomId', label: 'New room', type: 'select', options: rooms.filter((r) => Number(r.availableBeds) > 0).map((r) => ({ value: r.id, label: `Room ${r.roomNumber} · ${r.availableBeds} bed(s) free` })) },
  ], (values) => perform(() => put('/rooms/change', { studentId: Number(values.studentId), newRoomId: Number(values.newRoomId) }, user), 'Room changed successfully.'), 'Change room');

  const openNotice = () => openModal('Publish a notice', [
    { name: 'title', label: 'Notice title', placeholder: 'e.g. Water supply maintenance' },
    { name: 'category', label: 'Category', type: 'select', options: ['GENERAL', 'EXAM', 'MAINTENANCE', 'EVENT', 'EMERGENCY'].map((x) => ({ value: x, label: nice(x) })) },
    { name: 'expiryDate', label: 'Expires on', type: 'date', required: false, defaultValue: '' },
    { name: 'content', label: 'Message', type: 'textarea', wide: true, placeholder: 'Write the details for residents…' },
  ], (values) => perform(() => post('/notices', { ...values, expiryDate: values.expiryDate || null }, user), 'Notice published.'), 'Publish notice');

  const openVisitor = () => openModal('Register a visitor', [
    { name: 'studentId', label: 'Visiting student', type: 'select', options: students.map((s) => ({ value: s.id, label: `${s.fullName} · ${s.username}` })) },
    { name: 'visitorName', label: 'Visitor name' },
    { name: 'visitorPhone', label: 'Phone number', type: 'tel' },
    { name: 'purpose', label: 'Purpose of visit', type: 'textarea', wide: true },
  ], (values) => perform(() => post('/visitors', { ...values, studentId: Number(values.studentId) }, user), 'Visitor entry recorded.'), 'Register visitor');

  const openFee = () => openModal('Create a fee record', [
    { name: 'studentId', label: 'Student ID', type: 'number', placeholder: 'Enter the student ID' },
    { name: 'description', label: 'Fee description', placeholder: 'e.g. Semester hostel fee' },
    { name: 'totalAmount', label: 'Total amount (₹)', type: 'number', min: '1', step: '0.01' },
    { name: 'dueDate', label: 'Due date', type: 'date' },
  ], (values) => perform(() => post('/fees', { ...values, studentId: Number(values.studentId), totalAmount: Number(values.totalAmount) }, user), 'Fee record created.'), 'Create fee');

  const openPayment = (fee) => openModal(`Record payment · Fee #${fee.id}`, [
    { name: 'amount', label: `Amount (pending ${money(fee.pendingAmount)})`, type: 'number', min: '1', step: '0.01', max: fee.pendingAmount },
    { name: 'method', label: 'Payment method', type: 'select', options: ['UPI', 'CASH', 'CARD', 'BANK_TRANSFER'].map((x) => ({ value: x, label: nice(x) })) },
  ], (values) => perform(() => post(`/fees/${fee.id}/payments`, { amount: Number(values.amount), method: values.method }, user), 'Payment recorded.'), 'Record payment');

  const openMenu = () => openModal('Add mess menu', [
    { name: 'menuDate', label: 'Menu date', type: 'date', defaultValue: today() },
    { name: 'mealType', label: 'Meal', type: 'select', options: ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'].map((x) => ({ value: x, label: nice(x) })) },
    { name: 'items', label: 'Menu items', type: 'textarea', wide: true, placeholder: 'e.g. Dal, rice, roti and seasonal vegetables' },
  ], (values) => perform(() => post('/mess/menu', values, user), 'Mess menu added.'), 'Add menu');

  const openFeedback = (menuItem) => openModal('Share mess feedback', [
    { name: 'menuId', label: 'Meal', type: 'select', options: menu.map((item) => ({ value: item.id, label: `${nice(item.mealType)} · ${item.items}` })) , defaultValue: menuItem?.id ? String(menuItem.id) : '' },
    { name: 'rating', label: 'Rating (1–5)', type: 'select', options: [1, 2, 3, 4, 5].map((x) => ({ value: x, label: `${x} / 5` })) },
    { name: 'comment', label: 'Comment', type: 'textarea', wide: true, required: false, placeholder: 'What did you like or what could improve?' },
  ], (values) => perform(() => post('/mess/feedback', { menuId: Number(values.menuId), rating: Number(values.rating), comment: values.comment }, user), 'Thanks for sharing your feedback.'), 'Send feedback');

  const openPreference = () => openModal('Set meal preference', [
    { name: 'mealType', label: 'Meal', type: 'select', options: ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'].map((x) => ({ value: x, label: nice(x) })) },
    { name: 'preference', label: 'Preference', type: 'select', options: ['VEG', 'NON_VEG', 'JAIN'].map((x) => ({ value: x, label: nice(x) })) },
  ], (values) => perform(() => put('/mess/preferences', values, user), 'Meal preference updated.'), 'Save preference');

  const studentOptions = students.map((s) => ({ value: s.id, label: `${s.fullName} · ${s.username}` }));

  if (!user) return <LoginScreen onLogin={login} onRegister={register} busy={loginBusy} />;

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
      <div className="brand"><div className="brand-mark"><Building2 size={20} /></div><div><strong>haven<span>.</span></strong><small>SMART HOSTEL</small></div><button className="sidebar-close icon-button" onClick={() => setMobileNav(false)}><X size={18} /></button></div>
      <div className="hostel-switch"><div className="hostel-avatar">SH</div><div><strong>Sunrise Hostel</strong><small>Prayagraj, India</small></div><ChevronDown size={15} /></div>
      <div className="side-search"><Search size={15} /><input placeholder="Find a page…" value={search} onChange={(e) => setSearch(e.target.value)} /></div>
      <nav className="side-nav">{filteredNav.map((item, index) => <React.Fragment key={item.id}>{item.group && <div className="nav-group">{item.group}</div>}<button className={`nav-link ${page === item.id ? 'active' : ''}`} onClick={() => { setPage(item.id); setMobileNav(false); }}><item.icon size={18} strokeWidth={1.9} /><span>{item.label}</span>{item.id === 'notifications' && data.notifications?.length > 0 && <b className="nav-count">{data.notifications.length}</b>}</button></React.Fragment>)}</nav>
      <div className="sidebar-bottom"><div className="help-card"><div className="help-bubble"><Sparkles size={16} /></div><strong>Need a hand?</strong><p>Check the project guide for help with your hostel workspace.</p><button onClick={() => notify('Run the backend first, then use the frontend quick-start guide in README.', 'info')}>View quick guide <ArrowRight size={13} /></button></div><div className="sidebar-user"><div className="avatar avatar-small">{initials(user.fullName)}</div><div className="sidebar-user-text"><strong>{user.fullName}</strong><small>{roleName(user.role)}</small></div><button className="icon-button logout-button" onClick={logout} title="Sign out"><LogOut size={17} /></button></div></div>
    </aside>

    <main className="main-area">
      <header className="topbar"><div className="topbar-left"><button className="mobile-menu icon-button" onClick={() => setMobileNav(true)}><Menu size={20} /></button><div className="breadcrumbs"><span>Workspace</span><b>/</b><strong>{currentTitle}</strong></div></div><div className="topbar-right"><div className="date-chip"><CalendarDays size={15} />{new Date().toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' })}</div><button className="icon-button notification-button" onClick={() => setPage('notifications')} title="Notifications"><Bell size={18} />{data.notifications?.length > 0 && <i />}</button><div className="top-user"><div className="avatar">{initials(user.fullName)}</div><div><strong>{user.fullName}</strong><small>{roleName(user.role)}</small></div><ChevronDown size={14} /></div></div></header>
      <div className="content-area">
        {page === 'overview' && <Overview user={user} data={data} loading={loading} onNavigate={setPage} onRefresh={refresh} onPrimary={user.role === 'STUDENT' ? openComplaint : user.role === 'WARDEN' ? openAllocation : user.role === 'ACCOUNTANT' ? openFee : () => setPage('complaints')} />}
        {page === 'rooms' && <RoomsPage user={user} data={data} loading={loading} onAllocate={openAllocation} onChangeRoom={openChangeRoom} onRefresh={refresh} />}
        {page === 'complaints' && <ComplaintsPage user={user} data={data} loading={loading} onNew={openComplaint} onAction={(path, body, message) => perform(() => put(path, body, user), message)} onAssign={(complaint) => openModal(`Assign complaint #${complaint.id}`, [{ name: 'staffId', label: 'Maintenance staff', type: 'select', options: staff.map((s) => ({ value: s.id, label: `${s.fullName} · ${nice(s.specialization || '')}` })) }], (values) => perform(() => put(`/complaints/${complaint.id}/assign`, { staffId: Number(values.staffId) }, user), 'Complaint assigned.'), 'Assign task')} />}
        {page === 'visitors' && <VisitorsPage user={user} data={data} loading={loading} onNew={openVisitor} onAction={(path, message) => perform(() => put(path, {}, user), message)} />}
        {page === 'students' && <StudentsPage data={data} loading={loading} />}
        {page === 'staff' && <StaffPage data={data} loading={loading} />}
        {page === 'fees' && <FeesPage user={user} data={data} loading={loading} onNew={openFee} onPay={openPayment} onOnlinePay={payOnline} onReceipt={async (paymentId) => { try { const receipt = await get(`/fees/payments/${paymentId}/receipt`, user); setModal({ title: 'Payment receipt', receipt }); } catch (error) { notify(error.message, 'error'); } }} />}
        {page === 'notices' && <NoticesPage user={user} data={data} loading={loading} onNew={openNotice} />}
        {page === 'mess' && <MessPage user={user} data={data} loading={loading} onNew={openMenu} onFeedback={openFeedback} onPreference={openPreference} />}
        {page === 'notifications' && <NotificationsPage data={data} loading={loading} />}
        <footer className="page-footer"><span>© 2026 Haven · Smart Hostel Management</span><span><span className="live-dot" /> Backend service</span></footer>
      </div>
    </main>
    {mobileNav && <div className="mobile-scrim" onClick={() => setMobileNav(false)} />}
    {toast && <div className={`toast toast-${toast.type}`}><span>{toast.type === 'error' ? <AlertCircle size={17} /> : <CheckCircle2 size={17} />}</span>{toast.message}<button onClick={() => setToast(null)}><X size={15} /></button></div>}
    {modal && (modal.receipt ? <ReceiptModal receipt={modal.receipt} onClose={() => setModal(null)} /> : <Modal config={modal} onClose={() => setModal(null)} busy={actionBusy} />)}
    <button className="floating-refresh" onClick={refresh} title="Refresh data"><RefreshCw size={17} className={loading ? 'spin' : ''} /></button>
  </div>;
}

function LoginScreen({ onLogin, onRegister, busy }) {
  const [registering, setRegistering] = useState(false);
  const [registerBusy, setRegisterBusy] = useState(false);
  const [registerError, setRegisterError] = useState('');
  const [registerSuccess, setRegisterSuccess] = useState('');
  const [registration, setRegistration] = useState({ fullName: '', email: '', username: '', password: '', phone: '', rollNumber: '', course: '' });
  const [username, setUsername] = useState('warden1');
  const [password, setPassword] = useState('pass');
  const [showPass, setShowPass] = useState(false);
  return <div className="login-screen"><section className="login-visual"><div className="login-brand"><div className="brand-mark"><Building2 size={20} /></div><div><strong>haven<span>.</span></strong><small>SMART HOSTEL</small></div></div><div className="visual-copy"><span className="visual-kicker"><Sparkles size={14} /> A BETTER WAY TO LIVE ON CAMPUS</span><h1>Your hostel,<br /><em>in harmony.</em></h1><p>One thoughtful place for rooms, requests, visitors and everyday campus life.</p><div className="visual-stat-row"><div><strong>6</strong><small>Connected modules</small></div><span /><div><strong>4</strong><small>Access roles</small></div><span /><div><strong>24/7</strong><small>Hostel visibility</small></div></div></div><div className="art-board"><div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" /><div className="art-building"><div className="building-roof" /><div className="building-body"><div className="building-window-grid">{Array.from({ length: 12 }).map((_, i) => <i key={i} />)}</div><div className="building-door" /></div><div className="building-base" /></div><div className="art-leaf leaf-one" /><div className="art-leaf leaf-two" /><div className="art-plant plant-left"><i /><i /><i /><b /></div><div className="art-plant plant-right"><i /><i /><i /><b /></div><div className="art-cloud cloud-one" /><div className="art-cloud cloud-two" /></div><div className="visual-bottom"><span>DESIGNED FOR CAMPUS COMMUNITIES</span><span>PRAYAGRAJ · INDIA</span></div></section>
    <section className="login-panel"><div className="login-mobile-brand"><div className="brand-mark"><Building2 size={20} /></div><div><strong>haven<span>.</span></strong><small>SMART HOSTEL</small></div></div><div className="login-content"><div className="login-welcome"><span className="eyebrow">WELCOME BACK</span><h2>Sign in to your<br />hostel workspace.</h2><p>Enter your account details to continue.</p></div><form className="login-form" onSubmit={(e) => { e.preventDefault(); onLogin(username, password); }}><label className="field"><span>Username</span><div className="input-icon"><UserRound size={17} /><input autoComplete="username" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Enter your username" required /></div></label><label className="field"><span>Password</span><div className="input-icon"><ShieldCheck size={17} /><input autoComplete="current-password" type={showPass ? 'text' : 'password'} value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Enter your password" required /><button type="button" onClick={() => setShowPass(!showPass)}>{showPass ? 'Hide' : 'Show'}</button></div></label><button className="btn btn-primary login-submit" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}<ArrowRight size={17} /></button></form><button type="button" className="text-action" onClick={() => { setRegistering(!registering); setRegisterError(''); setRegisterSuccess(''); }}>Create student account</button>{registering && <form className="login-form" onSubmit={async (event) => { event.preventDefault(); setRegisterBusy(true); setRegisterError(''); setRegisterSuccess(''); try { await onRegister(registration); setRegisterSuccess(`Account created. A welcome email has been sent to ${registration.email}`); setRegistering(false); setRegistration({ fullName: '', email: '', username: '', password: '', phone: '', rollNumber: '', course: '' }); } catch (error) { setRegisterError(error.message); } finally { setRegisterBusy(false); } }}>{[['fullName','Full name','text'],['email','Email','email'],['username','Username','text'],['password','Password','password'],['phone','Phone','tel'],['rollNumber','Roll number','text'],['course','Course','text']].map(([key,label,type]) => <label className="field" key={key}><span>{label}</span><input required type={type} value={registration[key]} onChange={(event) => setRegistration((old) => ({ ...old, [key]: event.target.value }))} /></label>)}{registerError && <div className="form-error">{registerError}</div>}<button className="btn btn-primary login-submit" disabled={registerBusy}>{registerBusy ? 'Creating account…' : 'Register'}<ArrowRight size={17} /></button></form>}{registerSuccess && <div className="toast toast-success">{registerSuccess}</div>}<div className="demo-box"><div className="demo-title"><span>DEMO ACCESS</span><span className="demo-ready"><i /> Ready to explore</span></div><p>Use one of the project demo accounts (password: <b>pass</b>).</p><div className="demo-users">{DEMO_ACCOUNTS.map((account) => <button key={account.username} className={username === account.username ? 'selected' : ''} onClick={() => { setUsername(account.username); setPassword('pass'); }}><span className={`demo-avatar demo-${account.role.toLowerCase()}`}>{account.label[0]}</span><span>{account.label}</span></button>)}</div></div><div className="login-foot"><span><ShieldCheck size={14} /> Local demo environment</span><span>Need help? See README</span></div></div></section></div>;
}

function Overview({ user, data, loading, onNavigate, onRefresh, onPrimary }) {
  const role = user.role;
  const greeting = new Date().getHours() < 12 ? 'Good morning' : new Date().getHours() < 17 ? 'Good afternoon' : 'Good evening';
  if (role === 'WARDEN') {
    const totalBeds = (data.rooms || []).reduce((sum, room) => sum + Number(room.capacity || 0), 0);
    const occupied = (data.rooms || []).reduce((sum, room) => sum + Number(room.occupied || 0), 0);
    const activeComplaints = (data.complaints || []).filter((item) => item.status !== 'RESOLVED').length;
    const pendingVisitors = (data.visitors || []).filter((item) => !item.checkOut).length;
    return <><PageTitle eyebrow="WARDEN WORKSPACE" title={`${greeting}, ${user.fullName.split(' ')[0]}`} subtitle="Here’s what’s happening across Sunrise Hostel today." action={<button className="btn btn-light" onClick={onRefresh}><RefreshCw size={15} /> Refresh data</button>} />
      <div className="welcome-banner"><div><span className="banner-label"><Sparkles size={13} /> HOSTEL PULSE</span><h2>Your hostel is looking good.</h2><p>Keep an eye on room availability, open maintenance requests and visitor activity.</p><button onClick={() => onNavigate('rooms')}>Explore rooms <ArrowRight size={15} /></button></div><div className="banner-illustration"><div className="banner-sun" /><Building2 size={112} strokeWidth={1.1} /><span className="banner-plant">✳</span></div></div>
      <div className="stat-grid"><StatCard label="Total students" value={data.students?.length ?? '—'} note="Registered residents" icon={Users} tone="blue" /><StatCard label="Bed occupancy" value={totalBeds ? `${Math.round((occupied / totalBeds) * 100)}%` : '—'} note={`${occupied} of ${totalBeds} beds occupied`} icon={BedDouble} tone="purple" trend={totalBeds ? `${totalBeds - occupied} beds available` : null} /><StatCard label="Open complaints" value={activeComplaints} note="Awaiting resolution" icon={Wrench} tone="orange" /><StatCard label="Visitors inside" value={pendingVisitors} note="Not checked out yet" icon={UserRoundCheck} tone="green" /></div>
      <div className="dashboard-grid"><Panel title="Room occupancy" subtitle="Live view of room capacity" action={<button className="text-action" onClick={() => onNavigate('rooms')}>View all <ArrowRight size={14} /></button>}><div className="occupancy-summary"><div><strong>{occupied}<small> / {totalBeds || 0}</small></strong><span>Occupied beds</span></div><div className="occupancy-ring" style={{ '--progress': `${totalBeds ? occupied / totalBeds * 100 : 0}%` }}><div><strong>{totalBeds ? Math.round(occupied / totalBeds * 100) : 0}%</strong><small>filled</small></div></div></div><div className="occupancy-legend"><span><i className="legend-blue" /> Occupied</span><span><i className="legend-gray" /> Available</span></div></Panel>
        <Panel title="Recent complaints" subtitle="Latest reported issues" action={<button className="text-action" onClick={() => onNavigate('complaints')}>All complaints <ArrowRight size={14} /></button>}><div className="compact-list">{(data.complaints || []).slice(0, 4).map((complaint) => <div className="compact-item" key={complaint.id}><span className="compact-icon"><Wrench size={16} /></span><div className="compact-main"><strong>{nice(complaint.category)} issue <small>#{complaint.id}</small></strong><span>{complaint.student?.fullName || 'Student'} · {dateText(complaint.createdAt)}</span></div><Status value={complaint.status} /></div>)}{!data.complaints?.length && <EmptyState title="No complaints" detail="New requests will appear here." />}</div></Panel></div>
      <Panel title="Quick access" subtitle="Common hostel operations"><div className="quick-actions"><button onClick={() => onNavigate('rooms')}><IconBox icon={BedDouble} tone="blue" /><span><b>Room allocation</b><small>Manage residents and beds</small></span><ArrowRight size={16} /></button><button onClick={() => onNavigate('visitors')}><IconBox icon={UserRoundCheck} tone="green" /><span><b>Visitor register</b><small>Track entries and exits</small></span><ArrowRight size={16} /></button><button onClick={() => onNavigate('notices')}><IconBox icon={FileText} tone="purple" /><span><b>Publish notice</b><small>Share an update with students</small></span><ArrowRight size={16} /></button><button onClick={() => onNavigate('mess')}><IconBox icon={Utensils} tone="orange" /><span><b>Mess menu</b><small>Update today’s dining plan</small></span><ArrowRight size={16} /></button></div></Panel>
    </>;
  }
  if (role === 'STUDENT') {
    const open = (data.complaints || []).filter((item) => item.status !== 'RESOLVED').length;
    const feeDue = (data.fees || []).reduce((sum, fee) => sum + Number(fee.pendingAmount || 0), 0);
    const myRoom = data.room?.room;
    return <><PageTitle eyebrow="STUDENT PORTAL" title={`${greeting}, ${user.fullName.split(' ')[0]}`} subtitle="Your hostel life, all in one place." action={<button className="btn btn-light" onClick={onRefresh}><RefreshCw size={15} /> Refresh</button>} />
      <div className="student-hero"><div><span className="banner-label"><Sparkles size={13} /> YOUR CAMPUS HOME</span><h2>Make yourself at home.</h2><p>Keep track of your room, raise a request or see what’s cooking in the mess.</p><button onClick={() => onNavigate('complaints')}>Get support <ArrowRight size={15} /></button></div><div className="student-hero-art"><div className="hero-circle" /><BedDouble size={102} strokeWidth={1.15} /><span>✦</span></div></div>
      <div className="stat-grid"><StatCard label="My room" value={myRoom?.roomNumber ? `Room ${myRoom.roomNumber}` : 'Not allotted'} note={myRoom?.hostelName || 'Room allocation'} icon={BedDouble} tone="blue" /><StatCard label="Open complaints" value={open} note="Requests in progress" icon={MessageSquare} tone="orange" /><StatCard label="Fees due" value={money(feeDue)} note="Pending balance" icon={Wallet} tone="purple" /><StatCard label="New notices" value={data.notices?.length ?? 0} note="Active hostel updates" icon={Bell} tone="green" /></div>
      <div className="dashboard-grid"><Panel title="Today at the mess" subtitle={dateText(today())} action={<button className="text-action" onClick={() => onNavigate('mess')}>Full menu <ArrowRight size={14} /></button>}><div className="meal-list">{(data.menu || []).map((item) => <div className="meal-item" key={item.id}><span className={`meal-symbol meal-${item.mealType?.toLowerCase()}`}>{item.mealType === 'BREAKFAST' ? '☀' : item.mealType === 'LUNCH' ? '◒' : item.mealType === 'SNACKS' ? '✦' : '☾'}</span><div><strong>{nice(item.mealType)}</strong><span>{item.items}</span></div></div>)}{!data.menu?.length && <EmptyState title="Menu not published" detail="The warden has not added today’s menu yet." />}</div></Panel>
        <Panel title="My recent requests" subtitle="Stay updated on your complaints" action={<button className="text-action" onClick={() => onNavigate('complaints')}>View all <ArrowRight size={14} /></button>}><div className="compact-list">{(data.complaints || []).slice(0, 4).map((item) => <div className="compact-item" key={item.id}><span className="compact-icon"><MessageSquare size={15} /></span><div className="compact-main"><strong>{nice(item.category)} <small>#{item.id}</small></strong><span>{dateText(item.createdAt)}</span></div><Status value={item.status} /></div>)}{!data.complaints?.length && <EmptyState title="All quiet here" detail="You have not raised any complaints yet." />}</div></Panel></div>
      <Panel title="Your hostel shortcuts" subtitle="Jump right into what you need"><div className="quick-actions"><button onClick={() => onNavigate('rooms')}><IconBox icon={BedDouble} tone="blue" /><span><b>Room details</b><small>See your current allocation</small></span><ArrowRight size={16} /></button><button onClick={() => onNavigate('fees')}><IconBox icon={Wallet} tone="purple" /><span><b>Fee records</b><small>Review pending dues</small></span><ArrowRight size={16} /></button><button onClick={() => onNavigate('visitors')}><IconBox icon={UserRoundCheck} tone="green" /><span><b>Visitor history</b><small>See your registered visitors</small></span><ArrowRight size={16} /></button><button onClick={() => onNavigate('notices')}><IconBox icon={FileText} tone="orange" /><span><b>Notice board</b><small>Latest hostel announcements</small></span><ArrowRight size={16} /></button></div></Panel>
    </>;
  }
  if (role === 'MAINTENANCE_STAFF') {
    const assigned = data.complaints || [];
    const active = assigned.filter((item) => item.status !== 'RESOLVED').length;
    const critical = assigned.filter((item) => item.priority === 'CRITICAL').length;
    return <><PageTitle eyebrow="MAINTENANCE WORKSPACE" title={`${greeting}, ${user.fullName.split(' ')[0]}`} subtitle="Your assigned jobs and maintenance queue." action={<button className="btn btn-light" onClick={onRefresh}><RefreshCw size={15} /> Refresh</button>} /><div className="welcome-banner staff-banner"><div><span className="banner-label"><Wrench size={13} /> WORK ORDERS</span><h2>Let’s get things working.</h2><p>Review your assigned requests and update their progress as you go.</p><button onClick={() => onNavigate('complaints')}>View assigned tasks <ArrowRight size={15} /></button></div><div className="banner-illustration"><Wrench size={105} strokeWidth={1.1} /></div></div><div className="stat-grid"><StatCard label="Assigned tasks" value={assigned.length} note="Total requests assigned" icon={ClipboardList} tone="blue" /><StatCard label="In progress" value={active} note="Still need attention" icon={Activity} tone="orange" /><StatCard label="Critical priority" value={critical} note="Handle these first" icon={Zap} tone="purple" /><StatCard label="Resolved" value={assigned.length - active} note="Completed jobs" icon={CheckCircle2} tone="green" /></div><Panel title="Your task queue" subtitle="Prioritize by urgency"><ComplaintsTable rows={assigned.slice(0, 5)} showStudent={false} /></Panel></>;
  }
  const pending = (data.fees || []).filter((fee) => Number(fee.pendingAmount) > 0).length;
  const balance = (data.fees || []).reduce((sum, fee) => sum + Number(fee.pendingAmount || 0), 0);
  const billed = (data.fees || []).reduce((sum, fee) => sum + Number(fee.totalAmount || 0), 0);
  return <><PageTitle eyebrow="ACCOUNTS WORKSPACE" title={`${greeting}, ${user.fullName.split(' ')[0]}`} subtitle="Manage hostel billing, fee records and payment entries." action={<button className="btn btn-light" onClick={onRefresh}><RefreshCw size={15} /> Refresh</button>} /><div className="welcome-banner accounts-banner"><div><span className="banner-label"><Wallet size={13} /> FINANCE OVERVIEW</span><h2>Clear records. Smooth operations.</h2><p>Keep student fee records accurate and track every payment in one place.</p><button onClick={() => onNavigate('fees')}>Open fee management <ArrowRight size={15} /></button></div><div className="banner-illustration"><CreditCard size={105} strokeWidth={1.1} /></div></div><div className="stat-grid"><StatCard label="Fee records" value={data.fees?.length ?? 0} note="Total records in system" icon={FileText} tone="blue" /><StatCard label="Outstanding balance" value={money(balance)} note="Amount yet to be collected" icon={Wallet} tone="orange" /><StatCard label="Pending records" value={pending} note="Partially paid or unpaid" icon={Clock3} tone="purple" /><StatCard label="Total billed" value={money(billed)} note="Across all fee records" icon={CreditCard} tone="green" /></div><Panel title="Recent fee records" subtitle="Latest billing activity" action={<button className="text-action" onClick={() => onNavigate('fees')}>Manage fees <ArrowRight size={14} /></button>}><FeesTable rows={(data.fees || []).slice(0, 5)} onPay={() => onNavigate('fees')} /></Panel></>;
}

function RoomsPage({ user, data, loading, onAllocate, onChangeRoom, onRefresh }) {
  const isWarden = user.role === 'WARDEN';
  const isStudent = user.role === 'STUDENT';
  if (isStudent) {
    const allocation = data.room;
    const room = allocation?.room;
    return <><PageTitle eyebrow="MY HOSTEL" title="My room" subtitle="Your current room assignment and occupancy details." action={<button className="btn btn-light" onClick={onRefresh}><RefreshCw size={15} /> Refresh</button>} />
      {room ? <div className="room-detail-layout"><div className="room-feature"><div className="room-feature-top"><span className="room-chip"><DoorOpen size={14} /> ACTIVE ALLOCATION</span><span className="room-number">{room.roomNumber}</span></div><div className="room-feature-icon"><BedDouble size={82} strokeWidth={1.2} /></div><div><span className="eyebrow">YOUR ROOM</span><h2>Room {room.roomNumber}</h2><p>{room.hostelName || 'Sunrise Hostel'}</p></div><div className="room-feature-foot"><span><Users size={15} /> {room.occupied} / {room.capacity} occupied</span><span><CalendarDays size={15} /> Since {dateText(allocation.allocatedOn)}</span></div></div><div className="room-side-details"><Panel title="Room information" subtitle="Your current allocation"><div className="detail-list"><div><span>Hostel</span><b>{room.hostelName || 'Sunrise Hostel'}</b></div><div><span>Room number</span><b>{room.roomNumber}</b></div><div><span>Room capacity</span><b>{room.capacity} beds</b></div><div><span>Occupied beds</span><b>{room.occupied}</b></div><div><span>Available beds</span><b>{room.availableBeds ?? room.capacity - room.occupied}</b></div><div><span>Allocation status</span><Status value={allocation.active ? 'ACTIVE' : 'ENDED'} /></div></div></Panel><div className="info-callout"><ShieldCheck size={18} /><span>For room change or maintenance support, contact your hostel warden.</span></div></div></div> : <EmptyState icon={BedDouble} title="No room allocated yet" detail={data.roomError || 'Your warden has not assigned a room to you yet.'} />}</>;
  }
  const rooms = data.rooms || [];
  const totalBeds = rooms.reduce((sum, room) => sum + Number(room.capacity || 0), 0);
  const occupiedBeds = rooms.reduce((sum, room) => sum + Number(room.occupied || 0), 0);
  return <><PageTitle eyebrow="HOSTEL OPERATIONS" title="Room management" subtitle="Manage room availability, occupancy and student allocations." action={<><button className="btn btn-light" onClick={onRefresh}><RefreshCw size={15} /> Refresh</button>{isWarden && <button className="btn btn-primary" onClick={onAllocate}><Plus size={16} /> Allocate room</button>}</>} /><div className="stat-grid stat-grid-three"><StatCard label="Total rooms" value={rooms.length} note="Across this hostel" icon={Building2} tone="blue" /><StatCard label="Occupied beds" value={`${occupiedBeds} / ${totalBeds}`} note="Current occupancy" icon={BedDouble} tone="purple" /><StatCard label="Available beds" value={totalBeds - occupiedBeds} note="Ready for allocation" icon={DoorOpen} tone="green" /></div>
    <Panel title="Room inventory" subtitle="Capacity and current occupancy"><div className="room-grid">{rooms.map((room) => <div className="room-card" key={room.id}><div className="room-card-head"><span className="room-icon"><BedDouble size={19} /></span><Status value={room.availableBeds > 0 ? 'AVAILABLE' : 'FULL'} /></div><h3>Room {room.roomNumber}</h3><p>{room.hostelName || 'Sunrise Hostel'}</p><div className="room-occupancy-label"><span>Occupancy</span><b>{room.occupied} / {room.capacity}</b></div><div className="progress-track"><span style={{ width: `${room.capacity ? room.occupied / room.capacity * 100 : 0}%` }} /></div><div className="room-card-foot"><span>{room.availableBeds ?? room.capacity - room.occupied} beds available</span>{isWarden && room.availableBeds > 0 && <span className="room-available-dot" />}</div></div>)}{!rooms.length && <EmptyState icon={BedDouble} title="No rooms found" detail={data.roomsError || 'Room inventory is not available.'} />}</div></Panel>
    {isWarden && <Panel title="Active room allocations" subtitle="Students currently assigned to rooms"><DataTable rows={data.allocations || []} columns={[{ key: 'id', label: 'Allocation ID', render: (r) => <b className="table-id">#A-{r.id}</b> }, { key: 'student', label: 'Student', render: (r) => <div className="person-cell"><span className="table-avatar">{initials(r.student?.fullName)}</span><span><b>{r.student?.fullName || 'Student'}</b><small>{r.student?.username || '—'}</small></span></div> }, { key: 'room', label: 'Room', render: (r) => <b>Room {r.room?.roomNumber || '—'}</b> }, { key: 'allocatedOn', label: 'Allocated on', render: (r) => dateText(r.allocatedOn) }, { key: 'action', label: '', render: (r) => <button className="small-action" onClick={() => onChangeRoom(r.student?.id)}>Change room</button> }]} emptyTitle="No active allocations" emptyDetail="Allocate a room to a student to see it here." /></Panel>}
  </>;
}

function ComplaintsTable({ rows, showStudent = true, onAssign, onStart, onResolve }) {
  const columns = [
    { key: 'id', label: 'Ticket', render: (r) => <b className="table-id">#CMP-{r.id}</b> },
    { key: 'category', label: 'Issue', render: (r) => <div className="issue-cell"><span className="issue-icon"><Wrench size={15} /></span><span><b>{nice(r.category)}</b><small className="line-clamp">{r.description}</small></span></div> },
  ];
  if (showStudent) columns.push({ key: 'student', label: 'Reported by', render: (r) => <span>{r.student?.fullName || '—'}</span> });
  columns.push({ key: 'priority', label: 'Priority', render: (r) => <Status value={r.priority} /> });
  columns.push({ key: 'status', label: 'Status', render: (r) => <Status value={r.status} /> });
  columns.push({ key: 'createdAt', label: 'Reported', render: (r) => dateText(r.createdAt) });
  if (onAssign || onStart || onResolve) columns.push({ key: 'actions', label: 'Action', render: (r) => <div className="action-buttons">{onAssign && r.status !== 'RESOLVED' && <button className="small-action" onClick={() => onAssign(r)}>Assign</button>}{onStart && r.status === 'ASSIGNED' && <button className="small-action small-primary" onClick={() => onStart(r)}>Start</button>}{onResolve && r.status === 'IN_PROGRESS' && <button className="small-action small-primary" onClick={() => onResolve(r)}>Resolve</button>}</div> });
  return <DataTable rows={rows} columns={columns} emptyTitle="No complaints found" emptyDetail="New complaint records will appear here." />;
}

function ComplaintsPage({ user, data, loading, onNew, onAction, onAssign }) {
  const role = user.role;
  const isStudent = role === 'STUDENT';
  const isWarden = role === 'WARDEN';
  const isStaff = role === 'MAINTENANCE_STAFF';
  const rows = data.complaints || [];
  const open = rows.filter((item) => item.status !== 'RESOLVED').length;
  const handleStart = (complaint) => onAction(`/complaints/${complaint.id}/start`, { note: 'Work started by maintenance staff' }, 'Complaint marked as in progress.').catch(() => {});
  const handleResolve = (complaint) => onAction(`/complaints/${complaint.id}/resolve`, { note: 'Issue resolved by maintenance staff' }, 'Complaint marked as resolved.').catch(() => {});
  const roleTitle = isStudent ? 'My complaints' : isStaff ? 'Assigned tasks' : 'Complaint management';
  return <><PageTitle eyebrow={isStaff ? 'MAINTENANCE' : 'HOSTEL SERVICES'} title={roleTitle} subtitle={isStudent ? 'Raise an issue and follow its progress until it is resolved.' : isStaff ? 'Work through assigned maintenance requests and update each status.' : 'Review reported issues, assign staff and monitor resolution.'} action={<>{isStudent && <button className="btn btn-primary" onClick={onNew}><Plus size={16} /> Raise complaint</button>}</>} />
    <div className="stat-grid stat-grid-three"><StatCard label="Total requests" value={rows.length} note="All reported complaints" icon={MessageSquare} tone="blue" /><StatCard label="Open requests" value={open} note="Not yet resolved" icon={Clock3} tone="orange" /><StatCard label="Resolved" value={rows.length - open} note="Completed requests" icon={CheckCircle2} tone="green" /></div>
    <Panel title={isStaff ? 'My work queue' : 'All requests'} subtitle={isStudent ? 'Your submitted maintenance requests' : 'Latest requests submitted by hostel residents'}><ComplaintsTable rows={rows} showStudent={!isStudent && !isStaff} onAssign={isWarden ? onAssign : undefined} onStart={isStaff ? handleStart : undefined} onResolve={isStaff ? handleResolve : undefined} /></Panel>
    {!isStudent && rows.length > 0 && <Panel title="Complaint lifecycle" subtitle="Requests follow the State pattern implemented in the backend"><div className="lifecycle"><div className="life-step done"><span>1</span><b>Submitted</b></div><i /><div className="life-step done"><span>2</span><b>Assigned</b></div><i /><div className="life-step"><span>3</span><b>In progress</b></div><i /><div className="life-step"><span>4</span><b>Resolved</b></div></div></Panel>}
  </>;
}

function VisitorsPage({ user, data, loading, onNew, onAction }) {
  const isWarden = user.role === 'WARDEN';
  const visitors = data.visitors || [];
  const inside = visitors.filter((visitor) => !visitor.checkOut).length;
  const verified = visitors.filter((visitor) => visitor.verified).length;
  const cols = [
    { key: 'visitorName', label: 'Visitor', render: (r) => <div className="person-cell"><span className="table-avatar"><UserRound size={15} /></span><span><b>{r.visitorName}</b><small>{r.visitorPhone || 'No phone provided'}</small></span></div> },
    { key: 'student', label: 'Visiting student', render: (r) => <span>{r.student?.fullName || '—'}</span> },
    { key: 'purpose', label: 'Purpose' },
    { key: 'checkIn', label: 'Check-in', render: (r) => timeText(r.checkIn) },
    { key: 'checkOut', label: 'Check-out', render: (r) => r.checkOut ? timeText(r.checkOut) : <Status value="INSIDE" /> },
    { key: 'verified', label: 'Verification', render: (r) => <Status value={r.verified ? 'VERIFIED' : 'PENDING'} /> },
  ];
  if (isWarden) cols.push({ key: 'actions', label: 'Actions', render: (r) => <div className="action-buttons">{!r.verified && <button className="small-action" onClick={() => onAction(`/visitors/${r.id}/verify`, 'Visitor verified.').catch(() => {})}>Verify</button>}{!r.checkOut && <button className="small-action small-primary" onClick={() => onAction(`/visitors/${r.id}/checkout`, 'Visitor checked out.').catch(() => {})}>Check out</button>}</div> });
  return <><PageTitle eyebrow="GUEST REGISTER" title={isWarden ? 'Visitor management' : 'My visitors'} subtitle={isWarden ? 'Monitor guest entries, verification and check-out activity.' : 'A history of visitors registered for your room.'} action={isWarden && <button className="btn btn-primary" onClick={onNew}><Plus size={16} /> Register visitor</button>} /><div className="stat-grid stat-grid-three"><StatCard label="Total visits" value={visitors.length} note="Records in this view" icon={Users} tone="blue" /><StatCard label="Currently inside" value={inside} note="Awaiting check-out" icon={DoorOpen} tone="orange" /><StatCard label="Verified visits" value={verified} note="Checked by warden" icon={ShieldCheck} tone="green" /></div><Panel title="Visitor register" subtitle="Visitor log and entry status"><DataTable rows={visitors} columns={cols} emptyTitle="No visitor records" emptyDetail="Visitor entries will be listed here." /></Panel></>;
}

function StudentsPage({ data, loading }) {
  const rows = data.students || [];
  return <><PageTitle eyebrow="PEOPLE DIRECTORY" title="Students" subtitle="Registered hostel residents and their academic details." /><div className="stat-grid stat-grid-three"><StatCard label="Registered students" value={rows.length} note="Active student accounts" icon={Users} tone="blue" /><StatCard label="Student courses" value={new Set(rows.map((s) => s.course).filter(Boolean)).size} note="Distinct programmes" icon={FileText} tone="purple" /><StatCard label="Directory status" value="Live" note="Sourced from Spring Boot" icon={Activity} tone="green" /></div><Panel title="Student directory" subtitle="Student information from the backend"><DataTable rows={rows} columns={[{ key: 'fullName', label: 'Student', render: (r) => <div className="person-cell"><span className="table-avatar">{initials(r.fullName)}</span><span><b>{r.fullName}</b><small>{r.username}</small></span></div> }, { key: 'rollNumber', label: 'Roll number' }, { key: 'course', label: 'Course' }, { key: 'phone', label: 'Phone' }, { key: 'id', label: 'User ID', render: (r) => <b className="table-id">#{r.id}</b> }]} emptyTitle="No students found" emptyDetail={data.studentsError || 'Student records are not available.'} /></Panel></>;
}

function StaffPage({ data }) {
  const rows = data.staff || [];
  return <><PageTitle eyebrow="PEOPLE DIRECTORY" title="Staff directory" subtitle="Maintenance team members available for complaint assignments." /><div className="stat-grid stat-grid-three"><StatCard label="Maintenance staff" value={rows.length} note="Registered team members" icon={Wrench} tone="blue" /><StatCard label="Specializations" value={new Set(rows.map((s) => s.specialization).filter(Boolean)).size} note="Service categories" icon={Settings2} tone="purple" /><StatCard label="Directory status" value="Live" note="Sourced from Spring Boot" icon={Activity} tone="green" /></div><Panel title="Maintenance team" subtitle="Staff accounts and specializations"><div className="staff-grid">{rows.map((staff) => <div className="staff-card" key={staff.id}><div className="staff-card-top"><span className="staff-avatar">{initials(staff.fullName)}</span><Status value="ACTIVE" /></div><h3>{staff.fullName}</h3><p>{staff.username} · ID #{staff.id}</p><div className="specialty"><Wrench size={14} />{nice(staff.specialization || 'General maintenance')}</div><div className="staff-card-foot"><span>{staff.phone || 'No phone provided'}</span><span>Maintenance</span></div></div>)}{!rows.length && <EmptyState icon={Wrench} title="No staff records" detail={data.staffError || 'Staff directory is empty.'} />}</div></Panel></>;
}

function FeesTable({ rows, onPay, onReceipt, onOnlinePay }) {
  return <DataTable rows={rows} columns={[{ key: 'id', label: 'Fee ID', render: (r) => <b className="table-id">#F-{r.id}</b> }, { key: 'student', label: 'Student', render: (r) => <div className="person-cell"><span className="table-avatar">{initials(r.student?.fullName)}</span><span><b>{r.student?.fullName || '—'}</b><small>{r.student?.rollNumber || r.student?.username || ''}</small></span></div> }, { key: 'description', label: 'Description' }, { key: 'totalAmount', label: 'Total', render: (r) => <b>{money(r.totalAmount)}</b> }, { key: 'pendingAmount', label: 'Balance', render: (r) => <b className={Number(r.pendingAmount) > 0 ? 'amount-due' : 'amount-paid'}>{money(r.pendingAmount)}</b> }, { key: 'dueDate', label: 'Due date', render: (r) => dateText(r.dueDate) }, { key: 'status', label: 'Status', render: (r) => <Status value={r.status} /> }, { key: 'actions', label: '', render: (r) => <div className="action-buttons">{onPay && Number(r.pendingAmount) > 0 && <button className="small-action small-primary" onClick={() => onPay(r)}>Add payment</button>}{onOnlinePay && Number(r.pendingAmount) > 0 && <button className="small-action small-primary" onClick={() => onOnlinePay(r)}>Pay online</button>}{r.payments?.map((payment) => onReceipt && <button className="receipt-link" key={payment.id} onClick={() => onReceipt(payment.id)} title="View receipt"><FileText size={14} /> Receipt</button>)}</div> }]} emptyTitle="No fee records" emptyDetail="Fee records will appear once created." />;
}

function FeesPage({ user, data, loading, onNew, onPay, onOnlinePay, onReceipt }) {
  const isAccountant = user.role === 'ACCOUNTANT';
  const fees = data.fees || [];
  const total = fees.reduce((sum, fee) => sum + Number(fee.totalAmount || 0), 0);
  const paid = fees.reduce((sum, fee) => sum + Number(fee.paidAmount || 0), 0);
  const due = fees.reduce((sum, fee) => sum + Number(fee.pendingAmount || 0), 0);
  return <><PageTitle eyebrow="FEE & PAYMENTS" title={isAccountant ? 'Fee management' : 'My fees'} subtitle={isAccountant ? 'Create fee records, record student payments and review balances.' : 'Review your hostel fee records and outstanding balances.'} action={isAccountant && <button className="btn btn-primary" onClick={onNew}><Plus size={16} /> Create fee record</button>} /><div className="stat-grid stat-grid-three"><StatCard label="Total billed" value={money(total)} note="All fee records" icon={FileText} tone="blue" /><StatCard label="Amount collected" value={money(paid)} note="Payments recorded" icon={CheckCircle2} tone="green" /><StatCard label="Outstanding" value={money(due)} note="Pending balance" icon={Wallet} tone="orange" /></div><Panel title={isAccountant ? 'All fee records' : 'My fee records'} subtitle="Payment status is calculated by the backend from recorded payments."><FeesTable rows={fees} onPay={isAccountant ? onPay : undefined} onOnlinePay={!isAccountant ? onOnlinePay : undefined} onReceipt={onReceipt} /></Panel></>;
}

function NoticesPage({ user, data, loading, onNew }) {
  const isWarden = user.role === 'WARDEN';
  const notices = data.notices || [];
  const categoryTone = (category) => ({ EMERGENCY: 'danger', MAINTENANCE: 'warning', EVENT: 'success', EXAM: 'purple' }[category] || 'blue');
  return <><PageTitle eyebrow="HOSTEL COMMUNICATION" title="Notice board" subtitle={isWarden ? 'Publish updates and keep residents informed.' : 'Important announcements and updates from your hostel.'} action={isWarden && <button className="btn btn-primary" onClick={onNew}><Plus size={16} /> Publish notice</button>} /><div className="notice-grid">{notices.map((notice) => <article className="notice-card" key={notice.id}><div className="notice-top"><span className={`notice-category category-${categoryTone(notice.category)}`}>{nice(notice.category)}</span><span className="notice-date"><CalendarDays size={13} />{dateText(notice.publishedOn)}</span></div><h3>{notice.title}</h3><p>{notice.content}</p><div className="notice-foot"><span>Posted by {notice.postedByName || 'Hostel administration'}</span><span>{notice.expiryDate ? `Until ${dateText(notice.expiryDate)}` : 'No expiry'}</span></div></article>)}{!notices.length && <div className="full-width"><EmptyState icon={FileText} title="No active notices" detail={data.noticesError || 'There are no announcements to display right now.'} /></div>}</div></>;
}

function MessPage({ user, data, loading, onNew, onFeedback, onPreference }) {
  const isWarden = user.role === 'WARDEN';
  const isStudent = user.role === 'STUDENT';
  const meals = data.menu || [];
  return <><PageTitle eyebrow="DINING & WELLNESS" title="Mess & dining" subtitle={isWarden ? 'Plan meals and review dining feedback from residents.' : 'See today’s menu and share your dining preferences.'} action={<>{isWarden && <button className="btn btn-primary" onClick={onNew}><Plus size={16} /> Add menu</button>}{isStudent && <><button className="btn btn-light" onClick={onPreference}><Settings2 size={15} /> Preferences</button><button className="btn btn-primary" onClick={() => onFeedback()}><MessageSquare size={15} /> Give feedback</button></>}</>} /><div className="mess-date-banner"><div><span className="eyebrow">TODAY’S MENU</span><h2>{new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long' })}</h2><p>Freshly planned meals for the hostel community.</p></div><div className="mess-date-icon"><Utensils size={42} /></div></div><div className="meal-card-grid">{['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'].map((type) => { const item = meals.find((meal) => meal.mealType === type); return <article className="meal-card" key={type}><div className={`meal-large-symbol meal-${type.toLowerCase()}`}>{type === 'BREAKFAST' ? '☀' : type === 'LUNCH' ? '◒' : type === 'SNACKS' ? '✦' : '☾'}</div><span className="eyebrow">{type}</span><h3>{nice(type)}</h3><p>{item?.items || 'Menu not published yet'}</p><div className="meal-card-foot">{item ? <><span><CheckCircle2 size={14} /> Menu available</span>{isStudent && <button onClick={() => onFeedback(item)}>Rate meal</button>}</> : <span className="muted-label">Awaiting update</span>}</div></article>; })}</div>
    {isStudent && <Panel title="My meal preferences" subtitle="Your saved dietary choices"><div className="preference-chips">{(data.preferences || []).map((preference) => <span key={preference.id}><b>{nice(preference.mealType)}</b>{nice(preference.preference)}</span>)}{!data.preferences?.length && <EmptyState title="No preferences saved" detail="Set your preferences so the mess team can plan better." />}</div></Panel>}
    {isWarden && <Panel title="Dining overview" subtitle="Quick snapshot of today’s meal plan"><div className="dining-summary"><div><strong>{meals.length} / 4</strong><span>Meals published</span></div><div><strong>{(data.feedback || []).length}</strong><span>Feedback entries</span></div><div><strong>{meals.length === 4 ? 'Complete' : 'In progress'}</strong><span>Today’s menu status</span></div></div></Panel>}
  </>;
}

function NotificationsPage({ data, loading }) {
  const items = data.notifications || [];
  return <><PageTitle eyebrow="UPDATES" title="Notifications" subtitle="Activity updates and messages from your hostel services." /><Panel title="Recent notifications" subtitle="Updates generated by complaint status changes"><div className="notification-list">{items.map((item) => <div className="notification-item" key={item.id}><span className="notification-icon"><Bell size={17} /></span><div><strong>{item.message}</strong><span>{timeText(item.createdAt)}</span></div><Status value={item.read ? 'READ' : 'NEW'} /></div>)}{!items.length && <EmptyState icon={Bell} title="You’re all caught up" detail={data.notificationsError || 'New notifications will appear here.'} />}</div></Panel></>;
}

function ReceiptModal({ receipt, onClose }) {
  return <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}><div className="modal-card receipt-modal"><div className="receipt-success"><CheckCircle2 size={29} /></div><div className="receipt-heading"><span className="eyebrow">PAYMENT RECEIPT</span><h2>Payment recorded</h2><p>{receipt.receiptNo}</p></div><div className="receipt-amount">{money(receipt.amountPaid)}<small>Amount paid</small></div><div className="receipt-details"><div><span>Student</span><b>{receipt.student}</b></div><div><span>Roll number</span><b>{receipt.rollNumber || '—'}</b></div><div><span>Fee description</span><b>{receipt.feeDescription}</b></div><div><span>Payment method</span><b>{nice(receipt.method)}</b></div><div><span>Paid on</span><b>{timeText(receipt.paidOn)}</b></div><div><span>Remaining balance</span><b>{money(receipt.remainingBalance)}</b></div></div><div className="modal-actions"><button className="btn btn-ghost" onClick={onClose}>Close</button><button className="btn btn-primary" onClick={() => window.print()}><Download size={15} /> Print receipt</button></div></div></div>;
}

export default App;
