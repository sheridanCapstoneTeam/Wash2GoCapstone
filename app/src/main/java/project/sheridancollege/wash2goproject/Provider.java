package project.sheridancollege.wash2goproject;

public class Provider {
    private int Id;
    private String FirstName;
    private String LastName;
    private String Email;
    private int StreetNum;
    private String StreetName;
    private String PostalCode;
    private String City;
    private long PhoneNum;

    public Provider(int id, String firstName, String lastName, String email, int streetNum, String streetName, String postalCode, String city, long phoneNum) {
        this.Id = id;
        FirstName = firstName;
        LastName = lastName;
        Email = email;
        StreetNum = streetNum;
        StreetName = streetName;
        PostalCode = postalCode;
        City = city;
        PhoneNum = phoneNum;
    }
}
