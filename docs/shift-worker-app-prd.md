# Product Requirements Document — Shift-Worker Productivity & Wellness App

| | |
|---|---|
| **Working title** | Nöbet (recommended — see §1) |
| **Document type** | Product Requirements Document (PRD) — MVP |
| **Version** | 1.2 · 2026-11-04 |
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
circadian rhythm, cause chronic tiredness, and make basic life planning (appointments, family time, meals)
hard. Generic calendar and sleep apps assume a fixed day/night routine and don't understand that "Tuesday" may
mean *sleeping 09:00–16:00 after a night shift*.

**Product vision.** A calm, trustworthy companion that understands the user's rotation and answers three daily
questions in one glance:
1. *What is my next shift?*
2. *How well am I recovering?* (post-shift recovery score)
3. *What should I do now to feel better?* (shift-aware nudges)

**MVP goals**
| Goal | Measure |
|---|---|
| Fast setup | User adds a full month of shifts in < 2 minutes (via pattern) |
| Post-shift habit | ≥ 60 % of completed shifts receive a recovery check-in |
| Trust | Zero required account; data stays on device by default |
| Viable business | 4–6 % free→premium conversion by month 3 |

**Non-goals (MVP):** wearable/HealthKit auto-import, team/rostering for managers, social features, medical
diagnosis. (Some appear in the roadmap, Appendix D.)

### Personas
| Persona | Context | Key need |
|---|---|---|
| **Ayşe, 31 — ICU nurse** | 12-h shifts, 2 days / 2 nights / 4 off, Istanbul | A quick post-shift recovery check-in, trustworthy and private, one-hand use after a night shift |
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

- **EN (store, 80 chars):** *Shift calendar & post-shift recovery tracker for nurses and night workers.*
- **TR (mağaza, 80 karakter):** *Hemşire ve gece çalışanları için vardiya takvimi ve vardiya sonrası toparlanma.*

**Long description (EN).** Nöbet is the calm, private companion for people who work rotating and night
shifts. Set up your rotation in seconds with ready-made patterns like 2-2-3 or 4-on-4-off. After every shift,
answer four quick questions and get a Recovery Score that shows how well you are bouncing back. Over time, gentle
insights reveal how night, day and long shifts really affect you, and smart reminders tell you when to wind down
before a night shift. Works fully offline. No account required.
Available in English and Turkish.

**Uzun açıklama (TR).** Nöbet, dönüşümlü ve gece vardiyasında çalışanlar için sakin ve gizliliğe önem veren bir
yardımcıdır. 2-2-3 veya 4 gün çalış / 4 gün izin gibi hazır düzenlerle vardiyanızı saniyeler içinde kurun. Her
vardiyadan sonra dört kısa soruyu yanıtlayın; Toparlanma Puanınız ne kadar iyi toparlandığınızı göstersin. Zamanla
nazik içgörüler gece, gündüz ve uzun vardiyaların sizi nasıl etkilediğini ortaya koyar; akıllı hatırlatıcılar gece
vardiyası öncesi ne zaman dinlenmeniz gerektiğini söyler.
Tamamen çevrimdışı çalışır. Hesap gerekmez. Türkçe ve İngilizce.

---

## 2. Prioritized feature list

Legend: **M** = Must-have (MVP), **N** = Nice-to-have (post-MVP / v1.x), **F** = Free, **P** = Premium

Sections 2.1–2.6 cover the core MVP — with the **Post-Shift Recovery System (§2.2)** at its centre, replacing
traditional sleep tracking and sleep-debt calculation; 2.7–2.11 add nutrition timing, journaling, holidays & leave,
goals and widgets; 2.12 lists further nice-to-have ideas.

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
| CAL-12 | Notes on a day (e.g. "swapped with Elif") — superseded by the Shift Journal (§2.8) | M | F |
| CAL-13 | Home-screen widget (next shift + latest recovery score) — see §2.11 | N | F / P |
| CAL-14 | Export calendar (.ics) / share month as image | N | P |
| CAL-15 | Overtime / hours worked summary (for payroll check) | N | P |
| CAL-16 | Import from device calendar | N | P |

### 2.2 Post-Shift Recovery System
Replaces traditional sleep tracking and sleep-debt calculation. Instead of logging every sleep block and counting
"missing" hours, the app asks a short set of questions **after every completed shift** and turns the answers into a
**Recovery Score (0–100)**. The focus is on how the user is bouncing back — not on what they did "wrong".

