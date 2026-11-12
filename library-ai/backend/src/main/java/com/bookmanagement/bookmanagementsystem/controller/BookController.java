package com.bookmanagement.bookmanagementsystem.controller;

import com.bookmanagement.bookmanagementsystem.entity.Book;
import com.bookmanagement.bookmanagementsystem.service.BookService;
import com.bookmanagement.bookmanagementsystem.service.GoogleBookService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/books")
@Slf4j
public class BookController {

    private final GoogleBookService googleBookService;
    private final BookService bookService;

    public BookController(GoogleBookService googleBookService, BookService bookService) {
        this.googleBookService = googleBookService;
        this.bookService = bookService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBook(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "img", required = false) String img,
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please choose a PDF file.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().body("Only PDF files are allowed.");
        }
        try {
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setDescription(description);
            book.setGenre(genre);
            book.setImg(img);

            Book saved = bookService.saveBookWithPdf(book, file);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }

    @PostMapping("/addBook")
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @GetMapping("/getAllBooks")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/getBookById/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBook(id);
        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteBook/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        boolean deleted = bookService.deleteBook(id);
        return new ResponseEntity<>(
                deleted ? "Deleted Book Successfully" : "Book Not Found",
                deleted ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @DeleteMapping("/deleteAllBooks")
    public ResponseEntity<String> deleteAllBooks() {
        bookService.deleteAllBooks();
        return ResponseEntity.ok("Deleted All Books Successfully");
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> getBookPdf(@PathVariable Long id) {
        Book book = bookService.getBook(id);
        if (book == null || book.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path path = Paths.get(book.getPdfPath());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource file = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline()
                                    .filename((book.getTitle() == null ? "book" : book.getTitle()) + ".pdf",
                                            java.nio.charset.StandardCharsets.UTF_8)
                                    .build().toString())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(file);
        } catch (Exception e) {
            log.error("Failed to fetch PDF for id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String query) {
        return bookService.searchBooks(query);
    }

    @PostMapping("/nearest")
    public ResponseEntity<List<Book>> findNearestBooks(@RequestBody Map<String, Object> payload) {
        try {
            List<Book> nearestBooks = bookService.findNearestBooks(payload);
            return ResponseEntity.ok(nearestBooks);
        } catch (Exception e) {
            log.error("Nearest search failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
