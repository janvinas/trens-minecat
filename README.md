# trens-minecat
Gestió d'informació i funcionament de trens al servidor de Minecraft.

## Pantalles

`/tm crear pantalla <model> <pantalla> <nom>`
  - `model` Número de model de pantalla que es vol utilitzar.
  - `pantalla` Plantilla de línies de tren que es vol utilitzar. Veure apartat [Configuració](https://github.com/janvinas/trens-minecat#Configuració) per a més informació. 
  - `nom` Nom de l'estació que es mostra a la pantalla. Només és visible en certs models.

#### Models disponibles:
_Els models 1 i 2 provenen de versions antigues i probablement desapareixeran en el futur, per la qual cosa no s'haurien d'utilitzar.
###### 1: 
![imatge](/imatges/1.png)

###### 2:
![imatge](/imatges/2.png)

###### 3:
![imatge 1](/imatges/3a.png)

###### 4:
![imatge](/imatges/4.png)

## Pantalles Manuals

Visualment són iguals que les pantalles normals però la informació no s'actualitza mitjançant un horari sinó amb cartells Traincarts personalitzats.

Per actualitzar la seva informació cal col·locar un cartell Traincarts amb la informació següent:
```
[train]
displaymanual
<nom de la pantalla>
```
La pantalla s'actualitzarà quan s'activi el cartell o un tren passi per sobre i el cartell estigui activat.

Per generar una pantalla, caldrà utilitzar el comandament `/tm crear displaymanual <model> <nom>`, on el nom ha de coincidir amb el del cartell Traincarts.

Per reiniciar la informació de la pantalla i tornar-la al seu estat normal, es pot utilitzar el cartell:
```
[train]
reiniciardisplay
<nom de la pantalla>
```

Si passa un tren sense parada, es pot utilitzar el següent cartell:
```
[train]
noparadisplay
<nom de la pantalla>
```

#### Models disponibles:

###### 1:
_És exactament igual visualment que el model 3 de pantalla normal, però no mostrarà mai el temps fins al pròxim tren._

![imatge](/imatges/3a.png)

## Comandaments

`/tm crear pantalla <model> <plantilla> <nom>` Veure [Pantalles](#Pantalles).

`/tm crear displaymanual <model> <nom>` Veure [Pantalles Manuals](#Pantalles-Manuals).

`/tm recarregar` Recarregar la configuració.

`/tm spawn <món> <x> <y> <z>` Genera un tren a les coordenades especificades. Aquestes coordenades han de ser les d'un cartell `spawner`, i no les de la via on ha d'aparèixer un tren.

`/tm horn` Fa sonar el so definit a les propietats del tren com a `horn_nom.del.so`. També es pot executar amb un cartell `[train] horn`.

## Configuració

A la configuració es defineixen certes variables globals i les plantilles disponibles amb línies de tren per utilitzar. La configuració d'exemple és la següent:

```yaml
#Temps (en ticks) que el plugin esperarà abans de destruir trens que hagin estat aturats. 0 desactiva la funció.
destruir-trens-en: 0

#Els trens que tinguin aquest tag no seran afectats per la configuració anterior i, per tant, no es destruiran mai.
no-destrueixis: nodestrueixis

#Temps mínim (en segons) que es mostrarà a les pantalles. Si el temps fins al proper tren és menor que això es mostrarà
#un missatge personalitzable a cada pantalla, com "ara" o "imminent"
temps-minim-en-pantalla: 20

#pantalles d'informació que es poden utilitzar. El nom de la pantalla és el que s'utilitzarà en el comandament per
#generar-la.
#
#Format de les pantalles:
#  <nom>:
#    longitud: <longitud>
#    línies:
#      - <nom>|<expressió cron>|<destinació>|<via>|<observacions>
#
# [tots els camps són obligatoris, encara que no s'hagin d'utilitzar]
# [no es pot afegir un espai al voltant de '|', ja que s'estaria afegint al camp corresponent]

pantalles:
  exemple:
    longitud: 5
    linies:
      - R2N|45 2/10 * * * *|Sant Celoni|2|Para a totes les estacions
      - R2N|12 8/10 * * * *|Aeroport|2|Para a totes les estacions
```

## TODO

- [x] Afegir comandament `spawn`
- [x] Arreglar pantalles per ser més similars a les de rodalies.
- [ ] Afegir panell de sortides adif gran.
- [x] Secció de configuració
