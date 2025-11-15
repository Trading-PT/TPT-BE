# Recurring Payment Flow Diagrams

## Fix #1: Billing Key Registration Flow

### Before Fix (INCORRECT)
```
┌─────────────────────────────────────────────────────────────────┐
│ User Registers Billing Key                                     │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PaymentMethodCommandServiceImpl.completeBillingKeyRegistration │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ├─► Create PaymentMethod ✅
                 │
                 ├─► ALWAYS create Subscription ❌
                 │   (Even if ACTIVE subscription exists)
                 │
                 └─► Execute first payment ❌
                     (Duplicate payment if subscription exists)

PROBLEM: Creates duplicate subscriptions and payments
```

### After Fix (CORRECT)
```
┌─────────────────────────────────────────────────────────────────┐
│ User Registers Billing Key                                     │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PaymentMethodCommandServiceImpl.completeBillingKeyRegistration │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ├─► Create PaymentMethod ✅
                 │
                 ├─► Check for existing ACTIVE subscription
                 │
                 ├─────┬─────────────────────────────────────────┐
                 │     │                                         │
                 │     ▼                                         ▼
                 │  FOUND                                    NOT FOUND
                 │     │                                         │
                 │     ├─► Log: "결제수단만 등록"               ├─► Create Subscription ✅
                 │     │                                         │
                 │     └─► Return ✅                             ├─► Execute first payment ✅
                 │         (No duplicate)                        │
                 │                                               └─► Return ✅

SOLUTION: Only creates subscription when none exists
```

---

## Fix #2: Recurring Payment Flow

### Before Fix (INCORRECT)
```
┌─────────────────────────────────────────────────────────────────┐
│ Scheduler: Process Recurring Payments                          │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ RecurringPaymentService.handleRegularPayment                   │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ├─► Get subscription.paymentMethod
                 │
                 ├─► IF paymentMethod.billingKey == null
                 │   └─► Throw exception ❌
                 │       (Doesn't check if deleted or inactive)
                 │
                 └─► Execute payment
                     (Fails if PaymentMethod deleted)

PROBLEM: Doesn't validate PaymentMethod status, doesn't search alternatives
```

### After Fix (CORRECT)
```
┌─────────────────────────────────────────────────────────────────┐
│ Scheduler: Process Recurring Payments                          │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ RecurringPaymentService.handleRegularPayment                   │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ├─► Get subscription.paymentMethod
                 │
                 ├─► Validate PaymentMethod
                 │   (null? isDeleted? !isActive?)
                 │
                 ├─────┬─────────────────────────────────────────┐
                 │     │                                         │
                 │     ▼                                         ▼
                 │  INVALID                                   VALID
                 │     │                                         │
                 │     ├─► Log warning                          ├─► Check billingKey ✅
                 │     │                                         │
                 │     ├─► Search customer's valid             ├─► Execute payment ✅
                 │     │   primary PaymentMethod                │
                 │     │                                         └─► Success ✅
                 │     ├─────┬─────────────────┐
                 │     │     │                 │
                 │     │     ▼                 ▼
                 │     │  FOUND            NOT FOUND
                 │     │     │                 │
                 │     │     ├─► Log success  ├─► Log error
                 │     │     │                 │
                 │     │     ├─► Use new PM   ├─► Throw exception
                 │     │     │                 │
                 │     │     └─► Execute ✅    └─► Subscription fails
                 │     │         payment           (3 failures → PAYMENT_FAILED)

SOLUTION: Validates PaymentMethod, searches for valid alternatives dynamically
```

---

## Complete Scenario: User Flow

### Scenario: User Deletes and Re-registers Billing Key

