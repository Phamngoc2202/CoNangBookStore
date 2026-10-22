package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    public record BookRequest(
        @NotBlank String isbn,
        @NotBlank String bookName,
        @NotBlank String author,
        String publisher,
        @NotNull Integer categoryId,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @Min(0) Integer stockQuantity,
        String imageURL,
        String description,
        @Pattern(regexp = "ACTIVE|INACTIVE")
        String status
    ) {}

    // GET: Danh sách, tìm kiếm và lọc sách
    @GetMapping
    public List<Book> findAll(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String isbn,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Integer minStock,
        @RequestParam(required = false) Integer maxStock,
        @RequestParam(required = false) String status
    ) {
        return service.findAll(
            keyword,
            isbn,
            categoryId,
            minPrice,
            maxPrice,
            minStock,
            maxStock,
            status
        );
    }

    // GET: Chi tiết sách
    @GetMapping("/{id}")
    public Book findById(@PathVariable int id) {
        return service.findById(id);
    }

    // POST: Thêm sách
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(
        @Valid @RequestBody BookRequest request
    ) {
        return service.create(
            request.isbn(),
            request.bookName(),
            request.author(),
            request.publisher(),
            request.categoryId(),
            request.price(),
            request.stockQuantity(),
            request.imageURL(),
            request.description(),
            request.status()
        );
    }

    // PUT: Cập nhật sách
    @PutMapping("/{id}")
    public Book update(
        @PathVariable int id,
        @Valid @RequestBody BookRequest request
    ) {
        return service.update(
            id,
            request.isbn(),
            request.bookName(),
            request.author(),
            request.publisher(),
            request.categoryId(),
            request.price(),
            request.stockQuantity(),
            request.imageURL(),
            request.description(),
            request.status()
        );
    }

    // DELETE: Xóa sách
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        service.delete(id);
    }
}
// package com.example.demo.controller;

// import com.example.demo.model.Book;
// import com.example.demo.service.BookService;

// import jakarta.validation.Valid;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/books")
// @CrossOrigin(origins = "*")
// public class BookController {

//     private final BookService service;

//     public BookController(BookService service) {
//         this.service = service;
//     }

//     // GET /api/books
//     @GetMapping
//     public List<Book> findAll() {
//         return service.findAll();
//     }

//     // GET /api/books/{id}
//     @GetMapping("/{id}")
//     public Book findById(@PathVariable int id) {
//         return service.findById(id);
//     }

//     // POST /api/books
//     @PostMapping
//     public ResponseEntity<Book> create(
//             @Valid @RequestBody Book book) {

//         Book created = service.create(book);

//         return ResponseEntity
//             .status(HttpStatus.CREATED)
//             .body(created);
//     }

//     // PUT /api/books/{id}
//     @PutMapping("/{id}")
//     public Book update(
//             @PathVariable int id,
//             @Valid @RequestBody Book book) {

//         return service.update(id, book);
//     }

//     // DELETE /api/books/{id}
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> delete(
//             @PathVariable int id) {

//         service.delete(id);
//         return ResponseEntity.noContent().build();
//     }
// }
