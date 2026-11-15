// RemindFlow Interactive Android Prototype Engine
const DAY_LOGS = {};

const REMINDERS_DATA = [
  {
    id: 1,
    title: "Multivitamin & Zinc",
    notes: "Take 1 tablet with breakfast",
    time: "08:00 AM",
    time24: "08:00",
    quadrant: "MORNING",
    category: "HEALTH",
    categoryLabel: "Health & Meds",
    icon: "💊",
    color: "#EC4899",
    scheduleMode: "RECURRING",
    recurringDays: [1, 2, 3, 4, 5, 6, 7],
    recurringSummary: "Daily",
    hasEndDate: false,
    endDate: null,
    isStockTracked: true,
    currentStock: 28,
    dose: 1,
    unit: "tablets",
    lowThreshold: 5,
    status: "DONE"
  },
  {
    id: 2,
    title: "Morning Hydration Goal",
    notes: "Drink a 500ml glass before work",
    time: "09:30 AM",
    time24: "09:30",
    quadrant: "MORNING",
    category: "GENERAL",
    categoryLabel: "Hydration",
    icon: "💧",
    color: "#0EA5E9",
    scheduleMode: "RECURRING",
    recurringDays: [1, 2, 3, 4, 5],
    recurringSummary: "Mon–Fri",
    hasEndDate: false,
    endDate: null,
    isStockTracked: false,
    status: "DONE"
  },
  {
    id: 3,
    title: "Cardio & Core Workout",
    notes: "Gym session - Treadmill & planks",
    time: "02:15 PM",
    time24: "14:15",
    quadrant: "AFTERNOON",
    category: "FITNESS",
    categoryLabel: "Fitness",
    icon: "🏃",
    color: "#10B981",
    scheduleMode: "RECURRING",
    recurringDays: [2, 4], // Every Tuesday & Thursday
    recurringSummary: "Every Tue, Thu (until Dec 15)",
    hasEndDate: true,
    endDate: "2026-12-15",
    isStockTracked: false,
    status: "PENDING"
  },
  {
    id: 4,
    title: "Quarterly Dental Checkup",
    notes: "Dr. Smith Clinic - Room 402",
    time: "04:30 PM",
    time24: "16:30",
    quadrant: "AFTERNOON",
    category: "WORK",
    categoryLabel: "Appointments",
    icon: "🦷",
    color: "#6366F1",
    scheduleMode: "SINGLE", // 1-Time Reminder
    singleDate: "2026-11-01", // Due Today!
    recurringDays: [],
    recurringSummary: "1-Time • Today",
    hasEndDate: false,
    endDate: null,
    isStockTracked: false,
    status: "DONE"
  },
  {
    id: 5,
    title: "Omega-3 & Magnesium",
    notes: "Take 2 softgels with dinner",
    time: "07:30 PM",
    time24: "19:30",
    quadrant: "EVENING",
    category: "HEALTH",
    categoryLabel: "Health & Meds",
    icon: "💊",
    color: "#8B5CF6",
    scheduleMode: "RECURRING",
    recurringDays: [1, 2, 3, 4, 5, 6, 7],
    recurringSummary: "Daily",
    hasEndDate: false,
    endDate: null,
    isStockTracked: true,
    currentStock: 4, // Intentionally low for demo alert
    dose: 2,
    unit: "capsules",
    lowThreshold: 6,
    status: "PENDING"
  },
  {
    id: 6,
    title: "Replace Water Filter Cartridge",
    notes: "Order replacement cartridge & install",
    time: "10:15 PM",
    time24: "22:15",
    quadrant: "NIGHT",
    category: "HABITS",
    categoryLabel: "Maintenance",
    icon: "🔧",
    color: "#3B82F6",
    scheduleMode: "SINGLE", // 1-Time Reminder
    singleDate: "2026-11-03", // Future single reminder
    recurringDays: [],
    recurringSummary: "1-Time • Nov 3",
    hasEndDate: false,
    endDate: null,
    isStockTracked: false,
    status: "PENDING"
  }
];

let activeSnoozeId = null;
let selectedFormCategory = "HEALTH";
let currentScheduleMode = "RECURRING"; // "RECURRING" or "SINGLE"
let selectedRecurringDays = [1, 2, 3, 4, 5, 6, 7]; // 1=Mon .. 7=Sun
let currentActiveDate = new Date(2026, 10, 1); // Sunday, Nov 1, 2026

