package com.example.data

data class WorldCountry(
  val code: String,
  val name: String,
  val cities: List<String>
)

object LocationsData {

  val countries = listOf(
    WorldCountry(
      code = "TR",
      name = "Türkiye",
      cities = TurkeyProvinces.list.map { it.name }
    ),
    WorldCountry(
      code = "DE",
      name = "Almanya",
      cities = listOf("Berlin", "Köln", "Frankfurt", "Münih", "Hamburg", "Stuttgart", "Düsseldorf", "Dortmund", "Essen", "Hannover", "Bremen", "Nürnberg")
    ),
    WorldCountry(
      code = "NL",
      name = "Hollanda",
      cities = listOf("Amsterdam", "Rotterdam", "Lahey (Den Haag)", "Utrecht", "Eindhoven", "Groningen", "Tilburg", "Almere")
    ),
    WorldCountry(
      code = "FR",
      name = "Fransa",
      cities = listOf("Paris", "Lyon", "Marsilya", "Strazburg", "Bordeaux", "Toulouse", "Nice", "Nantes")
    ),
    WorldCountry(
      code = "AT",
      name = "Avusturya",
      cities = listOf("Viyana", "Graz", "Linz", "Salzburg", "Innsbruck", "Klagenfurt", "Bregenz")
    ),
    WorldCountry(
      code = "BE",
      name = "Belçika",
      cities = listOf("Brüksel", "Anvers (Antwerpen)", "Gent", "Charleroi", "Liege", "Brugge")
    ),
    WorldCountry(
      code = "GB",
      name = "İngiltere (Birleşik Krallık)",
      cities = listOf("Londra", "Birmingham", "Manchester", "Leeds", "Glasgow", "Liverpool", "Sheffield")
    ),
    WorldCountry(
      code = "CH",
      name = "İsviçre",
      cities = listOf("Zürih", "Cenevre", "Basel", "Bern", "Lozan", "Luzern")
    ),
    WorldCountry(
      code = "SE",
      name = "İsveç",
      cities = listOf("Stockholm", "Göteborg", "Malmö", "Uppsala", "Västerås")
    ),
    WorldCountry(
      code = "DK",
      name = "Danimarka",
      cities = listOf("Kopenhag", "Aarhus", "Odense", "Aalborg")
    ),
    WorldCountry(
      code = "NO",
      name = "Norveç",
      cities = listOf("Oslo", "Bergen", "Trondheim", "Stavanger")
    ),
    WorldCountry(
      code = "US",
      name = "Amerika Birleşik Devletleri",
      cities = listOf("New York", "Washington D.C.", "Chicago", "Houston", "Los Angeles", "Paterson", "Boston", "Dallas")
    ),
    WorldCountry(
      code = "CA",
      name = "Kanada",
      cities = listOf("Toronto", "Montreal", "Vancouver", "Ottawa", "Calgary", "Edmonton")
    ),
    WorldCountry(
      code = "AU",
      name = "Avustralya",
      cities = listOf("Sidney", "Melbourne", "Brisbane", "Perth", "Adelaide")
    ),
    WorldCountry(
      code = "AZ",
      name = "Azerbaycan",
      cities = listOf("Bakü", "Gence", "Sumgayıt", "Mingeçevir", "Şeki", "Lenkeran")
    ),
    WorldCountry(
      code = "SA",
      name = "Suudi Arabistan",
      cities = listOf("Mekke", "Medine", "Riyad", "Cidde", "Dammam")
    ),
    WorldCountry(
      code = "BA",
      name = "Bosna-Hersek",
      cities = listOf("Saraybosna", "Mostar", "Tuzla", "Zenica", "Banja Luka")
    ),
    WorldCountry(
      code = "CY",
      name = "Kuzey Kıbrıs (KKTC)",
      cities = listOf("Lefkoşa", "Gazimağusa", "Girne", "Güzelyurt", "İskele")
    )
  )
}