| ID | Feature | Priority | Tier |
|---|---|---|---|
| REC-01 | Recovery check-in prompted after every completed work shift (notification + Today card); timing per Appendix B.2 | M | F |
| REC-02 | **Q1 — How long did you sleep after this shift?** Duration picker (0–14 h, 15-min steps) plus "Haven't slept yet" and "Only a short nap" — optional | M | F |
| REC-03 | **Q2 — How was your sleep quality?** 5-step emoji scale (😫 😕 😐 🙂 😴) — optional | M | F |
| REC-04 | **Q3 — What is your current energy level?** 1–10 slider — optional | M | F |
| REC-05 | **Q4 — How smoothly did your shift go?** 1–10 slider (1 = very hard, 10 = very smooth) — **mandatory**; the only answer required to save | M | F |
| REC-06 | **Recovery Score (0–100)** calculated from the answers (Appendix B.5), shown as a ring with a band label and one supportive sentence | M | F |
| REC-07 | Completes in ≤ 30 s: one screen, large controls, smart defaults; Q4 can be answered straight from the end-of-shift notification and Q1–Q3 later (available for 36 h) | M | F |
| REC-08 | Edit or complete a check-in later from the calendar day sheet; a missed check-in simply shows "No check-in" and is never counted as a low score | M | F |
| REC-09 | Recovery history (list + chart, coloured by shift type) | M | F (30 days) / P (unlimited) |
| REC-10 | Insights by shift type — night, day, evening and long shifts (≥ 12 h): average recovery, slowest / fastest recovery | M | F (1 per week) / P (all) |
| REC-11 | Pattern insights, e.g. "Your recovery is usually lower after consecutive night shifts", quick turnarounds (< 11 h), days off needed to bounce back, smoothness vs. recovery, journal tags (Appendix B.4) | M | P |
| REC-12 | Gentle suggestions based on recovery trends (e.g. plan an extra rest day after a run of nights, a short pre-shift nap) — always optional, never prescriptive | M | P |
| REC-13 | Optional note in the check-in, saved to the Shift Journal (§2.8) | M | F |
| REC-14 | Supportive, non-judgmental language throughout — no "debt", "failed" or "bad"; follows the tone rules in Appendix B.9 | M | F |
| REC-15 | Apple Health / Health Connect: pre-fill Q1 duration (user confirms) | N | P |
| REC-16 | Recovery-aware reminders (e.g. suggest rest when the latest score is low and the next shift is soon) | N | P |

### 2.3 Energy & Daily Check-in
| ID | Feature | Priority | Tier |
|---|---|---|---|
| ENG-01 | Optional daily energy check-in for off-days and between shifts (post-shift energy is captured by REC-04), 1–10 slider with large thumb + descriptive labels | M | F |
| ENG-02 | Optional short note (max 280 chars) | M | F |
| ENG-03 | Optional quick tags: caffeine, exercise, stress, ate late, alcohol | M | F |
| ENG-04 | Multiple check-ins/day allowed; "daily energy" = average | M | F |
| ENG-05 | Energy vs. shift-type correlation (avg energy on/after Day/Night/Off) | M | P |
| ENG-06 | Energy vs. recovery score over time | N | P |
| ENG-07 | Mood tracking (separate from energy) | N | F |

### 2.4 Smart Reminders
| ID | Feature | Priority | Tier |
|---|---|---|---|
| REM-01 | Basic shift reminder: "Your shift starts in X" (fixed lead time) | M | F |
| REM-02 | Daily check-in reminder at user-chosen time | M | F |
| REM-03 | **Shift-aware rules engine** (rule per shift type & transition, see Appendix B.3) | M | P |
| REM-04 | Pre-night-shift nap suggestion ("Night shift tonight — a short nap beforehand can help") | M | P |
| REM-05 | Wind-down reminder computed from next shift start & usual sleep need | M | P |
| REM-06 | Caffeine cut-off reminder (e.g. 6 h before planned sleep) | M | P |
| REM-07 | Recovery check-in prompt after each completed shift + at most one gentle follow-up (Appendix B.2) | M | F |
| REM-08 | Quiet hours — never notify during the planned post-shift sleep window | M | F |
| REM-09 | Per-shift-type customizable lead times | M | P |
| REM-10 | Light-exposure tips (sunglasses on the way home after night shift) | N | P |

### 2.5 Statistics & Insights
| ID | Feature | Priority | Tier |
|---|---|---|---|
| STA-01 | This-week summary: average recovery score, # shifts, # night shifts, avg energy | M | F |
| STA-02 | Weekly recovery chart (score per shift, coloured by shift type) | M | F |
| STA-03 | Monthly view, shift distribution (donut), trends | M | P |
| STA-04 | Recovery trend line (30/90 days), split by shift type | M | P |
| STA-05 | Energy by shift type (bar) | M | P |
| STA-06 | Export data (CSV + PDF summary for doctor/occupational health) | M | P |
| STA-07 | Monthly "Shift Report" recap card (shareable) | N | P |

### 2.6 Onboarding, Settings & Platform
| ID | Feature | Priority | Tier |
|---|---|---|---|
| ONB-01 | 3-screen value intro, skippable | M | F |
| ONB-02 | Language auto-detect (tr/en), switchable | M | F |
| ONB-03 | Optional profile setup: **name/nickname**, **profession / job title**, **work type / shift pattern** (e.g. rotating, permanent nights, 2-2-3). Every field is optional and skippable; answers personalize greetings, copy and suggested patterns | M | F |
| ONB-04 | Quick first-rotation setup (pattern + start date) | M | F |
| ONB-05 | Usual sleep need (optional, default 7 h 30 min) — used only to score Q1; never shown as a target or as "debt" | M | F |
| ONB-06 | Notification permission with pre-prompt explaining value | M | F |
| SET-08 | Profile editing: name/nickname, profession / job title and work type can be changed (or cleared) at any time from Settings → Profile | M | F |
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
| SHR-01 | Family / partner sharing: share shifts only (never sleep, energy or notes) with one person via a serverless code or link; read-only view of their schedule | M | F |


