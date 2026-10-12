const fs=require('node:fs'),path=require('node:path');
fs.copyFileSync(path.join(__dirname,'../native/player-controls.js'),path.join(__dirname,'player-controls.js'));
