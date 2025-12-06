package dao;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.Assert.*;

/**
 * DBConnectionTest
 * ----------------
 * JUnit 4 tests for the {@link dao.DBConnection} utility class.
 *
 * These tests act as a small "smoke test" to verify that:
 * <ul>
 *     <li>A JDBC connection can be created successfully;</li>
 *     <li>The returned {@link Connection} is open and usable;</li>
 *     <li>The database metadata can be queried (at least one table exists).</li>
 * </ul>
 *
 * This is intentionally lightweight so it runs quickly as part of the normal
 * test suite, without modifying any database state.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class DBConnectionTest {

    /**
     * Verifies that {@link DBConnection#getConnection()} returns a non-null,
     * open {@link Connection} object.
     */
    @Test
    public void testGetConnection_NotNullAndOpen() throws Exception {
        Connection conn = DBConnection.getConnection();
        assertNotNull("Connection should not be null", conn);
        assertFalse("Newly acquired connection should not be closed", conn.isClosed());

        conn.close();
    }

    /**
     * Verifies that two independent connections can be obtained and both are
     * open. We do not assume they are the same physical connection, only that
     * both are valid.
     */
    @Test
    public void testGetConnection_CalledTwiceBothValid() throws Exception {
        Connection c1 = DBConnection.getConnection();
        Connection c2 = DBConnection.getConnection();

        assertNotNull("First connection should not be null", c1);
        assertNotNull("Second connection should not be null", c2);
        assertFalse("First connection should be open", c1.isClosed());
        assertFalse("Second connection should be open", c2.isClosed());

        c1.close();
        c2.close();
    }

    /**
     * Uses {@link DatabaseMetaData} as a light check that the schema is
     * reachable. We just assert that at least one table is present.
     */
    @Test
    public void testDatabaseMetadataAtLeastOneTableExists() throws Exception {
        Connection conn = DBConnection.getConnection();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            assertNotNull("DatabaseMetaData should not be null", meta);

            ResultSet rs = meta.getTables(null, null, "%", null);
            boolean hasAnyTable = rs.next();
            rs.close();

            assertTrue("Expected at least one table in the schema", hasAnyTable);
        } finally {
            conn.close();
        }
    }
}


