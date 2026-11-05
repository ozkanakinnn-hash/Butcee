// Ödüllü reklam ("reklam izle, elmas kazan"). Android'de AdMob; tarayıcıda simülasyon.
import { Capacitor } from '@capacitor/core';
import { AdMob, AdmobConsentStatus, MaxAdContentRating } from '@capacitor-community/admob';

/** Google'ın resmi TEST ödüllü reklam kimliği. Yayında VITE_ADMOB_REWARDED_ID ile değiştirin. */
const TEST_REWARDED = 'ca-app-pub-3940256099942544/5224354917';
const REWARDED_ID: string = (import.meta.env.VITE_ADMOB_REWARDED_ID as string | undefined) || TEST_REWARDED;
export const ADS_ARE_TEST = REWARDED_ID === TEST_REWARDED;

const native = Capacitor.isNativePlatform();
let init: Promise<boolean> | null = null;

/** AdMob + kullanıcı rızası (UMP). Rıza alınamazsa reklam istenmez. */
export function initAds(): Promise<boolean> {
  if (!native) return Promise.resolve(false);
  init ??= (async () => {
    try {
      await AdMob.initialize({
        initializeForTesting: ADS_ARE_TEST,
        maxAdContentRating: MaxAdContentRating.General,
      });
      const info = await AdMob.requestConsentInfo();
      if (info.status === AdmobConsentStatus.REQUIRED && info.isConsentFormAvailable) {
        const after = await AdMob.showConsentForm();
        return after.canRequestAds;
      }
      return info.canRequestAds ?? true;
    } catch {
      return false;
    }
  })();
  return init;
}

export type AdResult = 'rewarded' | 'skipped' | 'failed' | 'simulate';

/** Ödüllü reklamı gösterir. Tarayıcıda 'simulate' döner (arayüz geri sayım gösterir). */
export async function showRewardedAd(): Promise<AdResult> {
  if (!native) return 'simulate';
  try {
    const ok = await initAds();
    if (!ok) return 'failed';
    await AdMob.prepareRewardVideoAd({ adId: REWARDED_ID, isTesting: ADS_ARE_TEST });
    const reward = await AdMob.showRewardVideoAd();
    return reward && (reward.amount ?? 1) > 0 ? 'rewarded' : 'skipped';
  } catch {
    return 'failed';
  }
}