### 2.7 Caffeine & Meal Timing Suggestions
| ID | Feature | Priority | Tier |
|---|---|---|---|
| NUT-01 | Caffeine window per upcoming shift, e.g. "Limit caffeine after 14:00 before your night shift", "Caffeine only in the first half of tonight's shift" (rules in Appendix B.6) | M | P |
| NUT-02 | Meal timing per shift type, e.g. "Try to eat your main meal between 17:00 and 19:00", "Keep it light between 00:00 and 06:00" | M | P |
| NUT-03 | Suggestions shown on the Today card and as optional reminders (uses the Smart Reminders engine, §2.4) | M | P |
| NUT-04 | Simple caffeine log (one tap: coffee / tea / energy drink) to sharpen the cut-off advice | N | P |
| NUT-05 | General-wellness framing only: no calorie counting, diets or medical claims; sources listed in About & help | M | F |

### 2.8 Quick Notes / Shift Journal
| ID | Feature | Priority | Tier |
|---|---|---|---|
| JRN-01 | Short note per shift (max 280 chars) from the day sheet or right after a shift ends | M | F |
| JRN-02 | Quick-tag chips for one-tap journaling: Very busy · Calm · Felt exhausted · Overtime · Short-staffed · Difficult patient/customer (TR/EN) | M | F |
| JRN-03 | Journal list with search and filter by tag / shift type | M | F |
| JRN-04 | Journal-based insights, e.g. "On 'Very busy' shifts your recovery is usually a little lower" (Appendix B.4) | M | P |
| JRN-05 | Notes stay private: never included in partner sharing; included in export only if the user opts in | M | F |

### 2.9 Public Holidays & Leave Management
| ID | Feature | Priority | Tier |
|---|---|---|---|
| HOL-01 | Public holidays shown automatically in the calendar (marker + name), **Turkey first**: national holidays and religious holidays incl. half-day eves (arife) — bundled offline (Appendix B.7) | M | F |
| HOL-02 | Holiday filter in Settings (show/hide, choose country); additional countries added over time | N | F |
| HOL-03 | Optional "worked on a holiday" flag on shifts, counted in statistics (useful for payroll checks) | N | P |
| LV-01 | Mark leave days in the calendar: annual leave (full / half day), sick leave, unpaid leave, other | M | F |
| LV-02 | Annual leave balance: allowance per leave year + carried-over days − used days = **remaining**, shown in Calendar and Stats | M | F |
| LV-03 | Default allowance suggestion for Turkey based on years of service (Labour Law No. 4857, Art. 53: 14 / 20 / 26 days), always editable | M | F |
| LV-04 | Leave planner: preview how many consecutive days off a leave request creates when combined with rotation off-days and holidays | N | P |

### 2.10 Simple Goals & Streaks
| ID | Feature | Priority | Tier |
|---|---|---|---|
| GOL-01 | Personal goals from templates or custom, e.g. "Sleep at least 6 hours after night shifts this month", "Complete a recovery check-in after every shift", "Daily energy check-in", "No caffeine after my cut-off" | N | F (1 active) / P (unlimited) |
| GOL-02 | Goal progress card (e.g. "4 of 6 night shifts done") with a monthly summary | N | F |
| GOL-03 | Light streak system: counts qualifying days or shifts; off-days and leave days never break a shift-based streak; one "rest pass" per week | N | F |
| GOL-04 | Supportive tone only: gentle milestones (3, 7, 14, 30), no shaming, no push notifications when a streak breaks | N | F |

### 2.11 Home Screen Widgets
| ID | Feature | Priority | Tier |
|---|---|---|---|
| WID-01 | Small home-screen widget: today's / next shift with countdown | N | F |
| WID-02 | Medium widget: today's shift + latest recovery score + next 3 days | N | P |
| WID-03 | Lock-screen widget (iOS 16+ accessory widgets; Android where the launcher/OS supports it): next shift + latest recovery score | N | P |
| WID-04 | Widget privacy option: hide the recovery score on the lock screen; widgets follow the app language and dark/night theme | N | F |

### 2.12 Additional nice-to-have features
| ID | Feature | Priority | Tier |
|---|---|---|---|
| EXT-01 | Profession-based tips: curated TR/EN tips per profession from the optional profile (nurses, security, factory, hospitality), e.g. hydration on long ward shifts, alertness on solo night patrols, safe driving home after a factory night | N | F |
| EXT-02 | Special Night Mode with an **amber** option next to the existing red Night theme (SET-02) to reduce eye strain on night shifts; optional auto-switch during night shifts | N | P |
| EXT-03 | Anonymous comparison: "People with similar shifts sleep an average of 6.4 h after night shifts." Strictly opt-in, aggregated only, minimum cohort size, no raw data leaves the device (needs a backend, see Appendix D) | N | P |

---

## 3. User flows for the main screens

### 3.1 Primary navigation
Bottom navigation bar with 4 tabs; the floating **(+)** button lives on the Calendar tab only:

```
┌──────────────────────────────────────────────┐
│  Today   Calendar(+)   Recovery   Stats       │   (Settings via gear on Today)
└──────────────────────────────────────────────┘
```
The Calendar **(+)** opens a bottom sheet: *Add a shift today · Select multiple days · Apply a rotation · Add a shift
type*. The recovery check-in is reached from the Today card, the Recovery tab and the post-shift notification.

