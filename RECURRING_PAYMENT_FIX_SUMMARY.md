# Recurring Payment Logic Fix Summary

## Problem Statement

### Incorrect Behavior (Before Fix)
**Scenario:**
1. User registers billing key → Subscription(ACTIVE) + PaymentMethod A
2. User deletes billing key → PaymentMethod A.isDeleted = true
3. User registers NEW billing key → System INCORRECTLY creates duplicate subscription

**Issues:**
- ❌ Duplicate subscriptions created when billing key re-registered
- ❌ Recurring payment fails if subscription references deleted PaymentMethod
- ❌ System doesn't search for customer's valid PaymentMethod dynamically

### Expected Behavior (After Fix)
**Scenario:**
1. 2025-12-10: User registers billing key → Subscription(ACTIVE, expires 2026-01-10) + PaymentMethod A
2. User deletes billing key → PaymentMethod A.isDeleted = true, Subscription remains ACTIVE
3. User registers NEW billing key:
   - ✅ PaymentMethod B created
   - ✅ NO new subscription (existing ACTIVE subscription found)
   - ✅ NO immediate payment
4. Next billing date arrives:
   - IF valid PaymentMethod exists → payment succeeds
   - IF no valid PaymentMethod → payment fails

---

## Fix #1: Conditional Subscription Creation

**File:** `PaymentMethodCommandServiceImpl.java`
**Method:** `completeBillingKeyRegistration()`
**Lines:** 148-179

### Changes Made

**Added Import:**
```java
import java.util.Optional;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
```

**Added Dependency:**
```java
private final SubscriptionRepository subscriptionRepository;
```

**Logic Change:**
```java
// Before: ALWAYS created subscription
SubscriptionPlan activePlan = subscriptionPlanRepository.findByIsActiveTrue()...
Subscription subscription = subscriptionCommandService.createSubscriptionWithFirstPayment(...)

// After: Conditional subscription creation
Optional<Subscription> existingSubscription = subscriptionRepository
    .findByCustomer_IdAndStatus(customerId,
        com.tradingpt.tpt_api.domain.subscription.enums.Status.ACTIVE);

if (existingSubscription.isPresent()) {
    // 기존 활성 구독 존재 → 결제수단만 등록 (구독 생성 X, 첫 결제 X)
    log.info("기존 활성 구독 존재 - 결제수단만 등록: customerId={}, subscriptionId={}, paymentMethodId={}",
        customerId, existingSubscription.get().getId(), paymentMethod.getId());
} else {
    // 활성 구독 없음 → 신규 구독 생성 + 첫 결제 실행
    SubscriptionPlan activePlan = subscriptionPlanRepository.findByIsActiveTrue()...
    Subscription subscription = subscriptionCommandService.createSubscriptionWithFirstPayment(...)
}
```

### Behavior

| Scenario | Before Fix | After Fix |
|----------|-----------|-----------|
| New user registers billing key | ✅ PaymentMethod + Subscription + Payment | ✅ PaymentMethod + Subscription + Payment |
| User with ACTIVE subscription registers new billing key | ❌ Duplicate subscription created | ✅ PaymentMethod only (NO duplicate) |

---

## Fix #2: Dynamic PaymentMethod Resolution

**File:** `RecurringPaymentService.java`
**Method:** `handleRegularPayment()`
**Lines:** 182-219

### Changes Made

**Added Import:**
```java
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
```

**Added Dependency:**
```java
private final PaymentMethodRepository paymentMethodRepository;
```

**Logic Change:**
```java
// Before: Used subscription's referenced PaymentMethod directly
PaymentMethod paymentMethod = subscription.getPaymentMethod();
if (paymentMethod.getBillingKey() == null) {
    throw new SubscriptionException(...);
}

// After: Validate and dynamically resolve PaymentMethod
PaymentMethod paymentMethod = subscription.getPaymentMethod();

// 현재 결제수단 유효성 검증
if (paymentMethod == null
    || paymentMethod.getIsDeleted()
    || !paymentMethod.getIsActive()) {

    log.warn("구독의 결제수단이 유효하지 않음. 고객의 다른 결제수단 검색: subscriptionId={}, customerId={}",
        subscription.getId(), subscription.getCustomer().getId());

    // 고객의 유효한 주 결제수단 검색
    paymentMethod = paymentMethodRepository
        .findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(subscription.getCustomer())
        .orElse(null);

    if (paymentMethod == null) {
        log.error("유효한 결제수단 없음: customerId={}, subscriptionId={}",
            subscription.getCustomer().getId(), subscription.getId());
        throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_UPDATE_FAILED);
    }

    log.info("유효한 결제수단 발견: customerId={}, paymentMethodId={}",
        subscription.getCustomer().getId(), paymentMethod.getId());
}

if (paymentMethod.getBillingKey() == null) {
    log.error("결제수단에 빌링키 없음: paymentMethodId={}", paymentMethod.getId());
    throw new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_UPDATE_FAILED);
}
```

### Behavior

| Scenario | Before Fix | After Fix |
|----------|-----------|-----------|
| Recurring payment with valid PaymentMethod | ✅ Payment succeeds | ✅ Payment succeeds |
| Recurring payment with deleted PaymentMethod | ❌ Payment fails immediately | ✅ Searches for customer's valid PaymentMethod → Payment succeeds |
| Recurring payment with no valid PaymentMethod | ❌ Generic error | ✅ Clear error, subscription → PAYMENT_FAILED after 3 failures |

---

## Test Scenarios

### Scenario 1: New User Registration
**Steps:**
1. User registers billing key for the first time

**Expected:**
- ✅ PaymentMethod created
- ✅ Subscription created (ACTIVE)
- ✅ First payment executed

### Scenario 2: Existing User Registers New Billing Key
**Steps:**
1. User already has ACTIVE subscription
2. User registers new billing key (different card)