// Initialize on load
document.addEventListener("DOMContentLoaded", () => {
  initFormDates();
  renderCalendarStrip();
  renderQuadrants();
  renderInventory();
  updateProgressRing();
});

function initFormDates() {
  const todayStr = "2026-11-01";
  const endDefaultStr = "2026-11-30";
  const singleInput = document.getElementById("inputSingleDate");
  const endInput = document.getElementById("inputEndDate");
  if (singleInput) singleInput.value = todayStr;
  if (endInput) endInput.value = endDefaultStr;
}

// Render 7-day horizontal calendar strip
function renderCalendarStrip() {
  const container = document.getElementById("calendarStrip");
  container.innerHTML = "";
  
  const baseDate = new Date(2026, 10, 1); // Nov 1, 2026
  const dayNames = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

  for (let i = -3; i <= 10; i++) {
    const d = new Date(baseDate);
    d.setDate(baseDate.getDate() + i);

    const isCurrentActive = d.toDateString() === currentActiveDate.toDateString();
    const pill = document.createElement("div");
    pill.className = `calendar-day-pill ${isCurrentActive ? "active" : ""}`;
    pill.innerHTML = `
      <span class="day-txt">${dayNames[d.getDay()]}</span>
      <span class="date-num">${d.getDate()}</span>
    `;
    pill.onclick = () => {
      document.querySelectorAll(".calendar-day-pill").forEach(p => p.classList.remove("active"));
      pill.classList.add("active");
      currentActiveDate = new Date(d);
      
      const fullDateStr = d.toLocaleDateString("en-US", { weekday: 'long', month: 'long', day: 'numeric' });
      const headerDateEl = document.getElementById("headerDate");
      if (headerDateEl) headerDateEl.textContent = fullDateStr;

      renderQuadrants();
    };
    container.appendChild(pill);
  }
}

// Render the 4 Quadrants
function renderQuadrants() {
  const quadrants = {
    MORNING: document.getElementById("itemsMorning"),
    AFTERNOON: document.getElementById("itemsAfternoon"),
    EVENING: document.getElementById("itemsEvening"),
    NIGHT: document.getElementById("itemsNight")
  };

  Object.values(quadrants).forEach(el => el.innerHTML = "");

  const year = currentActiveDate.getFullYear();
  const month = String(currentActiveDate.getMonth() + 1).padStart(2, "0");
  const day = String(currentActiveDate.getDate()).padStart(2, "0");
  const activeDateStr = `${year}-${month}-${day}`;
  const jsDay = currentActiveDate.getDay(); // 0=Sun .. 6=Sat
  const activeDay1to7 = jsDay === 0 ? 7 : jsDay; // 1=Mon .. 7=Sun

  // Filter items matching active calendar date (always show if logged on that date, or scheduled for day)
  const visibleItems = REMINDERS_DATA.filter(item => {
    const hasDayLog = (typeof DAY_LOGS !== "undefined") && DAY_LOGS[activeDateStr] && DAY_LOGS[activeDateStr][item.id];
    if (hasDayLog) return true;
    if (item.scheduleMode === "SINGLE") {
      return item.singleDate === activeDateStr;
    } else {
      if (item.hasEndDate && item.endDate && activeDateStr > item.endDate) {
        return false;
      }
      return item.recurringDays && item.recurringDays.includes(activeDay1to7);
    }
  });

  visibleItems.forEach(item => {
    const targetEl = quadrants[item.quadrant];
    if (!targetEl) return;

    const dayStatus = ((typeof DAY_LOGS !== "undefined") && DAY_LOGS[activeDateStr] && DAY_LOGS[activeDateStr][item.id]) || item.status || "PENDING";
    const isDone = dayStatus === "DONE";
    const isSnoozed = dayStatus === "SNOOZED";
    const isSkipped = dayStatus === "SKIPPED";
    const isLowStock = item.isStockTracked && item.currentStock <= item.lowThreshold;

    const card = document.createElement("div");
    card.className = `reminder-card ${isDone ? "done" : ""}`;
    card.id = `remCard_${item.id}`;

    let stockBadgeHtml = "";
    if (item.isStockTracked) {
      if (isLowStock) {
        stockBadgeHtml = `<span class="stock-pill">⚠️ Low Stock (${item.currentStock} left)</span>`;
      } else {
        stockBadgeHtml = `<span style="font-size:10px; color:var(--text-muted);">• ${item.currentStock} ${item.unit} left</span>`;
      }
    }

    const scheduleBadgeHtml = item.scheduleMode === "SINGLE"
      ? `<span class="schedule-badge single">📅 1-Time</span>`
      : `<span class="schedule-badge recurring">🔁 ${item.recurringSummary}</span>`;

    card.innerHTML = `
      <div class="rem-leading">
        <div class="rem-icon-badge" style="background:${item.color}25; color:${item.color};">
          ${item.icon}
        </div>
        <div class="rem-details">
          <div class="rem-meta-row">
            <span class="rem-time" style="color:${item.color};">${item.time}</span>
            ${scheduleBadgeHtml}
            ${stockBadgeHtml}
            ${isSnoozed ? `<span style="color:var(--accent-amber); font-size:10px;">⏱ Snoozed</span>` : ""}
            ${isSkipped ? `<span style="color:var(--text-muted); font-size:10px;">✕ Skipped</span>` : ""}
          </div>
          <div class="rem-title">${item.title}</div>
          <div class="rem-notes">${item.notes}</div>
        </div>
      </div>
      <div class="rem-actions">
        ${!isDone ? `
          <button class="btn-action-round snooze" onclick="openSnoozeSheet(${item.id})" title="Snooze">⏱</button>
          <button class="btn-action-round" onclick="markSkip(${item.id})" title="Skip">✕</button>
        ` : ""}
        <button class="btn-action-round check ${isDone ? "active" : ""}" onclick="toggleDone(${item.id})" title="${isDone ? 'Undo' : 'Mark Done'}">
          ✓
        </button>
      </div>
    `;

    targetEl.appendChild(card);
  });

  // Show placeholder if quadrant has no reminders on this date
  Object.values(quadrants).forEach(el => {
    if (!el.hasChildNodes()) {
      el.innerHTML = '<div style="color:var(--text-muted); font-size:12px; padding:8px 4px; font-style:italic;">No reminders in this time window</div>';
    }
  });

  updateQuadrantBadges(visibleItems);
  updateProgressRing(visibleItems);
}

