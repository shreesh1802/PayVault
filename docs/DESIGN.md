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
