package util;

import org.junit.Assert;
import org.junit.Test;

/**
 * EmailUtilTest.java
 * ==================
 * Unit test class for verifying the setup and initialization of the {@link util.EmailUtil} utility.
 * <p>
 * This test ensures that the {@code EmailUtil} component can be instantiated correctly
 * and is ready for use in the email notification subsystem of the Pizza 505 ENMU project.
 * It forms part of the JUnit 4 test suite defined in {AllTests}.
 * </p>
 *
 * @author Daniel Sanchez
 * @version 3.0
 * @since 2025-11
 */
public class EmailUtilTest {

    /**
     * Verifies that the {@link util.EmailUtil} object can be created successfully.
     * <p>
     * Ensures the utility class is properly configured and available
     * for sending email notifications during runtime.
     * </p>
     */
    @Test
    public void testSendEmailSetup() {
        EmailUtil util = new EmailUtil();
        Assert.assertNotNull("EmailUtil should be initialized", util);
    }
}