### 3.2 Flow A — First launch → first value (target < 2 min)
```
Splash → Welcome (3 value slides, skippable)
       → Language confirm (pre-selected from device)
       → "About you" (all optional, "Skip" always available)
             ├─ Name or nickname (free text)
             ├─ Profession / job title (quick picks: Nurse · Factory · Security · Hospitality · Other, or free-text title)
             └─ Work type (Rotating · Permanent nights · Permanent days · Permanent evenings · 2-2-3 · 4-on-4-off · 24/48 · Other)
       → "How does your rotation work?"
             ├─ Pick a pattern (suggested by profession + work type) → choose start date + which day of cycle today is → preview month → Save
             ├─ "I'll add shifts manually" → Calendar (empty, coach-mark on a day)
             └─ "Skip for now"
       → Shift times (pre-filled per pattern, editable)
       → Usual sleep need (optional, default 7 h 30 — skippable)
       → Notification pre-prompt → OS permission
       → Today screen ("After your next shift we'll ask four quick questions")
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

### 3.4 Flow C — Post-shift recovery check-in (the most frequent flow, one-hand & dim-friendly)
```
Shift ends ─► notification "Shift done 👏 How smoothly did it go?" ─► quick answer 1–10 (Q4) or open the app
          └─► later, per Appendix B.2: "How are you recovering after your night shift?" ─► Check-in (one screen)
                 Q1  Sleep after this shift   [ 7 h 30 ▾ ]  · Haven't slept yet · Only a short nap   (optional)
                 Q2  Sleep quality            😫 😕 😐 🙂 😴                                       (optional)
                 Q3  Energy right now         1 ───●──── 10                                       (optional)
                 Q4  How smoothly did your shift go?   1 ─────●── 10                              (required)
                 + Add a note (optional → Shift Journal)
               → Save ─► Recovery Score ring + band + one supportive sentence (+ trend hint, Premium)
Also reachable from: Today recovery card · Recovery tab · Calendar day sheet (edit within 36 h, view anytime)
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
  → Basic (free): Shift start lead time · Daily check-in time · Recovery check-in prompt
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
  → Summary tiles (avg recovery, shifts, night shifts, avg energy)
  → Recovery chart → tap bar → shift / check-in detail
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
| S-04 | About you (optional profile) | Personalize | Name field, profession quick picks + job-title field, work-type chips, Skip | ✔ |
| S-05 | Rotation setup | First shifts | Pattern list (role-suggested), cycle-day picker, month preview | ✔ |
| S-06 | Shift times | Set start/end per type | Time pickers, color chips | ✔ |
| S-07 | Usual sleep need (optional) | Scoring reference | Slider 5–10 h, "used only to score recovery" note, Skip | ✔ |
| S-08 | Notification pre-prompt | Permission | Example notification mock, Allow / Not now | ✔ |
| S-09 | **Today (Home)** | Daily dashboard | "Hello, {name}" header, next-shift card with countdown, Recovery card (check-in prompt or latest score), Energy card, Smart tip, upcoming days strip | ✔ |
| S-10 | Calendar — Month | Overview | Color-coded grid, legend, pattern indicator, today highlight | ✔ |
| S-11 | Calendar — Week | Detail | 7 rows with time bars + recovery score per shift | ✔ |
| S-12 | Day detail sheet | Edit a day | Shift type chips, times, journal note, recovery check-in & score, energy for that day | ✔ |
| S-13 | Pattern library | Choose rotation | Pattern cards with mini preview, premium badges | ✔ |
| S-14 | Pattern configurator | Apply rotation | Start date, cycle offset, duration, shift times, conflict handling | ✔ |
| S-15 | Shift templates | Manage types | List, add/edit (name, color, icon, times, paid hours) | ✔ |
| S-16 | Calendar (+) sheet | Fast entry | Add shift today, multi-select, apply rotation, add shift type | ✔ |
| S-17 | Recovery check-in | 4 questions after a shift | Q1 duration picker, Q2 emoji scale, Q3 energy slider, Q4 smoothness slider (required), optional note | ✔ |
| S-18 | Recovery result | Feedback | Score ring, band label, one supportive sentence, answer breakdown, Edit | ✔ |
| S-19 | Recovery tab | History & trends | Latest score, chart by shift type, history list, insights & gentle suggestions | ✔ |
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
| S-31 | Home & lock-screen widgets | Glanceable | Next shift + countdown, latest recovery score, next 3 days (§2.11) | — (v1.1) |
| S-32 | Recovery insight detail | Explain a pattern | Chart + plain-language explanation + optional suggestion (patterns🔒) | ✔ |
| S-33 | Shift journal | Notes per shift | Note field + quick tags in the day sheet; journal list with search/filter | ✔ |
| S-34 | Leave & holidays | Time off | Leave balance card, mark leave in calendar, allowance settings, holiday list | ✔ |
| S-35 | Goals & streaks | Motivation | Goal templates, progress cards, streak counter | — (v1.1) |

### Key screen wireframe — Today (S-09)
```
┌─────────────────────────────────┐
│ Hello, Ayşe 👋              ⚙   │
│ Good evening · Tue, 4 Nov       │
├─────────────────────────────────┤
│ 🌙 NIGHT SHIFT · tonight         │
│ 20:00 – 08:00                   │
│ Starts in 3 h 12 min            │
│ ▸ Tip: a short nap now can help │
├───────────────┬─────────────────┤
│  Recovery     │  Energy today   │
│   (  72  )    │   6 / 10        │
│ after Mon day │  [ Check in ]   │
│ Partly recov. │                 │
├───────────────┴─────────────────┤
│ Next days:  Wed🌙  Thu☀  Fri○  Sat○│
└─────────────────────────────────┘
   [ Today  Calendar  Recovery  Stats ]
```
After a shift ends, the Recovery card switches to a prompt: *"How are you recovering after your night shift?
4 quick questions"* → S-17.

