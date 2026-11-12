@file:OptIn(InternalResourceApi::class)

package akinai.minikgurme.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/akinai.minikgurme.resources/"

public val Res.font.plus_jakarta_sans_bold: FontResource by lazy {
      FontResource("font:plus_jakarta_sans_bold", setOf(
        ResourceItem(setOf(), "${MD}font/plus_jakarta_sans_bold.ttf", -1, -1),
      ))
    }

public val Res.font.plus_jakarta_sans_extrabold: FontResource by lazy {
      FontResource("font:plus_jakarta_sans_extrabold", setOf(
        ResourceItem(setOf(), "${MD}font/plus_jakarta_sans_extrabold.ttf", -1, -1),
      ))
    }

public val Res.font.plus_jakarta_sans_medium: FontResource by lazy {
      FontResource("font:plus_jakarta_sans_medium", setOf(
        ResourceItem(setOf(), "${MD}font/plus_jakarta_sans_medium.ttf", -1, -1),
      ))
    }

public val Res.font.plus_jakarta_sans_regular: FontResource by lazy {
      FontResource("font:plus_jakarta_sans_regular", setOf(
        ResourceItem(setOf(), "${MD}font/plus_jakarta_sans_regular.ttf", -1, -1),
      ))
    }

public val Res.font.plus_jakarta_sans_semibold: FontResource by lazy {
      FontResource("font:plus_jakarta_sans_semibold", setOf(
        ResourceItem(setOf(), "${MD}font/plus_jakarta_sans_semibold.ttf", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainFont0Resources(map: MutableMap<String, FontResource>) {
  map.put("plus_jakarta_sans_bold", Res.font.plus_jakarta_sans_bold)
  map.put("plus_jakarta_sans_extrabold", Res.font.plus_jakarta_sans_extrabold)
  map.put("plus_jakarta_sans_medium", Res.font.plus_jakarta_sans_medium)
  map.put("plus_jakarta_sans_regular", Res.font.plus_jakarta_sans_regular)
  map.put("plus_jakarta_sans_semibold", Res.font.plus_jakarta_sans_semibold)
}
