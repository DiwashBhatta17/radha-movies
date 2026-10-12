// Installed by the native wrappers only into the trusted player frame.
(()=>{
  if(window===window.top||window.__radhaControlsInstalled)return;
  window.__radhaControlsInstalled=true;
  const HOME='https://radhamovies.netlify.app';
  const send=data=>window.parent.postMessage(data,HOME);
  const ready=()=>send({type:'RADHA_PLAYER_READY',version:1});
  function seek(delta){
    const videos=[...document.querySelectorAll('video')];
    const video=videos.find(v=>!v.paused&&v.readyState>0)||videos.find(v=>v.readyState>0);
    if(!video||!Number.isFinite(video.duration)||!Number.isFinite(delta)||Math.abs(delta)>120){send({type:'RADHA_SEEK_RESULT',ok:false});return false;}
    const target=Math.max(0,Math.min(video.currentTime+delta,Math.max(0,video.duration-.1)));
    try{video.currentTime=target;send({type:'RADHA_SEEK_RESULT',ok:true,currentTime:target});return true;}catch{send({type:'RADHA_SEEK_RESULT',ok:false});return false;}
  }
  window.addEventListener('message',e=>{
    if(e.source!==window.parent||e.origin!==HOME||!e.data||typeof e.data!=='object')return;
    if(e.data.type==='RADHA_PLAYER_PING')ready();
    if(e.data.type==='RADHA_PLAYER_SEEK')seek(e.data.seconds);
  });
  window.addEventListener('keydown',e=>{
    if(e.ctrlKey||e.altKey||e.metaKey||e.target?.isContentEditable||/^(INPUT|TEXTAREA|SELECT|BUTTON)$/.test(e.target?.tagName||''))return;
    if((e.key==='ArrowLeft'||e.key==='ArrowRight')&&seek(e.key==='ArrowLeft'?-10:10)){e.preventDefault();e.stopImmediatePropagation();}
  },true);
  ready();
})();