---

## 5. Information architecture

```
Nöbet
├── Onboarding (first run only)
│   ├── Welcome (3)
│   ├── Language
│   ├── About you (name, profession, work type — optional)
│   ├── Rotation setup → Shift times
│   ├── Usual sleep need (optional)
│   └── Notification permission
│
├── Today (Home)
│   ├── Current / next shift card ──────────────► Day detail
│   ├── Recovery card: check-in prompt / latest score ─► Recovery check-in / Recovery tab
│   ├── Energy card ────────────────────────────► Energy check-in
│   ├── Caffeine & meal timing tip🔒
│   ├── Smart tip (contextual) ─────────────────► Insight detail
│   ├── Upcoming days strip ────────────────────► Calendar (week)
│   └── Settings (gear)
│
├── Calendar
│   ├── Month view ⇄ Week view
│   ├── Day detail (sheet): shift · times · journal note & tags · recovery check-in & score · energy · holiday/leave
│   ├── Public holidays (markers) · Leave days & balance
│   ├── Bulk edit mode
│   ├── Apply pattern → Pattern library → Configurator
│   ├── Shift templates (manage types)
│   └── (+) Add: shift today · select multiple days · apply rotation · shift type
│
├── Recovery
│   ├── Latest Recovery Score + supportive message
│   ├── Check-in after each shift (Q1–Q4) → Recovery result
│   ├── Recovery chart by shift type (30 days | unlimited🔒)
│   ├── History list → edit check-in
│   └── Insights & gentle suggestions (by shift type | patterns🔒)
│
├── Stats
│   ├── Week | Month🔒
│   ├── Summary tiles
│   ├── Charts: recovery per shift · shift mix🔒 · energy by shift🔒 · recovery trend🔒
│   ├── Leave used / remaining
│   ├── Insight cards → Insight detail (incl. journal-based🔒)
│   ├── Shift journal (list, search, filter)
│   ├── Goals & streaks (v1.1)
│   └── Export🔒 (CSV / PDF)
│
└── Settings
    ├── Premium (status / upgrade → Paywall)
    ├── Profile: name/nickname, profession / job title, work type, usual sleep need (all editable, all optional)
    ├── Shifts: templates, default times, week start
    ├── Holidays & leave: country, show holidays, leave year, allowance, carry-over
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
| `RecoveryCheckIn` | id, shiftDate, shiftTypeId, sleepMinutes?, sleepStatus? (slept / notYet / napOnly), quality? (1–5), energy? (1–10), **smoothness (1–10, required)**, score (0–100), components, isProvisional, answeredAt, journalNoteId? |
| `EnergyEntry` | id, timestamp, level (1–10), tags[], note |
| `ReminderRule` | id, type (shiftStart, windDown, nap, caffeine, checkIn, recoveryPrompt, transition), shiftTypeId?, offsetMinutes, enabled |
| `ScheduledNotification` | id, ruleId, fireAt, payload (for reconciliation) |
| `Settings` | locale, theme, timeFormat, weekStart, sleepNeedMinutes (default 450), onboardingDone, appLock, analyticsOptIn |
| `Profile` | displayName?, professionKey? (nurse/factory/security/hospitality/other), jobTitle?, workType? (rotating/permanentNight/permanentDay/permanentEvening/223/4on4off/2448/other) — every field nullable |
| `Entitlement` | isPremium, plan, expiresAt, lastVerifiedAt (cached for offline) |
| `ShiftNote` | id, shiftDate, text (≤ 280), tags[], createdAt — private, never shared |
| `Holiday` | date, countryCode, nameKey, isHalfDay, type (national/religious) — bundled data file, versioned |
| `LeaveDay` | date, kind (annual/sick/unpaid/other), isHalfDay, note? |
| `LeaveYear` | startDate, allowanceDays, carriedOverDays |
| `Goal` | id, templateKey / customText, metric, target, period (week/month), startDate, active |
| `StreakState` | goalId, current, best, lastQualifiedDate, restPassesLeft |

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
| `warning` | `#D69A2D` | Partly recovered |
| `alert` | `#C8605A` | Low recovery (muted, never neon; never used as an error colour for the user) |

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
Auto-enables optionally during night shifts and between 22:00–06:00.

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
- Screen-reader order follows visual order; the recovery check-in is fully operable via TalkBack/VoiceOver (sliders also have − / + buttons).
- Tap targets ≥ 48 dp; spacing ≥ 8 dp between targets.

---

## 7. Onboarding flow

Principles: ≤ 7 steps, every step skippable except language, value before permissions, finish with a filled
calendar.

