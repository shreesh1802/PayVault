# M4 — Collections

**Phase 1 · Week 1 · ~2 days · Unit 7 (and 4)**

## Goal
So far PayVault uses only two collections: a `HashMap` of accounts and an `ArrayList` of transactions. Real banking screens need much more than that: the **last few** transactions, the history **sorted** in different ways, a statement **grouped by month**, the **set** of people you've paid, and admin tools that **remove** accounts safely.

In M4 you add these features, each with the collection that fits the job best, and you must be able to **justify every choice**. Unit 7 is one of the biggest EST units, and this milestone covers most of it.

| Feature | Collection | Why this one |
|---|---|---|
| Last *n* transactions | `ArrayDeque` | add at the front, drop from the back, both O(1) |
| History sorted by amount / newest first | `List.sort` + `Comparator` | the same data in different orders, without changing `history` |
| People this account has transferred with | `HashSet` | no duplicates, fast `contains` |
| Monthly statement | `TreeMap<YearMonth, List<Transaction>>` | keys come out **sorted** by month |
| All accounts, in a chosen order | `List` + a `Comparator` **parameter** | the caller decides the order |
| Remove unused accounts | `Iterator.remove()` | the only safe way to remove while looping |
| Two `Account` objects with the same number are "the same" | `equals` + `hashCode` | needed by `HashSet` / `HashMap` |

## 📚 Reference slides

| If you're stuck on… | Deck | Slides |
|---|---|---|
| The collection hierarchy (List / Set / Queue / Map) | U7 Collection Framework | 5 |
| `ArrayList` methods | U7 | 7–27 |
| Queue and stack operations (`ArrayDeque` works the same way) | U7 | 37–40 |
| ArrayList vs LinkedList | U7 | 42 |
| `HashSet` + set operations | U7 | 43–58 |
| `HashMap` methods, iterating over a map | U7 | 59–70 |
| HashSet vs HashMap | U7 | 71 |
| `Iterator`, safe `remove()` | U7 | 72–79 |
| `Comparator`, sorting user-defined objects | U7 | 80–90 |
| Generics (`Comparator<Account>`, `Map<K, V>`) | U7 | 91–121 |
| `Objects.hash`, `Objects.equals` | U7 | 124–133 (especially 126, 129) |
| Anonymous classes | U4 Nested classes… | 2–5 · also U13 JavaFX Events 10–12 (same idea with `EventHandler`) |
| `TreeMap` | Not in the slides. Same methods as `HashMap` (U7 59–70), but the keys are always **sorted**. Also has `firstKey()` / `lastKey()`. | — |
| `ArrayDeque` | Not in the slides. Works like the LinkedList queue/stack in U7 37–40: `addFirst`, `addLast`, `removeFirst`, `removeLast`, `peekFirst`. | — |
| `YearMonth` | Not in the slides. A year + month with no day, e.g. `2026-11`. `YearMonth.from(dateTime)` gets it from a `LocalDateTime`. It's `Comparable`, so it sorts in date order. | — |

## Files

| File | Package | Kind |
|---|---|---|
| `Account.java` | `model` | modified |
| `WalletService.java` | `service` | modified |
| `StatementService.java` | `service` | **new** (grows in M7 with statement export) |
| `Main.java` | — | modified: add `runM4Checks()` |

## Requirements

### `Account`
1. **`public List<Transaction> getRecentTransactions(int n)`**: the last `n` transactions, **newest first**.
   - Use an `ArrayDeque<Transaction>`: loop over `history`, add each transaction to the **front**, and whenever the deque holds more than `n`, remove from the **back**.
   - Return it as a new `ArrayList`, so the caller can't change your deque.
   - If the history has fewer than `n` entries, return all of them.
2. **`public List<Transaction> getHistoryByAmount()`**: a **copy** of the history sorted by **amount, largest first**; for equal amounts, **older first**.
   - Build the comparator with `Comparator.comparing(...)`, `.reversed()` and `.thenComparing(...)`.
   - Never sort `history` itself: `getHistory()` must still return the original order.
