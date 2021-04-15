# trens-minecat
Gestió d'informació i funcionament de trens al servidor de Minecraft.

## Pantalles

`/tm crear pantalla <model> <plantilla> <nom>`
  - `model` Número de model de pantalla que es vol utilitzar.
  - `plantilla` Plantilla de línies de tren que es vol utilitzar. Veure apartat [Configuració](https://github.com/janvinas/trens-minecat#Configuració) per a més informació. 
  - `nom` Nom de l'estació que es mostra a la pantalla. Només és visible en certs  models.

#### Models disponibles:
###### 1: 
Gran pantalla de 1×2 blocs que mostra una llista de les 9 properes sortides. Necessita un nom per mostrar a la cantonada superior dreta.
###### 2:
Pantalla que mostra una única propera sortida (nom de la línia, hora, destinació i informació addicional).
###### 3:
Pantalla estil Rodalies que mostra la propera sortida a menys de 5 minuts de l'hora actual.

## Comandaments

`/tm crear pantalla <model> <plantilla> <nom>` Veure [Pantalles](https://github.com/janvinas/trens-minecat#tPantalles).
`/tm recarregar` Recarregar la configuració.
`/tm spawn <món> <x> <y> <z>`

## Configuració

## TODO

[ ] Afegir comandament `spawn`
[ ] Arreglar pantalles per ser més similars a les de rodalies.
[ ] Afegir panell de sortides adif gran.
[ ] Secció de configuració