**Expected:**
- ✅ New PaymentMethod created
- ✅ NO duplicate subscription
- ✅ NO immediate payment
- ✅ Existing subscription remains ACTIVE

### Scenario 3: Recurring Payment with Valid PaymentMethod
**Steps:**
1. Subscription's next billing date arrives
2. Subscription references valid PaymentMethod

**Expected:**
- ✅ Payment succeeds using referenced PaymentMethod

### Scenario 4: Recurring Payment with Deleted PaymentMethod
**Steps:**
1. Subscription references deleted PaymentMethod (isDeleted=true)
2. Customer has registered new valid PaymentMethod
3. Recurring payment scheduled

**Expected:**
- ✅ System detects invalid PaymentMethod
- ✅ Searches for customer's valid primary PaymentMethod
- ✅ Payment succeeds using new PaymentMethod

### Scenario 5: Recurring Payment with No Valid PaymentMethod
**Steps:**
1. Subscription references deleted PaymentMethod
2. Customer has NO other valid PaymentMethod
3. Recurring payment scheduled

**Expected:**
- ❌ Payment fails
- ✅ Clear error logged
- ✅ Subscription status → PAYMENT_FAILED after 3 consecutive failures

---

## Database Impact

### No Schema Changes Required
- Uses existing `Subscription.paymentMethod` foreign key
- Uses existing `PaymentMethod.isDeleted` and `isActive` flags
- Uses existing `SubscriptionRepository.findByCustomer_IdAndStatus()` query
- Uses existing `PaymentMethodRepository.findByCustomerAndIsPrimaryTrueAndIsDeletedFalse()` query

---

## Logging Improvements

### Fix #1: Billing Key Registration
```log
// When existing ACTIVE subscription found:
INFO - 기존 활성 구독 존재 - 결제수단만 등록: customerId={}, subscriptionId={}, paymentMethodId={}

// When no ACTIVE subscription:
INFO - 신규 구독 생성 및 첫 결제 완료: customerId={}, subscriptionId={}, status={}
```

### Fix #2: Recurring Payment
```log
// When invalid PaymentMethod detected:
WARN - 구독의 결제수단이 유효하지 않음. 고객의 다른 결제수단 검색: subscriptionId={}, customerId={}

// When valid PaymentMethod found:
INFO - 유효한 결제수단 발견: customerId={}, paymentMethodId={}

// When no valid PaymentMethod:
ERROR - 유효한 결제수단 없음: customerId={}, subscriptionId={}

// When billing key missing:
ERROR - 결제수단에 빌링키 없음: paymentMethodId={}
```

---

## Compilation Status

✅ **Code compiles successfully** (verified via `./gradlew compileJava`)

---

## Related Files Modified

1. **`/src/main/java/com/tradingpt/tpt_api/domain/paymentmethod/service/command/PaymentMethodCommandServiceImpl.java`**
   - Added `SubscriptionRepository` dependency
   - Modified `completeBillingKeyRegistration()` method (lines 148-179)
   - Added conditional subscription creation logic

2. **`/src/main/java/com/tradingpt/tpt_api/domain/subscription/service/RecurringPaymentService.java`**
   - Added `PaymentMethodRepository` dependency
   - Modified `handleRegularPayment()` method (lines 182-219)
   - Added dynamic PaymentMethod resolution logic

---

## Recommended Next Steps

### 1. Integration Testing
Test the complete flow with real scenarios:
```bash
# Test 1: New user registration
POST /api/v1/payment-methods/billing-key/complete

# Test 2: Existing user re-registration
# (After deleting old billing key and having ACTIVE subscription)
POST /api/v1/payment-methods/billing-key/complete

# Test 3: Recurring payment trigger
# Execute scheduled job or manually trigger:
# RecurringPaymentService.processRecurringPayments()
```

### 2. Database Verification
```sql
-- Check subscription count per customer
SELECT customer_id, COUNT(*) as subscription_count
FROM subscriptions
WHERE status = 'ACTIVE'
GROUP BY customer_id
HAVING COUNT(*) > 1;
-- Expected: No results (no duplicate ACTIVE subscriptions)

-- Check PaymentMethod associations
SELECT c.id as customer_id,
       pm.id as payment_method_id,
       pm.is_deleted,
       s.id as subscription_id,
       s.status
FROM customers c
LEFT JOIN payment_methods pm ON c.id = pm.customer_id
LEFT JOIN subscriptions s ON c.id = s.customer_id
WHERE s.status = 'ACTIVE'
ORDER BY c.id;
```

### 3. Monitoring
Add application metrics for:
- Duplicate subscription prevention (count of billing key registrations with existing ACTIVE subscriptions)
- Dynamic PaymentMethod resolution success rate
- PaymentMethod validation failures during recurring payments

### 4. Documentation Updates
- Update API documentation for billing key registration endpoint
- Document recurring payment failure handling workflow
- Add troubleshooting guide for PaymentMethod resolution errors

---

## Risk Assessment

### Low Risk Changes ✅
- Pure business logic modifications
- No schema changes
- Uses existing repository queries
- Backward compatible

### Testing Requirements
- ✅ Unit tests for `PaymentMethodCommandServiceImpl.completeBillingKeyRegistration()`
- ✅ Unit tests for `RecurringPaymentService.handleRegularPayment()`
- ✅ Integration tests for complete billing key registration flow
- ✅ Integration tests for recurring payment with deleted PaymentMethod

---

## Rollback Plan

If issues arise, revert commits for:
1. `PaymentMethodCommandServiceImpl.java` (remove conditional logic, restore unconditional subscription creation)
2. `RecurringPaymentService.java` (remove dynamic PaymentMethod resolution)

No database rollback required (no schema changes).
