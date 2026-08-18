package com.king.aicustomerservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.king.aicustomerservice.entity.ChatMessage;
import com.king.aicustomerservice.mapper.ChatMessageMapper;
import com.king.aicustomerservice.tool.OrderQueryTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能客服对话服务
 * 调用带 RAG Advisor 的 ChatClient 生成回答，并支持按订单号查询当前用户订单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    /** 匹配常见订单号（纯数字，长度约 10~24） */
    private static final Pattern ORDER_NO_PATTERN = Pattern.compile("(?<!\\d)(\\d{10,24})(?!\\d)");

    private final ChatClient chatClient;
    private final ChatMessageMapper chatMessageMapper;
    private final OrderService orderService;

    /**
     * 发送用户问题并返回 AI 回答
     */
    public String chat(Long userId, String question) {
        if (!StringUtils.hasText(question)) {
            throw new RuntimeException("请输入问题");
        }
        saveMessage(userId, "user", question);
        try {
            OrderQueryTool orderTool = new OrderQueryTool(userId, orderService);
            String prompt = enrichWithOrderHint(question, orderTool);
            String answer = chatClient.prompt()
                    .user(prompt)
                    .tools(orderTool)
                    .call()
                    .content();
            if (!StringUtils.hasText(answer)) {
                answer = "暂时没有检索到相关知识，建议查看商品详情页或联系管理员。";
            }
            saveMessage(userId, "assistant", answer);
            return answer;
        } catch (Exception e) {
            log.error("智能客服调用失败", e);
            String fallback = "客服暂时不可用，请检查 DashScope API Key、Redis 向量库是否配置正确。原因：" + e.getMessage();
            saveMessage(userId, "assistant", fallback);
            return fallback;
        }
    }

    /**
     * 若用户消息中带有订单号，先查好再交给模型，提高工具调用成功率
     */
    private String enrichWithOrderHint(String question, OrderQueryTool orderTool) {
        Matcher matcher = ORDER_NO_PATTERN.matcher(question);
        if (!matcher.find()) {
            return question;
        }
        String orderNo = matcher.group(1);
        String toolResult = orderTool.queryOrderByOrderNo(orderNo);
        return question + "\n\n[系统已按订单号 " + orderNo + " 查询当前用户订单，结果如下，请据此回答，并保留详情页链接]\n"
                + toolResult;
    }

    /**
     * 查询用户最近聊天记录
     */
    public List<ChatMessage> history(Long userId) {
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getId)
                .last("LIMIT 50"));
    }

    /**
     * 保存一条聊天记录
     */
    private void saveMessage(Long userId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        chatMessageMapper.insert(message);
    }
}
