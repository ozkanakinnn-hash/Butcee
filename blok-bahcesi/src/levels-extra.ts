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
  "moves": 16,
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
  "moves": 10,
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
  "moves": 22,
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
  "moves": 15,
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
  "moves": 23,
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
  "moves": 14,
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
  "moves": 34,
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
  "moves": 24,
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
  "moves": 16
 },
 {
  "id": 23,
  "map": [
   "....STT.",
   "....KTS.",
   "S.TTTTST"
  ],
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 9,
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
   "count": 8
  },
  "moves": 19
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
  "moves": 11
 },
 {
  "id": 26,
  "map": [
   "..T.SST.",
   "..T.TKTT",
   ".TTSSTTT"
  ],
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 11,
  "colors": {
   "water": 19,
   "sun": 12
  }
 },
 {
  "id": 27,
  "map": [
   "....T...",
   ".T.TT...",
   "TKTTTT.T"
  ],
  "goal": {
   "type": "lines",
   "count": 9
  },
  "moves": 22
 },
 {
  "id": 28,
  "map": [
   "...T.TTH",
   "..TU.TPT",
   "TTTT.TTK"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 16
 },
 {
  "id": 29,
  "map": [
   "....T.T.",
   "....S.T.",
   "SSKTTS.T"
  ],
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 15,
  "colors": {
   "water": 19,
   "sun": 12
  }
 },
 {
  "id": 30,
  "map": [
   ".....T.T",
   "TTT..K.T",
   "TTTT.TTT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 30,
  "bossHp": 13,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 31,
  "map": [
   "H...TT..",
   "TP..T...",
   "ITT.TKTU"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 10
 },
 {
  "id": 32,
  "map": [
   "T.T.K..T",
   "TTT.SO..",
   "STSTS.TT"
  ],
  "goal": {
   "type": "seeds",
   "count": 3
  },
  "moves": 61,
  "colors": {
   "water": 19,
   "sun": 12
  }
 },
 {
  "id": 33,
  "map": [
   "T...T...",
   "T.....T.",
   "TTTKOTT."
  ],
  "goal": {
   "type": "lines",
   "count": 10
  },
  "moves": 23,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 34,
  "map": [
   "..U.....",
   "O.T.H..T",
   ".KT.TTTP"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 11
 },
 {
  "id": 35,
  "map": [
   "TS.TT.S.",
   "TTT.S.T.",
   "TTTSS.T.",
   "TTKTT.OT"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 18,
  "colors": {
   "water": 19,
   "sun": 12
  }
 },
 {
  "id": 36,
  "map": [
   "...TT...",
   "...ITT..",
   ".T..TT.T",
   ".TKTTTTT"
  ],
  "goal": {
   "type": "lines",
   "count": 10
  },
  "moves": 21
 },
 {
  "id": 37,
  "map": [
   "T.T..TU.",
   "I.HH.TT.",
   "..TPTTKT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 12
 },
 {
  "id": 38,
  "map": [
   "....T.T.",
   "...S..ST",
   "...TT.ST",
   "T.KSTSTO"
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
  "id": 39,
  "map": [
   "...T....",
   "T..O...T",
   "T.T..T.T",
   "TTTTKT.T"
  ],
  "goal": {
   "type": "lines",
   "count": 10
  },
  "moves": 23
 },
 {
  "id": 40,
  "map": [
   "T...T..T",
   "T..TK.TT",
   "TTT.T.OT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 30,
  "bossHp": 13,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 41,
  "map": [
   ".T....T.",
   ".T..T.K.",
   ".T..S...",
   "SOTKSSS."
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 10,
  "colors": {
   "water": 18,
   "sun": 12
  }
 },
 {
  "id": 42,
  "map": [
   "K....T..",
   "T...TT..",
   "T.TOTT.T",
   ".TTTTT.K"
  ],
  "goal": {
   "type": "lines",
   "count": 11
  },
  "moves": 28
 },
 {
  "id": 43,
  "map": [
   "U.P..TT.",
   "T.T..TT.",
   "T.I..TH.",
   ".THTTKKT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 18
 },
 {
  "id": 44,
  "map": [
   ".T.TS...",
   "TS.TT...",
   ".TTTTI..",
   ".SKKSTST"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 12,
  "colors": {
   "water": 18,
   "sun": 12
  }
 },
 {
  "id": 45,
  "map": [
   "...TT...",
   "KK.TT..T",
   "TT.TTT..",
   "TTTOT.TT"
  ],
  "goal": {
   "type": "lines",
   "count": 11
  },
  "moves": 22,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 46,
  "map": [
   "T.H..T..",
   "T.T.THO.",
   "..T.PTU.",
   "TTT.KTTK"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 21
 },
 {
  "id": 47,
  "map": [
   ".TTT.T..",
   "..TT.S.T",
   "TTTTSK.S",
   "SITKST.T"
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
  "id": 48,
  "map": [
   "..TTT...",
   "..TTT...",
   ".T.IT..K",
   "TTTTT.KT"
  ],
  "goal": {
   "type": "lines",
   "count": 11
  },
  "moves": 24
 },
 {
  "id": 49,
  "map": [
   ".UT.....",
   ".TTT.TH.",
   ".KTTOTT.",
   "PHTTTT.K"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 17
 },
 {
  "id": 50,
  "map": [
   ".T..TTT.",
   ".T.TTTT.",
   ".T..TIKT",
   ".TTTTKTT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 32,
  "bossHp": 14,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 51,
  "map": [
   "..T...T.",
   ".....I..",
   "TKTKTIT."
  ],
  "goal": {
   "type": "lines",
   "count": 12
  },
  "moves": 27
 },
 {
  "id": 52,
  "map": [
   ".U......",
   "HP......",
   "H..T.IK.",
   ".TTKITTT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 13
 },
 {
  "id": 53,
  "map": [
   ".T....S.",
   ".O....KT",
   ".O...T..",
   "S.TSSKST"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 51,
  "colors": {
   "water": 17,
   "sun": 12
  }
 },
 {
  "id": 54,
  "map": [
   ".TT..TIT",
   ".T.ITT.T",
   ".TTTTKTT",
   ".KTTTTTT"
  ],
  "goal": {
   "type": "lines",
   "count": 12
  },
  "moves": 25
 },
 {
  "id": 55,
  "map": [
   "..T.....",
   "..UT..TH",
   "..TT...P",
   "HTI.IKTK"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 10
 },
 {
  "id": 56,
  "map": [
   ".....T.T",
   ".S.TKTT.",
   "ST..SSTT",
   "ITTSKT.I"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 13,
  "colors": {
   "water": 16,
   "sun": 12
  }
 },
 {
  "id": 57,
  "map": [
   "......T.",
   "......T.",
   "T.KTK...",
   "TI.TTTTI"
  ],
  "goal": {
   "type": "lines",
   "count": 13
  },
  "moves": 32,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 58,
  "map": [
   "..T.....",
   "..T.P..U",
   "....TKTH",
   "THOTTKO."
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 14
 },
 {
  "id": 59,
  "map": [
   ".......T",
   "......ST",
   "K.STS.T.",
   "SKO.S.TO"
  ],
  "goal": {
   "type": "seeds",
   "count": 4
  },
  "moves": 15,
  "colors": {
   "water": 16,
   "sun": 12
  }
 },
 {
  "id": 60,
  "map": [
   ".O.....T",
   "T.OKTTK."
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 36,
  "bossHp": 15,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 61,
  "map": [
   "..U.P...",
   "..HT....",
   "..P.T.IK",
   "KH.TTTIT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 19
 },
 {
  "id": 62,
  "map": [
   ".....I..",
   "T...TS..",
   ".S.TIS..",
   "KT.TK...",
   "ST.SSTTT"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 14,
  "colors": {
   "water": 16,
   "sun": 12
  }
 },
 {
  "id": 63,
  "map": [
   ".T......",
   ".T..TT..",
   "...ITTK.",
   ".I.TK.T.",
   ".TTTTTTT"
  ],
  "goal": {
   "type": "lines",
   "count": 13
  },
  "moves": 32
 },
 {
  "id": 64,
  "map": [
   "....T...",
   "...PH.PT",
   "..TKT.KU",
   "I.HI.TTT",
   "T.TTT.TT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 27
 },
 {
  "id": 65,
  "map": [
   ".T.T....",
   "...TT...",
   ".S.S....",
   "KO.KT..S",
   "TSSST.TO"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 27,
  "colors": {
   "water": 16,
   "sun": 12
  }
 },
 {
  "id": 66,
  "map": [
   ".T.T.T..",
   ".TT..T..",
   ".T.T.O.K",
   ".KTTTOTT"
  ],
  "goal": {
   "type": "lines",
   "count": 14
  },
  "moves": 33
 },
 {
  "id": 67,
  "map": [
   "...T....",
   "P......T",
   "T.TUOK.P",
   "..HHKTTT",
   "TTTOTT.T"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 14
 },
 {
  "id": 68,
  "map": [
   "......ST",
   ".ST.TST.",
   ".SST.KTO",
   "SKO.TTTT"
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
  "id": 69,
  "map": [
   ".KI.....",
   "TT.....T",
   "TT.T.T.T",
   "KTTITT.T"
  ],
  "goal": {
   "type": "lines",
   "count": 14
  },
  "moves": 35,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 70,
  "map": [
   ".TO.....",
   "TTT...T.",
   "TTO..TT.",
   "TTK..TTT",
   "TT.TT.KT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 33,
  "bossHp": 16,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 71,
  "map": [
   "....ST.S",
   "....KT.T",
   ".O..T...",
   ".ST.SOSK",
   ".STTTO.T"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 15,
  "colors": {
   "water": 15,
   "sun": 12
  }
 },
 {
  "id": 72,
  "map": [
   "...I..T.",
   "...T.T.T",
   "...T.IT.",
   "T.KT..KT",
   "TTTT.TTI"
  ],
  "goal": {
   "type": "lines",
   "count": 14
  },
  "moves": 31
 },
 {
  "id": 73,
  "map": [
   "......P.",
   "....HU..",
   "...TT.H.",
   "...POT..",
   "OK.OTTKT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 27
 },
 {
  "id": 74,
  "map": [
   ".T.....T",
   ".S..T..S",
   ".S....OO",
   "TTT...SK",
   ".KS.STTO"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 20,
  "colors": {
   "water": 15,
   "sun": 12
  }
 },
 {
  "id": 75,
  "map": [
   "...O...T",
   ".......T",
   "...T..K.",
   "...K.TTT",
   "TTTO..TO"
  ],
  "goal": {
   "type": "lines",
   "count": 15
  },
  "moves": 36
 },
 {
  "id": 76,
  "map": [
   "....T..O",
   "....T..H",
   ".O.TUPOK",
   ".HTPT.TT",
   "TKT.TT.T"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 12
 },
 {
  "id": 77,
  "map": [
   "....S...",
   "....T...",
   ".K.IS...",
   ".TT.I..I",
   "S.SKSST."
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 44,
  "colors": {
   "water": 14,
   "sun": 12
  }
 },
 {
  "id": 78,
  "map": [
   "..K..TTT",
   "..T..TI.",
   ".TT..ITT",
   ".T.TTIT.",
   "TTKTTT.T"
  ],
  "goal": {
   "type": "lines",
   "count": 15
  },
  "moves": 36
 },
 {
  "id": 79,
  "map": [
   ".....T..",
   ".H.U.I.T",
   ".H.ITK.P",
   "TTPT.T.T",
   ".TTKI..T"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 20
 },
 {
  "id": 80,
  "map": [
   ".......O",
   "T......O",
   "T.T....T",
   "OT.T...T",
   "T..KKTTT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 33,
  "bossHp": 16,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 81,
  "map": [
   "....T...",
   ".T..T...",
   ".T..T.K.",
   ".OTTO.T.",
   "T.OK.K.T"
  ],
  "goal": {
   "type": "lines",
   "count": 16
  },
  "moves": 38,
  "colors": {
   "leaf": 18
  }
 },
 {
  "id": 82,
  "map": [
   "....H...",
   "....P.TP",
   "..TH..KK",
   "..UT..IT",
   "TIKTT.I."
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 14
 },
 {
  "id": 83,
  "map": [
   ".I......",
   ".S.T....",
   ".S..SIIS",
   "K.S.KSTK"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 27,
  "colors": {
   "water": 14,
   "sun": 12
  }
 },
 {
  "id": 84,
  "map": [
   "..T.....",
   "T.T....T",
   "..K..TT.",
   "OTT..KT.",
   "TTT.KOOT"
  ],
  "goal": {
   "type": "lines",
   "count": 16
  },
  "moves": 35
 },
 {
  "id": 85,
  "map": [
   "...U....",
   "T.IH.IH.",
   "P.KP.T..",
   "..TT.TK.",
   "TTKTTT.I"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 13
 },
 {
  "id": 86,
  "map": [
   ".O.S....",
   ".K.T.T..",
   ".OTS.T..",
   ".SS.SSK.",
   "K.OT.TTT"
  ],
  "goal": {
   "type": "seeds",
   "count": 5
  },
  "moves": 19,
  "colors": {
   "water": 13,
   "sun": 12
  }
 },
 {
  "id": 87,
  "map": [
   "....T.T.",
   ".OK.O.T.",
   ".TT...T.",
   "T.TTT.T.",
   "KO.KTTTT"
  ],
  "goal": {
   "type": "lines",
   "count": 16
  },
  "moves": 38
 },
 {
  "id": 88,
  "map": [
   "....P.U.",
   "....T.H.",
   "...IP.I.",
   ".T.KTTH.",
   "T.UKKTT.",
   ".ITTT.TT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 24
 },
 {
  "id": 89,
  "map": [
   ".......S",
   "S.S.S..S",
   "S.T..T.T",
   "K.T.KT.I",
   "I.TT...I",
   "S.TTTTTK"
  ],
  "goal": {
   "type": "seeds",
   "count": 6
  },
  "moves": 17,
  "colors": {
   "water": 13,
   "sun": 12
  }
 },
 {
  "id": 90,
  "map": [
   "T.......",
   "....T...",
   "T.T.....",
   "T.TIT...",
   "K.TTTK..",
   "ITT.TTKI"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 34,
  "bossHp": 17,
  "colors": {
   "stone": 11,
   "leaf": 12
  }
 },
 {
  "id": 91,
  "map": [
   "......UH",
   "......PK",
   "...HT.UK",
   "....T..P",
   "....TTTT",
   "OOTK.OTO"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 27
 },
 {
  "id": 92,
  "map": [
   ".S......",
   ".S.T...K",
   "....T.TT",
   "TO.SS.SK",
   ".OTST.TO",
   "STTOTKT."
  ],
  "goal": {
   "type": "seeds",
   "count": 6
  },
  "moves": 20,
  "colors": {
   "water": 13,
   "sun": 12
  }
 },
 {
  "id": 93,
  "map": [
   ".T...T..",
   ".I...TT.",
   ".T..TITI",
   ".TT.K.KI",
   "K...TTT."
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
  "id": 94,
  "map": [
   ".I......",
   "..P.....",
   "..K.P...",
   "H.HUK.U.",
   "TKTTT...",
   "I.ITTITT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 25
 },
 {
  "id": 95,
  "map": [
   ".K......",
   "........",
   "SS......",
   ".K......",
   ".S...IIK",
   "SII.SS.S"
  ],
  "goal": {
   "type": "seeds",
   "count": 6
  },
  "moves": 14,
  "colors": {
   "water": 13,
   "sun": 12
  }
 },
 {
  "id": 96,
  "map": [
   "....K...",
   "...TT..K",
   "....T.TT",
   ".TTTT.TT",
   "T.TTO.OO",
   "TTKTOT.."
  ],
  "goal": {
   "type": "lines",
   "count": 17
  },
  "moves": 42
 },
 {
  "id": 97,
  "map": [
   ".....P..",
   ".T...U.O",
   ".K..HUP.",
   "OK..HTOT",
   "TT..KTT.",
   "TOT...TT"
  ],
  "goal": {
   "type": "animals"
  },
  "moves": 14
 },
 {
  "id": 98,
  "map": [
   "..S..S.K",
   ".......T",
   "..I..T.S",
   "..S..IST",
   "..TSSTIT",
   ".K.IK.TT"
  ],
  "goal": {
   "type": "seeds",
   "count": 6
  },
  "moves": 19,
  "colors": {
   "water": 12,
   "sun": 12
  }
 },
 {
  "id": 99,
  "map": [
   "....T...",
   "...KT...",
   "...T.TT.",
   ".K.OOTT.",
   "TTOTTTT.",
   "...KTTOT"
  ],
  "goal": {
   "type": "lines",
   "count": 18
  },
  "moves": 42
 },
 {
  "id": 100,
  "map": [
   "..T.....",
   "TTT.....",
   "I.......",
   "IT..I..T",
   "KTT.K.TK",
   "TT.IT.TT"
  ],
  "goal": {
   "type": "boss"
  },
  "moves": 32,
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
 },
 {
  "id": 107,
  "map": [
   "TTI.TTTT",
   "T.TTTTIT",
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
  "waterEvery": 1,
  "title": "rescue.t7",
  "colors": {
   "sun": 16
  }
 },
 {
  "id": 108,
  "map": [
   "K.TTTTTK",
   "TTTT.TTT",
   "TT.TGT.T",
   "TTTT.TTT",
   "T.TTTTTT",
   "TTTTT.TT",
   "TT.TTTTT"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 7,
  "title": "rescue.t8"
 },
 {
  "id": 109,
  "map": [
   "TOTT.TOT",
   "OTT.TTTO",
   "TT.TGTTT",
   "T.TTT.TT",
   "TTTT.TTT",
   "TT.TTTTT",
   "TTTTTT.T"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 1,
  "title": "rescue.t9",
  "colors": {
   "sun": 18
  }
 },
 {
  "id": 110,
  "map": [
   "T.TTTTTT",
   "TTTTT.TT",
   "TT.TTTTK",
   "TTTTGT.T",
   "TTT.TTTT",
   "T.TTTTTT",
   "TTTTT.TT",
   "TT.TTTTT"
  ],
  "goal": {
   "type": "rescue"
  },
  "moves": null,
  "waterEvery": 2,
  "title": "rescue.t10"
 }
];
