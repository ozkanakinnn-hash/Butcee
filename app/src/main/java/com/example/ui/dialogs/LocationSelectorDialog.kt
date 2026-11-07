package com.example.ui.dialogs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.LocationsData
import com.example.data.TurkeyProvinces
import com.example.data.WorldCountry
import java.util.Locale

@Composable
fun LocationSelectorDialog(
  currentCountry: String = "Türkiye",
  currentCity: String,
  currentDistrict: String,
  onLocationSelected: (country: String, city: String, district: String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }

  var selectedCountry by remember {
    mutableStateOf(LocationsData.countries.find { it.name.equals(currentCountry, ignoreCase = true) } ?: LocationsData.countries.first())
  }
  var selectedCity by remember { mutableStateOf(currentCity) }
  var selectedDistrict by remember { mutableStateOf(currentDistrict) }

  // Step navigation: 0 = Choose Country, 1 = Choose City/Province, 2 = Choose District (if Turkey)
  var selectionStep by remember { mutableStateOf(1) } // Default to city selection

  // Permission launcher for optional GPS location
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    if (fineGranted || coarseGranted) {
      tryGetGpsLocation(context) { detectedCountry, detectedCity, detectedDistrict ->
        if (detectedCity != null) {
          onLocationSelected(detectedCountry ?: "Türkiye", detectedCity, detectedDistrict ?: "Merkez")
          Toast.makeText(context, "Konumunuz tespit edildi: $detectedCity", Toast.LENGTH_SHORT).show()
          onDismiss()
        } else {
          Toast.makeText(context, "Konumdan şehir tespit edilemedi, lütfen listeden seçin.", Toast.LENGTH_SHORT).show()
        }
      }
    } else {
      Toast.makeText(context, "Konum izni verilmedi. Manuel olarak seçebilirsiniz.", Toast.LENGTH_SHORT).show()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("location_selector_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = when (selectionStep) {
                0 -> "Ülke Seçin"
                2 -> "${selectedCity} İlçesi Seçin"
                else -> "${selectedCountry.name} — Şehir Seçin"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Diyanet destekli tüm dünya şehirleri",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step breadcrumb / Country toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedButton(
            onClick = {
              if (selectionStep > 0) selectionStep = 0 else selectionStep = 1
            },
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (selectionStep == 0) "Şehirlere Dön" else "Ülke: ${selectedCountry.name} ▾",
              fontSize = 12.sp
            )
          }

          if (selectionStep == 2) {
            OutlinedButton(
              onClick = { selectionStep = 1 },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("‹ İl Değiştir", fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // GPS Quick Option
        OutlinedButton(
          onClick = {
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            if (fine == PackageManager.PERMISSION_GRANTED) {
              tryGetGpsLocation(context) { detectedCountry, detectedCity, detectedDistrict ->
                if (detectedCity != null) {
                  onLocationSelected(detectedCountry ?: "Türkiye", detectedCity, detectedDistrict ?: "Merkez")
                  Toast.makeText(context, "Konumunuz: $detectedCity", Toast.LENGTH_SHORT).show()
                  onDismiss()
                } else {
                  Toast.makeText(context, "Konum alınamadı, lütfen listeden seçin.", Toast.LENGTH_SHORT).show()
                }
              }
            } else {
              locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
              )
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("use_gps_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Cihaz Konumunu Kullan (İsteğe Bağlı GPS)", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              when (selectionStep) {
                0 -> "Ülke ara (Örn: Almanya, Fransa...)"
                2 -> "İlçe ara..."
                else -> "Şehir ara (Örn: İstanbul, Berlin, Paris...)"
              }
            )
          },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_location_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Lists based on step
        when (selectionStep) {
          0 -> {
            // Country List
            val filteredCountries = remember(searchQuery) {
              if (searchQuery.isBlank()) LocationsData.countries
              else LocationsData.countries.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
              items(filteredCountries) { country ->
                val isSelected = country.code == selectedCountry.code
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable {
                      selectedCountry = country
                      selectedCity = country.cities.firstOrNull() ?: "Merkez"
                      selectionStep = 1
                      searchQuery = ""
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = country.name, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                  }
                  Text(text = "${country.cities.size} Şehir ›", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }
          1 -> {
            // City List
            if (selectedCountry.code == "TR") {
              // 81 Provinces of Turkey
              val filteredProvinces = remember(searchQuery) {
                if (searchQuery.isBlank()) TurkeyProvinces.list
                else TurkeyProvinces.list.filter {
                  it.name.contains(searchQuery, ignoreCase = true) || it.districts.any { d -> d.contains(searchQuery, ignoreCase = true) }
                }
              }
              LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(filteredProvinces) { province ->
                  val isSelected = province.name.equals(selectedCity, ignoreCase = true)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                      .clickable {
                        selectedCity = province.name
                        selectedDistrict = province.districts.firstOrNull() ?: "Merkez"
                        selectionStep = 2
                        searchQuery = ""
                      }
                      .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(text = "${province.id}. ${province.name}", fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    }
                    Text(text = "${province.districts.size} İlçe ›", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                  }
                }
              }
            } else {
              // World cities of selected country
              val filteredCities = remember(searchQuery, selectedCountry) {
                if (searchQuery.isBlank()) selectedCountry.cities
                else selectedCountry.cities.filter { it.contains(searchQuery, ignoreCase = true) }
              }
              LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(filteredCities) { city ->
                  val isSelected = city.equals(selectedCity, ignoreCase = true)
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                      .clickable {
                        selectedCity = city
                        selectedDistrict = "Merkez"
                        onLocationSelected(selectedCountry.name, city, "Merkez")
                        Toast.makeText(context, "${selectedCountry.name} / $city seçildi", Toast.LENGTH_SHORT).show()
                        onDismiss()
                      }
                      .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(text = city, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    }
                    if (isSelected) {
                      Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          }
          2 -> {
            // District Selection for Turkey
            val currentProvinceObj = TurkeyProvinces.list.find { it.name.equals(selectedCity, ignoreCase = true) } ?: TurkeyProvinces.list.first()
            val filteredDistricts = remember(searchQuery) {
              if (searchQuery.isBlank()) currentProvinceObj.districts
              else currentProvinceObj.districts.filter { it.contains(searchQuery, ignoreCase = true) }
            }
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
              items(filteredDistricts) { district ->
                val isSelected = district.equals(selectedDistrict, ignoreCase = true)
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable {
                      selectedDistrict = district
                      onLocationSelected("Türkiye", selectedCity, district)
                      Toast.makeText(context, "$selectedCity / $district seçildi", Toast.LENGTH_SHORT).show()
                      onDismiss()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = district, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                  if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun tryGetGpsLocation(context: Context, callback: (country: String?, city: String?, district: String?) -> Unit) {
  try {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
      ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

    if (lastKnown != null && Geocoder.isPresent()) {
      val geocoder = Geocoder(context, Locale("tr", "TR"))
      @Suppress("DEPRECATION")
      val addresses = geocoder.getFromLocation(lastKnown.latitude, lastKnown.longitude, 1)
      if (!addresses.isNullOrEmpty()) {
        val address = addresses[0]
        val country = address.countryName ?: "Türkiye"
        val city = address.adminArea ?: address.locality // e.g. "İstanbul", "Berlin"
        val district = address.subAdminArea ?: address.subLocality ?: "Merkez"
        callback(country, city, district)
        return
      }
    }
  } catch (e: Exception) {
    // Ignore location errors
  }
  callback(null, null, null)
}
