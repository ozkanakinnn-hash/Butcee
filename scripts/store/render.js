const { chromium } = require('playwright');
const jobs = require('/tmp/claude-0/promo/jobs.json');
(async () => {
  const b = await chromium.launch();
  for (const [html, out, w, h] of jobs) {
    const p = await b.newPage({ viewport: { width: w, height: h } });
    await p.goto('file://' + html); await p.waitForTimeout(500);
    await p.screenshot({ path: out }); await p.close(); console.log(out);
  }
  await b.close();
})();
