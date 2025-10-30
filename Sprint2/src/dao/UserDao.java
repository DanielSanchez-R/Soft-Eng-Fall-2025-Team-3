package dao;

import model.User;
import java.util.List;

/**
 * UserDao defines the standard data access operations
 * for managing {@link User} entities within the restaurant
 * booking and ordering system.
 * <p>
 * It serves as the abstraction layer between the application
 * logic and the database, ensuring consistent CRUD operations
 * (Create, Read, Update, Delete) for all user types.
 * Implementations such as {@link dao.UserDaoImpl} provide the
 * actual SQL or ORM-based persistence logic.
 * </p>
 *
 * <h3>Supported User Types:</h3>
 * <ul>
 *   <li>{@link model.Admin}</li>
 *   <li>{@link model.Staff}</li>
 *   <li>{@link model.Customer}</li>
 * </ul>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Insert new user records into the database.</li>
 *   <li>Retrieve users by email for authentication or lookup.</li>
 *   <li>Update user details (including password, role, or contact info).</li>
 *   <li>Delete users by their unique identifier.</li>
 *   <li>Retrieve all users for administrative display or reporting.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * UserDao userDao = new UserDaoImpl(DBUtil.getConnection());
 * User admin = new Admin(1, "Daniel", "admin@pizzas505.com", "securePass123");
 * userDao.addUser(admin);
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public interface UserDao {

    /**
     * Adds a new {@link User} record to the database.
     *
     * @param user the {@link User} to be added
     * @return true if the operation succeeded; false otherwise
     */
    boolean addUser(User user);

    /**
     * Retrieves a {@link User} from the database using their email address.
     * <p>
     * Typically used during login to authenticate users by their credentials.
     * </p>
     *
     * @param email the email address of the user
     * @return the matching {@link User}, or null if not found
     */
    User getUserByEmail(String email);

    /**
     * Updates an existing {@link User} record in the database.
     *
     * @param user the {@link User} containing updated data
     * @return true if the update was successful; false otherwise
     */
    boolean updateUser(User user);

    /**
     * Deletes a {@link User} from the database by their unique ID.
     *
     * @param id the unique identifier of the user to delete
     * @return true if the deletion was successful; false otherwise
     */
    boolean deleteUser(int id);

    /**
     * Retrieves a list of all {@link User} records in the database.
     *
     * @return a {@link List} containing all users
     */
    List<User> getAllUsers();
}