```
Timeline: 2025-12-10 → 2025-12-15 → 2026-01-10

┌──────────────────────────────────────────────────────────────────────┐
│ 2025-12-10: Initial Registration                                    │
└────────────────┬─────────────────────────────────────────────────────┘
                 │
                 ├─► Register billing key (Card A)
                 ├─► PaymentMethod A created (isDeleted=false, isPrimary=true)
                 ├─► Subscription created (status=ACTIVE, expires=2026-01-10)
                 └─► First payment executed (SUCCESS)

                 Database State:
                 ┌─────────────────────────────────────────────────────┐
                 │ PaymentMethods:                                     │
                 │  - PaymentMethod A (isDeleted=false, isPrimary=true)│
                 │ Subscriptions:                                      │
                 │  - Subscription #1 (status=ACTIVE, paymentMethod=A) │
                 └─────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ 2025-12-15: User Deletes Billing Key                                │
└────────────────┬─────────────────────────────────────────────────────┘
                 │
                 ├─► Delete billing key (Card A)
                 └─► PaymentMethod A updated (isDeleted=true)
                     Subscription #1 remains ACTIVE ✅

                 Database State:
                 ┌─────────────────────────────────────────────────────┐
                 │ PaymentMethods:                                     │
                 │  - PaymentMethod A (isDeleted=TRUE, isPrimary=true) │
                 │ Subscriptions:                                      │
                 │  - Subscription #1 (status=ACTIVE, paymentMethod=A) │
                 └─────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ 2025-12-16: User Registers New Billing Key (Card B)                 │
└────────────────┬─────────────────────────────────────────────────────┘
                 │
                 ├─► Register billing key (Card B)
                 │
                 ├─► ✅ FIX #1: Check for existing ACTIVE subscription
                 │   └─► FOUND: Subscription #1 (ACTIVE)
                 │
                 ├─► PaymentMethod B created (isDeleted=false, isPrimary=true)
                 ├─► NO duplicate subscription ✅
                 └─► NO immediate payment ✅

                 Database State:
                 ┌─────────────────────────────────────────────────────┐
                 │ PaymentMethods:                                     │
                 │  - PaymentMethod A (isDeleted=TRUE, isPrimary=false)│
                 │  - PaymentMethod B (isDeleted=false, isPrimary=true)│
                 │ Subscriptions:                                      │
                 │  - Subscription #1 (status=ACTIVE, paymentMethod=A) │
                 │    (Still references A, but B is now primary)       │
                 └─────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ 2026-01-10: Recurring Payment Scheduled                             │
└────────────────┬─────────────────────────────────────────────────────┘
                 │
                 ├─► Scheduler triggers: processRecurringPayments()
                 │
                 ├─► Get Subscription #1.paymentMethod → PaymentMethod A
                 │
                 ├─► ✅ FIX #2: Validate PaymentMethod A
                 │   ├─► Check: A.isDeleted? → TRUE (INVALID!)
                 │   └─► Log: "구독의 결제수단이 유효하지 않음"
                 │
                 ├─► Search customer's valid primary PaymentMethod
                 │   └─► FOUND: PaymentMethod B (isDeleted=false, isPrimary=true)
                 │
                 ├─► Log: "유효한 결제수단 발견: paymentMethodId={B}"
                 │
                 ├─► Execute payment using PaymentMethod B ✅
                 │
                 └─► Update Subscription #1:
                     ├─► status = ACTIVE
                     ├─► nextBillingDate = 2026-02-10
                     └─► currentPeriodEnd = 2026-02-09

                 Database State:
                 ┌─────────────────────────────────────────────────────┐
                 │ Payments:                                           │
                 │  - Payment (2025-12-10): SUCCESS (PaymentMethod A)  │
                 │  - Payment (2026-01-10): SUCCESS (PaymentMethod B)  │
                 │ Subscriptions:                                      │
                 │  - Subscription #1 (status=ACTIVE, nextBilling=02-10)│
                 └─────────────────────────────────────────────────────┘

SUCCESS: Payment executed with new PaymentMethod B ✅
```

---

## Edge Case: No Valid PaymentMethod Available

```
┌──────────────────────────────────────────────────────────────────────┐
│ User Deletes ALL Billing Keys Before Recurring Payment              │
└────────────────┬─────────────────────────────────────────────────────┘
                 │
                 ├─► Subscription #1.paymentMethod = PaymentMethod A (deleted)
                 │
                 ├─► Validate PaymentMethod A
                 │   └─► INVALID (isDeleted=true)
                 │
                 ├─► Search customer's valid primary PaymentMethod
                 │   └─► NOT FOUND (all deleted)
                 │
                 ├─► Log: "유효한 결제수단 없음: customerId={}, subscriptionId={}"
                 │
                 ├─► Throw SubscriptionException
                 │
                 ├─► Mark Payment as FAILED
                 │
                 ├─► Increment subscription.paymentFailedCount
                 │
                 └─► IF paymentFailedCount >= 3:
                     └─► Update subscription.status = PAYMENT_FAILED

                 After 3 Consecutive Failures:
                 ┌─────────────────────────────────────────────────────┐
                 │ Subscriptions:                                      │
                 │  - Subscription #1 (status=PAYMENT_FAILED)          │
                 │    paymentFailedCount=3                             │
                 └─────────────────────────────────────────────────────┘

User must re-register billing key to resume subscription
```

