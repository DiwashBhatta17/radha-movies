(()=>{
  const artwork=document.querySelector('#heroArt');
  let filled=false;
  function updateArtwork(){if(filled)return;const rows=films.filter(f=>f.image).slice(0,3);if(rows.length<3)return;artwork.replaceChildren(...rows.map(f=>{const image=document.createElement('img');image.src=f.image;image.alt='';image.decoding='async';return image;}));filled=true;}
  const renderBeforeLanding=render;
  render=function(){renderBeforeLanding();updateArtwork();};
  updateArtwork();
})();