3. **`public List<Transaction> getHistoryNewestFirst()`**: a copy sorted **newest first**, using an **anonymous class**: `new Comparator<Transaction>() { … compare(…) … }`. Writing it the long way once is how you see what `Comparator.comparing` does for you.
4. **`public Set<String> getCounterparties()`**: the account numbers this account has transferred money **to or from**, each listed once. Use a `HashSet<String>`. Transactions without a counterparty are skipped.
5. **`equals` and `hashCode`**: two `Account` objects are equal when they have the **same account number**. Nothing else is compared.
   - `equals(Object o)`: same object → `true`; not an `Account` → `false`; otherwise compare `accountNo` with `equals`.
   - `hashCode()`: `Objects.hash(accountNo)`.
   - Both get `@Override`. `instanceof` is allowed **here**: `equals` is the one standard place for it.

> **⚠️ Ordering by time:** don't use the timestamp to decide "newer" or "older". Transactions created a few microseconds apart usually get the **same** timestamp; Windows only updates the clock every few milliseconds, so 999 out of 1000 back-to-back `LocalDateTime.now()` calls return equal values. Use the **transaction id** instead: it's unique and only ever goes up, so a higher id always means a newer transaction. See **E3** below.

### `StatementService` (new)
- `public static Map<YearMonth, List<Transaction>> groupByMonth(List<Transaction> transactions)`
- Returns a `TreeMap`: one entry per month, holding that month's transactions in their original order.
- For each transaction, get its month with `YearMonth.from(t.getTimeStamp())`. If the month isn't a key yet, put a new empty `ArrayList` for it first, then add the transaction.
- It's `static` and takes a `List` (not an `Account`) so that it can group **any** list of transactions, including the backdated test data in `Main`.

### `WalletService`
1. **`public List<Account> getAllAccounts(Comparator<Account> order)`**: all accounts in a new list, sorted by the comparator the **caller** passes in. The same method gives "by balance" or "by account number" without any `if`.
2. **`public int removeEmptyAccounts()`**: removes every account whose balance is **zero and whose history is empty** (opened and never used). Returns how many were removed.
   - Loop with an explicit `Iterator<Account>` over `accounts.values()` and call **`iterator.remove()`**.
   - Removing from a `values()` view also removes the entry from the map itself.

## Rules
- ❌ Never call `accounts.remove(...)` or `history.remove(...)` inside a for-each loop over the same collection
- ❌ No sorting of `history` in place. Always sort a copy.
- ❌ Don't use timestamps to order transactions (see the warning above)
- ✅ Declare variables with the **interface** type: `List<…> x = new ArrayList<>()`, `Set<…>`, `Map<…>`, `Deque<…>`
- ✅ Every method that returns a collection returns a **new** collection or an unmodifiable view, never your internal one

## 🤔 Design decision (D6)
**`equals` and `hashCode` compare only `accountNo`. Why not also `balance`, `holderName` or `history`?** Think about what happens if an `Account` is inside a `HashSet` and then you deposit money into it: what would happen to its `hashCode`, and could the set still find it? Write up **D6** in `DESIGN.md`. Also mention why `equals` and `hashCode` must always be overridden **together**.

