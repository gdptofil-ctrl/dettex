/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import ru.recoin.api.feature.command.datatypes.IDatatypeContext;
import ru.recoin.api.feature.command.datatypes.IDatatypeFor;
import ru.recoin.api.feature.command.exception.CommandException;
import ru.recoin.api.feature.command.helpers.TabCompleteHelper;
import ru.recoin.api.repository.friend.Friend;
import ru.recoin.api.repository.friend.FriendUtils;

public enum FriendDataType implements IDatatypeFor<Friend>
{
    INSTANCE;


    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> friends = this.getFriends().stream().map(Friend::getName);
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(friends).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public Friend get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext.getConsumer().getString();
        return this.getFriends().stream().filter(s -> s.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    private List<? extends Friend> getFriends() {
        return FriendUtils.getFriends();
    }
}

