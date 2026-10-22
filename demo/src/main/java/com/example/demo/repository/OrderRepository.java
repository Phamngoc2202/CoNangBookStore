package com.example.demo.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.OrderStatusHistory;

@Repository
public class OrderRepository {

    public static final String STOCK_RESERVED_REASON = "Tạo đơn hàng - đã trừ kho";

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
public Integer findUserIdByUsername(String username) {
    if (username == null || username.isBlank()) {
        throw new IllegalArgumentException(
            "Không xác định được tài khoản đăng nhập"
        );
    }

    // JWT có thể trả về UserID = "3"
    try {
        Integer userId = Integer.valueOf(username);

        List<Integer> ids = jdbc.query(
            "SELECT UserID FROM Users WHERE UserID = ?",
            (rs, rowNum) -> rs.getInt("UserID"),
            userId
        );

        if (!ids.isEmpty()) {
            return ids.get(0);
        }
    } catch (NumberFormatException ignored) {
        // Nếu không phải ID thì tìm theo username
    }

    List<Integer> ids = jdbc.query(
        "SELECT UserID FROM Users WHERE Username = ?",
        (rs, rowNum) -> rs.getInt("UserID"),
        username
    );

    if (ids.isEmpty()) {
        throw new IllegalArgumentException(
            "Không tìm thấy tài khoản theo ID hoặc Username: "
                + username
        );
    }

    return ids.get(0);
}
    // Lấy UserID từ username trong JWT
    public Integer findUserIdByUsername1(String username) {
        List<Integer> ids = jdbc.query(
            "SELECT UserID FROM Users WHERE Username = ? or UserID = ?",
            (rs, rowNum) -> rs.getInt("UserID"),
            username, username
        );

        if (ids.isEmpty()) {
            throw new IllegalArgumentException(
                "Không tìm thấy tài khoản: " + username
            );
        }
        return ids.get(0);
    }

    // Lấy ID sách, giá và tồn kho
    public Optional<BookInfo> findBook(Integer bookId) {
        List<BookInfo> result = jdbc.query(
            """
            SELECT BookID, BookName, Price, StockQuantity, Status
            FROM Books
            WHERE BookID = ?
            """,
            (rs, rowNum) -> new BookInfo(
                rs.getInt("BookID"),
                rs.getString("BookName"),
                rs.getBigDecimal("Price"),
                rs.getInt("StockQuantity"),
                rs.getString("Status")
            ),
            bookId
        );

        return result.stream().findFirst();
    }

    public record BookInfo(
        Integer bookId,
        String bookName,
        BigDecimal price,
        Integer stockQuantity,
        String status
    ) {}

