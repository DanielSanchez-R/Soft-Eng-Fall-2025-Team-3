package util;

public class SMSUtil {

    /**
     * Sends an SMS message (stub for demo; Twilio or other gateway can be used).
     * @param phone The recipient's phone number (E.164 format recommended)
     * @param message The SMS text body
     */
    public static void sendSMS(String phone, String message) {
        if (phone == null || phone.isEmpty()) {
            System.out.println(" SMSUtil: No phone provided, skipping SMS.");
            return;
        }
        // Replace this with Twilio integration if desired
        System.out.println("📱 SMS to " + phone + ": " + message);
    }
}
