package com.bookmanagement.bookmanagementsystem.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.bookmanagement.bookmanagementsystem.entity.Book;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends CrudRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findByQrcode(String qrcode);

    @Query(value = "SELECT * FROM books ORDER BY embedding <-> CAST(:queryEmbedding AS vector) LIMIT :limit", nativeQuery = true)
    List<Book> findClosestByEmbedding(@Param("queryEmbedding") String queryEmbedding, @Param("limit") int limit);
}
