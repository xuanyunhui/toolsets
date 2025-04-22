package cn.ac.bestheme.toolsets.pdf;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class PdfResourceTest {

    private static final String TEST_PDF_PATH = "src/test/resources/test.pdf";

    @Test
    public void testIndexPage() {
        given()
            .when()
            .get("/")
            .then()
            .statusCode(200)
            .contentType("text/html")
            .body(containsString("PDF 压缩工具"));
    }

    @Test
    public void testCompressPdfSuccess() throws Exception {
        File pdfFile = new File(TEST_PDF_PATH);
        assertTrue(pdfFile.exists(), "测试PDF文件不存在");

        Response response = given()
            .multiPart("file", pdfFile)
            .when()
            .post("/api/pdf/compress")
            .then()
            .statusCode(200)
            .contentType("application/octet-stream")
            .extract()
            .response();

        // 验证压缩后的PDF不为空
        byte[] compressedPdf = response.asByteArray();
        assertTrue(compressedPdf.length > 0, "压缩后的PDF不应为空");

        // 验证Content-Disposition头
        String contentDisposition = response.getHeader("Content-Disposition");
        String expectedFileName = "test_compressed.pdf";
        assertTrue(contentDisposition.contains("attachment"), "Content-Disposition应该包含attachment");
        assertTrue(contentDisposition.contains("filename=\"" + expectedFileName + "\""), 
            "文件名应该是: " + expectedFileName);
    }

    @Test
    public void testUploadPdf() {
        File file = new File("src/test/resources/test.pdf");
        given()
            .multiPart("file", file)
            .when()
            .post("/api/pdf/compress")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    @ParameterizedTest
    @CsvSource({
        "test.pdf, test_compressed.pdf",
        "document.pdf, document_compressed.pdf",
        "sample file.pdf, sample file_compressed.pdf"
    })
    public void testCompressedFileNames(String inputFileName, String expectedFileName) throws IOException {
        File file = new File("src/test/resources/test.pdf");
        
        // 使用byte[]方式传递文件内容并指定文件名
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        
        Response response = given()
            .contentType("multipart/form-data")
            .multiPart("file", inputFileName, fileBytes, "application/pdf")
            .when()
            .post("/api/pdf/compress");

        response.then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", containsString(expectedFileName));
    }

    @Test
    public void testCompressPdfNoFile() {
        given()
            .contentType("multipart/form-data")
            .when()
            .post("/api/pdf/compress")
            .then()
            .statusCode(400)
            .contentType("text/plain")
            .body(containsString("没有收到文件"));
    }

    @Test
    public void testCompressPdfEmptyFile() {
        given()
            .contentType("multipart/form-data")
            .multiPart("file", "empty.pdf", new byte[0])
            .when()
            .post("/api/pdf/compress")
            .then()
            .statusCode(500)
            .contentType("text/plain")
            .body(containsString("PDF压缩失败"));
    }
} 