import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:geocoding/geocoding.dart';
import 'package:geolocator/geolocator.dart';
import 'package:http/http.dart' as http;

/// Anlık hava durumu.
class Weather {
  const Weather({required this.place, required this.temperature, required this.symbol});

  final String place;
  final double temperature;

  /// MET Norway sembol kodu (ör. "partlycloudy_day", "rain").
  final String symbol;

  String get baseSymbol => symbol.split('_').first;
}

/// Konum (kaba, il düzeyi) + MET Norway (api.met.no) hava durumu.
///
/// MET Norway API ücretsizdir, ticari kullanıma açıktır (CC BY 4.0, kaynak gösterilmeli) ve
/// tanımlayıcı bir User-Agent ister. Kullanım koşulları: https://api.met.no/doc/TermsOfService
class WeatherService {
  WeatherService({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;
  Weather? _cache;
  DateTime? _cachedAt;

  static const _userAgent = 'HaberAkisi/1.0 github.com/ozkanakinnn-hash/Butcee';

  /// Konum izni durumu (yalnızca okur, istemez).
  static Future<LocationPermission> permission() => Geolocator.checkPermission();

  /// İzin ister. Kalıcı reddedildiyse uygulama ayarlarını açar.
  static Future<LocationPermission> requestPermission() async {
    var p = await Geolocator.checkPermission();
    if (p == LocationPermission.deniedForever) {
      await Geolocator.openAppSettings();
      return p;
    }
    if (p == LocationPermission.denied) p = await Geolocator.requestPermission();
    return p;
  }

  static bool isGranted(LocationPermission p) => p == LocationPermission.always || p == LocationPermission.whileInUse;

  /// Hava durumunu getirir (30 dk önbellek). İzin yoksa veya konum servisi kapalıysa null.
  Future<Weather?> current({Locale? locale}) async {
    if (_cache != null && DateTime.now().difference(_cachedAt!) < const Duration(minutes: 30)) return _cache;
    if (!isGranted(await Geolocator.checkPermission())) return null;
    if (!await Geolocator.isLocationServiceEnabled()) return null;

    final pos =
        await Geolocator.getLastKnownPosition() ??
        await Geolocator.getCurrentPosition(
          locationSettings: const LocationSettings(accuracy: LocationAccuracy.low, timeLimit: Duration(seconds: 15)),
        );

    var place = '';
    try {
      final marks = await Geocoding(locale: locale).placemarkFromCoordinates(pos.latitude, pos.longitude);
      if (marks.isNotEmpty) {
        final m = marks.first;
        // Türkiye'de administrativeArea = il, subAdministrativeArea = ilçe
        place = [
          m.administrativeArea,
          m.locality,
          m.subAdministrativeArea,
        ].firstWhere((e) => e != null && e.isNotEmpty, orElse: () => '')!;
      }
    } catch (_) {}

    // MET Norway en fazla 4 ondalık basamak ister.
    final uri = Uri.https('api.met.no', '/weatherapi/locationforecast/2.0/compact', {
      'lat': pos.latitude.toStringAsFixed(3),
      'lon': pos.longitude.toStringAsFixed(3),
    });
    final res = await _client.get(uri, headers: {'User-Agent': _userAgent});
    if (res.statusCode != 200) return _cache;
    final w = parseMetNorway(jsonDecode(res.body) as Map<String, dynamic>, place);
    if (w != null) {
      _cache = w;
      _cachedAt = DateTime.now();
    }
    return w;
  }

  /// MET Norway "compact" yanıtından anlık değerleri çıkarır (test edilir).
  static Weather? parseMetNorway(Map<String, dynamic> json, String place) {
    final series = (json['properties']?['timeseries'] as List?) ?? const [];
    if (series.isEmpty) return null;
    final data = series.first['data'] as Map<String, dynamic>;
    final temp = (data['instant']?['details']?['air_temperature'] as num?)?.toDouble();
    final symbol = (data['next_1_hours'] ?? data['next_6_hours'])?['summary']?['symbol_code'] as String? ?? 'cloudy';
    if (temp == null) return null;
    return Weather(place: place, temperature: temp, symbol: symbol);
  }
}

/// MET sembolü → ikon ve Türkçe/İngilizce açıklama.
({IconData icon, String tr, String en}) describeWeather(String baseSymbol, {bool night = false}) {
  if (baseSymbol.contains('thunder')) return (icon: Icons.thunderstorm_outlined, tr: 'Gök gürültülü', en: 'Thunder');
  if (baseSymbol.contains('sleet')) return (icon: Icons.ac_unit, tr: 'Karla karışık yağmur', en: 'Sleet');
  if (baseSymbol.contains('snow')) return (icon: Icons.ac_unit, tr: 'Kar yağışlı', en: 'Snow');
  if (baseSymbol.contains('rain')) {
    return baseSymbol.contains('light')
        ? (icon: Icons.grain, tr: 'Hafif yağmurlu', en: 'Light rain')
        : (icon: Icons.water_drop_outlined, tr: 'Yağmurlu', en: 'Rain');
  }
  switch (baseSymbol) {
    case 'clearsky':
      return night
          ? (icon: Icons.nightlight_outlined, tr: 'Açık', en: 'Clear')
          : (icon: Icons.wb_sunny_outlined, tr: 'Güneşli', en: 'Sunny');
    case 'fair':
      return (icon: Icons.wb_sunny_outlined, tr: 'Az bulutlu', en: 'Fair');
    case 'partlycloudy':
      return (icon: Icons.wb_cloudy_outlined, tr: 'Parçalı bulutlu', en: 'Partly cloudy');
    case 'fog':
      return (icon: Icons.foggy, tr: 'Sisli', en: 'Fog');
    default:
      return (icon: Icons.cloud_outlined, tr: 'Bulutlu', en: 'Cloudy');
  }
}
