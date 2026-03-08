# Restorani laudade broneerimissüsteem

Tehnoloogiad:
- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf
- H2 in-memory andmebaas
- Gradle

# Rakenduse käivitamine
1. käivita rakendus
   - klassist RestobroneerimineApplication.java
   - terminalist ./gradlew bootRun
2. ava brauseris http://localhost:8080

# Rakendus võimaldab:

- kuvada restorani lauaplaani
- näha, millised lauad on hõivatud
- valida broneeringu aeg
- valida inimeste arv
- valida eelistused (nt aknakoht, vaikne nurk)
- saada süsteemilt soovitus sobivaima laua kohta
- broneerida soovitatud laud
- vältida broneeringute kattumist

# Laua soovitamise loogika

Sobivaima laua valimiseks kasutatakse lihtsat punktisüsteemi.
Iga laua jaoks arvutatakse punktisumma, mis koosneb kolmest komponendist:
1. **Laua suuruse sobivus**

Mida lähemal on laua mahutavus inimeste arvule, seda parem.
2. **Eelistuste vastavus**

Kui laud vastab kasutaja eelistustele (nt aknakoht), lisatakse punkte.
3. **Täpselt sobiva laua boonus**

Kui laud mahutab täpselt sama palju inimesi – 15 boonuspunkti.

Kasutajale soovitatakse kõige kõrgema punktisummaga lauda.

# Andmemudel

Rakendus kasutab kahte peamist entiteeti:
### TableEntity:
Salvestab info restorani laua kohta:
- laua nimi
- mahutavus
- tsoon (sisesaal, terrass jne)
- asukoht saalis
- laua omadused (features)

### ReservationEntity:
Salvestab broneeringu:
- laua ID
- broneeringu algusaeg
- broneeringu lõppaeg
- inimeste arv

# Demoandmed

Rakenduse käivitamisel lisatakse automaatselt:
- 5 näidislauda
- 8 juhuslikku broneeringut
- 
See võimaldab rakendust kohe testida ilma käsitsi andmeid lisamata.

# Tööle kulunud aeg

Ligikaudu:
Projekti ülesseadmine – 1h  
Andmemudel ja repositoryd – 1.5h  
Soovitussüsteemi loomine – 1h  
Kasutajaliides (Thymeleaf) – 2h  
Broneerimisloogika – 1h  
Parandused ja testimine – 1h  

Kokku 7–8 tundi

# Keerulisemad probleemid

### Broneeringute kattuvuse kontroll:
Üks keerulisemaid osasid oli kontrollida, kas uus broneering kattub olemasolevaga.

Selleks kasutasin Spring Data JPA päringut
existsByTableIdAndStartTimeLessThanAndEndTimeGreaterThan

See kontrollib, kas olemasolev broneering algab enne uue broneeringu lõppu ja lõpeb pärast uue broneeringu algust.

### Laua soovitusalgoritm:
Sobiva laua leidmine nõudis tasakaalu:
- mitte soovitada liiga suurt lauda
- arvestada kasutaja eelistustega

Selle lahendamiseks kasutati lihtsat punktisüsteemi.

# Abiallikad
Lahenduse tegemisel kasutasin järgmisi allikaid:
- Spring Boot dokumentatsioon
- StackOverflow (Spring Data JPA päringute näited)
- ChatGPT AI tööriista projekti üles seadmiseks ja selgituste/soovituste jaoks
- IntelliJ dokumentatsioon
AI tööriista kasutasin peamiselt:
- koodistruktuuri selgitamiseks
- CSS kujunduse parandamiseks

Kõik loogika ja lõplik kood on siiski kohandatud ja integreeritud projekti autori poolt.

# Eeldused

Lahenduse tegemisel tegin järgmised eeldused:
- broneering kestab alati 2 tundi
- iga laud mahutab maksimaalselt oma capacity väärtuse
- korraga saab broneerida ainult ühe laua

# Võimalikud edasiarendused
Kui projekti edasi arendada, võiks lisada:
- broneeringu tühistamise
- mitu lauda korraga
- kasutajakontod
- parema visuaalse saaliplaani
- broneeringute nimekirja