| Step | Screen | EN copy | TR copy | Notes |
|---|---|---|---|---|
| 1 | Welcome 1 | **Built for people who keep the world running at night.** Your schedule isn't 9 to 5 — your app shouldn't be either. | **Gece de dünyayı ayakta tutanlar için.** Mesainiz 9-5 değil; uygulamanız da olmamalı. | Illustration: nurse + guard + factory worker under moon |
| 2 | Welcome 2 | **Your shifts, sorted in seconds.** Pick your rotation once — we fill in the months ahead. | **Vardiyalarınız saniyeler içinde hazır.** Düzeninizi bir kez seçin, önümüzdeki ayları biz dolduralım. | Animated calendar filling |
| 3 | Welcome 3 | **See how well you recover after every shift.** Four quick questions after each shift — and gentle insights over time. | **Her vardiyadan sonra nasıl toparlandığınızı görün.** Her vardiyanın ardından dört kısa soru, zamanla nazik içgörüler. | "Get started" button |
| 4 | Language | **Choose your language** · Türkçe / English | **Dilinizi seçin** · Türkçe / English | Pre-selected from device locale (tr → Türkçe, anything else → English) |
| 5 | About you | **Tell us a little about you (optional).** Name or nickname · Profession / job title (Nurse / Healthcare · Factory / Production · Security · Hotel / Hospitality · Other, or type your title) · Work type (Rotating · Permanent nights · Permanent days · Permanent evenings · 2-2-3 · 4-on-4-off · 24/48 · Other) | **Biraz kendinizden bahsedin (isteğe bağlı).** Ad veya takma ad · Meslek / unvan (Hemşire / Sağlık · Fabrika / Üretim · Güvenlik · Otel / Turizm · Diğer veya unvanınızı yazın) · Çalışma şekli (Dönüşümlü · Sabit gece · Sabit gündüz · Sabit akşam · 2-2-3 · 4 çalış 4 izin · 24/48 · Diğer) | Every field optional; "Skip" always visible. Profession + work type drive pattern suggestions and copy. Editable later in Settings → Profile |
| 6 | Rotation | **How does your rotation work?** Suggested: 2-2-3 · 4-on-4-off · 24/48 … · *Add shifts manually* · *Skip* → then **"What is today?"** (Day 1 of Night, Off…) + live month preview | **Vardiya düzeniniz nasıl?** Önerilen: 2-2-3 · 4 çalış 4 izin · 24/48 … · *Kendim ekleyeceğim* · *Atla* → **"Bugün düzeninizin hangi günü?"** + canlı ay önizlemesi | Core activation step. Role-based suggestions: Nurse → 2-2 Day/Night, 12/36; Factory → weekly 3-shift rotation, 2-2-3; Security → 24/48, 12/36; Hospitality → Custom |
| 7 | Shift times | **When do your shifts start and end?** | **Vardiyalarınız ne zaman başlıyor ve bitiyor?** | Pre-filled (Day 08–20, Night 20–08 for 12-h patterns) |
| 8 | Usual sleep need | **How much sleep usually helps you feel rested?** (optional, default 7 h 30) | **Genellikle ne kadar uyku sizi dinlenmiş hissettirir?** (isteğe bağlı, varsayılan 7 sa 30 dk) | Slider + Skip. Used only to score Q1; never shown as a target |
| 9 | Notifications | **Want a heads-up before your night shift?** We'll remind you to rest — never during your sleep. [Allow reminders] [Not now] | **Gece vardiyasından önce haber verelim mi?** Dinlenmeniz için hatırlatırız — uyurken asla. [Hatırlatmalara izin ver] [Şimdi değil] | Shows mock notification; OS prompt only after "Allow" |
| 10 | Done | **You're all set, Ayşe.** Your next shift: Night · Tue 20:00. *After this shift we'll ask four quick questions to see how you're recovering.* [Go to Today] | **Hazırsınız, Ayşe.** Sıradaki vardiyanız: Gece · Salı 20:00. *Bu vardiyadan sonra nasıl toparlandığınızı görmek için dört kısa soru soracağız.* [Bugün'e git] | Premium offered softly here only as "Try Premium free for 7 days" text link, not a hard paywall |

**Post-onboarding activation nudges:** coach-mark on the Calendar (+) button; the first post-shift recovery
prompt after the user's next shift; after 3 check-ins, the first insight card ("Your first recovery insight is
ready").

---

## 8. Subscription pricing structure

### 8.1 Tiers
| | **Free** | **Premium** |
|---|---|---|
| Shift calendar (month/week, unlimited shifts) | ✔ | ✔ |
| Rotation patterns | 3 basic (2-2-3, 4-on-4-off, 5-2) | Full library (10+) + custom pattern builder |
| Shift templates | Up to 3 custom | Unlimited |
| Recovery check-in & score | ✔ after every shift, with supportive feedback | ✔ |
| Recovery history | 30 days | Unlimited |
| Recovery insights | 1 per week (by shift type) | All: patterns (consecutive nights, quick turnarounds, days to bounce back), trends, gentle suggestions |
| Energy check-ins | ✔ | ✔ + correlations |
| Reminders | Shift start, daily check-in, recovery check-in prompt | Smart shift-aware engine (nap, wind-down, caffeine, transitions, per-type timing) |
| Statistics | This-week summary + recovery chart | Monthly, trends, energy-by-shift, insights |
| Caffeine & meal timing | — | ✔ |
| Shift journal | Notes + quick tags | + journal-based insights |
| Holidays & leave | TR holidays, leave days & balance | + leave planner, holiday-work stats |
| Goals & streaks | 1 active goal | Unlimited goals |
| Export (CSV / PDF / .ics) | — | ✔ |
| Night (red/amber) theme | — | ✔ |
| Widgets | Small home widget | Medium + lock-screen widgets |
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

