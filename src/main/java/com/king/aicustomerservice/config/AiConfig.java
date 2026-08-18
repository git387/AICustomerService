package com.king.aicustomerservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置
 * 组装带 RAG 检索增强的 ChatClient
 */
@Configuration
public class AiConfig {

    /**
     * 商城智能客服 ChatClient
     * 通过 QuestionAnswerAdvisor 在回答前检索 Redis 向量库
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore) {
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(4).build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是「基于Spring AI2.0的带智能客服的商城系统」的智能客服助手。
                        请优先根据检索到的知识库内容，用简体中文友好地回答用户关于下单、支付、物流、售后的问题。
                        如果知识库中没有相关信息，请诚实说明，并建议用户查看订单页或联系管理员。
                        当用户询问某笔订单、提供订单号，或想查订单状态时：
                        1. 必须调用 queryOrderByOrderNo 工具，只能查询当前登录用户自己的订单；
                        2. 根据工具返回如实回答，不要编造订单状态、金额或物流；
                        3. 若查到订单，回复中必须保留工具给出的详情页链接，格式为 [查看订单详情](/orders/数字ID)；
                        4. 若未找到，提示核对订单号，并引导前往「我的订单」页面查看。
                        """)
                .defaultAdvisors(advisor)
                .build();
    }
}
