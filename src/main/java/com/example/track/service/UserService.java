package com.example.track.service;

import com.example.track.entity.Role;
import com.example.track.entity.User;
import com.example.track.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Value("${join.link}")
    private String link;

    @Transactional
    public String saveParent(Long chatId) {
        if (userRepository.existsById(chatId)) return "Siz allaqachon tizimdasiz ✅";
        userRepository.save(User.builder()
                .chatId(chatId)
                .role(Role.PARENT)
                .build());
        return "Assalomu alaykum! Siz ota-ona sifatida ro‘yxatdan o‘tdingiz 👨‍👩‍👧";
    }

    @Transactional
    public void addChild(Long parentChatId, Long childChatId) {
        userRepository.save(User.builder()
                .chatId(childChatId)
                .parentChatId(parentChatId)
                .role(Role.CHILD)
                .build());
    }

    public String generateLink(Long chatId){
        return link + chatId;
    }

    public Long findParent(Long childChatId) {
        return userRepository.findById(childChatId)
                .map(User::getParentChatId)
                .orElse(null);
    }

    public long countAllUsers(){
        return userRepository.count();
    }

    public long countByRole(Role role){
        return userRepository.countByRole(role);
    }
}
