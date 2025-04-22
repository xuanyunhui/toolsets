package cn.ac.bestheme.toolsets.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;

@ApplicationScoped
public class PdfService {
    private static final Logger LOG = Logger.getLogger(PdfService.class);
    private static final float IMAGE_QUALITY = 0.5f; // 图片质量，0.5表示50%
    private static final int MAX_IMAGE_DIMENSION = 1000; // 最大图片尺寸

    public byte[] compressPdf(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        return compressPdf(PDDocument.load(path.toFile()));
    }

    public byte[] compressPdf(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        return compressPdf(PDDocument.load(inputStream));
    }

    private byte[] compressPdf(PDDocument document) throws IOException {
        try {
            // 移除所有安全限制
            document.setAllSecurityToBeRemoved(true);

            // 压缩文档中的图片
            for (PDPage page : document.getPages()) {
                compressImagesInPage(document, page);
            }

            // 创建输出流并保存文档
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            
            return outputStream.toByteArray();
        } catch (IOException e) {
            LOG.error("PDF压缩失败", e);
            throw e;
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private void compressImagesInPage(PDDocument document, PDPage page) throws IOException {
        PDResources resources = page.getResources();
        if (resources == null) return;

        // 遍历页面中的所有图片
        for (COSName name : resources.getXObjectNames()) {
            PDXObject object = resources.getXObject(name);
            if (object instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) object;
                BufferedImage bufferedImage = image.getImage();

                // 如果图片尺寸过大，进行缩放
                if (bufferedImage.getWidth() > MAX_IMAGE_DIMENSION || 
                    bufferedImage.getHeight() > MAX_IMAGE_DIMENSION) {
                    bufferedImage = scaleImage(bufferedImage);
                }

                // 使用JPEG压缩重新创建图片
                PDImageXObject newImage = JPEGFactory.createFromImage(
                    document,
                    bufferedImage,
                    IMAGE_QUALITY
                );

                // 替换原图片
                resources.put(name, newImage);
            }
        }
    }

    private BufferedImage scaleImage(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // 计算新的尺寸，保持宽高比
        float scale = Math.min(
            (float) MAX_IMAGE_DIMENSION / originalWidth,
            (float) MAX_IMAGE_DIMENSION / originalHeight
        );
        
        int newWidth = Math.round(originalWidth * scale);
        int newHeight = Math.round(originalHeight * scale);

        // 创建缩放后的图片
        java.awt.Image scaledImage = original.getScaledInstance(
            newWidth,
            newHeight,
            java.awt.Image.SCALE_SMOOTH
        );

        // 转换为BufferedImage
        BufferedImage result = new BufferedImage(
            newWidth,
            newHeight,
            BufferedImage.TYPE_INT_RGB
        );
        result.getGraphics().drawImage(scaledImage, 0, 0, null);
        
        return result;
    }
} 