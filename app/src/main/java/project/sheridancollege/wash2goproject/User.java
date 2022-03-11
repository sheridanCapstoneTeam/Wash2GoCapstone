package project.sheridancollege.wash2goproject;

public class User {
    private int UserId;
    private String FirstName;
    private String LastName;
    private String Email;
    private int StreetNum;
    private String StreetName;
    private String City;
    private long Phone;
    private Boolean IsProvider;

    public User(int userId, String firstName, String lastName, String email, int streetNum, String streetName, String city, long phone, Boolean isProvider) {
        UserId = userId;
        FirstName = firstName;
        LastName = lastName;
        Email = email;
        StreetNum = streetNum;
        StreetName = streetName;
        City = city;
        Phone = phone;
        IsProvider = isProvider;
    }
}
