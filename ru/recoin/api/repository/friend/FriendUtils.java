/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 */
package ru.recoin.api.repository.friend;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import ru.recoin.api.repository.friend.Friend;

public final class FriendUtils {
    public static final List<Friend> friends = new ArrayList<Friend>();

    public static void addFriend(class_1657 player) {
        FriendUtils.addFriend(player.method_5477().getString());
    }

    public static void addFriend(String name) {
        friends.add(new Friend(name));
    }

    public static void removeFriend(class_1657 player) {
        FriendUtils.removeFriend(player.method_5477().getString());
    }

    public static void removeFriend(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public static boolean isFriend(class_1297 entity) {
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            return FriendUtils.isFriend(player.method_5477().getString());
        }
        return false;
    }

    public static boolean isFriend(String friend) {
        return friend != null && friends.stream().anyMatch(isFriend -> isFriend.getName().equalsIgnoreCase(friend));
    }

    public static void clear() {
        friends.clear();
    }

    private FriendUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static List<Friend> getFriends() {
        return friends;
    }
}

