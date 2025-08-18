package com.example.track.service;

import com.example.track.bot.BotCode;
import com.example.track.entity.Location;
import com.example.track.entity.User;
import com.example.track.repository.LocationRepository;
import com.example.track.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final BotCode botCode;

    public LocationService (LocationRepository locationRepository,UserRepository userRepository,@Lazy BotCode botCode){
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.botCode = botCode;
    }

    @Transactional
    public void saveLocation(Long chatId) {
        User child = userRepository.findById(chatId)
                .orElseThrow(NotFoundException::new);
        locationRepository.save(Location.builder()
                .child(child)
                .lastUpdate(LocalDateTime.now())
                .build());
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    void remindParents() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(7);
        List<Location> outdatedLocations = locationRepository.findAllByLastUpdateBefore(threshold);

        for (Location loc : outdatedLocations) {
            User child = loc.getChild();
            Long parentChatId = child.getParentChatId();
            if (parentChatId != null) {
                botCode.sendMessage(parentChatId,
                        "1 soatdan so'ng: " + child.getChatId() + " bilan aloqa uziladi." +
                                "Iltimos, malumotlarni qayta yuborishi uchun murojaat qiling 📍");
            }
        }
    }
}
