# 2026-06-02 Design Review Convergence Ledger

## Purpose

Track each independent review round for `docs/design/`, every finding, and its disposition.

## Round 1

- Reviewer: `general` subagent
- Evidence: `ses_17781aa7effeIvgNDoqYSa2YFo`
- Verdict: fail

Findings and disposition:

1. `docs/design/order-and-cart.md` mixed deferred groupon/comment/after-sale behavior into the current order baseline.
   - Disposition: fix in docs
2. `docs/design/app-overview.md` listed after-sales and marketing as current top-level workflows while marking the related domain objects deferred.
   - Disposition: fix in docs
3. `docs/design/system-configuration.md` treated comment-window timing as a current supported configuration category although comment/review is deferred.
   - Disposition: fix in docs
4. `docs/design/feature-inventory.md` did not distinguish MVP pre-shipment refund handling from deferred post-receipt after-sales.
   - Disposition: fix in docs
5. `docs/design/feature-inventory.md` made the `Search` feature ambiguous by mixing MVP catalog search with deferred search history and browse footprint semantics.
   - Disposition: fix in docs
6. `docs/design/system-configuration.md` did not define `Notice` clearly relative to deferred notifications.
   - Disposition: fix in docs
7. `docs/design/feature-inventory.md` pointed designed features back to raw input instead of the synthesized requirement owner doc.
   - Disposition: fix in docs
8. `docs/design/system-configuration.md` still contained an operational/idempotency-style task rule that belongs more naturally in architecture.
   - Disposition: fix in docs
9. `docs/design/app-overview.md` referenced stale `docs/requirements/product-scope.md` instead of the active MVP requirement.
   - Disposition: fix in docs

Conflict classification:

- Requirement-vs-design conflict: yes, but handled as editorial alignment to `docs/requirements/commercial-baseline.md`; no supported-behavior change proposed.
- Code-vs-design conflict: not assessed in this round.

## Round 2

- Reviewer: `general` subagent
- Evidence: `ses_17781aa7effeIvgNDoqYSa2YFo` continuation
- Verdict: fail

Findings and disposition:

1. `docs/design/order-and-cart.md` still mixed deferred groupon/comment/after-sale behavior into the table presented as the MVP state machine.
   - Disposition: fix in docs
2. `docs/design/feature-inventory.md` overlapped `Content management` and `Notice` scope.
   - Disposition: fix in docs

Conflict classification:

- Requirement-vs-design conflict: editorial alignment to `docs/requirements/commercial-baseline.md`; no supported-behavior change proposed.
- Code-vs-design conflict: not assessed in this round.

## Round 3

- Reviewer: `general` subagent
- Evidence: `ses_17781aa7effeIvgNDoqYSa2YFo` continuation
- Verdict: fail

Findings and disposition:

1. `docs/design/roles-and-permissions.md` still described deferred after-sales/refund-window behavior inconsistent with MVP scope.
   - Disposition: fix in docs
2. `docs/design/roles-and-permissions.md` still mixed business permissions with technical implementation ownership.
   - Disposition: fix in docs
3. `docs/design/roles-and-permissions.md` was ambiguous about whether admin-account management is super-admin-only.
   - Disposition: fix in docs

Conflict classification:

- Requirement-vs-design conflict: editorial alignment to `docs/requirements/commercial-baseline.md` and `docs/design/user-and-address.md`; no supported-behavior change proposed.
- Code-vs-design conflict: not assessed in this round.

## Round 4

- Reviewer: `general` subagent
- Evidence: `ses_17781aa7effeIvgNDoqYSa2YFo` continuation
- Verdict: pass

Findings and disposition:

- none

Conflict classification:

- Requirement-vs-design conflict: none requiring reopening the requirement; reviewer noted a possible narrower reading of MVP refund intent, but treated current design as acceptable MVP intent rather than a blocker.
- Code-vs-design conflict: not assessed in this round.

## Round 5

- Reviewer: `general` subagent
- Evidence: `ses_1777209f8ffeGEV66Niug4A9Ne`
- Verdict: fail

Findings and disposition:

1. `docs/design/order-and-cart.md` and `docs/design/roles-and-permissions.md` did not consistently express the MVP requirement for admin-initiated refund of paid orders.
   - Disposition: fix in docs

Conflict classification:

- Requirement-vs-design conflict: editorial alignment to `docs/requirements/commercial-baseline.md`; no supported-behavior change proposed.
- Code-vs-design conflict: not assessed in this round.

## Round 6

- Reviewer: `general` subagent
- Evidence: `ses_1777209f8ffeGEV66Niug4A9Ne` continuation
- Verdict: pass

Findings and disposition:

- none

Conflict classification:

- Requirement-vs-design conflict: none blocking; refund semantics aligned with `docs/requirements/commercial-baseline.md`.
- Code-vs-design conflict: not assessed in this round.

## Round 7

- Reviewer: `general` subagent
- Evidence: `ses_1776cff3effe9OPS67Wct3rYpg`
- Verdict: pass

Findings and disposition:

- none

Conflict classification:

- Requirement-vs-design conflict: none blocking.
- Code-vs-design conflict: not assessed in this round.

## Round 8

- Reviewer: `general` subagent
- Evidence: `ses_177664d92ffeuZqQcilSlLjilL`
- Verdict: fail

Findings and disposition:

1. `docs/design/order-and-cart.md` did not define the MVP mock/skipped payment-confirmation path needed to move non-zero orders into the paid state while WeChat Pay remains deferred.
   - Disposition: fix in docs

Conflict classification:

- Requirement-vs-design conflict: editorial alignment to `docs/requirements/commercial-baseline.md` local integration substitute allowance; no supported-behavior change proposed.
- Code-vs-design conflict: not assessed in this round.

## Round 9

- Reviewer: `general` subagent
- Evidence: `ses_177664d88ffeUtEIhRRP9xIrxQ`
- Verdict: pass

Findings and disposition:

- none

Conflict classification:

- Requirement-vs-design conflict: none blocking.
- Code-vs-design conflict: not assessed in this round.

## Round 10

- Reviewer: `general` subagent
- Evidence: `ses_17763ee71ffeeZDInsF5ViefL5`
- Verdict: pass

Findings and disposition:

- none

Conflict classification:

- Requirement-vs-design conflict: none blocking.
- Code-vs-design conflict: not assessed in this round.

## Round 11

- Reviewer: `general` subagent
- Evidence: `ses_17763ebcfffeu8xDJqN46EeliT`
- Verdict: pass

Findings and disposition:

- none

Conflict classification:

- Requirement-vs-design conflict: none blocking.
- Code-vs-design conflict: not assessed in this round.