    // Tạo đơn hàng và trả về OrderID
    public Integer insertOrder(
            Integer userId,
            BigDecimal totalAmount,
            String address,
            String phone,
            String note) {

        return jdbc.execute(
            (ConnectionCallback<Integer>) connection -> {
                String sql = """
                    INSERT INTO Orders
                    (UserID, OrderDate, TotalAmount,
                     ShippingAddress, Phone, Status, Note)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

                try (PreparedStatement ps =
                         connection.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.setTimestamp(2, Timestamp.valueOf(
                        java.time.LocalDateTime.now()
                    ));
                    ps.setBigDecimal(3, totalAmount);
                    ps.setString(4, address);
                    ps.setString(5, phone);
                    ps.setString(6, "PENDING");
                    ps.setString(7, note);
                    ps.executeUpdate();
                }

                try (Statement st = connection.createStatement();
                     ResultSet rs =
                         st.executeQuery("SELECT @@IDENTITY")) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }

                throw new IllegalStateException(
                    "Không lấy được OrderID"
                );
            }
        );
    }

    public void insertOrderItem(
            Integer orderId,
            Integer bookId,
            Integer quantity,
            BigDecimal unitPrice) {

        jdbc.update(
            """
            INSERT INTO OrderItems
            (OrderID, BookID, Quantity, UnitPrice, Subtotal)
            VALUES (?, ?, ?, ?, ?)
            """,
            orderId,
            bookId,
            quantity,
            unitPrice,
            unitPrice.multiply(BigDecimal.valueOf(quantity))
        );
    }

    // Trừ kho có điều kiện để tránh tồn kho âm
    public int decreaseStock(Integer bookId, Integer quantity) {
        return jdbc.update(
            """
            UPDATE Books
            SET StockQuantity = StockQuantity - ?
            WHERE BookID = ?
              AND StockQuantity >= ?
              AND Status = ?
            """,
            quantity, bookId, quantity, "ACTIVE"
        );
    }

    public int increaseStock(Integer bookId, Integer quantity) {
        return jdbc.update(
            """
            UPDATE Books
            SET StockQuantity = StockQuantity + ?
            WHERE BookID = ?
            """,
            quantity, bookId
        );
    }

    public Optional<Order> findOrder(Integer orderId) {
        List<Order> result = jdbc.query(
            """
            SELECT OrderID, UserID, OrderDate, TotalAmount,
                   ShippingAddress, Phone, Status, ApprovedBy,
                   ApprovedAt, Note
            FROM Orders
            WHERE OrderID = ?
            """,
            (rs, rowNum) -> new Order(
                rs.getInt("OrderID"),
                rs.getInt("UserID"),
                rs.getTimestamp("OrderDate").toLocalDateTime(),
                rs.getBigDecimal("TotalAmount"),
                rs.getString("ShippingAddress"),
                rs.getString("Phone"),
                rs.getString("Status"),
                nullableInt(rs, "ApprovedBy"),
                nullableDateTime(rs, "ApprovedAt"),
                rs.getString("Note")
            ),
            orderId
        );

        return result.stream().findFirst();
    }

    public List<Order> findOrdersByUser(Integer userId) {
        return jdbc.query(
            """
            SELECT OrderID, UserID, OrderDate, TotalAmount,
                   ShippingAddress, Phone, Status, ApprovedBy,
                   ApprovedAt, Note
            FROM Orders
            WHERE UserID = ?
            ORDER BY OrderDate DESC
            """,
            (rs, rowNum) -> new Order(
                rs.getInt("OrderID"),
                rs.getInt("UserID"),
                rs.getTimestamp("OrderDate").toLocalDateTime(),
                rs.getBigDecimal("TotalAmount"),
                rs.getString("ShippingAddress"),
                rs.getString("Phone"),
                rs.getString("Status"),
                nullableInt(rs, "ApprovedBy"),
                nullableDateTime(rs, "ApprovedAt"),
                rs.getString("Note")
            ),
            userId
        );
    }

    public List<OrderItem> findItems(Integer orderId) {
        return jdbc.query(
            """
            SELECT OrderItemID, OrderID, BookID,
                   Quantity, UnitPrice, Subtotal
            FROM OrderItems
            WHERE OrderID = ?
            """,
            (rs, rowNum) -> new OrderItem(
                rs.getInt("OrderItemID"),
                rs.getInt("OrderID"),
                rs.getInt("BookID"),
                rs.getInt("Quantity"),
                rs.getBigDecimal("UnitPrice"),
                rs.getBigDecimal("Subtotal")
            ),
            orderId
        );
    }

    public int updateStatus(
            Integer orderId,
            String expectedStatus,
            String status,
            Integer approvedBy,
            boolean setApproval) {

        if (setApproval) {
            return jdbc.update(
                """
                UPDATE Orders
                SET Status = ?, ApprovedBy = ?, ApprovedAt = ?
                WHERE OrderID = ? AND Status = ?
                """,
                status, approvedBy,
                Timestamp.valueOf(
                    java.time.LocalDateTime.now()
                ),
                orderId, expectedStatus
            );
        } else {
            return jdbc.update(
                "UPDATE Orders SET Status = ? WHERE OrderID = ? AND Status = ?",
                status, orderId, expectedStatus
            );
        }
    }

    public void insertHistory(
            Integer orderId,
            String oldStatus,
            String newStatus,
            Integer changedBy,
            String reason) {

        jdbc.update(
            """
            INSERT INTO OrderStatusHistory
            (OrderID, OldStatus, NewStatus, ChangedBy,
             ChangedAt, Reason)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            orderId,
            oldStatus,
            newStatus,
            changedBy,
            Timestamp.valueOf(
                java.time.LocalDateTime.now()
            ),
            reason
        );
    }

    // Orders created before immediate stock reservation have a different
    // PENDING history reason, so they can still follow the old approval path.
    public boolean wasStockReservedAtCreation(Integer orderId) {
        List<String> reasons = jdbc.query(
            """
            SELECT Reason FROM OrderStatusHistory
            WHERE OrderID = ? AND NewStatus = ?
            """,
            (rs, rowNum) -> rs.getString("Reason"),
            orderId, "PENDING"
        );
        return reasons.contains(STOCK_RESERVED_REASON);
    }

    public List<OrderStatusHistory> findHistory(Integer orderId) {
        return jdbc.query(
            """
            SELECT HistoryID, OrderID, OldStatus, NewStatus,
                   ChangedBy, ChangedAt, Reason
            FROM OrderStatusHistory
            WHERE OrderID = ?
            ORDER BY ChangedAt ASC
            """,
            (rs, rowNum) -> new OrderStatusHistory(
                rs.getInt("HistoryID"),
                rs.getInt("OrderID"),
                rs.getString("OldStatus"),
                rs.getString("NewStatus"),
                rs.getInt("ChangedBy"),
                rs.getTimestamp("ChangedAt").toLocalDateTime(),
                rs.getString("Reason")
            ),
            orderId
        );
    }

    private static Integer nullableInt(
            ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static java.time.LocalDateTime nullableDateTime(
            ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
// package com.example.demo.repository;

// import com.example.demo.model.Order;

// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.jdbc.core.RowMapper;
// import org.springframework.jdbc.support.GeneratedKeyHolder;
// import org.springframework.jdbc.support.KeyHolder;
// import org.springframework.stereotype.Repository;

// import java.sql.PreparedStatement;
// import java.sql.Statement;
// import java.sql.Timestamp;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public class OrderRepository {

//     private final JdbcTemplate jdbcTemplate;

//     public OrderRepository(JdbcTemplate jdbcTemplate) {
//         this.jdbcTemplate = jdbcTemplate;
//     }

//     private final RowMapper<Order> mapper = (rs, rowNum) -> {
//         Timestamp timestamp = rs.getTimestamp("date");

//         return new Order(
//             rs.getInt("id"),
//             rs.getString("username"),
//             timestamp == null ? null : timestamp.toLocalDateTime(),
//             rs.getString("status")
//         );
//     };

//     public List<Order> findAll() {
//         String sql = """
//             SELECT [id], [username], [date], [status]
//             FROM [Orders]
//             ORDER BY [id] DESC
//             """;

//         return jdbcTemplate.query(sql, mapper);
//     }

//     public Optional<Order> findById(Integer id) {
//         String sql = """
//             SELECT [id], [username], [date], [status]
//             FROM [Orders]
//             WHERE [id] = ?
//             """;

//         return jdbcTemplate.query(sql, mapper, id)
//             .stream().findFirst();
//     }

//     public List<Order> findByUsername(String username) {
//         String sql = """
//             SELECT [id], [username], [date], [status]
//             FROM [Orders]
//             WHERE [username] = ?
//             ORDER BY [date] DESC
//             """;

//         return jdbcTemplate.query(sql, mapper, username);
//     }

//     public int save(Order order) {
//         String sql = """
//             INSERT INTO [Orders]
//                 ([username], [date], [status])
//             VALUES (?, ?, ?)
//             """;

//         KeyHolder keyHolder = new GeneratedKeyHolder();

//         jdbcTemplate.update(connection -> {
//             PreparedStatement ps = connection.prepareStatement(
//                 sql, Statement.RETURN_GENERATED_KEYS
//             );

//             ps.setString(1, order.getUsername());
//             ps.setTimestamp(
//                 2, Timestamp.valueOf(order.getDate())
//             );
//             ps.setString(3, order.getStatus());

//             return ps;
//         }, keyHolder);

//         Number key = keyHolder.getKey();

//         if (key == null) {
//             throw new IllegalStateException(
//                 "Không lấy được ID của đơn hàng mới."
//             );
//         }

//         return key.intValue();
//     }

//     public int updateStatus(Integer id, String status) {
//         String sql = """
//             UPDATE [Orders]
//             SET [status] = ?
//             WHERE [id] = ?
//             """;

//         return jdbcTemplate.update(sql, status, id);
//     }

//     public int deleteById(Integer id) {
//         return jdbcTemplate.update(
//             "DELETE FROM [Orders] WHERE [id] = ?", id
//         );
//     }
// }
