package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public BookService(
        BookRepository bookRepo,
        CategoryRepository categoryRepo
    ) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

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
        validateFilters(
            categoryId, minPrice, maxPrice,
            minStock, maxStock, status
        );

        return bookRepo.findAll(
            keyword, isbn, categoryId,
            minPrice, maxPrice, minStock, maxStock, status
        );
    }

    public Book findById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "BookID phải lớn hơn 0"
            );
        }

        return bookRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy sách có ID = " + id
            ));
    }

    public Book create(
        String isbn,
        String bookName,
        String author,
        String publisher,
        Integer categoryId,
        BigDecimal price,
        Integer stockQuantity,
        String imageURL,
        String description,
        String status
    ) {
        validateBook(
            isbn, bookName, author, categoryId,
            price, stockQuantity, status
        );

        String normalizedIsbn = isbn.trim();

        if (bookRepo.existsByIsbn(normalizedIsbn, null)) {
            throw new IllegalArgumentException(
                "ISBN đã tồn tại"
            );
        }

        validateCategory(categoryId);

        int id = bookRepo.insertAndGetId(
            normalizedIsbn,
            bookName.trim(),
            author.trim(),
            publisher,
            categoryId,
            price,
            stockQuantity,
            imageURL,
            description,
            normalizeStatus(status)
        );

        return findById(id);
    }

    public Book update(
        int id,
        String isbn,
        String bookName,
        String author,
        String publisher,
        Integer categoryId,
        BigDecimal price,
        Integer stockQuantity,
        String imageURL,
        String description,
        String status
    ) {
        findById(id);

        validateBook(
            isbn, bookName, author, categoryId,
            price, stockQuantity, status
        );

        String normalizedIsbn = isbn.trim();

        if (bookRepo.existsByIsbn(normalizedIsbn, id)) {
            throw new IllegalArgumentException(
                "ISBN đã được sử dụng bởi sách khác"
            );
        }

        validateCategory(categoryId);

        int affected = bookRepo.update(
            id,
            normalizedIsbn,
            bookName.trim(),
            author.trim(),
            publisher,
            categoryId,
            price,
            stockQuantity,
            imageURL,
            description,
            normalizeStatus(status)
        );

        if (affected == 0) {
            throw new IllegalArgumentException(
                "Không cập nhật được sách"
            );
        }

        return findById(id);
    }

    public void delete(int id) {
        findById(id);
        bookRepo.delete(id);
    }

    private void validateCategory(Integer categoryId) {
        categoryRepo.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Danh mục có ID = " + categoryId + " không tồn tại"
            ));
    }

    private void validateBook(
        String isbn,
        String bookName,
        String author,
        Integer categoryId,
        BigDecimal price,
        Integer stockQuantity,
        String status
    ) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException(
                "ISBN không được để trống"
            );
        }

        if (isbn.trim().length() > 20) {
            throw new IllegalArgumentException(
                "ISBN không được vượt quá 20 ký tự"
            );
        }

        if (bookName == null || bookName.isBlank()) {
            throw new IllegalArgumentException(
                "BookName không được để trống"
            );
        }

        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException(
                "Author không được để trống"
            );
        }

        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException(
                "CategoryID không hợp lệ"
            );
        }

        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException(
                "Price phải lớn hơn hoặc bằng 0"
            );
        }

        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException(
                "StockQuantity phải lớn hơn hoặc bằng 0"
            );
        }

        if (status != null && !status.isBlank()
            && !status.equals("ACTIVE")
            && !status.equals("INACTIVE")) {
            throw new IllegalArgumentException(
                "Status phải là ACTIVE hoặc INACTIVE"
            );
        }
    }

    private void validateFilters(
        Integer categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minStock,
        Integer maxStock,
        String status
    ) {
        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException(
                "CategoryID phải lớn hơn 0"
            );
        }

        if (minPrice != null && minPrice.signum() < 0
            || maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException(
                "Giá lọc không được âm"
            );
        }

        if (minPrice != null && maxPrice != null
            && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                "minPrice không được lớn hơn maxPrice"
            );
        }

        if (minStock != null && minStock < 0
            || maxStock != null && maxStock < 0) {
            throw new IllegalArgumentException(
                "Tồn kho lọc không được âm"
            );
        }

        if (minStock != null && maxStock != null
            && minStock > maxStock) {
            throw new IllegalArgumentException(
                "minStock không được lớn hơn maxStock"
            );
        }

        if (status != null && !status.isBlank()
            && !status.equals("ACTIVE")
            && !status.equals("INACTIVE")) {
            throw new IllegalArgumentException(
                "Status phải là ACTIVE hoặc INACTIVE"
            );
        }
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank()
            ? "ACTIVE"
            : status;
    }
}
// package com.example.demo.service;