function updateQuadrantBadges(items = REMINDERS_DATA) {
  const y = currentActiveDate.getFullYear();
  const m = String(currentActiveDate.getMonth() + 1).padStart(2, "0");
  const d = String(currentActiveDate.getDate()).padStart(2, "0");
  const activeDateStr = `${y}-${m}-${d}`;

  const counts = { MORNING: [0, 0], AFTERNOON: [0, 0], EVENING: [0, 0], NIGHT: [0, 0] };
  items.forEach(item => {
    if (counts[item.quadrant]) {
      counts[item.quadrant][1]++;
      const st = (DAY_LOGS[activeDateStr] && DAY_LOGS[activeDateStr][item.id]) || item.status || "PENDING";
      if (st === "DONE") counts[item.quadrant][0]++;
    }
  });

  ['Morning', 'Afternoon', 'Evening', 'Night'].forEach(q => {
    const upper = q.toUpperCase();
    const badge = document.getElementById(`badge${q}`);
    const [done, total] = counts[upper];
    if (total === 0) {
      badge.textContent = "No routines";
      badge.className = "quadrant-badge";
    } else if (done === total) {
      badge.textContent = "All Done ✓";
      badge.className = "quadrant-badge completed";
    } else {
      badge.textContent = `${done} of ${total} Done`;
      badge.className = "quadrant-badge";
    }
  });
}

function updateProgressRing(items = REMINDERS_DATA) {
  const y = currentActiveDate.getFullYear();
  const m = String(currentActiveDate.getMonth() + 1).padStart(2, "0");
  const d = String(currentActiveDate.getDate()).padStart(2, "0");
  const activeDateStr = `${y}-${m}-${d}`;

  const total = items.length;
  const doneCount = items.filter(i => {
    const st = (DAY_LOGS[activeDateStr] && DAY_LOGS[activeDateStr][i.id]) || i.status || "PENDING";
    return st === "DONE";
  }).length;
  const pct = total === 0 ? 100 : Math.round((doneCount / total) * 100);

  const ringBar = document.getElementById("progressRingBar");
  const pctLabel = document.getElementById("progressPercentage");
  const subtext = document.getElementById("progressSubtext");

  if (pctLabel) pctLabel.textContent = `${pct}%`;
  if (subtext) subtext.textContent = `${doneCount} of ${total} routines completed`;

  if (ringBar) {
    const radius = 28;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (pct / 100) * circumference;
    ringBar.style.strokeDasharray = `${circumference} ${circumference}`;
    ringBar.style.strokeDashoffset = offset;
  }
}