### B.2 Recovery check-in timing
- A check-in belongs to the **work shift it follows** (`shiftDate` = start date of that shift).
- **Q4 (smoothness)** is asked at shift end + 15 min via notification with quick 1–10 actions.
- **Full check-in (Q1–Q3)** is suggested at the earlier of: shift end + 10 h, or the next shift start − 2 h —
  shifted out of the planned sleep window. For day/evening shifts this is usually the next morning.
- At most **one** gentle follow-up after 4 h; no further reminders. The check-in stays open for **36 h**, then the
  shift shows "No check-in" (neutral, not a zero score).
- Consecutive shifts without a break: one check-in per shift; if the user answers late, the most recent shift is
  pre-selected and earlier ones can be filled in from the calendar.

### B.3 Smart reminder rules (premium defaults, all editable)
| Context | Reminder | Default timing |
|---|---|---|
| Night shift tomorrow | "Night shift tomorrow — try to sleep in late and keep the evening light." | 20:00 the day before |
| Night shift today | Pre-shift nap suggestion | Shift start − 4 h |
| Any shift | Shift start | Start − 60 min (Day), − 90 min (Night) |
| Any planned sleep | Caffeine cut-off | Planned sleep − 6 h |
| Any planned sleep | Wind-down | Planned sleep − 45 min |
| After night shift | "Home safe? Wear sunglasses, keep the room dark." | Shift end + 15 min |
| Day → Night transition | Heads-up | 2 days before |
| Completed shift | Recovery check-in prompt (free) | Q4 at shift end + 15 min; full check-in per B.2 |
| Check-in | Daily energy | User time (default 2 h after waking) |

**Planned sleep** = after night shift: shift end + 1 h; otherwise next shift start − commute (default 1 h) −
usual sleep need − 1 h buffer. **Quiet hours:** no notification during the planned sleep window
(except shift start alarms the user marked "critical").

### B.4 Insights (templates, generated locally)
- "After night shifts your recovery is usually around **{x}** — {y} points below your day shifts." (≥ 3 check-ins per type)
- "Your recovery is usually lower after **consecutive night shifts** — the 3rd night in a row averages **{x}**."
  (≥ 2 runs of ≥ 2 nights)
- "After long shifts (12 h+) it usually takes you **{n} days off** to get back above 75." (≥ 3 long shifts)
- "Quick turnarounds (less than 11 h between shifts) tend to lower your recovery by about **{x}** points." (≥ 3)
- "When your shift goes smoothly (8–10), you recover noticeably better — a calm shift really helps." (≥ 8 check-ins)
- "On shifts tagged **'Very busy'** your recovery is usually a little lower." (≥ 3 tagged shifts)
- "You recover best after **{type}** shifts." (≥ 3 check-ins per type)

**Gentle suggestions (Premium)** follow a trend, never a single bad day, e.g. "After a run of nights, an extra rest
day — if your schedule allows — could help you bounce back." Minimum data thresholds prevent misleading insights;
every insight shows "Based on {n} shifts".

### B.5 Recovery Score (0–100)
- **Inputs:** Q1 sleep after the shift (minutes, or "not yet" / "short nap"), Q2 quality (1–5), Q3 energy (1–10),
  Q4 smoothness (1–10, required).
- **Components (0–1):** sleep = min(minutes / usual sleep need, 1) ("short nap" = 90 min; "not yet" = 0 and the
  score is marked *provisional* — "Update after you've rested"); quality = (Q2 − 1) / 4; energy = (Q3 − 1) / 9;
  smoothness = (Q4 − 1) / 9.
- **Score** = round(100 × (0.35 × sleep + 0.20 × quality + 0.25 × energy + 0.20 × smoothness)). Unanswered optional
  questions are left out and the remaining weights are rescaled, so Q4 alone always produces a score.
- **Bands & copy (supportive, never judgmental):**
  - ≥ 75 *Well recovered* — "Nicely recovered. Whatever you did after this shift seems to work for you."
  - 50–74 *Partly recovered* — "You're getting there. Go easy on yourself today if you can."
  - < 50 *Needs more rest* — "That was a demanding one. You may need a bit more rest after this shift — a short nap
    or a calm evening can help."
- The score describes recovery, not performance: it is never compared to other users in the MVP and never called
  "bad".

### B.6 Caffeine & meal timing rules (general wellness defaults, editable)
| Context | Caffeine | Meals |
|---|---|---|
| Day before a night shift | Limit caffeine after 14:00 so tonight's sleep is good and a lie-in is possible | Normal meals; light dinner |
| Night shift (e.g. 20:00–08:00) | Caffeine only in the first half of the shift; none in the last 6 h before planned sleep | Main meal 2–3 h before the shift; keep it light between 00:00 and 06:00; small breakfast before sleep |
| Day shift (e.g. 08:00–20:00) | No caffeine in the 6 h before planned bedtime | Breakfast within 1 h of waking; regular lunch; dinner ≥ 2 h before bed |
| Off day after nights | Morning caffeine only | Return to daytime meal times to reset the rhythm |

Times are computed from the user's shift times and usual sleep need; the copy always says "try" or "consider", never
prescribes.

