# PDF 压缩工具

这是一个基于Quarkus框架开发的PDF文件压缩工具，能够将上传的PDF文件压缩至小于5MB，便于电子邮件发送或在线共享。

## 功能特点

- 简洁直观的Web界面，支持拖放上传PDF文件
- 快速压缩PDF文件（保持原始尺寸，通过优化图像质量和压缩率降低文件大小）
- 自动生成压缩后的文件名（`原文件名_compressed.pdf`格式）
- 支持包含非ASCII字符（如中文）的文件名
- 服务端处理，无需在用户设备上安装额外软件

## 技术实现

- 后端：Quarkus框架（Java）+ Apache PDFBox
- 前端：纯HTML/CSS/JavaScript，无需外部依赖
- RESTful API设计，支持多部分表单数据处理

## 开发与修改过程

### 基础功能实现

1. 创建了基于Quarkus的Web应用框架
2. 使用Apache PDFBox实现PDF处理功能：
   - 文档图像压缩与优化
   - 图像尺寸调整（超过1000像素的图像会被等比例缩小）
   - 设置图像质量为50%以减小文件大小
3. 实现了RESTful API接口：
   - `/api/pdf/compress` 端点处理PDF上传和压缩
   - 使用multipart/form-data格式接收文件
   - 返回压缩后的PDF文件供用户下载
4. 开发了简洁的用户界面：
   - 文件选择和上传区域
   - 进度显示和状态反馈
   - 自动下载处理后的文件

### 功能完善与Bug修复

#### 文件名处理问题

**问题**：测试中发现，参数化测试`testCompressedFileNames`失败，无法正确处理上传文件的名称。

**修复**：
1. 在`PdfResource.java`中增强了文件名处理逻辑：
   ```java
   // 记录一下收到的文件名，便于调试
   String originalFileName = upload.file.fileName();
   LOG.info("接收到的原始文件名: " + originalFileName);
   
   // 如果文件名是路径格式，则只获取文件名部分
   if (originalFileName.contains("/") || originalFileName.contains("\\")) {
       originalFileName = Paths.get(originalFileName).getFileName().toString();
       LOG.info("处理后的文件名: " + originalFileName);
   }
   ```

2. 更新了单元测试方法`testCompressedFileNames`，使用更明确的方式传递文件名：
   ```java
   byte[] fileBytes = Files.readAllBytes(file.toPath());
   Response response = given()
       .contentType("multipart/form-data")
       .multiPart("file", inputFileName, fileBytes, "application/pdf")
       .when()
       .post("/api/pdf/compress");
   ```

#### 前端文件名下载问题

**问题**：前端JavaScript中硬编码了下载文件名为"compressed.pdf"，忽略了服务器返回的实际文件名。

**修复**：
1. 更新了前端代码，从Content-Disposition响应头中提取文件名：
   ```javascript
   const contentDisposition = response.headers.get('Content-Disposition');
   let filename = 'compressed.pdf'; // 默认文件名
   
   if (contentDisposition) {
       const filenameMatch = contentDisposition.match(/filename="(.+?)"/);
       if (filenameMatch && filenameMatch[1]) {
           filename = filenameMatch[1];
       }
   }
   ```

#### 非ASCII字符（如中文）文件名支持

**问题**：使用非ASCII字符（如中文）的文件名无法在下载时正确显示。

**修复**：
1. 在后端添加了RFC 5987标准支持：
   ```java
   // 处理非ASCII字符文件名
   String encodedFileName = encodeFileName(compressedFileName);

   return Response.ok(compressedPdf)
           .header("Content-Disposition", "attachment; filename=\"" + compressedFileName + "\"; filename*=UTF-8''" + encodedFileName)
           .type(MediaType.APPLICATION_OCTET_STREAM)
           .build();
   ```

2. 添加了URL编码方法：
   ```java
   private String encodeFileName(String fileName) {
       try {
           return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
       } catch (UnsupportedEncodingException e) {
           LOG.error("文件名编码失败", e);
           return fileName;
       }
   }
   ```

3. 更新了前端代码，增加对RFC 5987格式的解析：
   ```javascript
   // 首先尝试从filename*=UTF-8''获取编码文件名（RFC 5987格式）
   const filenameStarMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
   if (filenameStarMatch && filenameStarMatch[1]) {
       // 解码URL编码的文件名
       filename = decodeURIComponent(filenameStarMatch[1]);
   } else {
       // 退回到传统的filename参数
       const filenameMatch = contentDisposition.match(/filename="(.+?)"/);
       if (filenameMatch && filenameMatch[1]) {
           filename = filenameMatch[1];
       }
   }
   ```

## 运行应用

### 开发模式

你可以在开发模式下运行应用，这支持热加载：

```shell script
./mvnw quarkus:dev
```

然后访问：http://localhost:8080/

### 打包和运行

打包应用：

```shell script
./mvnw package
```

这将在`target/quarkus-app/`目录中生成`quarkus-run.jar`文件。
运行应用：

```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

### 创建本地可执行文件

```shell script
./mvnw package -Dnative
```

执行本地可执行文件：

```shell script
./target/toolsets-1.0.0-SNAPSHOT-runner
```

## 使用Docker

本项目包含Dockerfile，可以构建和运行Docker容器：

```shell script
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t pdf-compressor-jvm .
docker run -i --rm -p 8080:8080 pdf-compressor-jvm
```