// Check/Done Action
function toggleDone(id) {
  const item = REMINDERS_DATA.find(i => i.id === id);
  if (!item) return;

  const y = currentActiveDate.getFullYear();
  const m = String(currentActiveDate.getMonth() + 1).padStart(2, "0");
  const d = String(currentActiveDate.getDate()).padStart(2, "0");
  const activeDateStr = `${y}-${m}-${d}`;

  if (!DAY_LOGS[activeDateStr]) DAY_LOGS[activeDateStr] = {};
  const currentStatus = DAY_LOGS[activeDateStr][id] || item.status || "PENDING";

  if (currentStatus === "DONE") {
    DAY_LOGS[activeDateStr][id] = "PENDING";
    item.status = "PENDING";
    // Refund stock if medicine
    if (item.isStockTracked) item.currentStock += item.dose;
  } else {
    DAY_LOGS[activeDateStr][id] = "DONE";
    item.status = "DONE";
    // Decrement stock if medicine
    if (item.isStockTracked && item.currentStock > 0) {
      item.currentStock = Math.max(0, item.currentStock - item.dose);
    }
  }

  renderQuadrants();
  renderInventory();
}

function markSkip(id) {
  const item = REMINDERS_DATA.find(i => i.id === id);
  if (item) {
    const y = currentActiveDate.getFullYear();
    const m = String(currentActiveDate.getMonth() + 1).padStart(2, "0");
    const d = String(currentActiveDate.getDate()).padStart(2, "0");
    const activeDateStr = `${y}-${m}-${d}`;

    if (!DAY_LOGS[activeDateStr]) DAY_LOGS[activeDateStr] = {};
    DAY_LOGS[activeDateStr][id] = "SKIPPED";
    item.status = "SKIPPED";
    renderQuadrants();
  }
}

function toggleQuadrant(quadrantId) {
  const card = document.getElementById(quadrantId);
  if (card) {
    card.classList.toggle("collapsed");
  }
}

// Snooze Sheet Controls
function openSnoozeSheet(id) {
  activeSnoozeId = id;
  const item = REMINDERS_DATA.find(i => i.id === id);
  document.getElementById("sheetTitle").textContent = `Snooze "${item.title}"`;
  document.getElementById("snoozeBottomSheet").classList.add("active");
}

function closeSnoozeSheet() {
  document.getElementById("snoozeBottomSheet").classList.remove("active");
  activeSnoozeId = null;
}

function applySnooze(minutes) {
  const item = REMINDERS_DATA.find(i => i.id === activeSnoozeId);
  if (item) {
    item.status = "SNOOZED";
    closeSnoozeSheet();
    renderQuadrants();
    showToast(`⏱ Snoozed "${item.title}" for ${minutes} mins`);
  }
}

function applySkip() {
  const item = REMINDERS_DATA.find(i => i.id === activeSnoozeId);
  if (item) {
    item.status = "SKIPPED";
    closeSnoozeSheet();
    renderQuadrants();
    showToast(`✕ Skipped "${item.title}" for today`);
  }
}

// Category Selection in Add/Edit Form
function selectCategory(cat) {
  selectedFormCategory = cat;
  document.querySelectorAll(".cat-chip").forEach(c => {
    c.classList.toggle("active", c.getAttribute("data-cat") === cat);
  });

  const stockSection = document.getElementById("medicineStockSection");
  
  // Specific Rule: ONLY Health & Meds supports inventory counts! General reminders do NOT.
  if (cat === "HEALTH") {
    stockSection.style.display = "block";
  } else {
    stockSection.style.display = "none";
  }
}

function toggleStockFields(checked) {
  document.getElementById("stockInputFields").style.display = checked ? "flex" : "none";
}

