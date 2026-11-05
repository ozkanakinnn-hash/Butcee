import './ui/styles.css';
import { App } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { setLang } from './i18n';
import { drawBackground } from './render/background';
import { save } from './storage';
import { unlockAudio } from './audio';
import { Router } from './ui/router';
import { menuScreen } from './ui/menu';
import { pgStatus, signIn, submitGardenScore } from './playgames';
import './render/characters';

setLang(save.settings.lang);

const app = document.getElementById('app')!;
const bg = document.createElement('canvas');
bg.id = 'bg';
app.append(bg);

function paintBg() {
  const dpr = Math.min(2, window.devicePixelRatio || 1);
  const w = window.innerWidth, h = window.innerHeight;
  bg.width = w * dpr;
  bg.height = h * dpr;
  const ctx = bg.getContext('2d')!;
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  drawBackground(ctx, w, h, dpr, 0);
}
paintBg();
window.addEventListener('resize', paintBg);

export const router = new Router(app);
router.push(menuScreen(router));

// Android geri tuşu: oyundaysa duraklat, menüdeyse bir önceki ekran.
if (Capacitor.isNativePlatform()) {
  void App.addListener('backButton', () => router.back());
}
window.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' || e.key === 'Backspace') router.back();
});
window.addEventListener('pointerdown', unlockAudio, { once: true });
document.addEventListener('contextmenu', (e) => e.preventDefault());

// Play Games: platform kimliğiyle sessiz giriş (yapılandırılmışsa)
void (async () => {
  const st = await pgStatus();
  if (st === 'signed_out' || st === 'ready') { if (await signIn()) await submitGardenScore(); }
})();
