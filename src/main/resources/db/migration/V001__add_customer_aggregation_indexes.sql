-- 1. Level Test Attempt Index
CREATE INDEX idx_leveltest_customer_status_created
    ON level_test_attempt (customer_id, status, created_at);

-- 2. Lecture Progress Index (Total Count)
CREATE INDEX idx_lectureprogress_customer
    ON lecture_progress (customer_id);

-- 3. Lecture Progress Index (Completed Count)
CREATE INDEX idx_lectureprogress_customer_completed
    ON lecture_progress (customer_id, is_completed);

-- 4. Customer Assignment Index (Total Count)
CREATE INDEX idx_assignment_customer
    ON customer_assignment (customer_id);

-- 5. Customer Assignment Index (Unsubmitted Count)
CREATE INDEX idx_assignment_customer_submitted
    ON customer_assignment (customer_id, submitted);