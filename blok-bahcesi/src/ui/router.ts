import { App } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';

export interface Screen {
  el: HTMLElement;
  /** true dönerse geri tuşu ekran tarafından işlendi (ör. duraklat menüsü açıldı). */
  onBack?: () => boolean;
  onShow?: () => void;
  onHide?: () => void;
  destroy?: () => void;
}

export class Router {
  private stack: Screen[] = [];
  constructor(private root: HTMLElement) {}

  get top(): Screen | undefined { return this.stack[this.stack.length - 1]; }

  push(s: Screen) {
    this.top?.onHide?.();
    if (this.top) this.top.el.style.display = 'none';
    this.stack.push(s);
    this.root.append(s.el);
    s.onShow?.();
  }

  replace(s: Screen) {
    const old = this.stack.pop();
    if (old) { old.destroy?.(); old.el.remove(); }
    this.stack.push(s);
    this.root.append(s.el);
    s.onShow?.();
  }

  pop() {
    if (this.stack.length <= 1) return;
    const old = this.stack.pop()!;
    old.destroy?.();
    old.el.remove();
    const t = this.top!;
    t.el.style.display = '';
    t.onShow?.();
  }

  /** Kök ekran hariç her şeyi kapat. */
  popToRoot() {
    while (this.stack.length > 1) this.pop();
  }

  /** Android geri tuşu / Escape */
  back() {
    const t = this.top;
    if (t?.onBack?.()) return;
    if (this.stack.length > 1) this.pop();
    else if (Capacitor.isNativePlatform()) void App.exitApp();
  }
}
