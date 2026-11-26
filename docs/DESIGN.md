# PayVault — Design Log

A running record of design decisions, experiments and bugs, **written as you build**.
Add an entry whenever you make a choice, hit a bug, or learn something. These notes help you most in the viva.

---

## Decisions

<!-- Template for new decisions:
### D? — <short title>
- **Date:**
- **Decision:**
- **Alternatives considered:**
- **Why:**
-->

### D1 — Record the opening balance as a transaction
- **Date:** 26/11/2026
- **Decision:** When an account is opened with a balance greater than 0, the constructor records it as a `DEPOSIT` transaction with the note "Opening Balance". An account opened with 0 records nothing.
- **Alternatives considered:** Just set `balance` to the opening amount and leave the history empty. That's simpler and gives the history only "real" activity.
- **Why:** The history should fully explain the balance: opening deposit + later deposits − withdrawals = current balance. Without the opening entry, a customer who opened with ₹5000 would see a history that doesn't add up to their balance, and there'd be no record of where that money came from. A zero opening balance isn't recorded because a ₹0 "deposit" carries no information, and `Transaction` amounts should always be real money movements.

### D2 — Order of checks in deposit() and withdraw(): amount → frozen → balance
- **Date:** 26/11/2026
- **Decision:** Both methods run their checks in a fixed order, each one throwing straight away if it fails:
  1. **Amount** (`validateAmount`): null, ≤ 0, or more than 2 decimals → `InvalidAmountException`
  2. **Frozen**: the account is frozen → `AccountFrozenException`
  3. **Balance** (`withdraw` only): amount > `withdrawableAmount()` → `InsufficientBalanceException`

  Only after all checks pass do the balance change and the transaction get recorded. So a frozen account given −100 reports *"Amount must be positive"*, not *"Account is frozen"*.
- **Alternatives considered:**
  - **Frozen first:** a frozen account rejects everything with "frozen", whatever the amount. It's simple and reveals nothing about the account to someone it's locked away from. But it hides the caller's bug: −100 is wrong for *every* account, and the caller only finds out once the account is unfrozen.
  - **Balance before frozen:** this would tell a frozen account's user how much money it lacks, which they shouldn't learn while it's locked. And "insufficient balance" would mislead them: adding money wouldn't fix it, since deposits are blocked too.
  - **Collect all errors and report them together:** useful in forms (M15 validation), but overkill here. One clear reason per failure is enough.
- **Why:** The checks go from the cheapest and most general to the most specific:
  - **Amount** depends only on the input. It needs no account state, and a bad amount is a *programming or input mistake* (that's why it's unchecked). That's a bug to report first, wherever it happens.
  - **Frozen** depends on the account's *state*, not the amount. If the account is frozen, no valid amount can succeed, so there's no point comparing balances.
  - **Balance** needs a *valid* amount (you can't compute a shortfall from null or −100) and an *active* account, so it has to come last.

  **Key rule:** every check runs *before* `balance` is changed or `recordTransaction` is called. A thrown exception stops the method on that line, so a failed operation changes nothing: no balance change and no history entry. Done-when check 12 (3 history entries) proves this.

---

## Experiments

<!-- Template for new experiments:
### E? — <what you tried>
- **Date:**
- **What I expected:**
- **What actually happened:**
- **What I learned:**
-->

### E1 — Adding 0.1 + 0.2 with double vs BigDecimal
- **Date:** 26/11/2026
- **What I expected:** `0.3` from all three lines.
- **What actually happened:**
  ```
  0.1 + 0.2                                        → 0.30000000000000004
  new BigDecimal("0.1").add(new BigDecimal("0.2")) → 0.3
  new BigDecimal(0.1)                              → 0.1000000000000000055511151231257827021181583404541015625
  ```
- **What I learned:** `double` stores numbers in binary, and 0.1 has no exact binary form, so tiny errors appear even in simple sums. That's why PayVault uses `BigDecimal` for all money. The third line shows that `BigDecimal` only helps if it's built from a **String**: the `double` constructor copies the already-inaccurate binary value. So money values are always written as `new BigDecimal("...")`.

Planned experiments:
- [ ] Remove `serialVersionUID`, change the class, load old data → what happens?
- [ ] Add a `ConsoleHandler` without `setUseParentHandlers(false)` → what happens?
- [ ] Force a failure mid-transfer with and without `setAutoCommit(false)` → compare the balances
- [x] Use `double` instead of `BigDecimal` for `0.1 + 0.2` → what's printed? (E1)

---

## Bugs

Template:

### B1 — <symptom>
- **Date:**
- **Symptom:**
- **Cause:**
- **Fix:**
