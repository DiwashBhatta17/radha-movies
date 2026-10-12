const HOME='https://radhamovies.netlify.app';
const PLAYER_ORIGINS=new Set([HOME,'https://vidy.st','https://www.vidy.st']);
function origin(url){try{return new URL(url).origin}catch{return ''}}
function allowPermission(contents,permission,requestingUrl){
  return permission==='fullscreen' && !!contents && origin(contents.getURL())===HOME && PLAYER_ORIGINS.has(origin(requestingUrl));
}
module.exports={allowPermission};
