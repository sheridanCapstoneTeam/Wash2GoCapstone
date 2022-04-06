package project.sheridancollege.wash2goproject

class ProviderLocation {

    public var lat: Double = 0.0
    public var lng : Double = 0.0


    constructor(lat: Double?, lng: Double?){
        if (lat != null) {
            this.lat = lat
        }
        if (lng != null) {
            this.lng = lng
        }

    }
}