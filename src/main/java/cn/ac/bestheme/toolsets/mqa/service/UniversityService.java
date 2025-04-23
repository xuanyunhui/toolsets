package cn.ac.bestheme.toolsets.mqa.service;

import cn.ac.bestheme.toolsets.mqa.model.University;
import cn.ac.bestheme.toolsets.mqa.model.Program;
import cn.ac.bestheme.toolsets.mqa.model.Program.ProgramDetails;
import cn.ac.bestheme.toolsets.mqa.model.Program.ProgramDetails.DurationRow;
import cn.ac.bestheme.toolsets.mqa.model.Program.ProgramDetails.StudyScheduleRow;
import jakarta.enterprise.context.ApplicationScoped;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UniversityService {

    private static final String BASE_URL = "https://www2.mqa.gov.my/mqr/english/eakrbyipts.cfm";
    private static final String NEXT_PAGE_PATTERN = "eakrbyipts.cfm?StartRow=";
    private static final String DETAIL_URL = "https://www2.mqa.gov.my/mqr/english/eakrKPList.cfm";
    private static final String PROGRAM_DETAIL_URL = "https://www2.mqa.gov.my/mqr/english/epapar.cfm";

    public List<University> getAllUniversities() {
        List<University> universities = new ArrayList<>();
        try {
            String currentUrl = BASE_URL;
            while (currentUrl != null) {
                System.out.println("Fetching page: " + currentUrl);
                Document doc = Jsoup.connect(currentUrl).get();
                // 精确匹配目标表格
                Element table = doc.select("table[border=2][width=661][bordercolor=#80000]").first();
                if (table != null) {
                    Elements rows = table.select("tr");
                    for (Element row : rows) {
                        Elements cells = row.select("td");
                        if (cells.size() >= 3) {
                            String name = cells.get(1).text().trim();
                            String state = cells.get(2).text().trim();
                            
                            // 跳过表头和无效数据
                            if (name.equals("NO") || name.equals("IPTS NAME") || name.isEmpty() || 
                                name.contains("Programme that is marked in green colour") ||
                                name.contains("ACCREDITATION STATUS")) {
                                continue;
                            }

                            // 从链接中提取ID
                            String id = "";
                            Element link = cells.get(1).select("a").first();
                            if (link != null) {
                                String href = link.attr("href");
                                if (href.contains("IDAkrIPTS=")) {
                                    id = href.substring(href.indexOf("IDAkrIPTS=") + 10);
                                    if (id.contains("&")) {
                                        id = id.substring(0, id.indexOf("&"));
                                    }
                                }
                            }

                            String previousName = "";
                            // 提取括号中的前身名称
                            if (name.contains("(Previously known as :")) {
                                int start = name.indexOf("(Previously known as :");
                                int end = name.lastIndexOf(")");
                                previousName = name.substring(start + 21, end).trim();
                                name = name.substring(0, start).trim();
                            }

                            // 获取大学详细信息
                            University university = new University(id, name, state, previousName);
                            if (!id.isEmpty()) {
                                System.out.println("Fetching details for university: " + name + " (ID: " + id + ")");
                                fetchUniversityDetails(university);
                            }
                            universities.add(university);
                        }
                    }
                }

                // 查找下一页链接
                currentUrl = findNextPageUrl(doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return universities;
    }

    private void fetchProgramDetails(Program program) {
        try {
            String detailUrl = PROGRAM_DETAIL_URL + "?NoRujA=" + program.getReferenceNumber() + "&IdAkrKP=" + program.getId();
            System.out.println("Fetching program details from: " + detailUrl);
            Document doc = Jsoup.connect(detailUrl).get();

            // 获取包含所有信息的表格
            Element table = doc.select("table").first();
            if (table != null) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 3) {  // 确保至少有3个单元格（标签、冒号、值）
                        String label = cells.get(0).text().trim();
                        // 移除标签中的冒号
                        if (label.endsWith(":")) {
                            label = label.substring(0, label.length() - 1).trim();
                        }
                        String value = cells.get(2).text().trim();  // 跳过中间的冒号单元格
                        
                        System.out.println("Processing field: " + label + " = " + value); // 调试日志

                        // 解析基本信息字段
                        switch (label) {
                            case "Certificate Number":
                                program.getDetails().setCertificateNumber(value);
                                break;
                            case "Date of Accreditation (dd/mm/yyyy)":
                                program.getDetails().setAccreditationDate(value);
                                break;
                            case "Compliance Audit":
                                program.getDetails().setComplianceAudit(value);
                                break;
                            case "MQF Level":
                                program.getDetails().setMqfLevel(value);
                                break;
                            case "NEC Field (National Education Code)":
                                program.getDetails().setNecField(value);
                                break;
                            case "Number of Credits":
                                program.getDetails().setNumberOfCredits(value);
                                break;
                            case "Mode of Study":
                                program.getDetails().setModeOfStudy(value);
                                break;
                            case "Mode of Delivery":
                                program.getDetails().setModeOfDelivery(value);
                                break;
                            case "Remark(s)":
                                program.getDetails().setRemarks(value);
                                break;
                        }

                        // 检查是否包含前身名称
                        if (label.equals("Name of Qualification")) {
                            if (value.contains("Previously known as")) {
                                int start = value.indexOf("Previously known as :") + 19;
                                int end = value.lastIndexOf(")");
                                if (start > 18 && end > start) {
                                    program.getDetails().setPreviousName(value.substring(start, end).trim());
                                }
                            }
                        }

                        // 解析Duration of Study表格
                        if (label.equals("Duration of Study (years)")) {
                            Elements durationTables = cells.get(2).select("table");  // 获取所有表格
                            for (Element durationTable : durationTables) {
                                if (durationTable != null) {
                                    Elements durationRows = durationTable.select("tr");
                                    String studyMode = "";  // Full Time 或 Part Time
                                    String duration = "";   // Duration 值
                                    
                                    // 获取表头（Full Time 或 Part Time）
                                    Element headerRow = durationRows.first();
                                    if (headerRow != null) {
                                        Elements headerCells = headerRow.select("td");
                                        if (!headerCells.isEmpty()) {
                                            studyMode = headerCells.first().text().trim();
                                        }
                                    }

                                    // 获取 Duration 列的值（可能跨行）
                                    for (Element dRow : durationRows) {
                                        Elements dCells = dRow.select("td");
                                        if (dCells.size() >= 4) {  // 确保有足够的列
                                            String lastCellText = dCells.last().text().trim();
                                            if (lastCellText.contains("year/s")) {
                                                duration = lastCellText;
                                                break;
                                            }
                                        }
                                    }

                                    // 处理数据行
                                    for (int i = 1; i < durationRows.size(); i++) {  // 跳过表头
                                        Elements durationCells = durationRows.get(i).select("td");
                                        if (durationCells.size() >= 3) {
                                            String type = durationCells.get(0).text().trim();
                                            String weeks = durationCells.get(1).text().trim();
                                            String semesters = durationCells.get(2).text().trim();
                                            
                                            // 只有当类型不为空时才添加（跳过空行）
                                            if (!type.isEmpty()) {
                                                // 创建完整的类型（例如：Full Time - Long）
                                                String fullType = studyMode + " - " + type;
                                                DurationRow durationRow = new DurationRow(fullType, weeks, semesters, duration);
                                                program.getDetails().getDurationTable().add(durationRow);
                                                System.out.println("Added duration row: " + fullType + ", " + weeks + ", " + semesters + ", " + duration);
                                            }
                                        }
                                    }
                                }
                            }

                            // 查找并解析学习进度表（通常在Duration表格之后）
                            Element scheduleTable = cells.get(2).select("table").last();  // 使用第三个单元格中的最后一个表格
                            if (scheduleTable != null && !durationTables.contains(scheduleTable)) {
                                Elements scheduleRows = scheduleTable.select("tr");
                                // 跳过表头
                                for (int i = 1; i < scheduleRows.size(); i++) {
                                    Elements scheduleCells = scheduleRows.get(i).select("td");
                                    if (scheduleCells.size() >= 6) {
                                        String starting = scheduleCells.get(0).text().trim();
                                        String weeks = scheduleCells.get(1).text().trim();
                                        String semesters = scheduleCells.get(2).text().trim();
                                        String training = scheduleCells.get(3).text().trim();
                                        String years = scheduleCells.get(4).text().trim();
                                        String credits = scheduleCells.get(5).text().trim();
                                        
                                        // 只有当关键值不为空时才添加
                                        if (!starting.isEmpty() && !weeks.isEmpty() && !credits.isEmpty()) {
                                            StudyScheduleRow scheduleRow = new StudyScheduleRow(
                                                starting, weeks, semesters, training, years, credits
                                            );
                                            program.getDetails().getStudySchedule().add(scheduleRow);
                                            System.out.println("Added schedule row: " + starting + ", " + weeks + ", " + semesters + 
                                                            ", " + training + ", " + years + ", " + credits);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchUniversityDetails(University university) {
        try {
            String detailUrl = DETAIL_URL + "?IDAkrIPTS=" + university.getId();
            System.out.println("Fetching details from: " + detailUrl);
            Document doc = Jsoup.connect(detailUrl).get();
            
            // 使用更精确的表格选择器获取基本信息
            Element infoTable = doc.select("table[border=1][width=100%][bordercolor=80000][cellspacing=0][cellpadding=0]").first();
            if (infoTable != null) {
                System.out.println("Info table found");
                Elements rows = infoTable.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 3) {  // 确保有至少3个单元格
                        String label = cells.get(0).text().trim();
                        String value = cells.get(2).text().trim();  // 跳过中间的冒号单元格
                        
                        // 打印调试信息
                        System.out.println("Label: " + label + ", Value: " + value);
                        
                        if (label.equals("Address")) {
                            university.setAddress(value);
                            System.out.println("Set address: " + value);
                        } else if (label.equals("Telephone No.")) {
                            university.setTelephone(value);
                            System.out.println("Set telephone: " + value);
                        } else if (label.equals("Fax No.")) {
                            university.setFax(value);
                            System.out.println("Set fax: " + value);
                        } else if (label.equals("E-Mail")) {
                            university.setEmail(value);
                            System.out.println("Set email: " + value);
                        } else if (label.equals("Website")) {
                            university.setWebsite(value);
                            System.out.println("Set website: " + value);
                        }
                    }
                }
            } else {
                System.out.println("Info table not found for university: " + university.getName());
            }

            // 获取专业表格（获取第二个匹配的表格）
            Elements programTables = doc.select("table[border=1][width=100%][bordercolor=80000][style=border-collapse: collapse][cellpadding=0]");
            if (programTables.size() >= 2) {
                Element programTable = programTables.get(1);  // 获取第二个表格
                System.out.println("Program table found");
                Elements rows = programTable.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 6) {  // 确保有6个单元格
                        // 跳过表头
                        if (cells.get(0).text().trim().equals("No.")) {
                            continue;
                        }

                        // 从链接中提取专业ID和参考号
                        String programId = "";
                        String referenceNumber = "";
                        Element link = cells.get(1).select("a").first();
                        if (link != null) {
                            String href = link.attr("href");
                            if (href.contains("NoRujA=") && href.contains("IdAkrKP=")) {
                                referenceNumber = href.substring(href.indexOf("NoRujA=") + 7, href.indexOf("&IdAkrKP="));
                                programId = href.substring(href.indexOf("IdAkrKP=") + 8);
                            }
                        }

                        String programName = cells.get(1).text().trim();
                        String type = cells.get(2).text().trim();
                        String level = cells.get(3).text().trim();
                        String field = cells.get(4).text().trim();

                        // 跳过标题行
                        if (programName.equals("NAME OF QUALIFICATION") || 
                            type.equals("TYPE") || 
                            level.equals("NO OF CREDITS") || 
                            field.equals("NEC FIELD (National Education Code)")) {
                            continue;
                        }

                        if (!programName.isEmpty()) {
                            Program program = new Program(programId, referenceNumber, programName, type, level, field);
                            // 获取专业详细信息
                            fetchProgramDetails(program);
                            university.getPrograms().add(program);
                            System.out.println("Added program: " + programName + " (ID: " + programId + ")");
                        }
                    }
                }
            } else {
                System.out.println("Program table not found for university: " + university.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String findNextPageUrl(Document doc) {
        Elements links = doc.select("a[href*=" + NEXT_PAGE_PATTERN + "]");
        for (Element link : links) {
            String text = link.text().trim();
            if (text.equals("Next 25 Records") || text.equals("Next Record")) {
                String href = link.attr("href");
                if (href.startsWith("/")) {
                    return "https://www2.mqa.gov.my" + href;
                }
                return href;
            }
        }
        return null;
    }

    // 添加测试方法
    public University getTestUniversity() {
        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            // 精确匹配目标表格
            Element table = doc.select("table[border=2][width=661][bordercolor=#80000]").first();
            if (table != null) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 3) {
                        String name = cells.get(1).text().trim();
                        String state = cells.get(2).text().trim();
                        
                        // 跳过表头和无效数据
                        if (name.equals("NO") || name.equals("IPTS NAME") || name.isEmpty() || 
                            name.contains("Programme that is marked in green colour") ||
                            name.contains("ACCREDITATION STATUS")) {
                            continue;
                        }

                        // 从链接中提取ID
                        String id = "";
                        Element link = cells.get(1).select("a").first();
                        if (link != null) {
                            String href = link.attr("href");
                            if (href.contains("IDAkrIPTS=")) {
                                id = href.substring(href.indexOf("IDAkrIPTS=") + 10);
                                if (id.contains("&")) {
                                    id = id.substring(0, id.indexOf("&"));
                                }
                            }
                        }

                        String previousName = "";
                        // 提取括号中的前身名称
                        if (name.contains("(Previously known as :")) {
                            int start = name.indexOf("(Previously known as :");
                            int end = name.lastIndexOf(")");
                            previousName = name.substring(start + 21, end).trim();
                            name = name.substring(0, start).trim();
                        }

                        // 获取第一个有效大学
                        if (!id.isEmpty()) {
                            University university = new University(id, name, state, previousName);
                            System.out.println("Testing with university: " + name + " (ID: " + id + ")");
                            fetchTestUniversityDetails(university);
                            return university;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void fetchTestUniversityDetails(University university) {
        try {
            String detailUrl = DETAIL_URL + "?IDAkrIPTS=" + university.getId();
            System.out.println("Fetching details from: " + detailUrl);
            Document doc = Jsoup.connect(detailUrl).get();
            
            // 使用更精确的表格选择器获取基本信息
            Element infoTable = doc.select("table[border=1][width=100%][bordercolor=80000][cellspacing=0][cellpadding=0]").first();
            if (infoTable != null) {
                System.out.println("Info table found");
                Elements rows = infoTable.select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 3) {  // 确保有至少3个单元格
                        String label = cells.get(0).text().trim();
                        String value = cells.get(2).text().trim();  // 跳过中间的冒号单元格
                        
                        if (label.equals("Address")) {
                            university.setAddress(value);
                        } else if (label.equals("Telephone No.")) {
                            university.setTelephone(value);
                        } else if (label.equals("Fax No.")) {
                            university.setFax(value);
                        } else if (label.equals("E-Mail")) {
                            university.setEmail(value);
                        } else if (label.equals("Website")) {
                            university.setWebsite(value);
                        }
                    }
                }
            }

            // 获取专业表格（获取第二个匹配的表格）
            Elements programTables = doc.select("table[border=1][width=100%][bordercolor=80000][style=border-collapse: collapse][cellpadding=0]");
            if (programTables.size() >= 2) {
                Element programTable = programTables.get(1);  // 获取第二个表格
                System.out.println("Program table found");
                Elements rows = programTable.select("tr");
                boolean firstValidProgram = true;  // 标记是否是第一个有效的专业

                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 6 && firstValidProgram) {  // 只处理第一个有效的专业
                        // 跳过表头
                        if (cells.get(0).text().trim().equals("No.")) {
                            continue;
                        }

                        // 从链接中提取专业ID和参考号
                        String programId = "";
                        String referenceNumber = "";
                        Element link = cells.get(1).select("a").first();
                        if (link != null) {
                            String href = link.attr("href");
                            if (href.contains("NoRujA=") && href.contains("IdAkrKP=")) {
                                referenceNumber = href.substring(href.indexOf("NoRujA=") + 7, href.indexOf("&IdAkrKP="));
                                programId = href.substring(href.indexOf("IdAkrKP=") + 8);
                            }
                        }

                        String programName = cells.get(1).text().trim();
                        String type = cells.get(2).text().trim();
                        String level = cells.get(3).text().trim();
                        String field = cells.get(4).text().trim();

                        // 跳过标题行
                        if (programName.equals("NAME OF QUALIFICATION") || 
                            type.equals("TYPE") || 
                            level.equals("NO OF CREDITS") || 
                            field.equals("NEC FIELD (National Education Code)")) {
                            continue;
                        }

                        if (!programName.isEmpty()) {
                            Program program = new Program(programId, referenceNumber, programName, type, level, field);
                            // 获取专业详细信息
                            fetchProgramDetails(program);
                            university.getPrograms().add(program);
                            System.out.println("Added test program: " + programName + " (ID: " + programId + ")");
                            firstValidProgram = false;  // 设置标记，不再处理其他专业
                            break;  // 获取到一个专业后就退出
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 