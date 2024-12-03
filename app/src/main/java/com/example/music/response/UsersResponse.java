package com.example.music.response;

import com.example.music.models.Users;

import java.util.List;

public class UsersResponse {
    private List<Users> users;

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

}
