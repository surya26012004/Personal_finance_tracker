# WealthWise — Personal Wealth & Portfolio Tracker

WealthWise is a comprehensive personal wealth tracking application designed to monitor net worth, stocks, mutual funds, daily returns, income & expenses, loans, and financial goals.

## 🚀 Deploying to GitHub Pages

You can deploy this project to GitHub Pages in 2 simple ways:

### Option 1: Automated Deployment via GitHub Actions (Recommended)
1. Push this repository to your GitHub account (`main` or `master` branch).
2. Go to your GitHub repository **Settings** → **Pages**.
3. Under **Build and deployment** → **Source**, select **GitHub Actions**.
4. The workflow in `.github/workflows/deploy-pages.yml` will automatically build and publish your website!

### Option 2: Deploy directly from `/docs` or `/root`
1. Go to your GitHub repository **Settings** → **Pages**.
2. Under **Build and deployment** → **Source**, select **Deploy from a branch**.
3. Select branch `main` (or `master`) and choose either folder:
   - `/docs`
   - `/ (root)`
4. Click **Save**. Your site will be live at `https://<your-username>.github.io/<repo-name>/`.

---

## 📱 Features Included
- **Net Worth & Valuation**: Live calculated net worth, profit/loss, and historical valuation growth charts.
- **Holdings Management**: Support for Indian Equities, US Stocks, Mutual Funds, Gold/SGB, FD/Debt, PPF, NPS, and Real Estate.
- **Daily NAV / Price Adjustments**: Quick 1-tap price adjustments (+1% / -1%) and daily performance change previews.
- **Cashflow & Savings**: Monthly salary/dividends vs expenses and real-time savings rate gauge.
- **Loans & Liabilities**: Principal balance, monthly EMI tracker, and 1-tap EMI payoff deductions.
- **Transactions & Goals**: Detailed trade history, dividend logging, and FIRE milestone progress bars.
- **Client-Side Persistence**: Automatic LocalStorage sync with JSON Export and Import backup capabilities.
- **Android App**: Native Android companion app with Jetpack Compose, Room Database, and Material 3 design.
