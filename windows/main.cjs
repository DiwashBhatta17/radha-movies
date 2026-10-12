const {app,BrowserWindow,session,webFrameMain}=require('electron');
const fs=require('node:fs'),path=require('node:path');
const {allowPermission}=require('./permissions.cjs');
const HOME='https://radhamovies.netlify.app/';
function allowed(url){try{return new URL(url).origin===new URL(HOME).origin}catch{return false}}
app.whenReady().then(()=>{
  session.defaultSession.setPermissionRequestHandler((contents,permission,callback,details)=>callback(allowPermission(contents,permission,details?.requestingUrl)));
  session.defaultSession.setPermissionCheckHandler((contents,permission,requestingOrigin)=>allowPermission(contents,permission,requestingOrigin));
  session.defaultSession.on('will-download',event=>event.preventDefault());
  const win=new BrowserWindow({width:1280,height:840,minWidth:420,minHeight:600,title:'Radha Movies',fullscreenable:true,autoHideMenuBar:true,backgroundColor:'#101111',webPreferences:{nodeIntegration:false,contextIsolation:true,sandbox:true,webSecurity:true}});
  let htmlFullscreen=false;
  win.webContents.on('enter-html-full-screen',()=>{htmlFullscreen=true;});
  win.webContents.on('leave-html-full-screen',()=>{htmlFullscreen=false;});
  win.webContents.on('before-input-event',(event,input)=>{
    if(input.type!=='keyDown'||input.isAutoRepeat)return;
    if(input.key==='F11'){event.preventDefault();win.setFullScreen(!win.isFullScreen());}
    else if(input.key==='Escape'&&!htmlFullscreen&&win.isFullScreen()){event.preventDefault();win.setFullScreen(false);}
  });
  win.webContents.setWindowOpenHandler(()=>({action:'deny'}));
  win.webContents.on('will-navigate',(event,url)=>{if(!allowed(url))event.preventDefault()});
  win.webContents.on('will-redirect',(event,url,isInPlace,isMainFrame)=>{if(isMainFrame&&!allowed(url))event.preventDefault()});
  win.webContents.on('will-attach-webview',event=>event.preventDefault());
  const playerControls=fs.readFileSync(path.join(__dirname,'player-controls.js'),'utf8');
  win.webContents.on('did-frame-finish-load',(_event,isMainFrame,processId,routingId)=>{
    if(isMainFrame||!allowed(win.webContents.getURL()))return;
    const frame=webFrameMain.fromId(processId,routingId);
    if(!frame)return;
    let origin;try{origin=new URL(frame.url).origin;}catch{return;}
    if(!['https://vidy.st','https://www.vidy.st'].includes(origin))return;
    frame.executeJavaScript(playerControls).catch(()=>{});
  });
  win.loadURL(HOME);
});
app.on('window-all-closed',()=>app.quit());
