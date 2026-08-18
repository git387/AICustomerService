package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.king.aicustomerservice.entity.KnowledgeFile;
import com.king.aicustomerservice.mapper.KnowledgeFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库服务
 * 负责文件上传、解析、分块并写入 Redis 向量数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final Set<String> ALLOWED_TYPES = Set.of("txt", "doc", "docx", "pdf", "md", "markdown");
    /** DashScope 嵌入接口限制单次 contents 不超过 20 条 */
    private static final int EMBED_BATCH_SIZE = 20;

    private final KnowledgeFileMapper knowledgeFileMapper;
    private final FileUploadService fileUploadService;
    private final VectorStore vectorStore;

    /**
     * 查询全部知识库文件
     */
    public List<KnowledgeFile> listAll() {
        return knowledgeFileMapper.selectList(
                new LambdaQueryWrapper<KnowledgeFile>().orderByDesc(KnowledgeFile::getId));
    }

    /**
     * 上传知识库文件并向量化
     */
    public KnowledgeFile uploadAndEmbed(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String type = fileType(original);
        if (!ALLOWED_TYPES.contains(type)) {
            throw new RuntimeException("仅支持 txt、doc、pdf、markdown 格式");
        }

        Path saved = fileUploadService.saveKnowledgeFile(file);
        KnowledgeFile record = new KnowledgeFile();
        record.setOriginalName(original);
        record.setStoredPath(saved.toAbsolutePath().toString());
        record.setFileType("markdown".equals(type) ? "md" : ("docx".equals(type) ? "doc" : type));
        record.setStatus("PENDING");
        record.setChunkCount(0);
        knowledgeFileMapper.insert(record);

        try {
            List<Document> documents = parseDocuments(saved, type);
            TokenTextSplitter splitter = TokenTextSplitter.builder().build();
            List<Document> chunks = splitter.apply(documents);
            if (chunks.isEmpty()) {
                throw new RuntimeException("未能从文件中解析出文本内容");
            }
            List<Document> toStore = chunks.stream()
                    .map(chunk -> new Document(
                            chunk.getText(),
                            Map.of(
                                    "fileId", String.valueOf(record.getId()),
                                    "fileName", original
                            )))
                    .collect(Collectors.toList());
            addInBatches(toStore);
            String ids = toStore.stream().map(Document::getId).collect(Collectors.joining(","));
            record.setChunkCount(toStore.size());
            record.setVectorIds(ids);
            record.setStatus("SUCCESS");
            knowledgeFileMapper.updateById(record);
            return record;
        } catch (Exception e) {
            log.error("知识库向量化失败", e);
            record.setStatus("FAIL");
            record.setErrorMsg(e.getMessage());
            knowledgeFileMapper.updateById(record);
            throw new RuntimeException("解析或向量化失败: " + e.getMessage());
        }
    }

    /**
     * 删除知识库文件及其 Redis 向量
     */
    public void delete(Long id) {
        KnowledgeFile file = knowledgeFileMapper.selectById(id);
        if (file == null) {
            return;
        }
        if (file.getVectorIds() != null && !file.getVectorIds().isBlank()) {
            List<String> ids = Arrays.stream(file.getVectorIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!ids.isEmpty()) {
                vectorStore.delete(ids);
            }
        }
        try {
            Files.deleteIfExists(Path.of(file.getStoredPath()));
        } catch (Exception e) {
            log.warn("删除磁盘文件失败: {}", e.getMessage());
        }
        knowledgeFileMapper.deleteById(id);
    }

    /**
     * 分批写入向量库，避免超过 DashScope 单次最多 20 条的限制
     */
    private void addInBatches(List<Document> documents) {
        for (int i = 0; i < documents.size(); i += EMBED_BATCH_SIZE) {
            int end = Math.min(i + EMBED_BATCH_SIZE, documents.size());
            vectorStore.add(documents.subList(i, end));
        }
    }

    /**
     * 按文件类型解析文档
     */
    private List<Document> parseDocuments(Path path, String type) {
        FileSystemResource resource = new FileSystemResource(path.toFile());
        if ("md".equals(type) || "markdown".equals(type)) {
            MarkdownDocumentReader reader = new MarkdownDocumentReader(
                    resource, MarkdownDocumentReaderConfig.defaultConfig());
            return reader.get();
        }
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        return reader.get();
    }

    /**
     * 提取小写后缀名
     */
    private String fileType(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
