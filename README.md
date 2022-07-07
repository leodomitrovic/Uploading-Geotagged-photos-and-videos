# Uploading-Geotagged-photos-and-videos

Ideja aplikacije je bila omogućiti objavljivanje geooznačenih fotografija i videozapisa te prikazivanje na karti u aplikaciji. Kod objavljivanja se u pozadini provjerava ima li fotografija ili videozapis geooznaku u sebi i zapamti se. Ako nema geooznaku, kreatoru sadržaja se omogućuje da ručno upiše geografsku širinu i dužinu gdje je zapis nastao. Na taj način se fotografija prikazuje na karti na lokaciji na kojoj je nastala, u obliku markera koji sadrži umanjena sliku ili sliku iz videozapisa. Pritiskom na marker otvara se objava.

Postoje četiri vrste korisnika aplikacije:

- Administrator - provjerava i dopušta objave u aplikaciji te registrira nove administratore
- Kreator sadržaja - objavljuje sliku ili videozapis
- Korisnik - prati kreatore sadržaja i može "lajkati", komentirati i ocijeniti njihov sadržaj
- Neregistrirani korisnik - prikaz objava svih kreatora na karti te najbolje ocijenjenih i najpregledanijih objava

Svi korisnici aplikacije imaju mogućnost prikaza objava svih kreatora na karti te najbolje ocijenjenih i najpregledanijih objava. Klasični korisnik ima i popis objava kreatora koje prati sotirano od najnovijih prema starijim. Kreator sadržaja na svom profilu ima dva načina prikaza objava. Prvi način je prikaz u rešetki (eng. grid), a u drugom su sve njegove objave prikazane na karti.

Neregistrirani korisnik se može registrirati te pritom odabrati želi li biti kreator sadržaja ili korisnik.

Otvaranjem pojednine objave prikazuje se njen opis, ukoliko je dodan, ocjena i komentari koje su korisnici ostavili. Ako korisnik još nije ocijenio određenu objavu, prikazat će mu se prosječna ocjena ostalih korisnika. Svaki korisnik može više puta komentirati objavu.