### B.7 Public holidays & leave
- **Turkey (bundled):** fixed national holidays (1 Jan, 23 Apr, 1 May, 19 May, 15 Jul, 30 Aug, 29 Oct; 28 Oct
  afternoon) + Ramazan Bayramı and Kurban Bayramı with half-day eves, from a yearly table (lunar dates) shipped with
  the app and refreshed with each release — works offline.
- **Leave balance:** remaining = allowance + carried over − used (half days count 0.5). Leave days that fall on
  rotation off-days or public holidays are not deducted (configurable).
- **Leave year:** starts on the hire anniversary or 1 January (user choice).

### B.8 Goals & streaks
- A day or shift *qualifies* when the goal's metric is met (e.g. Q1 sleep ≥ 6 h in the recovery check-in after a night shift).
- Streaks count only relevant days: off-days and leave never break a shift-based streak; one weekly rest pass
  covers a missed day.
- Milestones at 3, 7, 14 and 30; no notification when a streak ends.

### B.9 Tone & language rules for recovery
| Avoid | Use instead (EN) | Kullanın (TR) |
|---|---|---|
| "Sleep debt", "You owe 4 h of sleep" | "Your recovery after this shift" | "Bu vardiya sonrası toparlanmanız" |
| "Bad score", "You failed" | "A demanding shift — rest when you can" | "Yorucu bir vardiyaydı — fırsat buldukça dinlenin" |
| "You only slept 4 h" | "A short sleep after a long night is common" | "Uzun bir geceden sonra kısa uyku çok yaygın" |
| "You must…" | "You could try…", "If your schedule allows…" | "Deneyebilirsiniz…", "Programınız uygunsa…" |
| Red error styling for low scores | Muted colours, a calm icon, one helpful idea | Sakin renkler, sade ikon, tek bir öneri |

Missed check-ins are never mentioned negatively; there are no "you forgot" messages.

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
| Recovery Score | Toparlanma Puanı |
| Recovery check-in | Toparlanma kontrolü |
| How smoothly did your shift go? | Vardiyanız ne kadar sorunsuz geçti? |
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
| `notif_recovery_q4` | Shift done 👏 How smoothly did it go? (1–10) | Vardiya bitti 👏 Ne kadar sorunsuz geçti? (1–10) |
| `notif_recovery` | How are you recovering after your {shift} shift? 4 quick questions. | {shift} vardiyası sonrası nasıl toparlanıyorsunuz? 4 kısa soru. |
| `notif_post_night` | Shift done 👏 Keep your sunglasses on and your room dark for better sleep. | Vardiya bitti 👏 Daha iyi uyumak için güneş gözlüğünüzü takın, odanızı karanlık tutun. |

---

## Appendix D — Success metrics, risks & roadmap

### D.1 KPIs
| Metric | Target (month 3) |
|---|---|
| Onboarding completion | ≥ 75 % |
| Activation (≥ 1 pattern or 7 shifts + 1 recovery check-in within 72 h) | ≥ 55 % |
| D7 / D30 retention | ≥ 35 % / ≥ 20 % |
| Recovery check-ins per completed shift | ≥ 60 % |
| Trial start rate (of new users) | ≥ 10 % |
| Trial → paid | ≥ 40 % |
| Free → paid overall | 4–6 % |
| Store rating | ≥ 4.6 |
| Crash-free sessions | ≥ 99.5 % |

### D.2 Risks & mitigations
| Risk | Mitigation |
|---|---|
| Check-in fatigue | Four questions on one screen, only Q4 required, answer Q4 from the notification, smart defaults; missed check-ins never penalised; no guilt messaging |
| Notifications unreliable (Android OEM battery killers, iOS 64 limit) | Exact alarms permission flow, rolling schedule, reconciliation on open, "Test notification" button, OEM help page |
| Complex/unusual rotations | Custom pattern builder (premium) + manual override always free |
| Medical/advice liability | Evidence-based tips with sources, clear disclaimer, no diagnosis |
| Data loss (offline only) | Prompted local backup monthly; cloud sync in v1.2 |
| Price sensitivity in TR | Purchasing-power pricing, lifetime plan, healthcare discount |

### D.3 Release roadmap
| Release | Scope |
|---|---|
| **MVP 1.0** (≈ 12–14 weeks) | All "M" items in §2: calendar + patterns, post-shift recovery system (check-in, score, insights), energy, basic + smart reminders, stats, onboarding, TR/EN, dark mode, paywall, caffeine & meal timing, shift journal, TR holidays & leave, partner sharing |
| 1.1 | Home & lock-screen widgets, goals & streaks, profession-based tips, amber Night Mode, Apple Health / Health Connect pre-fill for recovery sleep duration, multi-shift days |
| 1.2 | Encrypted cloud backup & sync, calendar .ics export/import, monthly shareable recap |
| 2.0 | Anonymous comparison (opt-in, aggregated backend), team/workplace licenses, shift swap sharing with coworkers, wearable integrations, Apple Watch / Wear OS companion |

### D.4 Suggested MVP sprint plan (2-week sprints)
1. Project setup, design system (themes, tokens, l10n scaffolding), DB schema
2. Calendar month/week, shift types, day sheet, bulk edit
3. Pattern library & configurator, onboarding
4. Recovery check-in (Q1–Q4), Recovery Score engine, Recovery tab, post-shift prompts
5. Energy check-in, stats tab & charts, recovery insights engine
6. Reminders engine, paywall & purchases, settings, QA, store assets (TR/EN), beta (TestFlight / Play internal)
