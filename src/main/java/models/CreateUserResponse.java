package models;

import java.util.List;

public class CreateUserResponse extends BaseModel{
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<String> accounts;
}
