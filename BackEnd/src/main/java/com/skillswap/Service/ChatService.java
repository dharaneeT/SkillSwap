package com.skillSwap.Service;

import com.skillSwap.Dto.chat.*;
import com.skillSwap.Entity.Message;
import com.skillSwap.Entity.User;
import com.skillSwap.Repository.MessageRepository;
import com.skillSwap.Security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public ChatService(
            MessageRepository messageRepository,
            UserService userService,
            CurrentUserService currentUserService
    ) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    public MessageResponseDTO send(MessageRequestDTO dto) {
        User me = currentUserService.getCurrentUser(); // sender is ALWAYS the logged-in user
        if (me.getId().equals(dto.getReceiverId())) {
            throw new IllegalArgumentException("You cannot message yourself");
        }
        User receiver = userService.getUserEntityById(dto.getReceiverId());

        Message m = new Message();
        m.setSender(me);
        m.setReceiver(receiver);
        m.setContent(dto.getContent().trim());
        m.setSentAt(LocalDateTime.now());
        return toDto(messageRepository.save(m));
    }

    public List<MessageResponseDTO> conversation(Integer otherUserId) {
        User me = currentUserService.getCurrentUser();
        return messageRepository.findConversation(me.getId(), otherUserId).stream().map(this::toDto).toList();
    }

    public List<ChatPartnerDTO> partners() {
        User me = currentUserService.getCurrentUser();
        Map<Integer, ChatPartnerDTO> map = new LinkedHashMap<>(); // newest conversation first
        for (Message m : messageRepository.findAllFor(me.getId())) {
            User other = m.getSender().getId().equals(me.getId()) ? m.getReceiver() : m.getSender();
            map.putIfAbsent(other.getId(), new ChatPartnerDTO(other.getId(), other.getName()));
        }
        return new ArrayList<>(map.values());
    }

    private MessageResponseDTO toDto(Message m) {
        return new MessageResponseDTO(
                m.getId(),
                m.getSender().getId(),
                m.getReceiver().getId(),
                m.getContent(),
                m.getSentAt()
        );
    }
}