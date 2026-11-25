# PayVault — Design Log

A running record of design decisions, experiments and bugs, **written as you build**.
Add an entry whenever you make a choice, hit a bug, or learn something. These notes help you most in the viva.

---

## Decisions

Template:

### D1 — <short title>
- **Date:**
- **Decision:**
- **Alternatives considered:**
- **Why:**

---

## Experiments

Template:

### E1 — <what you tried>
- **Date:**
- **What I expected:**
- **What actually happened:**
- **What I learned:**

Planned experiments:
- [ ] Remove `serialVersionUID`, change the class, load old data → what happens?
- [ ] Add a `ConsoleHandler` without `setUseParentHandlers(false)` → what happens?
- [ ] Force a failure mid-transfer with and without `setAutoCommit(false)` → compare the balances
- [ ] Use `double` instead of `BigDecimal` for `0.1 + 0.2` → what's printed?

---

## Bugs

Template:

### B1 — <symptom>
- **Date:**
- **Symptom:**
- **Cause:**
- **Fix:**