---

## State Transition Diagram

```
PaymentMethod States:
┌─────────────┐
│   CREATED   │ (isDeleted=false, isActive=true, isPrimary=true)
└──────┬──────┘
       │
       ├─► Used for payments ✅
       │
       ├─────────────────┐
       │                 │
       ▼                 ▼
┌─────────────┐   ┌─────────────┐
│   DELETED   │   │  INACTIVE   │
│(isDeleted=  │   │(isActive=   │
│    true)    │   │   false)    │
└─────────────┘   └─────────────┘
       │                 │
       └────────┬────────┘
                │
                ▼
        ❌ INVALID for payments
        ✅ Triggers search for valid alternative


Subscription States During Recurring Payment:
┌─────────────┐
│   ACTIVE    │ nextBillingDate arrives
└──────┬──────┘
       │
       ├─► Validate PaymentMethod
       │
       ├─────────────────┬─────────────────┐
       │                 │                 │
       ▼                 ▼                 ▼
┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│    VALID    │   │  INVALID    │   │ NO PAYMENT  │
│   PAYMENT   │   │  BUT FOUND  │   │   METHOD    │
│   METHOD    │   │ ALTERNATIVE │   │   FOUND     │
└──────┬──────┘   └──────┬──────┘   └──────┬──────┘
       │                 │                 │
       ├─► Execute      ├─► Execute       ├─► Payment fails
       │   payment       │   payment       │
       │                 │                 ├─► Increment
       ├─► SUCCESS      ├─► SUCCESS       │   failedCount
       │                 │                 │
       ├─► Reset        ├─► Reset         ├─────────────┐
       │   failedCount   │   failedCount   │             │
       │                 │                 │             │
       └─► ACTIVE       └─► ACTIVE        ▼             ▼
                                      failedCount  failedCount
                                          < 3          >= 3
                                           │             │
                                           └─► ACTIVE    └─► PAYMENT_FAILED
                                               (retry)         (suspended)
```

---

## Sequence Diagram: Recurring Payment with Dynamic Resolution

```
Customer    Scheduler    RecurringPaymentService    PaymentMethodRepository    NicePayService
   │            │                  │                         │                      │
   │            ├─► Trigger        │                         │                      │
   │            │   processRecurringPayments()               │                      │
   │            │                  │                         │                      │
   │            │                  ├─► Get subscription      │                      │
   │            │                  │   .paymentMethod (A)    │                      │
   │            │                  │                         │                      │
   │            │                  ├─► Validate A            │                      │
   │            │                  │   isDeleted? → TRUE ❌  │                      │
   │            │                  │                         │                      │
   │            │                  ├─────────────────────────►                      │
   │            │                  │   findByCustomerAndIsPrimaryTrueAndIsDeletedFalse()
   │            │                  │                         │                      │
   │            │                  ◄─────────────────────────┤                      │
   │            │                  │   Returns PaymentMethod B ✅                   │
   │            │                  │                         │                      │
   │            │                  ├─────────────────────────┼──────────────────────►
   │            │                  │                         │   executeRecurringPayment(B)
   │            │                  │                         │                      │
   │            │                  ◄─────────────────────────┼──────────────────────┤
   │            │                  │                         │   Payment SUCCESS ✅ │
   │            │                  │                         │                      │
   │            │                  ├─► Update subscription   │                      │
   │            │                  │   nextBillingDate       │                      │
   │            │                  │   resetFailedCount      │                      │
   │            │                  │                         │                      │
   │◄───────────┼──────────────────┤                         │                      │
   Payment      │                  │                         │                      │
   Success      │                  │                         │                      │
```
