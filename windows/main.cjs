const {app,BrowserWindow,session}=require('electron');
const HOME='https://radhamovies.netlify.app/';
function allowed(url){try{return new URL(url).origin===new URL(HOME).origin}catch{return false}}
app.whenReady().then(()=>{
  session.defaultSession.setPermissionRequestHandler((contents,permission,callback)=>callback(false));
  session.defaultSession.on('will-download',event=>event.preventDefault());
  const win=new BrowserWindow({width:1280,height:840,minWidth:420,minHeight:600,title:'Radha Movies',autoHideMenuBar:true,backgroundColor:'#101111',webPreferences:{nodeIntegration:false,contextIsolation:true,sandbox:true,webSecurity:true}});
  win.webContents.setWindowOpenHandler(()=>({action:'deny'}));
  win.webContents.on('will-navigate',(event,url)=>{if(!allowed(url))event.preventDefault()});
  win.webContents.on('will-redirect',(event,url,isInPlace,isMainFrame)=>{if(isMainFrame&&!allowed(url))event.preventDefault()});
  win.webContents.on('will-attach-webview',event=>event.preventDefault());
  win.loadURL(HOME);
});
app.on('window-all-closed',()=>app.quit());
