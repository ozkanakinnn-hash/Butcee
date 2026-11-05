// OTOMATİK ÜRETİLDİ: scripts/calibrate.ts (bot kalibrasyonu). Elle düzenlemeyin.
import type { LevelDef } from './game/types';

export const CALIBRATED: LevelDef[] = [
 {
  "id": 1,
  "goal": {
   "type": "lines",
   "count": 4
  },
  "moves": 12,
  "startHand": [
   "O",
   "O",
   "I",
   "O",
   "L"
  ],
  "hints": [
   "hint.select",
   "hint.drop"
  ],
  "map": [
   "TTTT....",
   "TTTT....",
   "TTTTTT..",
   "TTTTTT.."
  ]
 },
 {
  "id": 2,
  "goal": {
   "type": "animals"
  },
  "moves": 12,
  "startHand": [
   "L3",
   "I",
   "T",
   "O",
   "J"
  ],
  "hints": [
   "hint.rotate",
   "hint.animals"
  ],
  "map": [
   "TTT.....",
   "TTTTHT..",
   "TTHTTTT."
  ]
 },
 {
  "id": 3,
  "goal": {
   "type": "seeds",
   "count": 2
  },
  "moves": 12,
  "startHand": [
   "D2",
   "L3",
   "O",
   "I",
   "T"
  ],
  "colors": {
   "water": 25
  },
  "hints": [
   "hint.water",
   "hint.undo"
  ],
  "map": [
   "TT......",
   "bbbbbb..",
   "TSTTTST."
  ]
 },
 {
  "id": 4,
  "goal": {
   "type": "lines",
   "count": 6
  },
  "moves": 14,
  "hints": [
   "hint.combo"
  ],
  "map": [
   "T.......",
   "TT.....T",
   "TTT...TT",
   "TTTT.TTT",
   "TTTT.TTT"
  ]
 },
 {
  "id": 5,
  "goal": {
   "type": "animals"
  },
  "moves": 8,
  "hints": [
   "hint.rock"
  ],
  "map": [
   "K.......",
   "TT....TT",
   "THT..TPT",
   "TTTU.TTK"
  ]
 },
 {
  "id": 6,
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 10,
  "colors": {
   "water": 20
  },
  "map": [
   "T.....TS",
   "ST...TTT",
   "TST.TTST"
  ]
 },
 {
  "id": 7,
  "goal": {
   "type": "lines",
   "count": 6
  },
  "moves": 11,
  "colors": {
   "sun": 20
  },
  "hints": [
   "hint.sun"
  ],
  "map": [
   "T.....TT",
   "TT...TTT",
   "TTO.OTTT",
   "TOT.TOTT"
  ]
 },
 {
  "id": 8,
  "goal": {
   "type": "animals"
  },
  "moves": 22,
  "map": [
   ".K....T.",
   ".TT..HT.",
   "TTP..TTU",
   "THTT.TTK"
  ]
 },
 {
  "id": 9,
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 13,
  "colors": {
   "water": 16,
   "sun": 14
  },
  "hints": [
   "hint.ice"
  ],
  "map": [
   "ST....TS",
   "TIS..TTI",
   "TSTI.TST"
  ]
 },
 {
  "id": 10,
  "goal": {
   "type": "boss"
  },
  "moves": 23,
  "bossHp": 10,
  "colors": {
   "stone": 10
  },
  "hints": [
   "hint.boss",
   "hint.stone"
  ],
  "map": [
   "TT...TT.",
   "TTT.TTTT"
  ]
 },
 {
  "id": 11,
  "goal": {
   "type": "lines",
   "count": 10
  },
  "moves": 20,
  "map": [
   "T......T",
   "TTp....T",
   "TTT...yT",
   "TbTT..TT",
   "TTTT.TrT"
  ]
 },
 {
  "id": 12,
  "goal": {
   "type": "lines",
   "count": 6
  },
  "moves": 13,
  "colors": {
   "leaf": 32
  },
  "hints": [
   "hint.leaf"
  ],
  "map": [
   "g.......",
   "gggg..gg",
   "ggggg.gg"
  ]
 },
 {
  "id": 13,
  "goal": {
   "type": "animals"
  },
  "moves": 9,
  "colors": {
   "sun": 14
  },
  "map": [
   "H.....T.",
   "TOT...TT",
   "TTP..HTU",
   "THTO.TKT"
  ]
 },
 {
  "id": 14,
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 9,
  "colors": {
   "water": 16
  },
  "map": [
   "T......S",
   "SKT...TS",
   "TSTT.STK"
  ]
 },
 {
  "id": 15,
  "goal": {
   "type": "lines",
   "count": 12
  },
  "moves": 24,
  "colors": {
   "sun": 14
  },
  "map": [
   "TT....TT",
   "TTO..OTT",
   "TTT..TTT",
   "IT.TT.TI",
   "TTTT.TTT"
  ]
 },
 {
  "id": 16,
  "goal": {
   "type": "animals"
  },
  "moves": 8,
  "map": [
   "K......K",
   "TU....HT",
   "TTP..TTT",
   "THT..PTT",
   "TTTU.TTH"
  ]
 },
 {
  "id": 17,
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 11,
  "colors": {
   "water": 16,
   "sun": 14
  },
  "map": [
   "S.....IS",
   "TO...TTT",
   "TSI..STO",
   "TTTS.TTT"
  ]
 },
 {
  "id": 18,
  "goal": {
   "type": "lines",
   "count": 14
  },
  "moves": 32,
  "colors": {
   "leaf": 22
  },
  "map": [
   "g......g",
   "gg....gg",
   "TTT..TTT",
   "TKTT.TKT",
   "TTTT.TTT"
  ]
 },
 {
  "id": 19,
  "goal": {
   "type": "animals"
  },
  "moves": 32,
  "colors": {
   "sun": 14
  },
  "map": [
   "TK...KOU",
   "TTP..TTT",
   "HOTH.TPT",
   "TTTT.KTT"
  ]
 },
 {
  "id": 20,
  "goal": {
   "type": "boss"
  },
  "moves": 50,
  "bossHp": 14,
  "colors": {
   "stone": 10,
   "leaf": 13
  },
  "hints": [
   "hint.boss2"
  ],
  "map": [
   "K......K",
   "TT....TT",
   "TTT..TTT",
   "TTTT.TTT"
  ]
 },
 {
  "id": 21,
  "map": [
   "..K..T..",
   "T.TTTT..",
   "TTTTTTT."
  ],
  "goal": {
   "type": "lines",
   "count": 8
  },
  "moves": 23,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 22,
  "map": [
   ".PT.....",
   "TKHT.TTU"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 14
 },
 {
  "id": 23,
  "map": [
   ".ST...T.",
   ".SST..ST",
   "TTTT.TKT"
  ],
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 12,
  "colors": {
   "water": 20,
   "sun": 12
  }
 },
 {
  "id": 24,
  "map": [
   ".T.T....",
   "TTTTT...",
   "TT.TTTKT"
  ],
  "goal": {
   "type": "lines",
   "count": 9
  },
  "moves": 20
 },
 {
  "id": 25,
  "map": [
   ".U..T...",
   ".TH.TTT.",
   "TP.TTTKT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 12
 },
 {
  "id": 26,
  "map": [
   "TS......",
   "TSI...T.",
   ".TST.TKS"
  ],
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 8,
  "colors": {
   "water": 19,
   "sun": 12
  }
 },
 {
  "id": 27,
  "map": [
   "....T...",
   ".T.KT...",
   "T.TITT.T"
  ],
  "goal": {
   "type": "lines",
   "count": 10
  },
  "moves": 25
 },
 {
  "id": 28,
  "map": [
   "...T..H.",
   "..TIT.T.",
   "TTHTT.TT",
   "UKTP.TTT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 15
 },
 {
  "id": 29,
  "map": [
   ".....S..",
   ".....TTS",
   ".....T.S",
   "O.STSTKT"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 9,
  "colors": {
   "water": 18,
   "sun": 12
  }
 },
 {
  "id": 30,
  "map": [
   ".....T.T",
   ".....O.T",
   "TTKT.T..",
   "TTTT.TTT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 29,
  "bossHp": 13,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 31,
  "map": [
   ".P...H.T",
   ".T...T.U",
   "TTH..ITT",
   "KT.TTTTK"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 13
 },
 {
  "id": 32,
  "map": [
   "O.T.....",
   "S.T...S.",
   "..KTT.S.",
   "TT.KTSST"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 20,
  "colors": {
   "water": 18,
   "sun": 12
  }
 },
 {
  "id": 33,
  "map": [
   "....T...",
   "K...T...",
   "....T.T.",
   "TTTKT.IT"
  ],
  "goal": {
   "type": "lines",
   "count": 11
  },
  "moves": 27,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 34,
  "map": [
   "T.T....T",
   "H..I.U.T",
   "T.TP.T.T",
   "THKT.KTT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 23
 },
 {
  "id": 35,
  "map": [
   "..KT..T.",
   "..KS.TT.",
   "..ST.S..",
   "TIST.TST"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 16,
  "colors": {
   "water": 17,
   "sun": 12
  }
 },
 {
  "id": 36,
  "map": [
   "...IT...",
   "...KTT..",
   ".T..TT.K",
   ".TTI.TTT"
  ],
  "goal": {
   "type": "lines",
   "count": 12
  },
  "moves": 29
 },
 {
  "id": 37,
  "map": [
   "H.T..HO.",
   "K.TP.TU.",
   "..TOTK.T"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 10
 },
 {
  "id": 38,
  "map": [
   ".....T..",
   "TK....IT",
   "TIS..STT",
   "SK.STSTT"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 16,
  "colors": {
   "water": 17,
   "sun": 12
  }
 },
 {
  "id": 39,
  "map": [
   "...T....",
   "T..K...K",
   "T.T..T.T",
   "TTTIT.IT"
  ],
  "goal": {
   "type": "lines",
   "count": 13
  },
  "moves": 30
 },
 {
  "id": 40,
  "map": [
   "..I.T...",
   "T.TTI..T",
   "T.TTT...",
   "KTTT.KTT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 30,
  "bossHp": 15,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 41,
  "map": [
   "....S.S.",
   "....TIST",
   ".....SI.",
   "T.SKKSTT"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 13,
  "colors": {
   "water": 16,
   "sun": 12
  }
 },
 {
  "id": 42,
  "map": [
   "K.......",
   "O...TT..",
   "T.TKTT.T",
   "T.TTT..T",
   "TTTOTT.."
  ],
  "goal": {
   "type": "lines",
   "count": 13
  },
  "moves": 32
 },
 {
  "id": 43,
  "map": [
   "..H..TI.",
   "U.H..TP.",
   "T.T..PT.",
   "T.K..TT.",
   ".TTTK.TI"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 17
 },
 {
  "id": 44,
  "map": [
   ".S..T...",
   ".S.TS...",
   "SI.T....",
   "TTTTST.T",
   "SKK.TTIT"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 13,
  "colors": {
   "water": 15,
   "sun": 12
  }
 },
 {
  "id": 45,
  "map": [
   "...TI...",
   "...TK..T",
   "KT..T..T",
   "TT.T.T.T",
   "TIT.TTTT"
  ],
  "goal": {
   "type": "lines",
   "count": 14
  },
  "moves": 34,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 46,
  "map": [
   "..HK....",
   "..T.....",
   "..OHUTT.",
   "..PK.OTT",
   "TPTTTT.O"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 12
 },
 {
  "id": 47,
  "map": [
   "..ST....",
   ".T.T.S.S",
   "TSTTTT.T",
   "STSKTT..",
   "KII.TI.T"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 16,
  "colors": {
   "water": 15,
   "sun": 12
  }
 },
 {
  "id": 48,
  "map": [
   "..TT....",
   "..TTI...",
   "..TT....",
   ".KT.I..T",
   "ITTT..TK"
  ],
  "goal": {
   "type": "lines",
   "count": 15
  },
  "moves": 34
 },
 {
  "id": 49,
  "map": [
   ".OT.....",
   ".TKP.TT.",
   ".PTHUH..",
   ".TKTTTT.",
   "TOOTT..T"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 25
 },
 {
  "id": 50,
  "map": [
   ".....I..",
   "..T..T.T",
   ".T..TT.T",
   ".I..IT.T",
   "K.KTTTTT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 35,
  "bossHp": 16,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 51,
  "map": [
   "......T.",
   "..T.....",
   ".IKT.KI.",
   "T.TTI.K."
  ],
  "goal": {
   "type": "lines",
   "count": 16
  },
  "moves": 34
 },
 {
  "id": 52,
  "map": [
   "..T.....",
   "I.U....K",
   "P.HP...K",
   "KTH....T",
   "TT..II.T"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 13
 },
 {
  "id": 53,
  "map": [
   "......S.",
   ".K....O.",
   ".K....SS",
   ".K...S..",
   "OSTSO.TT"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 33,
  "colors": {
   "water": 13,
   "sun": 12
  }
 },
 {
  "id": 54,
  "map": [
   ".TT...T.",
   ".TT..KTT",
   "...TOOTT",
   ".TTTTTTT",
   ".TTTO.KT",
   ".TTTTTKT"
  ],
  "goal": {
   "type": "lines",
   "count": 16
  },
  "moves": 36
 },
 {
  "id": 55,
  "map": [
   "......I.",
   "......H.",
   ".P....H.",
   ".P....U.",
   "...T..UI",
   "I..KKTK."
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 20
 },
 {
  "id": 56,
  "map": [
   ".....S.T",
   ".....T.S",
   ".I.ST.SS",
   "T..T.TIS",
   "KI.TSKTT",
   "TTTT.IKT"
  ],
  "goal": {
   "type": "seeds",
   "count": 6
  },
  "moves": 22,
  "colors": {
   "water": 13,
   "sun": 12
  }
 },
 {
  "id": 57,
  "map": [
   "......T.",
   "......O.",
   "......T.",
   "T.TOT...",
   "....O.K.",
   "TTT.TOKK"
  ],
  "goal": {
   "type": "lines",
   "count": 17
  },
  "moves": 36,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 58,
  "map": [
   ".......H",
   "...U...I",
   ".IPT...I",
   "..K.P..T",
   ".HKUT..T",
   "TK.T.ITT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 19
 },
 {
  "id": 59,
  "map": [
   ".......S",
   ".......S",
   "......SO",
   "S......K",
   "..SSO.TS",
   "KOO...KT"
  ],
  "goal": {
   "type": "seeds",
   "count": 6
  },
  "moves": 20,
  "colors": {
   "water": 12,
   "sun": 12
  }
 },
 {
  "id": 60,
  "map": [
   "T.....T.",
   "....TTTT",
   "T.KKITTK",
   "T.T.TIIT",
   "T...TTIT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 31,
  "bossHp": 18,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 }
];

export const RESCUE_LEVELS: LevelDef[] = [
 {
  "id": 101,
  "map": [
   "TT.TTTTT",
   "TGT.T.TT",
   "TTTT.TTT",
   "TT.TTTTT",
   "TTTTT.TT"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 2,
  "title": "rescue.t1"
 },
 {
  "id": 102,
  "map": [
   "TTTT.TTT",
   "T.TTTTTT",
   "TT.GT.TT",
   "TTT.TTTT",
   "TTTTT.TT",
   "T.TTTTTT"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 2,
  "title": "rescue.t2"
 },
 {
  "id": 103,
  "map": [
   "TOT.TOTT",
   "TTTGT.TT",
   "TT.TTTTT",
   "TTTT.TTT",
   "T.TTTTTT",
   "TTTTTT.T"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 2,
  "title": "rescue.t3",
  "colors": {
   "sun": 18
  }
 },
 {
  "id": 104,
  "map": [
   "TT.TTTTT",
   "TTT.TKTT",
   "K.TGTT.K",
   "TTT.TTTT",
   "TT.TTTTT",
   "TTTTT.TT",
   "T.TTTTTT"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 2,
  "title": "rescue.t4"
 },
 {
  "id": 105,
  "map": [
   "TTI.TTTT",
   "TTTTT.IT",
   "T.TTTTTT",
   "TTG.TTT.",
   "TTTT.TTT",
   "T.TTTTTT",
   "TTTTTT.T"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 3,
  "title": "rescue.t5",
  "colors": {
   "sun": 16
  }
 },
 {
  "id": 106,
  "map": [
   "TOT.TOTT",
   "TTTTTT.T",
   "T.TGTT.T",
   "TTT.TTTT",
   "TT.TTTTT",
   "TTTTT.TT",
   "T.TTTTTT"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 2,
  "title": "rescue.t6",
  "colors": {
   "sun": 16
  }
 }
];
