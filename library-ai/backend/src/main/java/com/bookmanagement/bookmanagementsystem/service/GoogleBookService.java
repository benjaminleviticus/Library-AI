package com.bookmanagement.bookmanagementsystem.service;

import com.bookmanagement.bookmanagementsystem.entity.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Service
@Slf4j
public class GoogleBookService {

    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public GoogleBookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Book> fetchBookDetailsByIsbn(String isbn) {
        try {
            String url = GOOGLE_BOOKS_API_URL + isbn;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("items");
            if (items.isEmpty() || !items.isArray()) {
                return Optional.empty();
            }

            JsonNode volumeInfo = items.get(0).path("volumeInfo");

            Book book = Book.builder()
                    .isbn(isbn)
                    .title(volumeInfo.path("title").asText("Unknown Title"))
                    .description(volumeInfo.path("description").asText(""))
                    .genre(volumeInfo.path("categories").isArray() && volumeInfo.path("categories").size() > 0
                            ? volumeInfo.path("categories").get(0).asText() : "Unknown")
                    .img(volumeInfo.path("imageLinks").path("thumbnail").asText(""))
                    .author(volumeInfo.path("authors").isArray() && volumeInfo.path("authors").size() > 0
                            ? volumeInfo.path("authors").get(0).asText("Unknown Author") : "Unknown Author")
                    .build();

            return Optional.of(book);
        } catch (Exception e) {
            log.error("Failed to fetch google books data for isbn {}: {}", isbn, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
