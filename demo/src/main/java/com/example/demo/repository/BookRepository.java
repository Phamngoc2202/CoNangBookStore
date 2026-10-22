package com.example.demo.repository;


import com.example.demo.model.Book;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// import static org.mockito.Mockito.description;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbc;

    public BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String COLUMNS =
        "BookID, ISBN, BookName, Author, Publisher, CategoryID, " +
        "Price, StockQuantity, ImageURL, Description, Status, CreatedAt";

    private final org.springframework.jdbc.core.RowMapper<Book> mapper =
        (rs, rowNum) -> {
            Timestamp timestamp = rs.getTimestamp("CreatedAt");

            return new Book(
                rs.getInt("BookID"),
                rs.getString("ISBN"),
                rs.getString("BookName"),
                rs.getString("Author"),
                rs.getString("Publisher"),
                rs.getInt("CategoryID"),
                rs.getBigDecimal("Price"),
                rs.getInt("StockQuantity"),
                rs.getString("ImageURL"),
                rs.getString("Description"),
                rs.getString("Status"),
                timestamp == null ? null : timestamp.toLocalDateTime()
            );
        };

    public List<Book> findAll(
        String keyword,
        String isbn,
        Integer categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minStock,
        Integer maxStock,
        String status
    ) {
        StringBuilder sql = new StringBuilder(
            "SELECT " + COLUMNS + " FROM Books WHERE 1 = 1"
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(
                " AND (ISBN LIKE ? OR BookName LIKE ? " +
                "OR Author LIKE ? OR Publisher LIKE ?)"
            );

            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (isbn != null && !isbn.isBlank()) {
            sql.append(" AND ISBN = ?");
            params.add(isbn.trim());
        }

        if (categoryId != null) {
            sql.append(" AND CategoryID = ?");
            params.add(categoryId);
        }

        if (minPrice != null) {
            sql.append(" AND Price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND Price <= ?");
            params.add(maxPrice);
        }

        if (minStock != null) {
            sql.append(" AND StockQuantity >= ?");
            params.add(minStock);
        }

        if (maxStock != null) {
            sql.append(" AND StockQuantity <= ?");
            params.add(maxStock);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND Status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY BookName");

        return jdbc.query(sql.toString(), mapper, params.toArray());
    }

    public Optional<Book> findById(int id) {
        List<Book> result = jdbc.query(
            "SELECT " + COLUMNS + " FROM Books WHERE BookID = ?",
            mapper,
            id
        );

        return result.stream().findFirst();
    }

    public boolean existsByIsbn(String isbn, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM Books WHERE ISBN = ?";

        Integer count;

        if (excludeId != null) {
            sql += " AND BookID <> ?";
            count = jdbc.queryForObject(
                sql, Integer.class, isbn, excludeId
            );
        } else {
            count = jdbc.queryForObject(
                sql, Integer.class, isbn
            );
        }

        return count != null && count > 0;
    }

    public int insertAndGetId(
        String isbn,
        String bookName,
        String author,
        String publisher,
        int categoryId,
        BigDecimal price,
        int stockQuantity,
        String imageURL,
        String description,
        String status
    ) {
        return jdbc.execute((ConnectionCallback<Integer>)connection -> {
            String sql =
                "INSERT INTO Books " +
                "(ISBN, BookName, Author, Publisher, CategoryID, Price, " +
                "StockQuantity, ImageURL, Description, Status, CreatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {
                ps.setString(1, isbn);
                ps.setString(2, bookName);
                ps.setString(3, author);
                ps.setString(4, publisher);
                ps.setInt(5, categoryId);
                ps.setBigDecimal(6, price);
                ps.setInt(7, stockQuantity);
                ps.setString(8, imageURL);
                ps.setString(9, description);
                ps.setString(10, status);
                ps.setTimestamp(
                    11,
                    Timestamp.valueOf(LocalDateTime.now().withNano(0))
                );

                ps.executeUpdate();
            }

            try (Statement statement = connection.createStatement();
                 ResultSet rs =
                     statement.executeQuery("SELECT @@IDENTITY")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new java.sql.SQLException(
                "Không lấy được BookID vừa tạo"
            );
        });
    }

    public int update(
        int id,
        String isbn,
        String bookName,
        String author,
        String publisher,
        int categoryId,
        BigDecimal price,
        int stockQuantity,
        String imageURL,
        String description,
        String status
    ) {
        return jdbc.update(
            "UPDATE Books SET ISBN = ?, BookName = ?, Author = ?, " +
            "Publisher = ?, CategoryID = ?, Price = ?, StockQuantity = ?, " +
            "ImageURL = ?, Description = ?, Status = ? WHERE BookID = ?",
            isbn,
            bookName,
            author,
            publisher,
            categoryId,
            price,
            stockQuantity,
            imageURL,
            description,
            status,
            id
        );
    }

    public int delete(int id) {
        return jdbc.update(
            "DELETE FROM Books WHERE BookID = ?", id
        );
    }
}
// import com.example.demo.model.Book;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.jdbc.core.RowMapper;
// import org.springframework.jdbc.support.GeneratedKeyHolder;
// import org.springframework.jdbc.support.KeyHolder;
// import org.springframework.stereotype.Repository;

// import java.sql.PreparedStatement;
// import java.sql.Statement;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public class BookRepository {

//     private final JdbcTemplate jdbcTemplate;

//     public BookRepository(JdbcTemplate jdbcTemplate) {
//         this.jdbcTemplate = jdbcTemplate;
//     }

//     private final RowMapper<Book> bookRowMapper = (rs, rowNum) ->
//         new Book(
//             rs.getInt("bookid"),
//             rs.getString("bookname"),
//             rs.getBigDecimal("price"),
//             rs.getString("author"),
//             rs.getString("publisher"),
//             rs.getString("summary"),
//             rs.getBoolean("status")
//         );

//     // 1. Lấy tất cả sách
//     public List<Book> findAll() {
//         String sql = """
//             SELECT [bookid], [bookname], [price],
//                    [author], [publisher], [summary], [status]
//             FROM [Books]
//             ORDER BY [bookid]
//             """;

//         return jdbcTemplate.query(sql, bookRowMapper);
//     }

//     // 2. Lấy sách theo ID
//     public Optional<Book> findById(Integer id) {
//         String sql = """
//             SELECT [bookid], [bookname], [price],
//                    [author], [publisher], [summary], [status]
//             FROM [Books]
//             WHERE [bookid] = ?
//             """;

//         List<Book> books = jdbcTemplate.query(
//             sql, bookRowMapper, id
//         );

//         return books.stream().findFirst();
//     }

//     // 3. Thêm sách
//     public int save(Book book) {
//         String sql = """
//             INSERT INTO [Books]
//                 ([bookname], [price], [author],
//                  [publisher], [summary], [status])
//             VALUES (?, ?, ?, ?, ?, ?)
//             """;

//         KeyHolder keyHolder = new GeneratedKeyHolder();

//         jdbcTemplate.update(connection -> {
//             PreparedStatement ps = connection.prepareStatement(
//                 sql, Statement.RETURN_GENERATED_KEYS
//             );

//             ps.setString(1, book.getBookname());
//             ps.setBigDecimal(2, book.getPrice());
//             ps.setString(3, book.getAuthor());
//             ps.setString(4, book.getPublisher());
//             ps.setString(5, book.getSummary());
//             ps.setBoolean(6, book.isStatus());

//             return ps;
//         }, keyHolder);

//         Number key = keyHolder.getKey();

//         if (key == null) {
//             throw new IllegalStateException(
//                 "Không lấy được bookid sau khi thêm sách."
//             );
//         }

//         return key.intValue();
//     }

//     // 4. Cập nhật sách
//     public int update(Integer id, Book book) {
//         String sql = """
//             UPDATE [Books]
//             SET [bookname] = ?,
//                 [price] = ?,
//                 [author] = ?,
//                 [publisher] = ?,
//                 [summary] = ?,
//                 [status] = ?
//             WHERE [bookid] = ?
//             """;

//         return jdbcTemplate.update(
//             sql,
//             book.getBookname(),
//             book.getPrice(),
//             book.getAuthor(),
//             book.getPublisher(),
//             book.getSummary(),
//             book.isStatus(),
//             id
//         );
//     }

//     // 5. Xóa sách
//     public int deleteById(Integer id) {
//         String sql = """
//             DELETE FROM [Books]
//             WHERE [bookid] = ?
//             """;

//         return jdbcTemplate.update(sql, id);
//     }
// }