## 🧪 Experiment (E3)
Before you add `equals`/`hashCode`, put two `new SavingsAccount("S001", …)` objects into a `HashSet` and print its size. Then add the two methods and run it again. Record both results as **E3**, together with the timestamp-ties finding from the warning above (try it yourself: call `LocalDateTime.now()` 1000 times in a loop and count how often it's equal to the previous value).

## ✅ Done when: `runM4Checks()` demonstrates all of these

Setup: a **new** `WalletService` with `S001` (Savings, 10000), `C001` (Current, 5000) and `S002` (Savings, 3000). Then, through the service, in this order:
1. deposit 500 into S001, note `"salary"`
2. withdraw 300 from S001
3. transfer 1500 S001 → C001
4. transfer 700 S001 → S002
5. deposit 2500 into S001, note `"bonus"`
6. transfer 300 S001 → C001

| # | Action | Expected |
|---|---|---|
| 1 | Print S001 and its history size | balance **10200.00**, **7** entries |
| 2 | `getRecentTransactions(3)` | 3 entries, newest first: `TRANSFER_OUT 300 [C001]`, `DEPOSIT 2500 (bonus)`, `TRANSFER_OUT 700 [S002]` |
| 3 | `getRecentTransactions(10)` | all **7** (no error when asking for more than exist) |
| 4 | `getHistoryByAmount()` | 10000, 2500, 1500, 700, 500, then the two 300s: **WITHDRAWAL 300 before TRANSFER_OUT 300** (older first) |
| 5 | `getHistory()` after check 4 | still in the original order: the sort used a copy |
| 6 | `getHistoryNewestFirst().get(0)` | `TRANSFER_OUT 300 [C001]` |
| 7 | `getCounterparties()` | size **2**, contains `C001` and `S002`; `contains("X999")` is `false`. C001 appears twice in the history but once in the set. |
| 8 | `StatementService.groupByMonth(...)` on 5 backdated transactions, built with `Transaction.Builder().timeStamp(...)`: 2026-11-05, 2026-09-12, 2026-11-20, 2026-10-01, 2026-09-30 | keys in order **2026-09 (2), 2026-10 (1), 2026-11 (2)**, although they were added out of order |
| 9 | Print `firstKey()` and `lastKey()` of that `TreeMap` | **2026-09** and **2026-11** |
| 10 | `getAllAccounts(...)` by balance, largest first | **S001 10200.00, C001 6800.00, S002 3700.00** |
| 11 | `getAllAccounts(...)` by account number | **C001, S001, S002** |
| 12 | Add `E001` (Savings) and `E002` (Current), both opened with no balance, then `removeEmptyAccounts()` | returns **2**. 3 accounts left, and `findAccount("E001")` throws `AccountNotFoundException` |
| 13 | Two `new SavingsAccount("S001", …)` objects with different names and balances | `equals` → **true**, `==` → **false**, both added to a `HashSet` → size **1** |
| 14 | `List<String>` of `S001, E001, S002, E002`: remove the `E…` ones inside a **for-each** loop | `ConcurrentModificationException` caught. Print the list: `[S001, S002, E002]` (E001 was removed before the crash, E002 never was) |
| 15 | Same list, removing with an explicit `Iterator` and `it.remove()` | `[S001, S002]` |
| 16 | `runM2Checks()` and `runM3Checks()` still print what they did before | ✅ |

## 🎤 Be ready to answer (review + viva)
1. Why `ArrayDeque` for the recent transactions instead of an `ArrayList`? What does `addFirst` cost on each?
2. `TreeMap` vs `HashMap`: what's the difference in iteration order and speed? Why does the statement need a `TreeMap`?
3. What's the difference between `Comparable` and `Comparator`? Which one does `YearMonth` implement, and why does that matter for the `TreeMap`?
4. Rewrite your anonymous `Comparator` as a lambda, then as `Comparator.comparing(...)`. What does `.reversed()` do to a `thenComparing` chain that comes **after** it?
5. Why does check 14 throw `ConcurrentModificationException`? What is a **fail-fast** iterator? Why does `it.remove()` not throw?
6. Check 14 with a list of only **three** items (`S001, E001, S002`) does **not** throw, and that's a famous EST trap. Why? What goes wrong instead?
7. What's the **contract** between `equals` and `hashCode`? What breaks in a `HashSet` if you override only `equals`?
8. Why do the methods return copies instead of your internal lists? What would a caller be able to do otherwise?
9. `getAllAccounts(Comparator<Account> order)`: why is taking the comparator as a parameter better than writing `getAccountsByBalance()` and `getAccountsByNumber()`?

## 📝 EST link
Collection hierarchy (draw it) · List vs Set vs Map, and choosing one · ArrayList vs LinkedList · HashSet vs HashMap vs TreeMap · `Comparable` vs `Comparator` · `equals`/`hashCode` contract · fail-fast iterators and `ConcurrentModificationException` · output tracing of a sorted list · generics syntax (`<T>`, `Map<K, V>`) · anonymous classes vs lambdas

## Suggested commits
1. `Add recent, sorted and counterparty views of history`
2. `Add equals and hashCode to Account`
3. `Add StatementService with monthly grouping`
4. `Add sorted account listing and empty-account cleanup`
5. `Add M4 collections demo in Main`

Then push and say **"review M4"**. In between, you can ask **"check M4.1"** … after each commit.

## Stuck?
Check the 📚 slides first: U7 72–79 for the iterator, 80–90 for comparators. Then ask for **"M4 hint 1"**.