// import com.example.demo.model.Book;
// import com.example.demo.repository.BookRepository;

// import org.springframework.stereotype.Service;

// import java.math.BigDecimal;
// import java.util.List;

// @Service
// public class BookService {

//     private final BookRepository bookRepository;

//     public BookService(BookRepository bookRepository) {
//         this.bookRepository = bookRepository;
//     }

//     public List<Book> findAll() {
//         return bookRepository.findAll();
//     }

//     public Book findById(Integer id) {
//         validateId(id);

//         return bookRepository.findById(id)
//             .orElseThrow(() ->
//                 new BookNotFoundException(
//                     "Không tìm thấy sách có ID = " + id
//                 )
//             );
//     }

//     public Book create(Book book) {
//         validateBook(book);

//         // bookid do database tự sinh
//         book.setBookid(null);

//         int id = bookRepository.save(book);
//         return findById(id);
//     }

//     public Book update(Integer id, Book book) {
//         validateId(id);
//         validateBook(book);

//         if (bookRepository.findById(id).isEmpty()) {
//             throw new BookNotFoundException(
//                 "Không tìm thấy sách có ID = " + id
//             );
//         }

//         bookRepository.update(id, book);
//         return findById(id);
//     }

//     public void delete(Integer id) {
//         validateId(id);

//         if (bookRepository.findById(id).isEmpty()) {
//             throw new BookNotFoundException(
//                 "Không tìm thấy sách có ID = " + id
//             );
//         }

//         bookRepository.deleteById(id);
//     }

//     private void validateId(Integer id) {
//         if (id == null || id <= 0) {
//             throw new IllegalArgumentException(
//                 "bookid phải là số nguyên dương."
//             );
//         }
//     }

//     private void validateBook(Book book) {
//         if (book == null) {
//             throw new IllegalArgumentException(
//                 "Thông tin sách không được để trống."
//             );
//         }

//         if (book.getBookname() == null
//                 || book.getBookname().isBlank()) {
//             throw new IllegalArgumentException(
//                 "Tên sách không được để trống."
//             );
//         }

//         if (book.getBookname().trim().length() < 2) {
//             throw new IllegalArgumentException(
//                 "Tên sách phải có ít nhất 2 ký tự."
//             );
//         }

//         if (book.getPrice() == null
//                 || book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
//             throw new IllegalArgumentException(
//                 "Giá sách phải lớn hơn hoặc bằng 0."
//             );
//         }

//         if (book.getAuthor() == null
//                 || book.getAuthor().isBlank()) {
//             throw new IllegalArgumentException(
//                 "Tác giả không được để trống."
//             );
//         }

//         if (book.getPublisher() == null
//                 || book.getPublisher().isBlank()) {
//             throw new IllegalArgumentException(
//                 "Nhà xuất bản không được để trống."
//             );
//         }

//         if (book.getSummary() == null
//                 || book.getSummary().isBlank()) {
//             throw new IllegalArgumentException(
//                 "Tóm tắt sách không được để trống."
//             );
//         }

//         book.setBookname(book.getBookname().trim());
//         book.setAuthor(book.getAuthor().trim());
//         book.setPublisher(book.getPublisher().trim());
//         book.setSummary(book.getSummary().trim());
//     }
// }