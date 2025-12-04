package dao;

import org.junit.Assert;
import org.junit.Test;
import java.sql.Connection;

/**
 * DBConnectionTest.java
 * =====================
 * Unit test class for verifying the functionality of the {@link dao.DBConnection} utility
 * within the Pizza 505 ENMU project.
 * <p>
 * This test ensures that the application can successfully establish a valid connection
 * to the embedded H2 database through the {@code DBConnection.getConnection()} method.
 * It is part of the overall JUnit 4 test suite defined in {@link AllTests}.
 * </p>
 *
 * @author Daniel Sanchez
 * @version 3.0
 * @since 2025-11
 */
public class DBConnectionTest {

    /**
     * Verifies that a valid connection to the H2 database can be established.
     * <p>
     * Ensures {@link dao.DBConnection#getConnection()} returns a non-null {@link Connection}
     * object, confirming that the database driver and file path are configured correctly.
     * </p>
     */
    @Test
    public void testConnectionNotNull() {
        Connection conn = DBConnection.getConnection();
        Assert.assertNotNull("Connection should not be null", conn);
    }
}

