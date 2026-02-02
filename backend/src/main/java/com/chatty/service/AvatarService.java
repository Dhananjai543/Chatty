package com.chatty.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AvatarService {

    private static final String USER_AVATARS_PATH = "classpath:static/avatars/users/*";
    private static final String GROUP_AVATARS_PATH = "classpath:static/avatars/groups/*";
    private static final String AVATAR_URL_PREFIX = "/avatars";
    private static final String[] SUPPORTED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif", ".webp"};

    private final List<String> userAvatars = new ArrayList<>();
    private final List<String> groupAvatars = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        loadAvatars();
    }

    private void loadAvatars() {
        userAvatars.clear();
        groupAvatars.clear();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Load user avatars
        try {
            Resource[] userResources = resolver.getResources(USER_AVATARS_PATH);
            for (Resource resource : userResources) {
                String filename = resource.getFilename();
                if (filename != null && isSupportedImage(filename)) {
                    userAvatars.add(AVATAR_URL_PREFIX + "/users/" + filename);
                }
            }
            log.info("Loaded {} user avatars", userAvatars.size());
        } catch (IOException e) {
            log.warn("Could not load user avatars: {}", e.getMessage());
        }

        // Load group avatars
        try {
            Resource[] groupResources = resolver.getResources(GROUP_AVATARS_PATH);
            for (Resource resource : groupResources) {
                String filename = resource.getFilename();
                if (filename != null && isSupportedImage(filename)) {
                    groupAvatars.add(AVATAR_URL_PREFIX + "/groups/" + filename);
                }
            }
            log.info("Loaded {} group avatars", groupAvatars.size());
        } catch (IOException e) {
            log.warn("Could not load group avatars: {}", e.getMessage());
        }
    }

    private boolean isSupportedImage(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public String getRandomUserAvatar() {
        if (userAvatars.isEmpty()) {
            log.debug("No user avatars available");
            return null;
        }
        return userAvatars.get(random.nextInt(userAvatars.size()));
    }

    public String getRandomGroupAvatar() {
        if (groupAvatars.isEmpty()) {
            log.debug("No group avatars available");
            return null;
        }
        return groupAvatars.get(random.nextInt(groupAvatars.size()));
    }

    public void reloadAvatars() {
        loadAvatars();
    }

    public int getUserAvatarCount() {
        return userAvatars.size();
    }

    public int getGroupAvatarCount() {
        return groupAvatars.size();
    }
}
