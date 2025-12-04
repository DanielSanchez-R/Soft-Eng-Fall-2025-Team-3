package util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginAttemptTracker is a utility class that monitors and controls failed login attempts
 * on a per-user basis to mitigate brute-force or credential stuffing attacks.
 * <p>
 * Features:
 * <ul>
 *   <li>Tracks failed login attempts per email address.</li>
 *   <li>Locks accounts for a fixed duration after a threshold is exceeded.</li>
 *   <li>Automatically resets attempts after the time window expires or upon successful login.</li>
 *   <li>Thread-safe implementation using synchronized maps.</li>
 * </ul>
 *
 * <p><b>Lockout Policy:</b></p>
 * <ul>
 *   <li>5 failed attempts within 60 seconds → 2-minute lockout.</li>
 *   <li>Accounts automatically unlock after lockout duration expires.</li>
 *   <li>Admin account admin@restaurant.com is excluded from rate limiting.</li>
 * </ul>
 *
 * <p>This utility can be integrated into authentication logic (e.g., AuthController)
 * to enforce security and prevent excessive login attempts from a single user.</p>
 *
 * <p><b>Thread-Safety:</b> All maps are wrapped with
 * {@link java.util.Collections#synchronizedMap(Map)} and access is further synchronized
 * to ensure consistency under concurrent access.</p>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class LoginAttemptTracker {

    /** Tracks failed login attempts per user email. */
    private static final Map<String, Integer> attemptCount =
            Collections.synchronizedMap(new HashMap<>());

    /** Records the timestamp of the first failed attempt within the current time window. */
    private static final Map<String, Long> firstAttemptTime =
            Collections.synchronizedMap(new HashMap<>());

    /** Stores lockout expiration timestamps for temporarily locked accounts. */
    private static final Map<String, Long> lockedUntil =
            Collections.synchronizedMap(new HashMap<>());

    /** Maximum allowed failed attempts within the time window before lockout. */
    private static final int MAX_ATTEMPTS = 5;

    /** Duration of the attempt tracking window in milliseconds (default: 60 seconds). */
    private static final long ATTEMPT_WINDOW_MS = 60 * 1000;

    /** Duration of the lockout period in milliseconds (default: 2 minutes). */
    private static final long LOCKOUT_DURATION_MS = 2 * 60 * 1000;

    /**
     * Determines whether the specified email address is currently locked out.
     * If the lockout period has expired, the account is automatically unlocked and
     * all associated tracking data is cleared.
     *
     * @param email the email address to check
     * @return {@code true} if the account is locked; {@code false} otherwise
     */
    public static boolean isLocked(String email) {
        synchronized (lockedUntil) {
            if (!lockedUntil.containsKey(email)) return false;

            long unlockTime = lockedUntil.get(email);
            if (System.currentTimeMillis() > unlockTime) {
                // Lock expired — remove tracking data
                lockedUntil.remove(email);
                attemptCount.remove(email);
                firstAttemptTime.remove(email);
                System.out.println("Lock expired for: " + email);
                return false;
            }
            return true;
        }
    }

    /**
     * Retrieves the remaining lockout duration (in milliseconds) for a given email address.
     *
     * @param email the email address to query
     * @return remaining lock time in milliseconds, or {@code 0} if not locked
     */
    public static long getRemainingLockTime(String email) {
        synchronized (lockedUntil) {
            if (!lockedUntil.containsKey(email)) return 0;
            return Math.max(0, lockedUntil.get(email) - System.currentTimeMillis());
        }
    }

    /**
     * Records a failed login attempt for the specified email.
     * This method automatically manages:
     * Time window reset after 60 seconds.
     * Incrementing attempt counts.
     *  Account lockout when thresholds are exceeded.
     * Admin email (<code>admin@restaurant.com</code>) is exempt from tracking.
     *
     * @param email the email address that failed authentication
     */
    public static void recordFailedAttempt(String email) {
        if (email == null || email.trim().isEmpty()) return;
        if (email.equalsIgnoreCase("admin@restaurant.com")) {
            System.out.println("Admin login attempt ignored for rate limit.");
            return;
        }

        long now = System.currentTimeMillis();

        synchronized (attemptCount) {
            // Reset window if older than 60 seconds
            if (!firstAttemptTime.containsKey(email) ||
                    now - firstAttemptTime.get(email) > ATTEMPT_WINDOW_MS) {

                firstAttemptTime.put(email, now);
                attemptCount.put(email, 1);
                return;
            }

            // Increment attempt count
            int count = attemptCount.getOrDefault(email, 0) + 1;
            attemptCount.put(email, count);

            // Exceeded max attempts → lock account
            if (count >= MAX_ATTEMPTS) {
                lockedUntil.put(email, now + LOCKOUT_DURATION_MS);
                logLockEvent(email);
            }
        }
    }

    /**
     * Resets all tracking data (attempt count, timestamps, lock status)
     * for the specified email after a successful login.
     *
     * @param email the email address whose data should be reset
     */
    public static void resetAttempts(String email) {
        if (email == null) return;
        attemptCount.remove(email);
        firstAttemptTime.remove(email);
        lockedUntil.remove(email);
        System.out.println("Reset attempts for: " + email);
    }

    /**
     * Logs a lockout event for audit or future admin dashboard integration.
     * Currently, this method outputs to the console but may later be extended
     * to persist data to a database or monitoring system.
     *
     * @param email the locked email address
     */
    private static void logLockEvent(String email) {
        long until = lockedUntil.get(email);
        long minutes = (LOCKOUT_DURATION_MS / 1000) / 60;
        System.out.println("Account locked: " + email +
                " | Locked until: " + new java.util.Date(until) +
                " | Duration: " + minutes + " min");
    }
}
