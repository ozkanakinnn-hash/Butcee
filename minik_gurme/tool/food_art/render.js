const { chromium } = require('playwright'); const fs=require('fs');
(async () => { const A=JSON.parse(fs.readFileSync('art.json')); const N=JSON.parse(fs.readFileSync('names.json'));
 const b=await chromium.launch(); const p=await b.newPage({viewport:{width:144,height:144}});
 for (const [k,svg] of Object.entries(A)) {
   await p.setContent(`<html><body style="margin:0;background:transparent">${svg.replace('<svg ','<svg width="144" height="144" ')}</body></html>`);
   await p.screenshot({path:`png/${N[k]}.png`, omitBackground:true, clip:{x:0,y:0,width:144,height:144}});
 } await b.close(); })();
