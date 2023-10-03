Instalace:

Server:
Je potřeba v souboru BookSharingServer/src/main/kotlin/com/example/Application.kt 
změnit řádek 19. Je potřeba změnit IP adresu na svou lokální, nebo veřejnou IP adresu v "host", na které bude server poslouchat.


Klient:
Je potřeba v souboru java/com/example/booksharingapp/data/network/RemoteDataSource.kt
změnit řádek 25. Je potřeba přepsat stávající IP adresu na stejnou IP adresu jako na serveru, viz. výše. Port zůstane stejný.


Databáze:
Pro přístup na databázi je potřeba prvně zapnout databázový server apod. přes aplikaci XAMPP, která je ke stažení zde https://www.apachefriends.org/
Poté se přesměrujte na PHPMyAdmina zde http://localhost/phpmyadmin/
Importujte sem soubor bookshare.sql, který obsahuje databázové tabulky a předvyplněná data.

Pro připojení databáze k serveru slouží tento soubor: BookSharingServer/src/main/kotlin/com/example/mysql/DbConnection.kt
Ale nemělo by být potřeba tam něco měnit.

Zapněte aplikaci XAMPP a nastartujte databázový server. Poté zapněte server například v IDE InteliJ IDEA.
V poslední řadě je potřeba nainstalovat aplikaci z IDE Android Studio na své lokální mobilní zařízení, nebo v emulátoru na daném IDE.
Obsažený apk soubor nebude fungovat, protože ta volá na server na jiné IP adrese, než vaší. Právě proto je potřeba přepsat IP adresu a nainstalovat aplikaci.

Pokud bude potřeba přístup na již vytvořené uživatele v aplikaci, tak heslo se rovná slovu před zavínáčem v uživatelovým e-mailu. Heslo je v DB totiž zašifrované.
