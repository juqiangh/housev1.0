package com.learning.house.web.interceptor;

import com.learning.house.common.model.User;

public class UserContext {

    public static final ThreadLocal<User> USER_HODLER = new ThreadLocal<>();

    public static User getUser() {
        return USER_HODLER.get();
    }

    public static void setUser(User user) {
        USER_HODLER.set(user);
    }

    public static void remove () {
        USER_HODLER.remove();
    }
}
