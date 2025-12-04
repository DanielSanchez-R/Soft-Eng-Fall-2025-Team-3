import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * AllTests.java
 * =============
 * This class serves as a central JUnit 4 test suite for the Pizza 505 ENMU project.
 * <p>
 * It aggregates and runs multiple individual test classes from the {@code dao}, {@code model},
 * and {@code util} packages. By including all major test classes here, developers can quickly
 * verify the stability of the entire system with a single run.
 * </p>
 *
 * @author Daniel
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * AllTests.java
 * =============
 * This class serves as a central JUnit 4 test suite for the Pizza 505 ENMU project.
 * <p>
 * It aggregates and runs multiple individual test classes from the {@code dao}, {@code model},
 * and {@code util} packages. By including all major test classes here, developers can quickly
 * verify the stability of the entire system with a single run.
 * </p>
 *
 * @author Daniel Sanchez
 * @version 3.0
 * @since 2025-11
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        dao.DBConnectionTest.class,
        model.MenuItemTest.class,
        util.EmailUtilTest.class
        // add more tests here if you want
})
public class AllTests {
    // JUnit will run all classes listed above
}
