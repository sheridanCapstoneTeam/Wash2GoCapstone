package project.sheridancollege.wash2goproject


data class User (val UserId: String,
                 val FirstName: String,
                 var LastName: String,
                 var Email: String,
                 var StreetNum: String,
                 var StreetName: String,
                 var City: String,
                 var Phone: String,
                 var IsProvider: Boolean)
