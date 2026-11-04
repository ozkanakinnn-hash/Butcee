# Product Requirements Document — Shift-Worker Productivity & Wellness App

| | |
|---|---|
| **Working title** | Nöbet (recommended — see §1) |
| **Document type** | Product Requirements Document (PRD) — MVP |
| **Version** | 1.0 · 2026-11-04 |
| **Platforms** | iOS 15+ / Android 8.0+ (API 26) via Flutter |
| **Languages** | Turkish (tr) and English (en), full parity |
| **Status** | Draft for review |

---

## Table of contents
1. [App name suggestions & product description](#1-app-name-suggestions--product-description)
2. [Prioritized feature list](#2-prioritized-feature-list)
3. [User flows for the main screens](#3-user-flows-for-the-main-screens)
4. [Screen list](#4-screen-list)
5. [Information architecture](#5-information-architecture)
6. [Color palette & design direction](#6-color-palette--design-direction)
7. [Onboarding flow](#7-onboarding-flow)
8. [Subscription pricing structure](#8-subscription-pricing-structure)
9. [Appendix A — Technical architecture (Flutter)](#appendix-a--technical-architecture-flutter)
10. [Appendix B — Business rules & calculations](#appendix-b--business-rules--calculations)
11. [Appendix C — Localization requirements](#appendix-c--localization-requirements)
12. [Appendix D — Success metrics, risks & roadmap](#appendix-d--success-metrics-risks--roadmap)

---

## 0. Problem statement & goals

**Problem.** Around 1 in 5 workers works outside a standard 9-to-5 schedule. Rotating and night shifts disrupt
circadian rhythm, cause chronic sleep debt, and make basic life planning (appointments, family time, meals)
hard. Generic calendar and sleep apps assume a fixed day/night routine and don't understand that "Tuesday" may
mean *sleeping 09:00–16:00 after a night shift*.

**Product vision.** A calm, trustworthy companion that understands the user's rotation and answers three daily
questions in one glance:
1. *What is my next shift?*
2. *How rested am I?* (sleep debt)
3. *What should I do now to feel better?* (shift-aware nudges)

**MVP goals**
| Goal | Measure |
|---|---|
| Fast setup | User adds a full month of shifts in < 2 minutes (via pattern) |
| Daily habit | ≥ 35 % of active users log sleep or a check-in on ≥ 4 days/week |
| Trust | Zero required account; data stays on device by default |
| Viable business | 4–6 % free→premium conversion by month 3 |

**Non-goals (MVP):** wearable/HealthKit auto-import, team/rostering for managers, social features, medical
diagnosis. (Some appear in the roadmap, Appendix D.)

### Personas
| Persona | Context | Key need |
|---|---|---|
| **Ayşe, 31 — ICU nurse** | 12-h shifts, 2 days / 2 nights / 4 off, Istanbul | Sleep debt visibility, trustworthy and private, quick one-hand logging after a night shift |
| **Mehmet, 44 — factory operator** | Weekly rotating 3-shift (morning/evening/night), Bursa | Simple pattern setup, reminders for the week the shift changes |
| **Daniel, 27 — hotel night auditor** | Mostly nights, irregular days off, English speaker | Plan life around shifts, energy trends |
| **Zeynep, 38 — security supervisor** | 24/48 rotation | Custom pattern support, monthly overview for family planning |

---

## 1. App name suggestions & product description

| # | Name | Rationale | Works in TR & EN? |
|---|---|---|---|
| 1 | **Nöbet** *(recommended)* | Turkish for "shift / on-duty watch" — instantly understood by Turkish nurses, guards, doctors. Short, ownable, warm. EN store title: *"Nöbet – Shift Planner & Sleep"* | Yes (strong TR identity; EN users read it as a brand name) |
| 2 | **ShiftRest** | Says exactly what it does: shifts + rest. Easy ASO for "shift" keyword. | EN-first, pronounceable in TR |
| 3 | **Rotawell** | *Rotation* + *well-being*. Friendly, wellness-forward. | Yes |
| 4 | **Nightline** | Evokes night shifts, calm, guidance. Risk: name collisions (helplines). | Mostly EN |
| 5 | **Vardiya+** | Turkish for "shift"; very clear for factory/hospitality workers. | TR-first |

**Recommendation:** **Nöbet** as the brand, with the English store subtitle doing the explaining. Back-up:
**ShiftRest** if an English-first launch is chosen.

### Short description

- **EN (store, 80 chars):** *Shift calendar, sleep debt & energy tracker built for nurses and night workers.*
- **TR (mağaza, 80 karakter):** *Hemşireler ve gece çalışanları için vardiya takvimi, uyku borcu ve enerji takibi.*

**Long description (EN).** Nöbet is the calm, private companion for people who work rotating and night
shifts. Set up your rotation in seconds with ready-made patterns like 2-2-3 or 4-on-4-off, log your sleep with
one tap, and see your sleep debt at a glance. Daily energy check-ins reveal how your shifts really affect you,
and smart reminders tell you when to wind down before a night shift. Works fully offline. No account required.
Available in English and Turkish.

**Uzun açıklama (TR).** Nöbet, dönüşümlü ve gece vardiyasında çalışanlar için sakin ve gizliliğe önem veren bir
yardımcıdır. 2-2-3 veya 4 gün çalış / 4 gün izin gibi hazır düzenlerle vardiyanızı saniyeler içinde kurun, uykunuzu
tek dokunuşla kaydedin ve uyku borcunuzu bir bakışta görün. Günlük enerji kayıtları vardiyaların sizi nasıl
etkilediğini gösterir; akıllı hatırlatıcılar gece vardiyası öncesi ne zaman dinlenmeniz gerektiğini söyler.
Tamamen çevrimdışı çalışır. Hesap gerekmez. Türkçe ve İngilizce.

---

## 2. Prioritized feature list

Legend: **M** = Must-have (MVP), **N** = Nice-to-have (post-MVP / v1.x), **F** = Free, **P** = Premium

### 2.1 Shift Calendar
| ID | Feature | Priority | Tier |
|---|---|---|---|
| CAL-01 | Add / edit / delete a shift on a day (tap day → bottom sheet) | M | F |
| CAL-02 | Built-in shift types: **Day, Evening, Night, Off** + **Custom** (name, color, icon, start/end time) | M | F |
| CAL-03 | Shifts crossing midnight handled correctly (e.g. 20:00–08:00 belongs to start day) | M | F |
| CAL-04 | Rotation pattern generator: pick pattern, start date, length (e.g. 3 months / until date) | M | F |
| CAL-05 | Built-in patterns: 2-2-3 (Panama), 4-on-4-off, 2-2 (Day/Night), 24/48, 12/36, 3-shift weekly rotation, 5-2 day, DuPont, Continental (2-2-3 / 2-3-2) | M | F (3 patterns) / P (full library) |
| CAL-06 | Monthly view (color dots/blocks) and weekly view (time bars) | M | F |
| CAL-07 | Color-coded shifts with a high-contrast palette + icon (color is never the only signal) | M | F |
| CAL-08 | User shift templates (save a custom shift and reuse) | M | F (up to 3) / P (unlimited) |
| CAL-09 | "Today" card: current/next shift with countdown | M | F |
| CAL-10 | Bulk edit (select multiple days → apply shift / clear) | M | F |
| CAL-11 | Swap / override a single day without breaking the pattern | M | F |
| CAL-12 | Notes on a day (e.g. "swapped with Elif") | N | F |
| CAL-13 | Home-screen widget (next shift + sleep debt) | N | P |
| CAL-14 | Export calendar (.ics) / share month as image | N | P |
| CAL-15 | Overtime / hours worked summary (for payroll check) | N | P |
| CAL-16 | Import from device calendar | N | P |

### 2.2 Sleep Tracking
| ID | Feature | Priority | Tier |
|---|---|---|---|
| SLP-01 | One-tap "Going to sleep" / "I'm awake" logging (timer persists if app is killed) | M | F |
| SLP-02 | Manual entry / edit of start & end (for forgotten logs) | M | F |
| SLP-03 | Multiple sleep blocks per day, incl. naps (core sleep vs. nap flag) | M | F |
| SLP-04 | Quality rating: 5-step emoji scale (😫 😕 😐 🙂 😴) | M | F |
| SLP-05 | Sleep debt — basic: rolling 7-day debt vs. personal sleep goal | M | F |
| SLP-06 | Sleep history list (last 30 days free) | M | F (30 d) / P (unlimited) |
| SLP-07 | Basic insight cards ("You slept 1.8 h less after night shifts") | M | F (1/week) / P (all) |
| SLP-08 | Advanced sleep debt: 14-day weighted debt, recovery plan, debt by shift type | M | P |
| SLP-09 | Optional "wake-up mood" tag (groggy / ok / fresh) | N | F |
| SLP-10 | Apple Health / Health Connect read/write | N | P |
| SLP-11 | Recommended sleep window per upcoming shift (anchor-sleep suggestion) | N | P |

### 2.3 Energy & Daily Check-in
| ID | Feature | Priority | Tier |
|---|---|---|---|
| ENG-01 | Daily energy check-in, 1–10 slider with large thumb + descriptive labels | M | F |
| ENG-02 | Optional short note (max 280 chars) | M | F |
| ENG-03 | Optional quick tags: caffeine, exercise, stress, ate late, alcohol | M | F |
| ENG-04 | Multiple check-ins/day allowed; "daily energy" = average | M | F |
| ENG-05 | Energy vs. shift-type correlation (avg energy on/after Day/Night/Off) | M | P |
| ENG-06 | Energy vs. sleep-hours correlation scatter | N | P |
| ENG-07 | Mood tracking (separate from energy) | N | F |

### 2.4 Smart Reminders
| ID | Feature | Priority | Tier |
|---|---|---|---|
| REM-01 | Basic shift reminder: "Your shift starts in X" (fixed lead time) | M | F |
| REM-02 | Daily check-in reminder at user-chosen time | M | F |
| REM-03 | **Shift-aware rules engine** (rule per shift type & transition, see Appendix B.3) | M | P |
| REM-04 | Pre-night-shift nap reminder ("Night shift tonight — a 90-min nap before 18:00 helps") | M | P |
| REM-05 | Wind-down reminder computed from next shift start & sleep goal | M | P |
| REM-06 | Caffeine cut-off reminder (e.g. 6 h before planned sleep) | M | P |
| REM-07 | "Forgot to log?" reminder if sleep timer running > 14 h | M | F |
| REM-08 | Quiet hours — never notify during logged/planned sleep | M | F |
| REM-09 | Per-shift-type customizable lead times | M | P |
| REM-10 | Light-exposure tips (sunglasses on the way home after night shift) | N | P |

### 2.5 Statistics & Insights
| ID | Feature | Priority | Tier |
|---|---|---|---|
| STA-01 | This-week summary: total sleep, avg sleep, # night shifts, avg energy | M | F |
| STA-02 | Weekly sleep-hours bar chart | M | F |
| STA-03 | Monthly view, shift distribution (donut), trends | M | P |
| STA-04 | Sleep debt trend line (30/90 days) | M | P |
| STA-05 | Energy by shift type (bar) | M | P |
| STA-06 | Export data (CSV + PDF summary for doctor/occupational health) | M | P |
| STA-07 | Monthly "Shift Report" recap card (shareable) | N | P |

### 2.6 Onboarding, Settings & Platform
| ID | Feature | Priority | Tier |
|---|---|---|---|
| ONB-01 | 3-screen value intro, skippable | M | F |
| ONB-02 | Language auto-detect (tr/en), switchable | M | F |
| ONB-03 | Role selection (personalizes copy & default patterns) | M | F |
| ONB-04 | Quick first-rotation setup (pattern + start date) | M | F |
| ONB-05 | Sleep goal setup (default 7 h 30 min) | M | F |
| ONB-06 | Notification permission with pre-prompt explaining value | M | F |
| SET-01 | Language switch (System / Türkçe / English) — instant, no restart | M | F |
| SET-02 | Theme: System / Light / Dark / **Night (red-shifted, ultra-dim)** | M | F (System/Light/Dark) / P (Night) |
| SET-03 | Time format 12/24 h, week start Mon/Sun (defaults from locale) | M | F |
| SET-04 | App lock (biometric / PIN) | M | F |
| SET-05 | Local backup / restore (file) | M | F |
| SET-06 | Encrypted cloud backup & multi-device sync | N | P |
| SET-07 | Delete all data | M | F |
| PLT-01 | Fully offline; no account required | M | F |
| PLT-02 | Accessibility: dynamic type, screen reader labels, 48 dp targets | M | F |
| PLT-03 | Privacy-friendly analytics (opt-in, anonymous) | M | F |

---

## 3. User flows for the main screens

### 3.1 Primary navigation
Bottom navigation bar with 4 tabs + a central floating action button (thumb zone):

```
┌──────────────────────────────────────────┐
│  Today   Calendar   [ + ]   Sleep   Stats │   (Settings via avatar/gear on Today)
└──────────────────────────────────────────┘
```
The **[+] Quick Action** opens a bottom sheet: *Start sleep · Log sleep · Energy check-in · Add shift*.

### 3.2 Flow A — First launch → first value (target < 2 min)
```
Splash → Welcome (3 value slides, skippable)
       → Language confirm (pre-selected from device)
       → "What's your job?" (Nurse / Factory / Security / Hospitality / Other)
       → "How does your rotation work?"
             ├─ Pick a pattern (suggested by role) → choose start date + which day of cycle today is → preview month → Save
             ├─ "I'll add shifts manually" → Calendar (empty, coach-mark on a day)
             └─ "Skip for now"
       → Shift times (pre-filled per pattern, editable)
       → Sleep goal (slider, default 7 h 30)
       → Notification pre-prompt → OS permission
       → Today screen (with first insight placeholder + "Log your last sleep" CTA)
```

### 3.3 Flow B — Adding/editing shifts
```
Calendar (month) → tap a day
   → Bottom sheet: [Day] [Evening] [Night] [Off] [Template…] [Custom]
        → tap type = saved instantly (undo snackbar 5 s)
        → long-press type = edit times for this day only
Calendar → long-press a day → multi-select mode → choose days → apply type / clear
Calendar → ⋮ → "Apply rotation pattern" → Pattern Library → configure → preview diff → Apply
          (conflict prompt: "Overwrite 12 existing shifts?" Overwrite / Only empty days / Cancel)
```

### 3.4 Flow C — Logging sleep (the most frequent flow, must be one-hand & dim-friendly)
```
Option 1 (live):  [+] → "Start sleep" → confirm → screen dims to Sleep Mode (big clock, "I'm awake")
                  Wake → "I'm awake" → Quality emoji (1 tap) → optional "nap?" toggle → Saved → Today updates debt
Option 2 (after the fact): [+] → "Log sleep" → start/end wheels (smart defaults: after last shift end → +8 h)
                  → Quality → Save
Option 3 (from notification): "Good morning — how did you sleep?" → actions [Log 😴 / Edit]
```

### 3.5 Flow D — Daily energy check-in
```
Notification at chosen time OR Today card "How's your energy?"
  → Slider 1–10 (labels: 1 Exhausted … 5 Okay … 10 Fully charged)
  → Optional tags (chips) → Optional note → Save (≤ 3 taps without note)
  → Confirmation micro-feedback: "Logged. 3-day streak."
```

### 3.6 Flow E — Smart reminders setup (Premium)
```
Settings → Reminders
  → Basic (free): Shift start lead time · Daily check-in time · Forgot-to-log
  → Smart (premium, lock icon → Paywall if free):
       Per shift type: Day / Evening / Night / Off
         ├─ Bedtime wind-down (auto / custom offset)
         ├─ Pre-shift nap (Night only, default on)
         ├─ Caffeine cut-off (hours before planned sleep)
         └─ Shift start lead time
       Transitions: "Day → Night switch" alert 1 day before
  → Preview: "Next 3 reminders" list
```

### 3.7 Flow F — Insights & stats
```
Stats tab → segmented control [Week | Month]
  → Summary tiles (sleep avg, sleep debt, night shifts, avg energy)
  → Sleep hours chart → tap bar → day detail
  → Energy by shift type (premium blur + "Unlock")
  → Insight cards (swipe)
  → ⋮ → Export (premium) → CSV / PDF → share sheet
```

### 3.8 Flow G — Upgrade
```
Any lock icon / premium card → Paywall (contextual headline for the feature tapped)
  → Plan selector (Annual pre-selected, Monthly, Lifetime) → 7-day free trial (annual)
  → Store purchase sheet → Success screen ("Smart reminders are on — here's your next one") → back to origin
```

---

## 4. Screen list

| # | Screen | Purpose | Key components | MVP |
|---|---|---|---|---|
| S-01 | Splash | Brand, load local DB | Logo, theme-aware background | ✔ |
| S-02 | Welcome carousel (3) | Value proposition | Illustration, title, body, Skip / Next | ✔ |
| S-03 | Language | Confirm tr/en | Two large cards | ✔ |
| S-04 | Role selection | Personalize | 5 large tiles with icons | ✔ |
| S-05 | Rotation setup | First shifts | Pattern list (role-suggested), cycle-day picker, month preview | ✔ |
| S-06 | Shift times | Set start/end per type | Time pickers, color chips | ✔ |
| S-07 | Sleep goal | Personal target | Slider 5–10 h, guidance text | ✔ |
| S-08 | Notification pre-prompt | Permission | Example notification mock, Allow / Not now | ✔ |
| S-09 | **Today (Home)** | Daily dashboard | Next-shift card with countdown, Sleep-debt ring, Energy card, Smart tip, Upcoming 3 days strip | ✔ |
| S-10 | Calendar — Month | Overview | Color-coded grid, legend, pattern indicator, today highlight | ✔ |
| S-11 | Calendar — Week | Detail | 7 rows with time bars (shift + logged sleep overlay) | ✔ |
| S-12 | Day detail sheet | Edit a day | Shift type chips, times, note, logged sleep & energy for that day | ✔ |
| S-13 | Pattern library | Choose rotation | Pattern cards with mini preview, premium badges | ✔ |
| S-14 | Pattern configurator | Apply rotation | Start date, cycle offset, duration, shift times, conflict handling | ✔ |
| S-15 | Shift templates | Manage types | List, add/edit (name, color, icon, times, paid hours) | ✔ |
| S-16 | Quick action sheet | Fast entry | 4 large actions | ✔ |
| S-17 | Sleep Mode (active) | Running sleep timer | Ultra-dim, huge "I'm awake" button, elapsed time | ✔ |
| S-18 | Sleep log / edit | Manual entry | Start/end wheels, nap toggle, quality emojis | ✔ |
| S-19 | Sleep tab | History & debt | Debt ring, 7-day bars, history list, insights | ✔ |
| S-20 | Energy check-in | Log energy | Slider, tags, note | ✔ |
| S-21 | Stats tab | Insights | Week/Month toggle, tiles, charts, insight cards | ✔ |
| S-22 | Insight detail | Explain an insight | Chart + plain-language explanation + tip | ✔ |
| S-23 | Paywall | Upgrade | Contextual header, benefits list, plans, trial, restore, terms | ✔ |
| S-24 | Settings | Preferences | Sections (see IA) | ✔ |
| S-25 | Reminders settings | Notification rules | Basic + Smart sections, preview list | ✔ |
| S-26 | Appearance | Theme | System/Light/Dark/Night, preview | ✔ |
| S-27 | Language | Switch | System / Türkçe / English | ✔ |
| S-28 | Data & privacy | Control | Backup, restore, export, delete all, analytics opt-in | ✔ |
| S-29 | App lock | Security | Biometric toggle, PIN | ✔ |
| S-30 | About / Help | Trust | FAQ, sleep-science sources, disclaimer, contact, version | ✔ |
| S-31 | Home widget | Glanceable | Next shift + debt | — (v1.1) |

### Key screen wireframe — Today (S-09)
```
┌─────────────────────────────────┐
│ Good evening, Ayşe          ⚙   │
│ Tue, 4 Nov                      │
├─────────────────────────────────┤
│ 🌙 NIGHT SHIFT · tonight         │
│ 20:00 – 08:00                   │
│ Starts in 3 h 12 min            │
│ ▸ Tip: a 60–90 min nap now helps │
├───────────────┬─────────────────┤
│  Sleep debt   │  Energy today   │
│   (  4.5 h )  │   6 / 10        │
│  last 7 days  │  [ Check in ]   │
├───────────────┴─────────────────┤
│ Next days:  Wed🌙  Thu☀  Fri○  Sat○│
└─────────────────────────────────┘
        [ Today Calendar (+) Sleep Stats ]
```

---

## 5. Information architecture

```
Nöbet
├── Onboarding (first run only)
│   ├── Welcome (3)
│   ├── Language
│   ├── Role
│   ├── Rotation setup → Shift times
│   ├── Sleep goal
│   └── Notification permission
│
├── Today (Home)
│   ├── Current / next shift card ──────────────► Day detail
│   ├── Sleep debt summary ─────────────────────► Sleep tab
│   ├── Energy card ────────────────────────────► Energy check-in
│   ├── Smart tip (contextual) ─────────────────► Insight detail
│   ├── Upcoming days strip ────────────────────► Calendar (week)
│   └── Settings (gear)
│
├── Calendar
│   ├── Month view ⇄ Week view
│   ├── Day detail (sheet): shift · times · note · sleep · energy
│   ├── Bulk edit mode
│   ├── Apply pattern → Pattern library → Configurator
│   └── Shift templates (manage types)
│
├── (+) Quick actions
│   ├── Start sleep → Sleep Mode → Wake & rate
│   ├── Log sleep (manual)
│   ├── Energy check-in
│   └── Add shift (today/selected date)
│
├── Sleep
│   ├── Sleep debt (basic | advanced🔒)
│   ├── Last 7 days chart
│   ├── History list → Sleep log edit
│   └── Sleep insights
│
├── Stats
│   ├── Week | Month🔒
│   ├── Summary tiles
│   ├── Charts: sleep hours · shift mix🔒 · energy by shift🔒 · debt trend🔒
│   ├── Insight cards → Insight detail
│   └── Export🔒 (CSV / PDF)
│
└── Settings
    ├── Premium (status / upgrade → Paywall)
    ├── Profile: name, role, sleep goal
    ├── Shifts: templates, default times, week start
    ├── Reminders: basic · smart🔒
    ├── Appearance: System / Light / Dark / Night🔒
    ├── Language: System / Türkçe / English
    ├── Privacy & security: app lock, analytics opt-in
    ├── Data: backup, restore, export🔒, delete all
    └── About & help: FAQ, sources, disclaimer, contact, licenses
```
🔒 = Premium

### 5.1 Data model (local, offline-first)

| Entity | Fields (key) |
|---|---|
| `ShiftType` | id, nameKey / customName, color, icon, defaultStart, defaultEnd, isOff, isNight (auto if overlaps 00:00–05:00), paidHours, isBuiltIn, sortOrder |
| `Shift` | id, date (local date of start), shiftTypeId, start (DateTime), end (DateTime), note, patternRunId?, isOverride |
| `Pattern` | id, nameKey, sequence `[shiftTypeId|OFF]`, cycleLength, isPremium, isBuiltIn |
| `PatternRun` | id, patternId, startDate, cycleOffset, endDate, createdAt |
| `SleepEntry` | id, start, end, quality (1–5), isNap, wakeMood?, source (live/manual/health), note |
| `EnergyEntry` | id, timestamp, level (1–10), tags[], note |
| `ReminderRule` | id, type (shiftStart, windDown, nap, caffeine, checkIn, forgotLog, transition), shiftTypeId?, offsetMinutes, enabled |
| `ScheduledNotification` | id, ruleId, fireAt, payload (for reconciliation) |
| `Settings` | locale, theme, timeFormat, weekStart, sleepGoalMinutes, role, onboardingDone, appLock, analyticsOptIn |
| `Entitlement` | isPremium, plan, expiresAt, lastVerifiedAt (cached for offline) |

All timestamps stored in UTC + the device time-zone ID at creation, so travel/DST changes don't corrupt
shift times.

---

## 6. Color palette & design direction

### 6.1 Design principles
1. **Calm over clever** — soft blues, generous spacing, no aggressive reds or streak shaming.
2. **Glanceable** — the Today screen answers "next shift / how rested / what now" in < 3 seconds.
3. **Thumb-first** — primary actions in the lower 40 % of the screen; min tap target 48×48 dp (prefer 56 dp
   for primary buttons).
4. **Night-safe** — dark mode is a first-class citizen; a special *Night theme* reduces blue light and brightness
   for 3 a.m. use on the ward.
5. **Clinical trust** — clear typography, honest data, sources for advice, no medical claims, privacy messaging.
6. **Never color alone** — every shift type has color **+ icon + letter** (D/E/N/O) for color-blind users.

### 6.2 Core palette

**Light theme**
| Token | Hex | Use |
|---|---|---|
| `primary` | `#3B6FB6` | Buttons, active tab, links |
| `primaryContainer` | `#DCE7F7` | Selected chips, cards highlight |
| `onPrimary` | `#FFFFFF` | Text on primary |
| `background` | `#F6F8FB` | App background |
| `surface` | `#FFFFFF` | Cards, sheets |
| `surfaceVariant` | `#EBEFF5` | Inputs, secondary cards |
| `textPrimary` | `#1A2433` | Headlines, body |
| `textSecondary` | `#5B6878` | Captions |
| `outline` | `#C9D1DC` | Dividers |
| `success` | `#3E9B7A` | Good sleep, goal met |
| `warning` | `#D69A2D` | Moderate debt |
| `alert` | `#C8605A` | High debt (muted, never neon) |

**Dark theme (default at night when "System")**
| Token | Hex | Use |
|---|---|---|
| `primary` | `#8AB4F0` | Buttons, active tab |
| `primaryContainer` | `#24406A` | Selected chips |
| `onPrimary` | `#0B1A2E` | Text on primary |
| `background` | `#0E141C` | App background (not pure black — reduces smearing on OLED scroll) |
| `surface` | `#161E29` | Cards |
| `surfaceVariant` | `#1F2935` | Inputs |
| `textPrimary` | `#E8EEF6` | Body (contrast ≈ 15:1 on background) |
| `textSecondary` | `#9AA8B8` | Captions (≥ 4.5:1) |
| `outline` | `#2C3846` | Dividers |
| `success` | `#6CC4A1` | |
| `warning` | `#E8B65A` | |
| `alert` | `#E38B85` | |

**Night theme (premium) — red-shifted for use in dark rooms/wards**
| Token | Hex |
|---|---|
| `background` | `#000000` |
| `surface` | `#120A08` |
| `primary` | `#D9785F` |
| `textPrimary` | `#E6B8A8` |
| `textSecondary` | `#9E7468` |
Auto-enables optionally during logged Sleep Mode and between 22:00–06:00.

### 6.3 Shift-type colors (consistent across themes; tuned per theme for contrast)
| Shift | Icon | Light | Dark | Letter |
|---|---|---|---|---|
| Day | ☀ sun | `#F2B33D` | `#F5C36A` | D / G (Gündüz) |
| Evening | 🌇 sunset | `#E07B53` | `#EE9B78` | E / A (Akşam) |
| Night | 🌙 moon | `#4B5BC4` | `#8C97F0` | N / G (Gece)* |
| Off | ○ circle | `#9AA7B4` | `#5E6B78` | O / İ (İzin) |
| Custom 1–6 | user icon | teal `#2FA3A0`, purple `#8B66C9`, pink `#D0679B`, green `#5FA35A`, brown `#A07855`, slate `#5C7A99` | lightened variants | user-defined |

\*TR letter collision Gündüz/Gece → use **"Gü" / "Ge"** abbreviations in Turkish.

### 6.4 Typography & components
- **Font:** *Inter* (excellent Turkish glyph support: ç ğ ı İ ö ş ü) — or platform default (SF Pro / Roboto).
- **Scale:** Display 32 · Title 22 · Body 16 (min) · Label 14 · Caption 13. Support OS text scaling to 200 %.
- **Numbers:** tabular figures for times and durations.
- **Shape:** 16 dp card radius, 28 dp bottom sheets, pill buttons.
- **Charts:** simple bars/lines, no 3D, soft gridlines, values labeled directly.
- **Motion:** subtle 200–250 ms fades/slides; respect "reduce motion".
- **Haptics:** light haptic on save; none at night theme.
- **Illustrations:** soft line art, moon/sun motifs, diverse workers in scrubs/uniforms.
- **Tone of voice:** supportive, never judgmental. ✗ "You failed your sleep goal." ✓ "Short night — be gentle with
  yourself today. A 20-min nap can help."
- **Framework:** Material 3 with custom `ColorScheme`; Cupertino-adapted pickers on iOS.

### 6.5 Accessibility checklist
- WCAG 2.2 AA contrast for all text; 3:1 for UI components/charts.
- Semantic labels on every icon button and chart (chart has a text summary).
- Screen-reader order follows visual order; Sleep Mode fully operable via TalkBack/VoiceOver.
- Tap targets ≥ 48 dp; spacing ≥ 8 dp between targets.

---

## 7. Onboarding flow

Principles: ≤ 7 steps, every step skippable except language, value before permissions, finish with a filled
calendar.

| Step | Screen | EN copy | TR copy | Notes |
|---|---|---|---|---|
| 1 | Welcome 1 | **Built for people who keep the world running at night.** Your schedule isn't 9 to 5 — your app shouldn't be either. | **Gece de dünyayı ayakta tutanlar için.** Mesainiz 9-5 değil; uygulamanız da olmamalı. | Illustration: nurse + guard + factory worker under moon |
| 2 | Welcome 2 | **Your shifts, sorted in seconds.** Pick your rotation once — we fill in the months ahead. | **Vardiyalarınız saniyeler içinde hazır.** Düzeninizi bir kez seçin, önümüzdeki ayları biz dolduralım. | Animated calendar filling |
| 3 | Welcome 3 | **Know how rested you really are.** Track sleep and energy, get gentle reminders that fit your shifts. | **Ne kadar dinlendiğinizi bilin.** Uykunuzu ve enerjinizi takip edin, vardiyanıza uygun nazik hatırlatmalar alın. | "Get started" button |
| 4 | Language | **Choose your language** · Türkçe / English | **Dilinizi seçin** · Türkçe / English | Pre-selected from device locale (tr → Türkçe, anything else → English) |
| 5 | Role | **What do you do?** Nurse / Healthcare · Factory / Production · Security · Hotel / Hospitality · Other | **Ne iş yapıyorsunuz?** Hemşire / Sağlık · Fabrika / Üretim · Güvenlik · Otel / Turizm · Diğer | Drives pattern suggestions & example copy |
| 6 | Rotation | **How does your rotation work?** Suggested: 2-2-3 · 4-on-4-off · 24/48 … · *Add shifts manually* · *Skip* → then **"What is today?"** (Day 1 of Night, Off…) + live month preview | **Vardiya düzeniniz nasıl?** Önerilen: 2-2-3 · 4 çalış 4 izin · 24/48 … · *Kendim ekleyeceğim* · *Atla* → **"Bugün düzeninizin hangi günü?"** + canlı ay önizlemesi | Core activation step. Role-based suggestions: Nurse → 2-2 Day/Night, 12/36; Factory → weekly 3-shift rotation, 2-2-3; Security → 24/48, 12/36; Hospitality → Custom |
| 7 | Shift times | **When do your shifts start and end?** | **Vardiyalarınız ne zaman başlıyor ve bitiyor?** | Pre-filled (Day 08–20, Night 20–08 for 12-h patterns) |
| 8 | Sleep goal | **How much sleep do you need to feel good?** (default 7 h 30) — *Most adults need 7–9 hours.* | **Kendinizi iyi hissetmek için ne kadar uykuya ihtiyacınız var?** — *Çoğu yetişkinin 7–9 saate ihtiyacı vardır.* | Slider |
| 9 | Notifications | **Want a heads-up before your night shift?** We'll remind you to rest — never during your sleep. [Allow reminders] [Not now] | **Gece vardiyasından önce haber verelim mi?** Dinlenmeniz için hatırlatırız — uyurken asla. [Hatırlatmalara izin ver] [Şimdi değil] | Shows mock notification; OS prompt only after "Allow" |
| 10 | Done | **You're all set, Ayşe.** Your next shift: Night · Tue 20:00. *Tip: log last night's sleep to start your sleep-debt score.* [Log last sleep] [Go to Today] | **Hazırsınız, Ayşe.** Sıradaki vardiyanız: Gece · Salı 20:00. [Son uykumu kaydet] [Bugün'e git] | Premium offered softly here only as "Try Premium free for 7 days" text link, not a hard paywall |

**Post-onboarding activation nudges (days 1–3):** coach-mark on (+) button; day-2 notification "How did you
sleep?"; day-3 first insight card ("Your first sleep-debt score is ready").

---

## 8. Subscription pricing structure

### 8.1 Tiers
| | **Free** | **Premium** |
|---|---|---|
| Shift calendar (month/week, unlimited shifts) | ✔ | ✔ |
| Rotation patterns | 3 basic (2-2-3, 4-on-4-off, 5-2) | Full library (10+) + custom pattern builder |
| Shift templates | Up to 3 custom | Unlimited |
| Sleep logging + quality | ✔ | ✔ |
| Sleep debt | 7-day basic | Advanced 14-day weighted, by shift type, recovery plan |
| Sleep history | 30 days | Unlimited |
| Energy check-ins | ✔ | ✔ + correlations |
| Reminders | Shift start, daily check-in, forgot-to-log | Smart shift-aware engine (nap, wind-down, caffeine, transitions, per-type timing) |
| Statistics | This-week summary + sleep chart | Monthly, trends, energy-by-shift, insights |
| Export (CSV / PDF / .ics) | — | ✔ |
| Night (red) theme, widgets | — | ✔ |
| Backup | Local file | Local file (+ encrypted cloud sync, v1.2) |
| Ads | **None** (trust for healthcare users) | None |

### 8.2 Pricing (localized price tiers; final values to be validated via store price-tier tables)
| Plan | Turkey (TRY) | US (USD) | EU (EUR) | Notes |
|---|---|---|---|---|
| Monthly | ₺79,99 | $3.99 | €3.99 | No trial |
| **Annual** *(default, highlighted)* | ₺499,99 (≈ ₺41,67/mo, −48 %) | $24.99 (≈ $2.08/mo) | €24.99 | **7-day free trial** |
| Lifetime | ₺1.199,99 | $59.99 | €59.99 | One-time; appeals to subscription-averse users |

Rationale: priced below mainstream sleep apps (typically $40–70/yr) because the audience is often lower/middle
income and price-sensitive; Turkish pricing reflects purchasing power, not FX conversion.

### 8.3 Offers & growth levers
- **Healthcare Heroes discount:** 30 % off annual for verified healthcare workers (honor-based promo code via
  hospitals/unions; partnerships with nursing associations).
- **Team / workplace licenses (v2):** bulk codes for hospitals & factories (occupational health budget).
- **Win-back:** 40 % off annual offer to lapsed subscribers after 30 days.
- **Introductory offer:** first month ₺29,99 / $0.99 for monthly plan (A/B test vs. trial).
- **Paywall placement:** contextual (when tapping a locked feature) + soft mention after onboarding + after the
  first weekly insight. Never block core logging.
- **Implementation:** RevenueCat (or `in_app_purchase` + server-side receipt validation); entitlement cached
  locally so premium works offline (re-verify every 7 days, 30-day grace).

---

## Appendix A — Technical architecture (Flutter)

| Concern | Choice | Notes |
|---|---|---|
| Framework | Flutter 3.x (stable), Dart 3 | Single codebase iOS/Android |
| Architecture | Feature-first folders, clean layers (presentation / domain / data) | `features/calendar`, `features/sleep`, … |
| State management | Riverpod 2 | Testable, compile-safe |
| Local DB | **Drift (SQLite)** | Relational queries for stats; migrations; reactive streams |
| Settings | `shared_preferences` | Small key-values |
| Secure storage | `flutter_secure_storage` | PIN hash, encryption key |
| Notifications | `flutter_local_notifications` + `timezone` | Exact scheduling; iOS 64-pending limit → schedule rolling 14-day window, reconcile on app open & daily background task (`workmanager`) |
| Localization | `flutter_localizations` + `intl` + ARB files (`app_en.arb`, `app_tr.arb`) | Generated `AppLocalizations`; ICU plurals |
| Charts | `fl_chart` | Lightweight |
| Navigation | `go_router` | Deep links from notifications |
| Purchases | `purchases_flutter` (RevenueCat) | Offline entitlement cache |
| Biometrics | `local_auth` | App lock |
| Health (v1.1) | `health` package | HealthKit / Health Connect |
| Analytics | Privacy-first, opt-in (e.g. PostHog EU / Firebase with consent) | No health data in events |
| Crash reporting | Sentry or Crashlytics | PII scrubbing |
| CI/CD | GitHub Actions + Fastlane / Codemagic | Lint, tests, build |

**Performance budgets:** cold start < 1.5 s on mid-range Android; calendar month render < 16 ms/frame; DB
queries for stats < 50 ms for 2 years of data; APK < 25 MB.

**Offline-first:** 100 % of MVP features work without network; network only for purchases, optional analytics
and (later) sync. Sync design (v1.2): end-to-end encrypted, last-write-wins per record with `updatedAt` +
soft-delete tombstones.

**Privacy & compliance:** KVKK (Turkey) and GDPR compliant; health data is sensitive personal data → stored only
on-device by default, explicit consent for any cloud feature, data export & delete in-app. Not a medical device;
display disclaimer: *"Nöbet provides general wellness information, not medical advice."*

---

## Appendix B — Business rules & calculations

### B.1 Shift rules
- A shift belongs to the **calendar date on which it starts**.
- A shift is a **night shift** if ≥ 3 h of it falls between 23:00 and 06:00 (configurable).
- Only one primary shift per day in MVP (double shifts → custom type "Day+Night"; multi-shift days in v1.1).
- Pattern application never overwrites manual overrides unless the user chooses "Overwrite all".
- DST: shift duration is computed from actual instants (a 20:00–08:00 night shift on DST change = 11 h or 13 h).

### B.2 Sleep debt
- **Sleep day:** sleep is attributed to the "sleep day" in which the sleep block *ends* (handles day-sleep after
  nights).
- **Basic (free):** `debt_7d = Σ_{d=1..7} max(0, goal − sleep_d)` — capped display at 20 h; surplus doesn't cancel
  debt beyond +1 h/day (recovery is realistic, not instant).
- **Advanced (premium):** 14-day exponentially weighted debt (recent days weigh more, half-life 4 days), with
  surplus recovery `min(surplus, 2 h)` per day; split by preceding shift type; recovery plan suggests extra
  sleep/naps over upcoming off days.
- Naps count toward total sleep; days with no log are **excluded** (not counted as 0) and shown as "no data".
- Status bands: 0–3 h *Well rested* (success) · 3–7 h *Mild debt* (warning) · > 7 h *High debt* (alert, with
  supportive tip).

### B.3 Smart reminder rules (premium defaults, all editable)
| Context | Reminder | Default timing |
|---|---|---|
| Night shift tomorrow | "Night shift tomorrow — try to sleep in late and keep the evening light." | 20:00 the day before |
| Night shift today | Pre-shift nap | Shift start − 4 h (if no sleep logged in last 6 h) |
| Any shift | Shift start | Start − 60 min (Day), − 90 min (Night) |
| Any planned sleep | Caffeine cut-off | Planned sleep − 6 h |
| Any planned sleep | Wind-down | Planned sleep − 45 min |
| After night shift | "Home safe? Wear sunglasses, keep the room dark." | Shift end + 15 min |
| Day → Night transition | Heads-up | 2 days before |
| Running sleep timer > 14 h | Forgot to log (free) | — |
| Check-in | Daily energy | User time (default 2 h after waking) |

**Planned sleep** = after night shift: shift end + 1 h; otherwise next shift start − commute (default 1 h) −
sleep goal − 1 h buffer. **Quiet hours:** no notification during a running sleep timer or planned sleep window
(except shift start alarms the user marked "critical").

### B.4 Insights (templates, generated locally)
- "After night shifts you sleep **{x} h** less on average." (≥ 3 night shifts with sleep data)
- "Your energy is **{x} points** higher on days you sleep ≥ {goal}." (≥ 10 check-ins)
- "This month: **{n}** night shifts — {delta} vs last month."
- "Best recovery: you feel best on the **2nd off day**."
Minimum data thresholds prevent misleading insights; every insight shows "Based on {n} days".

---

## Appendix C — Localization requirements

- Supported locales: `tr-TR`, `en` (en-US base, en-GB date formats via locale).
- **Default:** device locale `tr*` → Turkish; anything else → English. User override in Settings (System /
  Türkçe / English), applied instantly without restart; stored in settings.
- **100 % parity:** all UI strings, notifications (scheduled with the language active at scheduling time —
  re-scheduled on language change), onboarding, paywall, store listings, push action buttons, widget, export
  PDF headers, error messages, FAQ.
- Use ICU plural/select in ARB: `"nightShiftCount": "{count, plural, =0{No night shifts} =1{1 night shift} other{{count} night shifts}}"`
  / TR: `"{count, plural, =0{Gece vardiyası yok} other{{count} gece vardiyası}}"`.
- **Turkish casing:** use locale-aware `toUpperCase('tr')` (i → İ, ı → I); never hard-code uppercase strings.
- Date/time via `intl` `DateFormat` with locale; 24 h default for TR, locale default for EN.
- Week start: Monday for TR; locale-based for EN.
- Number format: TR decimal comma ("7,5 sa"), EN period ("7.5 h").
- Expect TR strings ~20–30 % longer → no fixed-width text containers; test with pseudo-locale.
- Glossary (consistent terms):

| EN | TR |
|---|---|
| Shift | Vardiya (UI) / Nöbet (healthcare context) |
| Day / Evening / Night / Off | Gündüz / Akşam / Gece / İzin |
| Rotation pattern | Vardiya düzeni |
| Sleep debt | Uyku borcu |
| Nap | Şekerleme / Kısa uyku |
| Check-in | Günlük kayıt |
| Energy level | Enerji seviyesi |
| Reminder | Hatırlatıcı |
| Insights | İçgörüler |
| Premium | Premium |

Sample notification pairs:
| Key | EN | TR |
|---|---|---|
| `notif_night_tomorrow` | You have a night shift tomorrow — try to sleep in and keep tonight relaxed. | Yarın gece vardiyanız var — sabah biraz geç kalkmaya ve bu akşamı sakin geçirmeye çalışın. |
| `notif_nap` | Night shift at {time}. A 60–90 minute nap now can help you stay sharp. | {time}'de gece vardiyanız var. Şimdi 60–90 dakikalık bir kestirme dikkatinizi korumanıza yardımcı olur. |
| `notif_caffeine` | Last call for coffee ☕ — caffeine now may affect your sleep at {time}. | Kahve için son çağrı ☕ — şimdi alınan kafein {time}'deki uykunuzu etkileyebilir. |
| `notif_shift_start` | Your {shift} shift starts in {duration}. | {shift} vardiyanız {duration} sonra başlıyor. |
| `notif_checkin` | How's your energy today? It takes 5 seconds. | Bugün enerjiniz nasıl? Sadece 5 saniye sürer. |
| `notif_post_night` | Shift done 👏 Keep your sunglasses on and your room dark for better sleep. | Vardiya bitti 👏 Daha iyi uyumak için güneş gözlüğünüzü takın, odanızı karanlık tutun. |

---

## Appendix D — Success metrics, risks & roadmap

### D.1 KPIs
| Metric | Target (month 3) |
|---|---|
| Onboarding completion | ≥ 75 % |
| Activation (≥ 1 pattern or 7 shifts + 1 sleep log in 48 h) | ≥ 55 % |
| D7 / D30 retention | ≥ 35 % / ≥ 20 % |
| Weekly logging days per active user | ≥ 4 |
| Trial start rate (of new users) | ≥ 10 % |
| Trial → paid | ≥ 40 % |
| Free → paid overall | 4–6 % |
| Store rating | ≥ 4.6 |
| Crash-free sessions | ≥ 99.5 % |

### D.2 Risks & mitigations
| Risk | Mitigation |
|---|---|
| Logging fatigue | One-tap flows, notification actions, smart defaults, widget; no guilt messaging |
| Notifications unreliable (Android OEM battery killers, iOS 64 limit) | Exact alarms permission flow, rolling schedule, reconciliation on open, "Test notification" button, OEM help page |
| Complex/unusual rotations | Custom pattern builder (premium) + manual override always free |
| Medical/advice liability | Evidence-based tips with sources, clear disclaimer, no diagnosis |
| Data loss (offline only) | Prompted local backup monthly; cloud sync in v1.2 |
| Price sensitivity in TR | Purchasing-power pricing, lifetime plan, healthcare discount |

### D.3 Release roadmap
| Release | Scope |
|---|---|
| **MVP 1.0** (≈ 10–12 weeks) | All "M" items in §2: calendar + patterns, sleep, energy, basic + smart reminders, stats, onboarding, TR/EN, dark mode, paywall |
| 1.1 | Home widgets, Apple Health / Health Connect, multi-shift days, day notes, iOS Live Activity for running sleep |
| 1.2 | Encrypted cloud backup & sync, calendar .ics export/import, monthly shareable recap |
| 2.0 | Team/workplace licenses, shift swap sharing with coworkers, wearable integrations, Apple Watch / Wear OS companion |

### D.4 Suggested MVP sprint plan (2-week sprints)
1. Project setup, design system (themes, tokens, l10n scaffolding), DB schema
2. Calendar month/week, shift types, day sheet, bulk edit
3. Pattern library & configurator, onboarding
4. Sleep logging (live + manual), sleep debt, sleep tab
5. Energy check-in, stats tab & charts, insights engine
6. Reminders engine, paywall & purchases, settings, QA, store assets (TR/EN), beta (TestFlight / Play internal)
