package fitsnap.profile_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fitsnap.profile_service.entity.Profile;
import fitsnap.profile_service.event.UserCreatedEvent;
import fitsnap.profile_service.repository.ProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileCreationConsumer {
    ProfileRepository profileRepository;

    @KafkaListener(
            topics = "profile-creation",
            groupId = "profile-service-group",
            properties = {"spring.json.value.default.type=fitsnap.profile_service.event.UserCreatedEvent"})
    public void listen(UserCreatedEvent event) {
        log.info("Received profile-creation event for userId: {}", event.getUserId());
        try {
            if (profileRepository.findByUserId(event.getUserId()).isPresent()) {
                log.info("Profile already exists for userId: {}", event.getUserId());
                return;
            }

            Profile profile = Profile.builder()
                    .userId(event.getUserId())
                    .firstname(event.getFirstname())
                    .lastname(event.getLastname())
                    .dateOfBirth(event.getDateOfBirth())
                    .height(event.getHeight())
                    .weight(event.getWeight())
                    .gender(event.getGender())
                    .age(event.getAge())
                    .job(event.getJob())
                    .styles(event.getStyles())
                    .colors(event.getColors())
                    .purposes(event.getPurposes())
                    .build();

            profileRepository.save(profile);
            log.info("Profile created successfully for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to create profile for userId: {}. Error: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
