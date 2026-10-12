const assert=require('node:assert/strict'),fs=require('fs');
const c=require('./dist/core.js'),{parseRecommendations}=require('./netlify/functions/recommendations-parser.cjs');
assert.equal(c.correct('spiter man'),'Spider-Man');assert.equal(c.correct('spiderman'),'Spider-Man');assert.equal(c.correct('strnger things'),'Stranger Things');assert.equal(c.correct('a totally new movie'),'a totally new movie');assert.equal(c.correct('Dagnal'),'Dangal');
assert.equal(c.eventData({event:'timeupdate',currentTime:-1}),null);assert.equal(c.eventData('invalid'),null);assert.equal(c.eventData({type:'PLAYER_EVENT',data:{event:'timeupdate',currentTime:123}}).currentTime,123);
const f={id:'tv-1396',title:'Breaking Bad'},p=c.progress(null,f,{event:'timeupdate',currentTime:200,duration:2500,season:2,episode:3},1,1);assert.equal(p.season,2);assert.equal(p.episode,3);assert.equal(p.completed,false);assert.equal(p.film,f);assert.equal(c.progress(p,f,{event:'ended',currentTime:2500,duration:2500},2,3).completed,true);
const rows=parseRecommendations(fs.readFileSync(__dirname+'/fixtures/recommendations.html','utf8'),'movie');assert(rows.length>5);assert.equal(rows[0].title,'Paycheck');assert.equal(rows[0].tmdb,'9620');
console.log('PASS: typo correction, progress events, episode identity, completion, recommendation parsing');
