import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.blokbahcesi.app',
  appName: 'Blok Bahçesi',
  webDir: 'dist',
  backgroundColor: '#3b2416',
  android: { allowMixedContent: false },
  plugins: {
    // Varsayılan 'css': WebView'a --safe-area-inset-* değişkenlerini enjekte eder (çentik/gezinme çubuğu)
    SystemBars: { insetsHandling: 'css', initialViewportFitValueHint: 'cover', style: 'DARK' },
  },
};

export default config;
