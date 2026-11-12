package com.bookmanagement.bookmanagementsystem.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.io.IOException;

@Service
@Slf4j
public class BarcodeService {

    @Value("${app.upload-dir:uploads}")
    private String baseUploadDir;

    public String generateQrCodeFile(String text, String fileName) {
        return generateCodeFile(text, BarcodeFormat.QR_CODE, 250, 250, "qrcodes", fileName);
    }

    public String generateBarcodeFile(String text, String fileName) {
        return generateCodeFile(text, BarcodeFormat.CODE_128, 300, 100, "barcodes", fileName);
    }

    private String generateCodeFile(String content, BarcodeFormat format, int width, int height, String subDir, String fileName) {
        try {
            Path dir = Paths.get(baseUploadDir, subDir);
            Files.createDirectories(dir);

            Path filePath = dir.resolve(fileName + ".png");
            BitMatrix matrix = new MultiFormatWriter().encode(content, format, width, height);
            MatrixToImageWriter.writeToPath(matrix, "PNG", filePath);

            String webPath = "/" + baseUploadDir.replace("\\", "/") + "/" + subDir + "/" + fileName + ".png";
            return webPath;
        } catch (WriterException e) {
            log.error("Failed to encode barcode/qr: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate code", e);
        } catch (IOException e) {
            log.error("Failed to write barcode/qr to disk: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save code file", e);
        }
    }
}