function autoAssignQuadrant(timeValue) {
  if (!timeValue) return;
  const hour = parseInt(timeValue.split(":")[0], 10);
  const badge = document.getElementById("displayQuadrantBadge");

  if (hour >= 6 && hour < 12) {
    badge.innerHTML = "<span>🌅 Morning</span>";
  } else if (hour >= 12 && hour < 18) {
    badge.innerHTML = "<span>☀️ Afternoon</span>";
  } else if (hour >= 18 && hour < 22) {
    badge.innerHTML = "<span>🌆 Evening</span>";
  } else {
    badge.innerHTML = "<span>🌙 Night</span>";
  }
}

// Schedule Mode Handlers
function setScheduleMode(mode) {
  currentScheduleMode = mode;
  const btnRec = document.getElementById("btnModeRecurring");
  const btnSing = document.getElementById("btnModeSingle");
  const secRec = document.getElementById("recurringModeSection");
  const secSing = document.getElementById("singleModeSection");

  if (mode === "RECURRING") {
    btnRec.classList.add("active");
    btnSing.classList.remove("active");
    secRec.style.display = "block";
    secSing.style.display = "none";
  } else {
    btnSing.classList.add("active");
    btnRec.classList.remove("active");
    secSing.style.display = "block";
    secRec.style.display = "none";
  }
}

function setQuickDays(preset) {
  document.querySelectorAll(".quick-chip").forEach(c => c.classList.remove("active"));
  const allDayBtns = document.querySelectorAll("#recurringDaysRow .day-circle");

  if (preset === "DAILY") {
    document.getElementById("chipDaily").classList.add("active");
    selectedRecurringDays = [1, 2, 3, 4, 5, 6, 7];
    allDayBtns.forEach(btn => btn.classList.add("active"));
  } else if (preset === "WEEKDAYS") {
    document.getElementById("chipWeekdays").classList.add("active");
    selectedRecurringDays = [1, 2, 3, 4, 5];
    allDayBtns.forEach(btn => {
      const d = parseInt(btn.getAttribute("data-day"), 10);
      if (d <= 5) btn.classList.add("active");
      else btn.classList.remove("active");
    });
  } else if (preset === "WEEKENDS") {
    document.getElementById("chipWeekends").classList.add("active");
    selectedRecurringDays = [6, 7];
    allDayBtns.forEach(btn => {
      const d = parseInt(btn.getAttribute("data-day"), 10);
      if (d >= 6) btn.classList.add("active");
      else btn.classList.remove("active");
    });
  }

  updateSelectedDaysLabel();
}

function toggleDay(btn) {
  const day = parseInt(btn.getAttribute("data-day"), 10);
  const isCurrentlyActive = btn.classList.contains("active");

  if (isCurrentlyActive && selectedRecurringDays.length === 1) {
    showToast("At least 1 recurring day must be selected");
    return;
  }

  if (isCurrentlyActive) {
    btn.classList.remove("active");
    selectedRecurringDays = selectedRecurringDays.filter(d => d !== day);
  } else {
    btn.classList.add("active");
    selectedRecurringDays.push(day);
    selectedRecurringDays.sort((a, b) => a - b);
  }

  document.querySelectorAll(".quick-chip").forEach(c => c.classList.remove("active"));
  if (selectedRecurringDays.length === 7) document.getElementById("chipDaily").classList.add("active");
  else if (selectedRecurringDays.length === 5 && selectedRecurringDays.every(d => d <= 5)) document.getElementById("chipWeekdays").classList.add("active");
  else if (selectedRecurringDays.length === 2 && selectedRecurringDays.includes(6) && selectedRecurringDays.includes(7)) document.getElementById("chipWeekends").classList.add("active");

  updateSelectedDaysLabel();
}

function updateSelectedDaysLabel() {
  const labelEl = document.getElementById("selectedDaysLabel");
  if (!labelEl) return;

  const names = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
  if (selectedRecurringDays.length === 7) {
    labelEl.textContent = "Everyday (Mon, Tue, Wed, Thu, Fri, Sat, Sun)";
  } else if (selectedRecurringDays.length === 5 && selectedRecurringDays.every(d => d <= 5)) {
    labelEl.textContent = "Every Weekday (Mon–Fri)";
  } else if (selectedRecurringDays.length === 2 && selectedRecurringDays.includes(6) && selectedRecurringDays.includes(7)) {
    labelEl.textContent = "Every Weekend (Sat, Sun)";
  } else {
    const list = selectedRecurringDays.map(d => names[d - 1]).join(", ");
    labelEl.textContent = `Every ${list}`;
  }
}

