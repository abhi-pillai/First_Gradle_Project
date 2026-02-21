# TrackIt — User Manual

> **Track your money, own your day.**

TrackIt is a personal expense tracker desktop application that helps you manage your income, expenses, budgets, and categories — all stored locally on your device. No internet connection required.

---

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Installation](#installation)
   - [Windows](#windows)
   - [macOS](#macos)
   - [Linux (Debian/Ubuntu)](#linux-debianubuntu)
3. [Uninstallation](#uninstallation)
   - [Windows](#windows-1)
   - [macOS](#macos-1)
   - [Linux (Debian/Ubuntu)](#linux-debianubuntu-1)
4. [Getting Started](#getting-started)
   - [Creating an Account](#creating-an-account)
   - [Logging In](#logging-in)
5. [Using TrackIt](#using-trackit)
   - [Dashboard Overview](#dashboard-overview)
   - [Managing Expenses](#managing-expenses)
   - [Managing Income](#managing-income)
   - [Categories](#categories)
   - [Budget](#budget)
   - [Reports & PDF Export](#reports--pdf-export)
   - [Account Settings](#account-settings)
6. [Data Storage](#data-storage)
7. [Important Notes & Tips](#important-notes--tips)
8. [Troubleshooting](#troubleshooting)

---

## System Requirements

| Platform | Requirement |
|---|---|
| Windows | Windows 10 or later (64-bit) |
| macOS | macOS 11 (Big Sur) or later |
| Linux | Debian 10 / Ubuntu 20.04 or later (64-bit) |
| Java | ✅ Not required — JVM is bundled inside the installer |
| Disk Space | ~200 MB |
| RAM | 256 MB minimum |

---

## Installation

### Windows

1. Download **`TrackIt-1.0.0.exe`** from the [Releases page](https://github.com/abhi-pillai/First_Gradle_Project/releases)
2. Double-click the `.exe` file to run the installer
3. If Windows shows a **SmartScreen warning**:
   - Click **More info**
   - Click **Run anyway**
4. Follow the installer steps: **Next → Install → Finish**
5. Launch TrackIt from the **Start Menu** or **Desktop shortcut**

> **Where is it installed?**
> `C:\Program Files\TrackIt\`

---

### macOS

1. Download **`TrackIt-1.0.0.dmg`** from the [Releases page](https://github.com/abhi-pillai/First_Gradle_Project/releases)
2. Double-click the `.dmg` file to open it
3. Drag **TrackIt** into the **Applications** folder
4. Open **Launchpad** or **Finder → Applications** and click TrackIt
5. If macOS blocks it with a security warning:
   - Go to **System Settings → Privacy & Security**
   - Scroll down and click **Open Anyway**

> **Where is it installed?**
> `/Applications/TrackIt.app`

---

### Linux (Debian/Ubuntu)

1. Download **`trackit_1.0.0_amd64.deb`** from the [Releases page](https://github.com/abhi-pillai/First_Gradle_Project/releases)
2. Open a terminal in the folder where you downloaded the file
3. Run the following command:
   ```bash
   sudo apt install ./trackit_1.0.0_amd64.deb
   ```
4. Launch TrackIt from the **App Menu** (press Super key → search "TrackIt")
   - Or from terminal: `trackit`

> **Where is it installed?**
> `/opt/trackit/` — main app files
> `/usr/bin/trackit` — terminal launcher

> **Note:** This `.deb` package works on **Debian and Ubuntu-based** distributions only (Ubuntu, Linux Mint, Pop!_OS, etc.). It does **not** work on Fedora, Red Hat, or Arch Linux.

---

## Uninstallation

### Windows

**Step 1 — Uninstall the app:**
- Go to **Settings → Apps → Installed Apps**
- Search for **TrackIt**
- Click **Uninstall**

**Step 2 — Remove leftover data (optional):**
- Open File Explorer
- Navigate to: `C:\Users\YourName\AppData\Roaming\`
- Delete the **TrackIt** folder

> ⚠️ Deleting the `AppData\Roaming\TrackIt` folder will permanently erase all your saved data (expenses, income, categories, users). Only do this if you want a complete clean removal.

---

### macOS

**Step 1 — Remove the app:**
- Open **Finder → Applications**
- Drag **TrackIt.app** to **Trash**
- Or right-click → **Move to Trash**

**Step 2 — Remove leftover data (optional):**
- In Finder, press **Cmd + Shift + G**
- Type: `~/Library/Application Support/`
- Find and delete the **TrackIt** folder

---

### Linux (Debian/Ubuntu)

**Step 1 — Remove the package:**
```bash
sudo apt purge trackit
sudo apt autoremove
```

**Step 2 — Remove leftover data (optional):**
```bash
rm -rf ~/.local/share/TrackIt
```

**Step 3 — Verify complete removal:**
```bash
which trackit          # should return nothing
ls /opt | grep track   # should return nothing
```

---

## Getting Started

### Creating an Account

1. Launch TrackIt
2. On the Login screen, click **"Register"**
3. Enter a **username** and **password**
4. Click **Register**
5. TrackIt will automatically create 9 default expense categories for you:
   - Food, Rent, Transport, Entertainment, Healthcare, Shopping, Utilities, Education, Other

> 💡 **Tip:** Choose a strong password — TrackIt stores your password securely using salted SHA-256 hashing.

---

### Logging In

1. Enter your **username** and **password**
2. Click **Login**
3. You will be taken to the **Dashboard**

> ⚠️ **Important:** There is no "Forgot Password" feature. If you forget your password, you will not be able to recover your account. Keep your password safe.

---

## Using TrackIt

### Dashboard Overview

The Dashboard is the main screen you see after logging in. It shows:

| Section | Description |
|---|---|
| **Total Income** | Sum of all income entries ever recorded |
| **Total Expenses** | Sum of all expense entries ever recorded |
| **Net Savings** | Total Income minus Total Expenses |
| **Expenses Table** | A list of all your recorded expenses |
| **Income Table** | A list of all your recorded income entries |

From the Dashboard you can access all features via the buttons and menu options.

---

### Managing Expenses

#### Adding an Expense
1. Click **"Add Expense"** on the Dashboard
2. Fill in the following fields:
   - **Amount (₹)** — must be a positive number
   - **Date** — defaults to today, can be changed
   - **Category** — select from your categories
   - **Note** — optional description
   - **Payment Method** — Cash, Card, Bank Transfer, or UPI
3. Click **"Save Expense"**

#### Editing an Expense
1. Select an expense from the expenses table
2. Click **"Edit"**
3. Modify the fields and click **"Update Expense"**

#### Deleting an Expense
1. Select an expense from the expenses table
2. Click **"Delete"**
3. Confirm the deletion

#### Filtering & Searching Expenses
- Filter by **date range**, **category**, or **keyword**
- Keyword search looks through both the category and the note fields

---

### Managing Income

#### Adding Income
1. Click **"Add Income"** on the Dashboard
2. Fill in the following fields:
   - **Amount (₹)** — must be a positive number
   - **Date** — defaults to today
   - **Source** — e.g. Salary, Freelance, Business (cannot be empty)
   - **Note** — optional description
3. Click **"Save Income"**

#### Editing & Deleting Income
- Works the same way as expenses — select a row and use the **Edit** or **Delete** buttons

---

### Categories

Categories help you organise your expenses. TrackIt gives you 9 default categories which you can customise.

#### Opening Category Management
- Click **"Manage Categories"** from the Dashboard

#### Adding a Category
1. Click **"➕ Add Category"**
2. Enter a **category name**
3. Enter a **monthly budget** (optional, enter 0 to skip)
4. Click **OK**

#### Editing a Category Budget
1. Select a category from the list
2. Click **"✏ Edit Budget"**
3. Enter the new monthly budget amount
4. Click **OK**

#### Deleting a Category
1. Select a category from the list
2. Click **"🗑 Delete"**
3. Confirm deletion

> ⚠️ **Important:** Deleting a category does **not** delete the expenses under it. Those expenses will remain but will reference a category that no longer exists. It is recommended to reassign expenses before deleting a category.

---

### Budget

The Budget feature lets you set a monthly spending limit and track how much you have remaining.

#### Opening Budget Overview
- Click **"Budget"** from the Dashboard

#### Setting a Monthly Budget
1. Enter the budget amount in the **"Monthly Budget (₹)"** field
2. Click **"Set Budget"**

#### Reading the Budget Overview

| Field | Description |
|---|---|
| **Month** | Current month being tracked |
| **Budget** | Your set monthly spending limit |
| **Remaining** | Budget minus this month's expenses |
| **⚠ OVER BUDGET!** | Appears when you have exceeded your limit |
| **✔** | Appears when you are within budget |

#### Category Budgets
- If a category has a monthly budget set (greater than 0), it will appear in the category budgets section
- Shows how much of each category budget remains for the current month

> 💡 **Tip:** Set category budgets in **Manage Categories** and they will automatically appear in the Budget overview.

---

### Reports & PDF Export

#### Opening Reports
- Click **"Reports"** from the Dashboard

#### Monthly Report
1. Select a month from the dropdown (last 12 months available)
2. Click **"View Report"**
3. The report shows:
   - **Income, Expenses, and Savings** for that month
   - **Category Breakdown** — how much was spent in each category
   - **All-Time Summary** — total income, expenses, and net savings

#### Pie Chart
- A visual pie chart shows the category spending breakdown for the selected month
- Each slice shows the category name, amount, and percentage

#### Exporting to PDF

**Expense PDF:**
1. Click **"📄 Expenses PDF"**
2. Choose where to save the file
3. A formatted PDF is generated with all your expenses and total

**Income PDF:**
1. Click **"📄 Income PDF"**
2. Choose where to save the file
3. A formatted PDF is generated with all your income entries and total

> 💡 **Tip:** PDF exports include **all records** (not just the selected month). Use filters on the Dashboard first if you want to review specific data before exporting.

---

### Account Settings

#### Opening Account Settings
- Click **"Account Settings"** from the Dashboard

#### Changing Your Password
1. Enter your **current password**
2. Enter a **new password**
3. Confirm the new password
4. Click **"Change Password"**

#### Deleting Your Account
1. Enter your password to confirm
2. Click **"Delete Account"**

> ⚠️ **Warning:** Deleting your account is **permanent and irreversible**. All your expenses, income, categories, and budget data will be erased immediately. There is no way to recover this data.

---

## Data Storage

All your data is stored **locally on your device** — nothing is sent to the internet.

| Platform | Data Location |
|---|---|
| Windows | `C:\Users\YourName\AppData\Roaming\TrackIt\data\` |
| macOS | `~/Library/Application Support/TrackIt/data/` |
| Linux | `~/.local/share/TrackIt/data/` |

The following files are stored:

| File | Contents |
|---|---|
| `users.csv` | Registered user accounts (passwords are hashed, never plain text) |
| `expenses.csv` | All expense records |
| `income.csv` | All income records |
| `categories.csv` | All categories and their budgets |
| `budget.properties` | Monthly budget settings per user |

> 💡 **Tip:** You can back up your data by copying the entire `TrackIt/data/` folder to a safe location. To restore, simply paste it back to the same location.

---

## Important Notes & Tips

- **Multiple users** can register and use TrackIt on the same device — each user's data is kept completely separate
- **There is no forgot password** option — remember your password or store it safely
- **Data is local** — uninstalling the app does not delete your data unless you manually delete the data folder
- **PDF export includes all records** — there is no date filter for PDF exports currently
- **Category deletion** does not delete linked expenses — only the category label is removed
- **Budget tracking** is per month — it resets automatically at the start of each new month
- **Date format** used throughout the app is `yyyy-MM-dd` (e.g. 2026-02-20)
- **Amount fields** only accept positive numbers — zero or negative values will show an error
- **Source field** in income is mandatory — it cannot be left empty

---

## Troubleshooting

| Problem | Solution |
|---|---|
| App doesn't open after installation | Try running it as Administrator (Windows) or check permissions (Linux/macOS) |
| Data not saving | Check that the data folder exists and is writable at the path listed in [Data Storage](#data-storage) |
| PDF export fails | Make sure you have write permission to the folder where you're saving the PDF |
| Can't log in | Username and password are case-sensitive — check your credentials carefully |
| Category not showing in Add Expense | Make sure you have added at least one category in Manage Categories |
| App shows blank or crashes on startup | Delete the data folder and relaunch — the app will recreate it fresh (⚠️ this erases all data) |
| Linux: `trackit` command not found | Run using full path: `/opt/trackit/bin/trackit` |
| macOS: "App is damaged" warning | Run: `xattr -cr /Applications/TrackIt.app` in Terminal, then reopen |

---

## Support

For issues, feature requests, or bug reports, visit the project repository:

**GitHub:** [https://github.com/abhi-pillai/First_Gradle_Project](https://github.com/abhi-pillai/First_Gradle_Project)

---

*TrackIt v1.0.0 — Track your money, own your day.*