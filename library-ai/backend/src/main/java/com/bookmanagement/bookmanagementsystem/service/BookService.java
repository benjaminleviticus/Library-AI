package com.bookmanagement.bookmanagementsystem.service;

import com.bookmanagement.bookmanagementsystem.entity.Book;
import com.bookmanagement.bookmanagementsystem.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BarcodeService barcodeService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public BookService(BookRepository bookRepository, BarcodeService barcodeService) {
        this.bookRepository = bookRepository;
        this.barcodeService = barcodeService;
    }

    public Book saveBookWithPdf(Book book, MultipartFile pdfFile) throws IOException {
        Book savedBook = bookRepository.save(book);

        String pdfPath = savePdfFile(savedBook.getId(), pdfFile);
        savedBook.setPdfPath(pdfPath);

        generateCodes(savedBook);
        return bookRepository.save(savedBook);
    }

    public Book saveBook(Book book) {
        Book savedBook = bookRepository.save(book);
        generateCodes(savedBook);
        return bookRepository.save(savedBook);
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        bookRepository.findAll().forEach(books::add);
        return books;
    }

    public List<Book> findNearestBooks(Map<String, Object> payload) {
        return Collections.emptyList();
    }

    public Book getBook(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            // Optional: delete files here (pdf, qrcode, barcode). See note below.
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void deleteAllBooks() {
        bookRepository.deleteAll();
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    private void generateCodes(Book book) {
        try {
            if (book.getIsbn() != null && !book.getIsbn().isBlank()) {
                String barcodeFileName = "barcode_" + book.getId();
                String barcodeWebPath = barcodeService.generateBarcodeFile(book.getIsbn(), barcodeFileName);
                book.setBarcodePath(barcodeWebPath);
            }

            String readerUrl = baseUrl + "/reader.html?id=" + book.getId();
            String qrFileName = "qr_" + book.getId();
            String qrWebPath = barcodeService.generateQrCodeFile(readerUrl, qrFileName);
            book.setQrcodePath(qrWebPath);

        } catch (Exception e) {
            log.error("Failed to generate codes for book id {} : {}", book.getId(), e.getMessage(), e);
        }
    }

    private String savePdfFile(Long bookId, MultipartFile pdfFile) throws IOException {
        Path dir = Paths.get(uploadDir, "books");
        Files.createDirectories(dir);

        String fileName = "book_" + bookId + ".pdf";
        Path filePath = dir.resolve(fileName);
        pdfFile.transferTo(filePath.toFile());
        return filePath.toAbsolutePath().toString();
    }
}