function toggleEndDateField(checked) {
  const wrap = document.getElementById("endDateInputWrap");
  if (wrap) wrap.style.display = checked ? "block" : "none";
}

function formatDaysSummary(days, hasEndDate, endDate) {
  const names = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
  let baseText = "Daily";

  if (days.length === 7) baseText = "Daily";
  else if (days.length === 5 && days.every(d => d <= 5)) baseText = "Mon–Fri";
  else if (days.length === 2 && days.includes(6) && days.includes(7)) baseText = "Sat–Sun";
  else baseText = `Every ${days.map(d => names[d - 1]).join(", ")}`;

  if (hasEndDate && endDate) {
    baseText += ` (until ${endDate})`;
  }
  return baseText;
}

function saveReminderForm() {
  const title = document.getElementById("inputTitle").value.trim();
  if (!title) {
    alert("Please enter a reminder title");
    return;
  }

  const time24 = document.getElementById("inputTime").value;
  const hour = parseInt(time24.split(":")[0], 10);
  const minute = time24.split(":")[1];
  const period = hour >= 12 ? "PM" : "AM";
  const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
  const formattedTime = `${displayHour}:${minute} ${period}`;

  let quadrant = "NIGHT";
  if (hour >= 6 && hour < 12) quadrant = "MORNING";
  else if (hour >= 12 && hour < 18) quadrant = "AFTERNOON";
  else if (hour >= 18 && hour < 22) quadrant = "EVENING";

  const isHealth = selectedFormCategory === "HEALTH";
  const isStockTracked = isHealth && document.getElementById("checkTrackStock").checked;

  let scheduleMode = currentScheduleMode;
  let singleDate = null;
  let recurringDays = [];
  let hasEndDate = false;
  let endDate = null;
  let recurringSummary = "";

  if (scheduleMode === "SINGLE") {
    singleDate = document.getElementById("inputSingleDate").value || "2026-11-01";
    recurringSummary = `1-Time • ${singleDate}`;
  } else {
    recurringDays = [...selectedRecurringDays];
    hasEndDate = document.getElementById("checkHasEndDate").checked;
    endDate = hasEndDate ? document.getElementById("inputEndDate").value : null;
    recurringSummary = formatDaysSummary(recurringDays, hasEndDate, endDate);
  }

  const newReminder = {
    id: Date.now(),
    title: title,
    notes: document.getElementById("inputNotes").value.trim(),
    time: formattedTime,
    time24: time24,
    quadrant: quadrant,
    scheduleMode: scheduleMode,
    singleDate: singleDate,
    recurringDays: recurringDays,
    hasEndDate: hasEndDate,
    endDate: endDate,
    recurringSummary: recurringSummary,
    category: selectedFormCategory,
    categoryLabel: selectedFormCategory === "HEALTH" ? "Health & Meds" : "General Reminder",
    icon: isHealth ? "💊" : "📌",
    color: isHealth ? "#EC4899" : "#6366F1",
    isStockTracked: isStockTracked,
    currentStock: isStockTracked ? parseInt(document.getElementById("inputCurrentStock").value, 10) || 30 : 0,
    dose: isStockTracked ? parseInt(document.getElementById("inputDosePerTake").value, 10) || 1 : 0,
    unit: isStockTracked ? document.getElementById("inputUnit").value : "",
    lowThreshold: isStockTracked ? parseInt(document.getElementById("inputLowThreshold").value, 10) || 5 : 0,
    status: "PENDING"
  };

  REMINDERS_DATA.push(newReminder);
  renderQuadrants();
  renderInventory();
  switchScreen("screenDashboard");
  showToast(`Created "${newReminder.title}" (${scheduleMode === 'SINGLE' ? '1-Time' : recurringSummary})`);
}

