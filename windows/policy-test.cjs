const assert=require('node:assert/strict'),vm=require('node:vm'),fs=require('node:fs');
const handlers={},sessionHandlers={};let windowOptions,permission,checkPermission,open,fullscreen=false;
class FakeWindow{constructor(options){windowOptions=options;this.webContents={getURL:()=> 'https://radhamovies.netlify.app/',setWindowOpenHandler:fn=>open=fn,on:(name,fn)=>handlers[name]=fn};}loadURL(url){assert.equal(new URL(url).origin,'https://radhamovies.netlify.app');assert.equal(new URL(url).searchParams.get('appVersion'),'1.0.2');}setFullScreen(value){fullscreen=value;}isFullScreen(){return fullscreen;}}
let injected=0,frameURL='https://vidy.st/movie/557';
const electron={webFrameMain:{fromId:()=>({url:frameURL,executeJavaScript:()=>{injected++;return Promise.resolve();}})},app:{whenReady:()=>({then:fn=>fn()}),on(){}},BrowserWindow:FakeWindow,session:{defaultSession:{setPermissionCheckHandler:fn=>checkPermission=fn,setPermissionRequestHandler:fn=>permission=fn,on:(name,fn)=>sessionHandlers[name]=fn}}};
vm.runInNewContext(fs.readFileSync(__dirname+'/main.cjs','utf8'),{URL,__dirname,require:name=>name==='electron'?electron:require(name)});
assert.deepEqual(JSON.parse(JSON.stringify(open({url:'https://ads.example'}))),{action:'deny'});
function prevented(fn,...args){let blocked=false;fn({preventDefault(){blocked=true}},...args);return blocked;}
assert(prevented(handlers['will-navigate'],'https://ads.example'));assert(!prevented(handlers['will-navigate'],'https://radhamovies.netlify.app/'));assert(prevented(handlers['will-navigate'],'https://radhamovies.netlify.app.evil.example/'));
assert(prevented(handlers['will-redirect'],'https://ads.example',false,true));assert(!prevented(handlers['will-redirect'],'https://vidy.st',false,false));assert(prevented(sessionHandlers['will-download']));assert.equal(windowOptions.webPreferences.nodeIntegration,false);assert.equal(windowOptions.webPreferences.sandbox,true);permission(null,'camera',result=>assert.equal(result,false));console.log('PASS: window denial, navigation/redirect policy, download denial, renderer isolation, permission denial');
const page={getURL:()=> 'https://radhamovies.netlify.app/'};
for(const url of ['https://radhamovies.netlify.app/','https://vidy.st/movie/557','https://www.vidy.st/tv/1396/1/1']){
  permission(page,'fullscreen',result=>assert.equal(result,true),{requestingUrl:url});
  assert.equal(checkPermission(page,'fullscreen',new URL(url).origin),true);
}
for(const p of ['camera','media','notifications','automatic-fullscreen'])assert.equal(checkPermission(page,p,'https://vidy.st'),false);
assert.equal(checkPermission(page,'fullscreen','https://vidy.st.evil.example'),false);
assert.equal(checkPermission({getURL:()=> 'https://ads.example'},'fullscreen','https://vidy.st'),false);
assert.equal(checkPermission(null,'fullscreen','https://vidy.st'),false);
assert(prevented(handlers['before-input-event'],{type:'keyDown',key:'F11'}));assert.equal(fullscreen,true);
assert(prevented(handlers['before-input-event'],{type:'keyDown',key:'Escape'}));assert.equal(fullscreen,false);
handlers['enter-html-full-screen']();fullscreen=true;assert(!prevented(handlers['before-input-event'],{type:'keyDown',key:'Escape'}));assert.equal(fullscreen,true);
handlers['leave-html-full-screen']();assert(prevented(handlers['before-input-event'],{type:'keyDown',key:'Escape'}));assert.equal(fullscreen,false);
console.log('PASS: trusted fullscreen permission/checks, untrusted denial, F11 toggle, Escape behavior');
handlers['did-frame-finish-load']({},false,1,2);assert.equal(injected,1);
frameURL='https://ads.example';handlers['did-frame-finish-load']({},false,1,2);assert.equal(injected,1);
frameURL='https://vidy.st/movie/557';handlers['did-frame-finish-load']({},true,1,2);assert.equal(injected,1);
console.log('PASS: native controls installed only in trusted player frames');
