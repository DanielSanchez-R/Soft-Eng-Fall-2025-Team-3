import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * ViewportTest verifies that required JSP files contain the
 * responsive <meta name="viewport"> tag for proper mobile scaling.
 *
 * <p>This ensures that each JSP targeted for testing includes
 * the exact viewport meta tag string defined in this test.</p>
 *
 * @author Daniel Sanchez
 * @version 4.0
 */
public class ViewportTest {

    /**
     * Tests that specific JSP files contain the required viewport meta tag.
     *
     * @throws Exception if file access fails during reading
     */
    @Test
    public void testJspHasViewportMetaTag() throws Exception {
        // List of JSP files to check (paths relative to project root)
        String[] jspFiles = {
                "web/orderPizza.jsp",
                "web/checkout.jsp"
        };

        // The required viewport tag that must appear in each JSP
        String viewportTag = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";

        // Check each JSP file
        for (String filePath : jspFiles) {
            Path path = Paths.get(filePath);

            // Ensure the file exists
            if (Files.exists(path)) {
                String content = new String(Files.readAllBytes(path));

                // Assert that the viewport tag is present
                assertTrue("Missing viewport meta tag in file: " + filePath,
                        content.contains(viewportTag));

            } else {
                // File missing = test should fail
                fail("File not found: " + filePath);
            }
        }
    }
}