// Inventory Screen Render
function renderInventory() {
  const container = document.getElementById("inventoryList");
  container.innerHTML = "";

  const medicines = REMINDERS_DATA.filter(i => i.isStockTracked);

  document.getElementById("invTotalCount").textContent = `${medicines.length} Medicines`;
  const lowCount = medicines.filter(m => m.currentStock <= m.lowThreshold).length;
  document.getElementById("invLowCount").textContent = `${lowCount} Alert${lowCount === 1 ? '' : 's'}`;

  medicines.forEach(item => {
    const isLow = item.currentStock <= item.lowThreshold;
    const progressPct = Math.min(100, Math.round((item.currentStock / 40) * 100));

    const card = document.createElement("div");
    card.className = "supply-item-card";
    card.innerHTML = `
      <div class="supply-top">
        <div>
          <strong style="font-size:15px;">${item.title}</strong>
          <p style="font-size:12px; color:var(--text-muted); margin-top:2px;">
            Takes ${item.dose} ${item.unit} per reminder
          </p>
        </div>
        ${isLow ? `
          <span style="font-size:11px; font-weight:800; background:rgba(244,63,94,0.18); color:var(--accent-rose); padding:3px 8px; border-radius:6px;">
            ⚠️ Refill Alert
          </span>
        ` : `
          <span style="font-size:11px; font-weight:700; color:var(--accent-emerald);">In Stock</span>
        `}
      </div>

      <div class="supply-bar-wrap">
        <div class="supply-bar ${isLow ? 'low' : ''}" style="width: ${progressPct}%;"></div>
      </div>

      <div class="supply-footer">
        <span style="font-weight:700; color:${isLow ? 'var(--accent-rose)' : 'var(--text-main)'};">
          ${item.currentStock} ${item.unit} remaining
        </span>
        <div class="refill-chips">
          <button class="btn-refill-chip" onclick="refillMedicine(${item.id}, 10)">+10 Refill</button>
          <button class="btn-refill-chip" onclick="refillMedicine(${item.id}, 30)">+30 Refill</button>
        </div>
      </div>
    `;

    container.appendChild(card);
  });
}

function refillMedicine(id, amount) {
  const item = REMINDERS_DATA.find(i => i.id === id);
  if (item) {
    item.currentStock += amount;
    renderInventory();
    renderQuadrants();
    showToast(`Refilled +${amount} ${item.unit} for "${item.title}"`);
  }
}

// Screen Switcher
function switchScreen(screenId) {
  document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
  document.querySelectorAll(".nav-tab").forEach(t => t.classList.remove("active"));

  const target = document.getElementById(screenId);
  if (target) target.classList.add("active");

  const fab = document.getElementById("fabAdd");
  if (screenId === "screenDashboard") {
    fab.style.display = "flex";
    document.getElementById("tabHome").classList.add("active");
  } else {
    fab.style.display = "none";
    if (screenId === "screenAddEdit") {
      const timeVal = document.getElementById("inputTime") ? document.getElementById("inputTime").value : "08:00";
      autoAssignQuadrant(timeVal);
    }
    if (screenId === "screenNotifications") {
      const tab = document.getElementById("tabNotifications");
      if (tab) tab.classList.add("active");
    }
    if (screenId === "screenInventory") {
      const tab = document.getElementById("tabInventory");
      if (tab) tab.classList.add("active");
    }
    if (screenId === "screenAnalytics") {
      const tab = document.getElementById("tabAnalytics");
      if (tab) tab.classList.add("active");
    }
    if (screenId === "screenSettings") {
      const tab = document.getElementById("tabSettings");
      if (tab) tab.classList.add("active");
    }
  }
}

// Notification Screen Actions
function handleNotificationAction(action) {
  const card = document.getElementById("activeNotificationCard");
  const historyList = document.getElementById("notifHistoryList");
  const now = new Date();
  const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  if (action === "DONE") {
    toggleDone(5); // Mark Omega-3 done
    if (card) {
      card.style.opacity = "0.5";
      card.style.pointerEvents = "none";
    }
    if (historyList) {
      const item = document.createElement("div");
      item.className = "history-item";
      item.innerHTML = `
        <div class="history-icon done">✓</div>
        <div class="history-details">
          <strong>Omega-3 & Magnesium</strong>
          <p>Action: [✓ Done] taken from active alert card • ${timeStr}</p>
        </div>
      `;
      historyList.prepend(item);
    }
    showToast("✓ Marked Done! Dose decremented from inventory.");
  } else if (action === "SNOOZE") {
    if (card) {
      card.style.opacity = "0.5";
      card.style.pointerEvents = "none";
    }
    if (historyList) {
      const item = document.createElement("div");
      item.className = "history-item";
      item.innerHTML = `
        <div class="history-icon snooze">⏱</div>
        <div class="history-details">
          <strong>Omega-3 & Magnesium</strong>
          <p>Action: [⏱ Snooze 15m] reminder pushed • ${timeStr}</p>
        </div>
      `;
      historyList.prepend(item);
    }
    showToast("⏱ Snoozed for 15 minutes");
  } else if (action === "SKIP") {
    if (card) {
      card.style.opacity = "0.5";
      card.style.pointerEvents = "none";
    }
    if (historyList) {
      const item = document.createElement("div");
      item.className = "history-item";
      item.innerHTML = `
        <div class="history-icon skip">✕</div>
        <div class="history-details">
          <strong>Omega-3 & Magnesium</strong>
          <p>Action: [✕ Skip] skipped for tonight • ${timeStr}</p>
        </div>
      `;
      historyList.prepend(item);
    }
    showToast("✕ Routine skipped for tonight");
  }
}

