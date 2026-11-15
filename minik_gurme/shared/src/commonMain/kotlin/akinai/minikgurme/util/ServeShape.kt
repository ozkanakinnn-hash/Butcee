package akinai.minikgurme.util

/** Besinin yaşa göre servis şekli; detay ekranında çizimle gösterilir. Metinden otomatik çıkarılır. */
enum class ServeShape(val label: String, val keywords: List<String>) {
    MASH("Ezme / püre", listOf("püre", "ezil", "ezme", "lapa", "çorba", "sos", "bastırıl")),
    STICK("Parmak şerit", listOf("parmak", "şerit", "çubuk", "dilim", "dal", "uzun", "çiçek", "koçan", "sap", "makarna")),
    BITES("Küçük lokma", listOf("küp", "lokma", "küçük parça", "minik", "nohut büyüklüğ", "bezelye büyüklüğ", "parça")),
    SHRED("Rendelenmiş / didiklenmiş", listOf("rende", "didik", "lif")),
    QUARTER("Bölünmüş", listOf("dörde", "ikiye", "bölün", "uzunlamasına", "yarım")),
    POWDER("Öğütülmüş toz", listOf("toz", "öğüt")),
    SPREAD("İnce sürülmüş", listOf("sürül", "sürerek", "ince tabaka")),
    PATTY("Köfte / pankek", listOf("köfte", "mücver", "pankek", "omlet", "muffin", "krep", "topları", "falafel")),
    MINCED("İnce kıyılmış", listOf("kıyılmış", "doğranmış")),
    DISH("Yemeğin içinde", listOf("yemeğ", "yemek", "pilav", "salata", "kuskus", "karıştır", "tabak", "humus", "cacık", "fırın")),
    SPOON("Kaşıkla / bardakta", listOf("kaşık", "kaşığ", "bardak", "puding", "tanecik", "taneler"));

    companion object {
        /** Metinde geçiş sırasına göre en fazla [max] şekil (ör. "parmak dilim veya püre" → STICK, MASH). */
        fun detect(text: String, max: Int = 2): List<ServeShape> {
            val t = text.lowercaseTr()
            if ("önerilmez" in t) return emptyList()   // bu yaşta verilmeyen besin: çizim yok
            return entries.mapNotNull { s -> s.keywords.mapNotNull { k -> t.indexOf(k).takeIf { it >= 0 } }.minOrNull()?.let { s to it } }
                .sortedBy { it.second }.map { it.first }.take(max)
        }
    }
}
