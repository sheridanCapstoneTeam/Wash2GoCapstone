package project.sheridancollege.wash2goproject.common


data class User (val userId: String="",
                 val firstName: String="",
                 var lastName: String="",
                 var email: String="",
                 var streetNum: String="",
                 var streetName: String="",
                 var city: String="",
                 var phone: String="",
                 var isProvider: Boolean=false)
