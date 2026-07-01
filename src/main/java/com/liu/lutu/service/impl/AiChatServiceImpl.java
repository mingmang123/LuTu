package com.liu.lutu.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.lutu.domain.dto.AiChatMessageDTO;
import com.liu.lutu.domain.emun.ChatEventTypeEnum;
import com.liu.lutu.domain.emun.MessageTypeEnum;
import com.liu.lutu.domain.po.AiChatSession;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlan;
import com.liu.lutu.domain.vo.AiChatMessageVo;
import com.liu.lutu.domain.vo.ChatEventVO;
import com.liu.lutu.domain.vo.MessageVO;
import com.liu.lutu.mapper.AiChatSessionMapper;
import com.liu.lutu.service.AiChatservice;
import com.liu.lutu.service.IAiChatMessageService;
import com.liu.lutu.service.ITravelPlanService;
import com.liu.lutu.tools.TravePlanTool;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatservice {

  private final ChatClient chatClient;
  private final ChatMemory chatMemory;
  private final IAiChatMessageService aiChatMessageService;
  private final AiChatMessageServiceImpl aiMessageService;
  private final TravePlanTool travePlanTool;
  private final ITravelPlanService travelPlanService;
  private final AiChatSessionMapper aiChatSessionMapper;
  private final ToolCallingManager toolCallingManager;

  // 存储大模型的生成状态，这里采用ConcurrentHashMap是确保线程安全
  // 目前的版本暂时用Map实现，如果考虑分布式环境的话，可以考虑用redis来实现
  private static final Map<Long, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

  // 大模型输出内容的缓存器，用于在输出中断后的数据存储
  StringBuilder outputBuilder = new StringBuilder();

  @Override
  public Flux<ChatEventVO> chatStream(String question, Long sessionId) {
    // 获取当前用户ID（在切换到boundedElastic线程前获取）
    Long userId = ThreadlocalUtil.getUserId();
    // 获取对话id
    var conversationId = userId.toString() + "_" + sessionId.toString();

    // 构建系统信息（使用SystemMessage传递，不会保存到对话历史）
    String systemInfo = "当前会话ID: " + sessionId + "，调用工具时需要使用此ID。";

    // 使用非流式方式处理工具调用，然后模拟流式输出
    // 这是为了规避 Spring AI M6 流式工具调用的 bug
    return Mono.fromCallable(() -> {
      // 将userId和sessionId存入ThreadLocal，供工具方法使用
      // 必须在Mono.fromCallable内部设置，因为工具调用在这个线程中执行
      ThreadlocalUtil.set(userId);
      ThreadlocalUtil.setSessionId(sessionId);

      // 标记开始生成
      GENERATE_STATUS.put(sessionId, true);
      outputBuilder.setLength(0);

      // 第一轮调用 AI
      // 使用system()传递系统信息，不会保存到对话历史；user()只传用户原始问题
      ChatResponse response = chatClient.prompt()
          .advisors(advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
          .system(systemInfo)
          .tools(travePlanTool)
          .user(question)
          .call()
          .chatResponse();

      // 检查是否有工具调用
      AssistantMessage assistantMessage = response.getResult().getOutput();
      if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
        log.info("检测到工具调用，执行工具...");

        // 执行工具调用
        ToolExecutionResult toolResult = toolCallingManager.executeToolCalls(
            new Prompt(new SystemMessage("")),
            response);

        // 构建包含工具结果的新 prompt
        List<Message> messages = toolResult.conversationHistory();

        // 第二轮调用 AI，获取最终回复
        response = chatClient.prompt()
            .advisors(
                advisor -> advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
            .messages(messages)
            .call()
            .chatResponse();
      }

      // 获取最终文本
      String finalText = response.getResult().getOutput().getText();
      outputBuilder.append(finalText);

      // 清理状态
      GENERATE_STATUS.remove(sessionId);

      return finalText;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(finalText -> {
          // 将完整文本模拟成流式输出
          return Flux.<ChatEventVO>create(sink -> {
            // 按字符或句子分割输出
            String[] sentences = finalText.split("(?<=[。！？.!?\\n])");
            int index = 0;

            for (String sentence : sentences) {
              if (sentence.trim().isEmpty())
                continue;

              sink.next(ChatEventVO.builder()
                  .eventData(sentence)
                  .eventType(ChatEventTypeEnum.DATA.getValue())
                  .build());
              index++;

              // 模拟打字延迟
              try {
                Thread.sleep((long) (Math.random() * 50 + 20));
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
              }

              // 检查是否被取消
              if (!Optional.ofNullable(GENERATE_STATUS.get(sessionId)).orElse(true)) {
                log.info("流式输出被取消");
                break;
              }
            }

            // 发送结束标记
            sink.next(ChatEventVO.builder()
                .eventType(ChatEventTypeEnum.STOP.getValue())
                .build());
            sink.complete();
          });
        })
        .onErrorResume(throwable -> {
          log.error("流式处理失败", throwable);
          GENERATE_STATUS.remove(sessionId);

          String errorMsg = throwable.getMessage();

          // 处理内容安全检测错误
          if (errorMsg != null && errorMsg.contains("data_inspection_failed")) {
            return Flux.just(
                ChatEventVO.builder()
                    .eventData("抱歉，AI 生成的内容触发了安全检测。请尝试换个方式提问。")
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .build(),
                ChatEventVO.builder()
                    .eventType(ChatEventTypeEnum.STOP.getValue())
                    .build());
          }

          // 处理 JSON 解析错误（通常是 AI 生成的参数格式不正确）
          if (errorMsg != null && errorMsg.contains("Conversion from JSON")
              || errorMsg != null && errorMsg.contains("JsonEOFException")) {
            return Flux.just(
                ChatEventVO.builder()
                    .eventData("抱歉，AI 调用工具时参数格式不正确。请提供更明确的指令，例如：\\n'帮我添加一个游玩环节，名称是天安门，时间09:00-12:00'")
                    .eventType(ChatEventTypeEnum.DATA.getValue())
                    .build(),
                ChatEventVO.builder()
                    .eventType(ChatEventTypeEnum.STOP.getValue())
                    .build());
          }

          // 其他错误
          return Flux.just(
              ChatEventVO.builder()
                  .eventData("抱歉，处理过程中出现错误："
                      + (errorMsg != null ? errorMsg.substring(0, Math.min(errorMsg.length(), 100)) : "未知错误"))
                  .eventType(ChatEventTypeEnum.DATA.getValue())
                  .build(),
              ChatEventVO.builder()
                  .eventType(ChatEventTypeEnum.STOP.getValue())
                  .build());
        })
        .doOnCancel(() -> {
          log.info("流式输出被取消");
          GENERATE_STATUS.remove(sessionId);
          this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
        });
  }

  @Override
  public String chat(String question, Long sessionId, Long tripId) {
    // 首先获取用户ID
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      log.error("用户不存在");
      return "用户不存在";
    }

    // 校验 sessionId，防止空参数问题
    if (sessionId == null) {
      log.error("会话ID不能为空");
      return "会话ID不能为空";
    }

    // 获取上一次的5条消息
    List<AiChatMessageVo> chatHistory = aiMessageService.getLimit(sessionId, 10);
    log.info("获取到历史消息: {}", chatHistory);

    // 首先保存用户的问题
    AiChatMessageDTO userMessage = new AiChatMessageDTO();
    userMessage.setSessionId(sessionId);
    userMessage.setRole("user");
    userMessage.setContent(question);
    Result<?> userResult = aiChatMessageService.add(userMessage, userId);
    if (userResult.getCode() != 200) {
      log.error("保存用户消息失败: {}", userResult.getMsg());
    }

    // 构建聊天历史，按照正确的格式
    StringBuilder historyBuilder = new StringBuilder();
    // 反转历史消息顺序，使最早的消息在前
    for (int i = chatHistory.size() - 1; i >= 0; i--) {
      AiChatMessageVo message = chatHistory.get(i);
      historyBuilder.append(message.getRole()).append(": " + message.getContent()).append("\n");
    }
    String historyContent = historyBuilder.toString();
    log.info("构建的聊天历史: {}", historyContent);

    // 构建旅行计划信息（每次对话都注入，确保AI始终知道旅行计划背景）
    String travelPlanInfo = "";
    if (tripId != null) {
      // 获取旅行计划信息
      TravelPlan travelPlan = travelPlanService.getById(tripId);
      if (travelPlan != null) {
        StringBuilder planInfoBuilder = new StringBuilder();
        planInfoBuilder.append("【系统信息 - 旅行计划背景】\n");
        planInfoBuilder.append("用户正在计划一次旅行，基本信息如下：\n");
        planInfoBuilder.append("- 旅行标题：").append(travelPlan.getTitle()).append("\n");
        if (travelPlan.getStartDate() != null) {
          planInfoBuilder.append("- 开始日期：").append(travelPlan.getStartDate()).append("\n");
        }
        if (travelPlan.getEndDate() != null) {
          planInfoBuilder.append("- 结束日期：").append(travelPlan.getEndDate()).append("\n");
        }
        planInfoBuilder.append("\n重要提示：你为用户规划的所有行程环节，时间安排必须在开始日期和结束日期范围内，不能超出这个时间范围。\n");
        planInfoBuilder.append("【系统信息结束】\n\n");
        travelPlanInfo = planInfoBuilder.toString();
        log.info("注入旅行计划信息: {}", travelPlanInfo);
      }
    }

    // 构建包含历史消息、会话ID、旅行ID和旅行计划信息的完整问题
    String fullQuestion = "会话ID: " + sessionId + "\n旅行ID: " + tripId + "\n" + travelPlanInfo + historyContent
        + "\nuser: " + question;
    log.info("完整问题: {}", fullQuestion);

    String aiResponse = chatClient.prompt()
        // 设置会话记忆参数
        .advisors(advisor -> {
          advisor.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId);
        })
        .user(fullQuestion)
        .tools(travePlanTool)
        .call()
        .content();

    log.info("AI响应: {}", aiResponse);

    // 保存AI的完整响应
    try {
      AiChatMessageDTO aiMessage = new AiChatMessageDTO();
      aiMessage.setSessionId(sessionId);
      aiMessage.setRole("assistant");
      aiMessage.setContent(aiResponse);
      Result<?> aiResult = aiChatMessageService.add(aiMessage, userId);
      if (aiResult.getCode() != 200) {
        log.error("保存AI消息失败: {}", aiResult.getMsg());
      }
    } catch (Exception e) {
      log.error("保存聊天记录失败", e);
    }

    return aiResponse;
  }

  /**
   * 停止流式聊天会话
   *
   * @param sessionId 会话ID
   */
  @Override
  public void stop(String sessionId) {
    GENERATE_STATUS.remove(sessionId);
  }

  // 历史消息数量，默认1000条
  public static final int HISTORY_MESSAGE_COUNT = 1000;

  @Override
  public List<MessageVO> queryBySessionId(Long sessionId) {
    // 在访问ThreadLocal之前先获取用户ID（同步方法中获取）
    Long userId = ThreadlocalUtil.getUserId();
    if (userId == null) {
      log.error("用户未登录，无法查询历史消息");
      return List.of();
    }
    String conversationId = userId + "_" + sessionId;
    // 从Redis中获取历史消息
    List<Message> messageList = this.chatMemory.get(conversationId, HISTORY_MESSAGE_COUNT);
    // 过滤并转换消息列表
    return StreamUtil.of(messageList)
        // 过滤掉非用户消息和助手消息
        .filter(message -> message.getMessageType() == MessageType.ASSISTANT
            || message.getMessageType() == MessageType.USER)
        // 转换为MessageVO对象
        .map(message -> MessageVO.builder()
            .content(message.getText())
            .type(MessageTypeEnum.valueOf(message.getMessageType().name()))
            .build())
        .toList();
  }

  /**
   * 保存停止输出的记录
   *
   * @param conversationId 会话id
   * @param content        大模型输出的内容
   */
  private void saveStopHistoryRecord(String conversationId, String content) {
    if (content == null || content.isEmpty()) {
      return;
    }

    try {
      // 构建助手消息
      AssistantMessage assistantMessage = new AssistantMessage(content);
      // 获取历史消息
      List<Message> history = chatMemory.get(conversationId, HISTORY_MESSAGE_COUNT);
      // 添加助手消息到历史
      history.add(assistantMessage);
      // 保存历史消息
      chatMemory.add(conversationId, history);
      log.info("保存停止输出的记录成功");
    } catch (Exception e) {
      log.error("保存停止输出的记录失败", e);
    }
  }
}
