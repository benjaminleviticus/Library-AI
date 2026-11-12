package com.bookmanagement.bookmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "books")
@ToString(exclude = {"embedding"})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private String genre;
    @JsonIgnore // absolute server path; clients use /books/{id}/pdf
    private String pdfPath;
    private String img;
    private String qrcode;


    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;


    // Store web paths to generated images (e.g. "/uploads/qrcodes/qr_1.png")
    private String barcodePath;
    private String qrcodePath;

    @JsonIgnore // large vector, only used for similarity search on the server
    @Column(name = "embedding", columnDefinition = "vector")
    private float[] embedding;

    /** Lets the front end know whether a PDF can be read, without exposing the file path. */
    @JsonProperty("hasPdf")
    public boolean hasPdf() {
        return pdfPath != null && !pdfPath.isBlank();
    }
}