// Fullscreen Alarm Simulation Modal
function openFullscreenAlarm() {
  playAlarmSound(true);
  const modal = document.getElementById("fullscreenAlarmModal");
  if (modal) modal.classList.add("active");
}

function dismissFullscreenAlarm(action) {
  const modal = document.getElementById("fullscreenAlarmModal");
  if (modal) modal.classList.remove("active");

  if (action === "DONE") {
    handleNotificationAction("DONE");
  } else if (action === "SNOOZE") {
    handleNotificationAction("SNOOZE");
  }
}

// Audible Web Audio Alarm Synthesizer
function playAlarmSound(isUrgent = false) {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();
    if (ctx.state === 'suspended') {
      ctx.resume();
    }
    if (isUrgent) {
      // 3-pulse urgent alarm sound
      [0, 0.18, 0.36].forEach(timeOffset => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = "square";
        osc.frequency.setValueAtTime(880, ctx.currentTime + timeOffset);
        gain.gain.setValueAtTime(0.18, ctx.currentTime + timeOffset);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + timeOffset + 0.12);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(ctx.currentTime + timeOffset);
        osc.stop(ctx.currentTime + timeOffset + 0.12);
      });
    } else {
      // Pleasant two-tone chime (523Hz -> 659Hz)
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(523.25, ctx.currentTime);
      osc.frequency.setValueAtTime(659.25, ctx.currentTime + 0.12);
      gain.gain.setValueAtTime(0.2, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.45);
    }
  } catch (err) {
    console.log("Audio alert playback:", err);
  }
}

// Sound Tone Selector
function selectSound(btn) {
  document.querySelectorAll(".sound-pill").forEach(p => p.classList.remove("active"));
  btn.classList.add("active");
  playAlarmSound(btn.textContent.includes("Alarm") || btn.textContent.includes("Urgent"));
  showToast(`🔊 Sound preview: ${btn.textContent.trim()}`);
}

// Notification Banner Simulation
function triggerTestNotificationBanner() {
  playAlarmSound(false);
  const banner = document.getElementById("simulatedNotification");
  banner.classList.add("show");
}

function notifActionDone() {
  const banner = document.getElementById("simulatedNotification");
  banner.classList.remove("show");
  toggleDone(5); // Mark Omega-3 done
  showToast("✓ Marked reminder Done from notification bar!");
}

function notifActionSnooze() {
  const banner = document.getElementById("simulatedNotification");
  banner.classList.remove("show");
  showToast("⏱ Snoozed 15 mins from notification action!");
}

function notifActionSkip() {
  const banner = document.getElementById("simulatedNotification");
  banner.classList.remove("show");
  showToast("✕ Skipped from notification action!");
}

function showToast(msg) {
  const existing = document.getElementById("appToast");
  if (existing) existing.remove();

  const toast = document.createElement("div");
  toast.id = "appToast";
  toast.style.cssText = `
    position: absolute;
    bottom: 84px;
    left: 20px;
    right: 20px;
    background: #1E293B;
    border: 1px solid rgba(255,255,255,0.18);
    color: white;
    padding: 12px 16px;
    border-radius: 14px;
    font-size: 13px;
    font-weight: 600;
    text-align: center;
    box-shadow: 0 8px 24px rgba(0,0,0,0.8);
    z-index: 100;
    transition: opacity 0.3s;
  `;
  toast.textContent = msg;
  document.querySelector(".mobile-frame").appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    setTimeout(() => toast.remove(), 300);
  }, 2200);
}
