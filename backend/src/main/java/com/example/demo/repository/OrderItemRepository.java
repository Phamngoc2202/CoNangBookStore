
// package com.example.demo.repository;

// import com.example.demo.model.OrderItem;

// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.jdbc.core.RowMapper;
// import org.springframework.stereotype.Repository;

// import java.util.List;
// import java.util.Optional;

// @Repository
// public class OrderItemRepository {

//     private final JdbcTemplate jdbcTemplate;

//     public OrderItemRepository(JdbcTemplate jdbcTemplate) {
//         this.jdbcTemplate = jdbcTemplate;
//     }

//     private final RowMapper<OrderItem> mapper = (rs, rowNum) ->
//         new OrderItem(
//             rs.getInt("orderid"),
//             rs.getInt("bookid"),
//             rs.getInt("quantity"),
//             rs.getBigDecimal("price")
//         );

//     public List<OrderItem> findAll() {
//         String sql = """
//             SELECT [orderid], [bookid], [quantity], [price]
//             FROM [OrderItems]
//             ORDER BY [orderid], [bookid]
//             """;

//         return jdbcTemplate.query(sql, mapper);
//     }

//     public List<OrderItem> findByOrderId(Integer orderid) {
//         String sql = """
//             SELECT [orderid], [bookid], [quantity], [price]
//             FROM [OrderItems]
//             WHERE [orderid] = ?
//             """;

//         return jdbcTemplate.query(sql, mapper, orderid);
//     }

//     public Optional<OrderItem> findById(
//             Integer orderid, Integer bookid) {

//         String sql = """
//             SELECT [orderid], [bookid], [quantity], [price]
//             FROM [OrderItems]
//             WHERE [orderid] = ? AND [bookid] = ?
//             """;

//         return jdbcTemplate.query(
//             sql, mapper, orderid, bookid
//         ).stream().findFirst();
//     }

//     public int save(OrderItem item) {
//         String sql = """
//             INSERT INTO [OrderItems]
//                 ([orderid], [bookid], [quantity], [price])
//             VALUES (?, ?, ?, ?)
//             """;

//         return jdbcTemplate.update(
//             sql,
//             item.getOrderid(),
//             item.getBookid(),
//             item.getQuantity(),
//             item.getPrice()
//         );
//     }

//     public int update(OrderItem item) {
//         String sql = """
//             UPDATE [OrderItems]
//             SET [quantity] = ?, [price] = ?
//             WHERE [orderid] = ? AND [bookid] = ?
//             """;

//         return jdbcTemplate.update(
//             sql,
//             item.getQuantity(),
//             item.getPrice(),
//             item.getOrderid(),
//             item.getBookid()
//         );
//     }

//     public int delete(Integer orderid, Integer bookid) {
//         String sql = """
//             DELETE FROM [OrderItems]
//             WHERE [orderid] = ? AND [bookid] = ?
//             """;

//         return jdbcTemplate.update(sql, orderid, bookid);
//     }

//     public int deleteByOrderId(Integer orderid) {
//         return jdbcTemplate.update(
//             "DELETE FROM [OrderItems] WHERE [orderid] = ?",
//             orderid
//         );
//     }
// }