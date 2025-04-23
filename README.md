# 工具集

本项目包含多个实用工具，用于满足不同的业务需求。

## 1. PDF压缩工具

这是一个基于Quarkus框架开发的PDF文件压缩工具，能够将上传的PDF文件压缩至小于5MB，便于电子邮件发送或在线共享。

### 功能特点

- 简洁直观的Web界面，支持拖放上传PDF文件
- 快速压缩PDF文件（保持原始尺寸，通过优化图像质量和压缩率降低文件大小）
- 自动生成压缩后的文件名（`原文件名_compressed.pdf`格式）
- 支持包含非ASCII字符（如中文）的文件名
- 服务端处理，无需在用户设备上安装额外软件

### 技术实现

- 后端：Quarkus框架（Java）+ Apache PDFBox
- 前端：纯HTML/CSS/JavaScript，无需外部依赖
- RESTful API设计，支持多部分表单数据处理

### 使用说明

1. 开发模式运行：
```shell script
./mvnw quarkus:dev
```
访问：http://localhost:8080/

2. 打包和运行：
```shell script
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

3. Docker部署：
```shell script
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t pdf-compressor-jvm .
docker run -i --rm -p 8080:8080 pdf-compressor-jvm
```

## 2. MQA数据采集工具

这是一个用于从马来西亚学术资格认证机构（Malaysian Qualifications Agency，MQA）网站采集大学和课程信息的工具。

### 功能特性

1. 大学信息采集
   - 采集大学基本信息（名称、地址、联系方式等）
   - 支持获取大学前身名称
   - 自动处理分页数据

2. 课程信息采集
   - 采集课程详细信息
   - 支持多种学习模式（Full Time/Part Time）
   - 课程持续时间表格解析
   - 学习进度表解析

3. 数据结构
   - 大学（University）
     - 基本信息（ID、名称、州属、地址等）
     - 前身名称
     - 课程列表
   - 课程（Program）
     - 基本信息（ID、参考号、名称、类型、等级等）
     - 详细信息（证书编号、认证日期等）
     - 学习时长表（Duration Table）
     - 学习进度表（Study Schedule）

### 技术实现

- Java + Quarkus框架
- JSoup（网页解析）
- Maven（项目管理）

### 数据源

- MQA官方网站：https://www2.mqa.gov.my/mqr/
- 入口页面：https://www2.mqa.gov.my/mqr/english/eakrbyipts.cfm

### 使用说明

1. 获取所有大学信息
```java
UniversityService service = new UniversityService();
List<University> universities = service.getAllUniversities();
```

2. 获取测试数据（单个大学）
```java
UniversityService service = new UniversityService();
University testUniversity = service.getTestUniversity();
```

### 数据格式

1. Duration Table 格式示例：
```
Full Time:
Type      | Weeks/Semester | Semesters | Duration
Long      | 14            | 8         | 2 year/s to 4 year/s
Short     | 14            | 6         | 2 year/s to 4 year/s

Part Time:
Type      | Weeks/Semester | Semesters | Duration
Long      | 14            | 12        | 4 year/s to 6 year/s
Short     | 14            | 10        | 4 year/s to 6 year/s
```

2. Study Schedule 格式示例：
```
Starting | Weeks | Semesters | Training | Years | Credits
Jan      | 14    | 1         | Yes      | 1     | 20
June     | 14    | 2         | No       | 1     | 20
```

### 注意事项

1. 网络连接
   - 确保有稳定的网络连接
   - 建议使用代理或VPN以提高访问稳定性

2. 数据更新
   - MQA网站数据可能会定期更新
   - 建议定期运行程序以获取最新数据

3. 错误处理
   - 程序包含完整的错误处理机制
   - 网络错误会被记录但不会中断整体执行

## 开发者

- 机构：北京理工大学珠海学院
- 项目：工具集开发项目

## 许可证

Copyright © 2024 北京理工大学珠海学院
