package cn.ac.bestheme.toolsets.pdf;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/api/pdf")
public class PdfResource {
    private static final Logger LOG = Logger.getLogger(PdfResource.class);

    @Inject
    PdfService pdfService;

    public static class PdfUpload {
        @RestForm("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public FileUpload file;
    }

    @POST
    @Path("/compress")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response compressPdf(PdfUpload upload) {
        try {
            if (upload == null || upload.file == null) {
                LOG.error("没有收到文件");
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.TEXT_PLAIN)
                        .entity("没有收到文件")
                        .build();
            }

            // 记录一下收到的文件名，便于调试
            String originalFileName = upload.file.fileName();
            LOG.info("接收到的原始文件名: " + originalFileName);
            
            // 如果文件名是路径格式，则只获取文件名部分
            if (originalFileName.contains("/") || originalFileName.contains("\\")) {
                originalFileName = Paths.get(originalFileName).getFileName().toString();
                LOG.info("处理后的文件名: " + originalFileName);
            }
            
            String compressedFileName = getCompressedFileName(originalFileName);
            LOG.info("压缩后的文件名: " + compressedFileName);

            // 使用 InputStream 而不是 Path，因为我们不需要实际的文件
            try (InputStream inputStream = Files.newInputStream(upload.file.uploadedFile())) {
                byte[] compressedPdf = pdfService.compressPdf(inputStream);
                
                if (compressedPdf == null || compressedPdf.length == 0) {
                    LOG.error("压缩后的PDF为空");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .type(MediaType.TEXT_PLAIN)
                            .entity("压缩后的PDF为空")
                            .build();
                }

                // 处理非ASCII字符文件名
                String encodedFileName = encodeFileName(compressedFileName);

                return Response.ok(compressedPdf)
                        .header("Content-Disposition", "attachment; filename=\"" + compressedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                        .type(MediaType.APPLICATION_OCTET_STREAM)
                        .build();
            }
        } catch (IllegalArgumentException e) {
            LOG.error("请求参数错误", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            LOG.error("PDF压缩失败", e);
            return Response.serverError()
                    .type(MediaType.TEXT_PLAIN)
                    .entity("PDF压缩失败: " + e.getMessage())
                    .build();
        }
    }

    private String getCompressedFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return "compressed.pdf";
        }

        String baseName = originalFileName;
        if (originalFileName.toLowerCase().endsWith(".pdf")) {
            baseName = originalFileName.substring(0, originalFileName.length() - 4);
        }
        return baseName + "_compressed.pdf";
    }
    
    private String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            LOG.error("文件名编码失败", e);
            return fileName;
        }
    }
} 