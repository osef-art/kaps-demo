<!-- CheckImageSize -->
<!--suppress CheckImageSize -->
<div style="text-align: center;">    

# ![>](android/assets/sprites/icons/icon.png "hey") KAPS! ![<](android/assets/sprites/icons/icon.png "you")

Un mini-jeu à la *'Dr. Mario'* dont j'aimerais bien faire une appli un de ces quatre.  
Déplace et matche les gélules de la grille afin de dégommer toutes les germes façon Pfizer ! 💊  
*(en vrai je laisse le README en français en attendant que ça devienne un vrai truc)*

[🎮 EH COMMENT ON LANCE LE JEU ??](#lancer-le-jeu-)  
[⌨ EH COMMENT ON JOUE ??????](#comment-jouer-)  
~  
[📜 EH C'EST QUOI LES RÈGLES (j'arrête)](#rgles-du-jeu-)  
[🤝 LES SIDEKICKS](#sidekicks-)  
[🦠 LES GERMES](#germs-)  
[💊 GÉLULES SPÉCIALES](#glules-spciales-)   
~  
[💡 PETITS TIPS](#tips-)  
[🇬🇧 ENGLISH README](README_EN.md)

<img alt="ptite démo" src="android/assets/footages/kaps-fullgame-sample.gif" title="Quick gameplay footage" width="200"/>
</div>

### UPDATES 🚨

- 🧭 J'ai mis un **menu de sélection** des sidekicks avant les niveaux 😏 *(c'est archi long à faire)*
- ➕ J'ai rajouté **5 niveaux au début** *parce qu'askip "c'était trop dur dès le début"*
- ➕ J'ai aussi rajouté une touche permettant de tourner des gélules **dans l'autre sens**  *(ça évite de devoir tourner 3 fois dans le même sens)*
- 📃 On peut maintenant voir les **stats** des sidekicks dans le **menu pause**.
- ➕ Il y a maintenant plus de **commandes** pour lancer le jeu (pour pouvoir lancer une série de niveaux précise avec une équipe de sidekicks précise) *(comment ça tu t'en fous)*
- ⚖ J'ai de ouf rééquilibré le pouvoir de certains sidekicks et leur mana (la jauge à remplir)
- 👁 Visuellement j'ai rendu le truc plus propre, y'a des petites animations au niveau de l'interface

##### CE QUI VA (SÛREMENT) BOUGER
- Je vais donner des noms plus génériques aux sidekicks. Ils sont imprononçables...
- Y'aura peut être un script `install` pour que le jeu soit moins lourd à télécharger
- DES CONTRÔLES SOURIS !!!!!
- un leaderboard ?

## LANCER LE JEU 🎮

⚠ Il faut avoir [**Java 11** or +](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html) d'installé sur ta machine, sinon rien se lancera :/

#### WINDOWS

- Clique sur `kaps.exe`

#### LINUX

- Lance le script `./kaps.sh`

... ou alors tu ouvres un terminal depuis le dossier source comme un grand, et tu lances:

```sh
> java -jar bin/kaps-demo.jar
```

#### COMMANDES ! ⌨

```sh
> ... -l [number1] [number2] ...
```

Lance une séquence de niveaux (dont le numéro est précisé dans `number`).  
Mettre `?` en guise de niveau lance un niveau au hasard.  
Mettre `!` en guise de niveau lance une grille aléatoire !

```sh
> ... -s [name1] [name2] ...
```

Ajoute le sidekick de nom `name` à l'équipe. 🤝
*[Voir les sidekicks...](#sidekicks-)*  
Là aussi, `?` chosis un sidekick au hasard.

## COMMENT JOUER 🕹

#### 💊 En partie

`⬅`, `➡` / `Q`, `D` : bouge la gélule à **gauche**/**droite**  
`⬆` / `Z` : **tourne** la gélule  
`⬇` / `S` : bouge gélule en bas là, **en bas**  
`[SPACEBAR]` : fais **tomber** la gélule   
`🇭` : conserve la gélule dans **HOLD**  
✨**NOUV.** - `V` / `B` : tourne la gélule dans le sens **anti-horaire** 🔄 / 🔁 **horaire**

#### ⚙ General

`🇵` : mettre le jeu sur **pause**  
`Esc` : **quitter** le jeu

---

# RÈGLES DU JEU 📜

Fais bouger les gélules qui tombent au fur et à mesure dans la grille et réalise des matchs de **4 éléments ou +** de la même couleur pour les détruire.  
Les éléments détruits libèrent de la **mana✨**, qui remplissent la jauge des sidekicks de leur couleur. Une fois la jauge pleine, l'attaque du sidekick est déclenchée. 💥

<img alt="la même démo" src="android/assets/footages/kaps-fullgame-sample.gif" title="Quick gameplay footage" width="200"/>

Le niveau est fini lorsque toutes les germes de la grille ont été dégommées !
(attention, elles ne subissent pas la gravité)
Fais gaffe par contre à ne pas faire déborder la grille, d'autant plus que le jeu va de plus en plus vite avec le temps.

## SIDEKICKS 🤜‍🤛

| Nom | | Mana | Dégâts | Pouvoir |  
|---:|:---:|:---:|:---:|:---|   
| JIM      | ![ JIM  ](android/assets/sprites/sidekicks/Jim_0.png "Jim")       | 20       | 1 | Découpe un élément, et tous les autres sur la même ligne
| SEAN     | ![ SEAN ](android/assets/sprites/sidekicks/Sean_0.png "Sean")     | 20       | 2 | Frappe un élément, puis les cases adjacentes
| ZYRAME   | ![ZYRAME](android/assets/sprites/sidekicks/Zyrame_0.png "Zyrame") | 20       | 2 | Découpe deux germes aux hasard
| PAINT    | ![PAINT ](android/assets/sprites/sidekicks/Paint_0.png "Paint")   | 10       |   | Repeins 8 gélules de la grille au hasard
| COLOR    | ![COLOR ](android/assets/sprites/sidekicks/Color_0.png "Color")   | 4 turns  |   | Génère une gélules avec deux couleurs identiques
| MIMAPS   | ![MIMAPS](android/assets/sprites/sidekicks/Mimaps_0.png "Mimaps") | 15       | 2 | Brule 3 éléments de la grille au hasard
| BOMBER   | ![BOMBER](android/assets/sprites/sidekicks/Bomber_0.png "Bomber") | 13 turns | 1 | Génère une gélules explosive
| SNIPER   | ![SNIPER](android/assets/sprites/sidekicks/Sniper_0.png "Sniper") | 15       | 3 | Tire sur la germe qui a le plus de vie
| RED      | ![ RED  ](android/assets/sprites/sidekicks/Red_0.png "Red")       | 25       | 2 | Découpe toute une colonne au hasard
| XERETH   | ![XERETH](android/assets/sprites/sidekicks/Xereth_0.png "Xereth") | 25       | 1 | Découpe deux diagonales
| SHUFFLER  (✨new!) | *(Coming soon !)* | 14       |   | Repeint TOUTE la grille avec des couleurs au pif
| SHADOW    (✨new!) | *(Coming soon !)* | 10 turns | 1 | Enlève toutes les gélules de sa propre couleur de la grille
| ???      | *(Coming soon !)* | 18       | 1 | Frappe un élément et les cases sur ses côtés (gauche/droite), puis un autre (haut/bas) *(bon j'avoue il est chaud à comprendre lui)*
| ???      | *(Coming soon !)* | 12       |   | Freeze définitivement une germe avec un cooldown
| ???      | *(Coming soon !)* | 15       | 3 | Frappe 4 cases au hasard dans la partie basse de la grille
| ???      | *(Coming soon !)* | 20       | 1 | Découpe une diagonale, dans un sens au pif

## GERMS 🦠

| Nom | | Cooldown | Pouvoir |  
|---:|:---:|:---:|:---|   
| BASIC | ![BASIC](android/assets/sprites/germs/basic/color1/idle_0.png "Basic") | - | "ah gros on est là hein"
| WALL  | ![WALL ](android/assets/sprites/germs/wall/level4/color2/idle_0.png "Wall")  | - | A besoin de plusieurs coups (4 max.) pour être détruit
| VIRUS | ![VIRUS](android/assets/sprites/germs/virus/color5/idle_0.png "Virus") | 8 | Transforme une gélule de la grille au hasard en virus
| THORN | ![THORN](android/assets/sprites/germs/thorn/color4/idle_0.png "Thorn") | 5 | Détruit une gélule adjacente au hasard
| ??? | *(Coming soon !)* | 6 | Peut soigner un WALL (de 1PV), ou transformer une germe BASIC en WALL
| ??? | *(Coming soon !)* | 6 | Échange de place avec la gélule la plus proche
| ??? | *(Coming soon !)* | 6 | Change sa propre couleur. comme ça.
| ??? | *(Coming soon !)* | 10 | Émet de la fumée devant un élément, masquant sa couleur. (peut être dissipé au contact, ou si l'émetteur est détruit)
| ??? | *(Coming soon !)* | | Protège une germe, et doit être détruit pour que la germe devienne atteignable.
| ??? | *(Coming soon !)* | | Pareil qu'au dessus, mais fais en sorte de masquer la couleur de la germe protégée.

## GÉLULES SPÉCIALES ✨💊

| Nom | | Effet |  
|---:|:---:|:---|   
| EXPLOSIVE | ![EXPLOSIVE](android/assets/sprites/caps/bomb/color7/unlinked.png "Explosive") | Explose quand elle est détruite, brûlant toutes les cases autour
| ??? | ? | (Coming soon !)

---

### TIPS 💡

- Les matchs de **5 éléments ou +** génèrent du mana supplémentaire pour les sidekicks
- D'ailleurs, les matchs de 5 éléments ou + font plus de dégâts sur les WALLS que les matchs classiques
- Fume les **virus** en priorité. Ça peut vite être le bordel.
- N'oublie pas que le HOLD peut te sauver la vie.
- Le choix des sidekicks et leurs **synergies** peuvent être déterminants pour certains niveaux.
- Ah oui au fait ! On peut faire des matchs de carrés 3x3, qui rapportent vraiment pas mal de mana.
- Poser des gélules explosives près des THORNS les font se suicider. C'est marrant
