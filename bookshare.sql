-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Počítač: localhost
-- Vytvořeno: Sob 29. dub 2023, 10:48
-- Verze serveru: 10.4.27-MariaDB
-- Verze PHP: 8.1.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Databáze: `bookshare`
--

-- --------------------------------------------------------

--
-- Struktura tabulky `ceka_na`
--

CREATE TABLE `ceka_na` (
  `id_cekani` int(11) NOT NULL,
  `id_uzivatele` int(11) NOT NULL,
  `id_knihy` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

-- --------------------------------------------------------

--
-- Struktura tabulky `kniha`
--

CREATE TABLE `kniha` (
  `id_knihy` int(11) NOT NULL,
  `zapujcuje` int(11) NOT NULL,
  `je_pujcena` int(11) DEFAULT NULL,
  `nazev` varchar(100) NOT NULL,
  `fotka` varchar(200) DEFAULT NULL,
  `autor` varchar(100) NOT NULL,
  `stav` varchar(50) NOT NULL,
  `popis` varchar(1000) NOT NULL,
  `cena` int(11) NOT NULL DEFAULT 0,
  `pristupnost` varchar(25) NOT NULL,
  `max_delka_zapujceni` int(11) NOT NULL,
  `datum_zapujceni` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `kniha`
--

INSERT INTO `kniha` (`id_knihy`, `zapujcuje`, `je_pujcena`, `nazev`, `fotka`, `autor`, `stav`, `popis`, `cena`, `pristupnost`, `max_delka_zapujceni`, `datum_zapujceni`) VALUES
(6, 12, NULL, 'Summer Holiday', 'images/12/books/SummerHoliday.webp', 'Lily Smith', 'nová', 'Aenean fermentum risus id tortor. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.', 0, 'pro vsechny', 150, NULL),
(9, 24, 25, 'the Book of Chaos', 'images/24/books/TheBookOfChaos.jpg', 'Jessica Renwick', 'zastaralá', 'Aenean fermentum risus id tortor. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. In dapibus augue non sapien.', 2, 'pro vsechny', 60, '2023-04-24'),
(13, 25, NULL, 'A Game of Thrones', 'images/25/books/book_image.jpg', 'George R. R. Martin', 'zachovalá ', 'Kniha sleduje rody Starků, Lannisterů, Baratheonů a mnoho dalších jak usilují o trůn v Západozemí, zatímco jiná hrozba se žene ze Severu.', 2, 'nad 18', 120, NULL),
(33, 25, NULL, 'Crack the code', 'images/25/books/CrackTheCode.jpeg', 'Patrick C. Harless', 'zachovalá, ohlé rohy', 'Aenean fermentum risus id tortor. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.', 0, 'pro vsechny', 100, NULL),
(81, 24, NULL, 'Lunar Storm', 'images/24/books/book_image.jpg', 'Terry Crosby', 'starší', 'Tato kniha se odehrává na měsíci, kde se nachází spoustu marťanů, kteří se zde schovávají před Celestiály.', 0, 'pro vsechny', 100, NULL),
(89, 25, NULL, 'A Million to one', 'images/25/books/book_image_1.jpg', 'Tony Faggioli', 'stará, ohlé rohy', '', 2, 'pro vsechny', 160, NULL);

-- --------------------------------------------------------

--
-- Struktura tabulky `ma`
--

CREATE TABLE `ma` (
  `id_knihy` int(11) NOT NULL,
  `id_moznosti` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `ma`
--

INSERT INTO `ma` (`id_knihy`, `id_moznosti`) VALUES
(6, 2),
(9, 1),
(13, 1),
(13, 2),
(33, 2),
(81, 1),
(89, 1),
(89, 2);

-- --------------------------------------------------------

--
-- Struktura tabulky `moznosti_zapujceni`
--

CREATE TABLE `moznosti_zapujceni` (
  `id_moznosti` int(11) NOT NULL,
  `typ` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `moznosti_zapujceni`
--

INSERT INTO `moznosti_zapujceni` (`id_moznosti`, `typ`) VALUES
(1, 'Osobní předání'),
(2, 'Zaslání na odběrné místo');

-- --------------------------------------------------------

--
-- Struktura tabulky `obsahuje`
--

CREATE TABLE `obsahuje` (
  `id_knihy` int(11) DEFAULT NULL,
  `id_zanru` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `obsahuje`
--

INSERT INTO `obsahuje` (`id_knihy`, `id_zanru`) VALUES
(6, 31),
(6, 7),
(9, 4),
(9, 10),
(9, 8),
(13, 4),
(13, 8),
(9, 3),
(9, 31),
(9, 29),
(33, 35),
(81, 7),
(81, 8),
(89, 11);

-- --------------------------------------------------------

--
-- Struktura tabulky `oznameni`
--

CREATE TABLE `oznameni` (
  `id_oznameni` int(11) NOT NULL,
  `id_uzivatele` int(11) NOT NULL,
  `id_druheho_uzivatele` int(11) DEFAULT NULL,
  `id_knihy` int(11) DEFAULT NULL,
  `id_typu_oznameni` int(11) NOT NULL,
  `nazev_knihy` varchar(100) DEFAULT NULL,
  `datum_prichodu` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `oznameni`
--

INSERT INTO `oznameni` (`id_oznameni`, `id_uzivatele`, `id_druheho_uzivatele`, `id_knihy`, `id_typu_oznameni`, `nazev_knihy`, `datum_prichodu`) VALUES
(303, 24, 25, NULL, 6, 'the Book of Chaos', '2023-04-18 15:35:47'),
(305, 24, 12, 6, 4, NULL, '2023-04-18 15:40:09'),
(308, 31, 25, 13, 4, NULL, '2023-03-01 17:00:27'),
(312, 24, 31, NULL, 6, 'Lunar Storm', '2023-04-18 16:14:07'),
(328, 24, 12, NULL, 5, 'Summer Holiday', '2023-04-18 16:29:45'),
(329, 31, 12, 6, 8, NULL, '2023-04-18 16:29:45'),
(330, 31, 25, NULL, 6, 'The Mind of leader', '2023-04-18 16:42:38'),
(334, 31, 12, 6, 4, NULL, '2023-04-24 09:35:23'),
(336, 25, 24, 9, 2, NULL, '2023-04-24 10:35:24'),
(337, 24, 25, 9, 3, NULL, '2023-04-24 10:35:24');

-- --------------------------------------------------------

--
-- Struktura tabulky `recenze`
--

CREATE TABLE `recenze` (
  `id_recenze` int(11) NOT NULL,
  `vytvoril` int(11) NOT NULL,
  `na` int(11) NOT NULL,
  `obsah` varchar(150) DEFAULT NULL,
  `hodnoceni` int(11) NOT NULL,
  `datum_vytvoreni` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `recenze`
--

INSERT INTO `recenze` (`id_recenze`, `vytvoril`, `na`, `obsah`, `hodnoceni`, `datum_vytvoreni`) VALUES
(3, 24, 12, 'Půjčka proběhla poštou, kniha přišla hned další den po potvrzení v aplikaci.', 5, '2022-12-08'),
(4, 12, 24, 'Knihu vrátil ve stejném stavu a včas', 5, '2022-12-08'),
(5, 12, 25, 'Čekal jsem asi 15 minut na místě doručení, jinak pohoda.', 4, '2023-02-02'),
(6, 25, 12, 'Půjčka proběhla skvěle, majitel byl na místě včas a byl velice příjemný, i když jsem přišel později.', 5, '2023-02-02'),
(11, 42, 24, NULL, 5, '2023-04-12'),
(12, 24, 42, 'vyborne', 5, '2023-04-12'),
(13, 24, 42, 'Nepřišel na místo předání, ale dál mi vědět předem.', 3, '2023-04-13'),
(22, 31, 24, 'Super kniha, super majitelka.', 5, '2023-04-19'),
(23, 25, 31, 'Vypujcka probehla v pohode.', 4, '2023-04-24'),
(24, 25, 31, 'Zadny problem', 5, '2023-04-24'),
(25, 25, 24, 'Zadny problem', 5, '2023-04-24'),
(26, 12, 24, 'Vypujcka probehla bez problemu', 5, '2023-04-24'),
(27, 25, 12, 'Vypujcka proběhla bez problémů.', 5, '2023-04-24'),
(28, 12, 25, NULL, 5, '2023-04-24');

-- --------------------------------------------------------

--
-- Struktura tabulky `typ_oznameni`
--

CREATE TABLE `typ_oznameni` (
  `id_typu_oznameni` int(11) NOT NULL,
  `typ` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `typ_oznameni`
--

INSERT INTO `typ_oznameni` (`id_typu_oznameni`, `typ`) VALUES
(1, 'žádost o knihu'),
(2, 'kontaktování majitele'),
(3, 'kontaktování půjčujícího'),
(4, 'majitel odmítnul půjčku jeho knihy.'),
(5, 'hodnocení majitele'),
(6, 'hodnocení půjčujícího'),
(7, 'časový limit výpůjčky vypršel'),
(8, 'kniha je již dostupná');

-- --------------------------------------------------------

--
-- Struktura tabulky `uzivatel`
--

CREATE TABLE `uzivatel` (
  `id_uzivatele` int(11) NOT NULL,
  `uziv_jmeno` varchar(25) NOT NULL,
  `email` varchar(320) NOT NULL,
  `sekundarni_email` varchar(320) NOT NULL,
  `tel_cislo` int(11) DEFAULT NULL,
  `profilovka` varchar(200) DEFAULT NULL,
  `misto_predani` varchar(100) DEFAULT NULL,
  `dat_narozeni` date NOT NULL,
  `heslo` varchar(300) NOT NULL,
  `PSC` int(11) DEFAULT NULL,
  `hodnoceni` double DEFAULT NULL,
  `verifikacni_token` varchar(200) DEFAULT NULL,
  `verifikovan` int(11) NOT NULL DEFAULT 0,
  `token` varchar(400) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `uzivatel`
--

INSERT INTO `uzivatel` (`id_uzivatele`, `uziv_jmeno`, `email`, `sekundarni_email`, `tel_cislo`, `profilovka`, `misto_predani`, `dat_narozeni`, `heslo`, `PSC`, `hodnoceni`, `verifikacni_token`, `verifikovan`, `token`) VALUES
(12, 'zkouska243', 'hashik@seznam.cz', 'hashik@seznam.cz', 789852963, 'images/12/man.jpeg', 'Brno, Královo pole', '2001-06-27', '3a42d337b72b80c243ae4ec55345b337fc8cca70', 61200, 5, NULL, 0, 'fgcDXqwcQRGwSQag4cwXTd:APA91bEessEBYfxDEaDSOkgR1X0V3ioDvPBPgWkPuIoYwhylFGZX1tAypeFfsPYwfAZuHvVoWuyUZM3XEkJsOll6HMDHG5SfgNf5yR4Gg9hViI_KG69r775O1Wa5cxzvklsrKNUwl0lh'),
(24, 'bali', 'bali@seznam.cz', 'bali@seznam.cz', 725487963, 'images/24/human.jpeg', 'Jihlava u Morového sloupu', '2001-06-07', 'ad97bacef25323d334604cd1f457d4b96404794c', 58842, 5, NULL, 0, 'fBZnbLhtQk6XGHyFZJsi7Z:APA91bGDh6kbd2K5sRQ4W8Y7XVNjaynopiwvYQWGZC46zCKm9Dftv2oKdiOnEfoPTODWShgVknNoi3CpSzocBDamc5zHKVEO5xcdPam0se3_yGtvz21gXCHuFsz_o5YrKHnrxzfPFRx5'),
(25, 'balik', 'balik@seznam.cz', 'balik@seznam.cz', 741888995, 'images/25/profile_picture.jpg', 'Brno u černého orloje', '2001-03-01', '347da42d5e032928f774520344eb36ac551b61f1', 60200, 4.5, NULL, 0, 'c_qnbXqkE67qHfxYz3v8us:APA91bHEEnqdk1K-LNGWB7MzgLZ7p4Pn3gPAN1Hw7AMqCMHKj4TK7zyBNLe0TW4FkohjarCTSEMwLD6oIQTYrDCmpAR0I0Ox64MnoFLrSiYiqKuTAAEKB7t9IVLHA6FvvkZEjXZL8F_D'),
(28, 'booking', 'booking@seznam.cz', 'booking@seznam.cz', 169235487, NULL, NULL, '1995-05-13', '1e91a681e9979a4d12637c9b02c84fcae93829c3', NULL, NULL, NULL, 0, 'fBZnbLhtQk6XGHyFZJsi7Z:APA91bGfLMOaVGthviOi5PxD6z5wsQDolRzjsnT5x0wIMUB0OC6rbOk6ff5kmdTR6AHH8mnEN38kS8s3LBZPW67vOMs43ML7ZcYuPGPoxl9d2Jq6j6IoZoOPn4-Z0URb7WVSbmko-x8v'),
(30, 'testik', 'testik@seznam.cz', 'testik@seznam.cz', NULL, NULL, NULL, '2013-03-01', '3a42d337b72b80c243ae4ec55345b337fc8cca70', NULL, NULL, NULL, 0, NULL),
(31, 'zelenka', 'zelenka@seznam.cz', 'zelenka@seznam.cz', NULL, NULL, NULL, '1990-12-05', 'be6c15fc0d99f73951a77cfa31a09a9a399e9b01', 58842, 4.5, NULL, 0, 'fBZnbLhtQk6XGHyFZJsi7Z:APA91bGDh6kbd2K5sRQ4W8Y7XVNjaynopiwvYQWGZC46zCKm9Dftv2oKdiOnEfoPTODWShgVknNoi3CpSzocBDamc5zHKVEO5xcdPam0se3_yGtvz21gXCHuFsz_o5YrKHnrxzfPFRx5'),
(41, 'fgjgfz', 'hjiu@dgjg.cz', 'hjiu@dgjg.cz', NULL, NULL, NULL, '2001-12-12', '3f912a11de5c8e724bad00f2a48a55ffe67eff96', NULL, NULL, NULL, 0, NULL),
(42, 'Stanislav', '1.stanik@seznam.cz', '1.stanik@seznam.cz', 222313567, NULL, 'Jihlava', '2000-12-27', 'cc0661807846d8b1702cc4d7b9e5163787e606be', 58601, 4, NULL, 0, 'dkoHG6DhSOWHxs5g3UesOo:APA91bHg9y4VimhOLtcKSGx_kP7rrO4fNP62Hd6NcQvRXTAQHlrxoY59NOlsCVYddz2Q8k6G4CcdNTcgrbHURWZRFdUAbtGHHjH4k0P10CQPyshHG7PXoa-BToamfJjNwVPXoR-YC_bZ'),
(51, 'Pepa', 'tralala@sranda.cz', 'tralala@sranda.cz', 696906969, 'images/51/profile_picture.jpg', 'Praha - pod ocasem', '1999-01-01', 'bf625e8202122acd9b6981256fb580e872bc90c7', NULL, NULL, NULL, 0, 'fMPnlYLlSWex3_ayIloraB:APA91bGxBIiLIulxjIx9ErByeiKaX4NNbIUOaV72o-HivgemMw4FuVv375qJkfTPB5fWV4moZQU3N29Th-nHytJfBLLk6J8oR1yesfWG-ufv0INLk_ADd2ZfJmv2GJDgJRqOdN3Sd6jx'),
(52, 'Mája', 'list@email.cz', 'list@email.cz', NULL, NULL, NULL, '2003-07-21', 'c8b679b7da232a9156dd062e56ee6fc85fcda06d', NULL, NULL, NULL, 0, 'dHU5OhYyQ7SO6OR24nDzKB:APA91bE9Ik_A1mhAn8EduStvY5AD0I-vD_wVk8o_u1LbFawI-eyNq-BQVO08Mm8sPdfzeu32K4BAihYgpLMXyeqR8KJOMAPX3mmNG29a30PUtWSc51X-6q_DTY1c9qYJV87ITEMqF0oE');

-- --------------------------------------------------------

--
-- Struktura tabulky `zanr`
--

CREATE TABLE `zanr` (
  `id_zanru` int(11) NOT NULL,
  `typ_zanru` varchar(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_czech_ci;

--
-- Vypisuji data pro tabulku `zanr`
--

INSERT INTO `zanr` (`id_zanru`, `typ_zanru`) VALUES
(3, 'Horror'),
(4, 'Drama'),
(7, 'Sci-fi'),
(8, 'Fantasy'),
(9, 'Povídka'),
(10, 'Román'),
(11, 'Biografie'),
(25, 'Poezie'),
(27, 'Publicistika'),
(28, 'Komiks'),
(29, 'Báje, mýty a pověsti'),
(30, 'Dívčí román'),
(31, 'Detektivka'),
(32, 'Román pro ženy'),
(33, 'Cestopis'),
(34, 'Humor a satira'),
(35, 'Literatura faktu'),
(36, 'Erotika'),
(37, 'Literatura pro děti a mládež');

--
-- Indexy pro exportované tabulky
--

--
-- Indexy pro tabulku `ceka_na`
--
ALTER TABLE `ceka_na`
  ADD PRIMARY KEY (`id_cekani`),
  ADD KEY `id_uzivatele` (`id_uzivatele`),
  ADD KEY `id_knihy` (`id_knihy`);

--
-- Indexy pro tabulku `kniha`
--
ALTER TABLE `kniha`
  ADD PRIMARY KEY (`id_knihy`),
  ADD KEY `zapujcuje` (`zapujcuje`),
  ADD KEY `je_pujcena` (`je_pujcena`);

--
-- Indexy pro tabulku `ma`
--
ALTER TABLE `ma`
  ADD KEY `id_knihy` (`id_knihy`),
  ADD KEY `id_moznosti` (`id_moznosti`);

--
-- Indexy pro tabulku `moznosti_zapujceni`
--
ALTER TABLE `moznosti_zapujceni`
  ADD PRIMARY KEY (`id_moznosti`);

--
-- Indexy pro tabulku `obsahuje`
--
ALTER TABLE `obsahuje`
  ADD KEY `id_knihy` (`id_knihy`),
  ADD KEY `id_zanru` (`id_zanru`);

--
-- Indexy pro tabulku `oznameni`
--
ALTER TABLE `oznameni`
  ADD PRIMARY KEY (`id_oznameni`),
  ADD KEY `id_druheho_uzivatele` (`id_druheho_uzivatele`),
  ADD KEY `id_knihy` (`id_knihy`),
  ADD KEY `id_uzivatele` (`id_uzivatele`),
  ADD KEY `id_typu_oznameni` (`id_typu_oznameni`) USING BTREE;

--
-- Indexy pro tabulku `recenze`
--
ALTER TABLE `recenze`
  ADD PRIMARY KEY (`id_recenze`),
  ADD KEY `vytvoril` (`vytvoril`),
  ADD KEY `na` (`na`);

--
-- Indexy pro tabulku `typ_oznameni`
--
ALTER TABLE `typ_oznameni`
  ADD PRIMARY KEY (`id_typu_oznameni`);

--
-- Indexy pro tabulku `uzivatel`
--
ALTER TABLE `uzivatel`
  ADD PRIMARY KEY (`id_uzivatele`),
  ADD UNIQUE KEY `uziv_jmeno` (`uziv_jmeno`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `sekundarni_email` (`sekundarni_email`),
  ADD UNIQUE KEY `tel_cislo` (`tel_cislo`);

--
-- Indexy pro tabulku `zanr`
--
ALTER TABLE `zanr`
  ADD PRIMARY KEY (`id_zanru`);

--
-- AUTO_INCREMENT pro tabulky
--

--
-- AUTO_INCREMENT pro tabulku `ceka_na`
--
ALTER TABLE `ceka_na`
  MODIFY `id_cekani` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pro tabulku `kniha`
--
ALTER TABLE `kniha`
  MODIFY `id_knihy` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=90;

--
-- AUTO_INCREMENT pro tabulku `moznosti_zapujceni`
--
ALTER TABLE `moznosti_zapujceni`
  MODIFY `id_moznosti` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pro tabulku `oznameni`
--
ALTER TABLE `oznameni`
  MODIFY `id_oznameni` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=348;

--
-- AUTO_INCREMENT pro tabulku `recenze`
--
ALTER TABLE `recenze`
  MODIFY `id_recenze` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT pro tabulku `typ_oznameni`
--
ALTER TABLE `typ_oznameni`
  MODIFY `id_typu_oznameni` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pro tabulku `uzivatel`
--
ALTER TABLE `uzivatel`
  MODIFY `id_uzivatele` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT pro tabulku `zanr`
--
ALTER TABLE `zanr`
  MODIFY `id_zanru` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- Omezení pro exportované tabulky
--

--
-- Omezení pro tabulku `ceka_na`
--
ALTER TABLE `ceka_na`
  ADD CONSTRAINT `ceka_na_ibfk_1` FOREIGN KEY (`id_uzivatele`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `ceka_na_ibfk_2` FOREIGN KEY (`id_knihy`) REFERENCES `kniha` (`id_knihy`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Omezení pro tabulku `kniha`
--
ALTER TABLE `kniha`
  ADD CONSTRAINT `kniha_ibfk_1` FOREIGN KEY (`zapujcuje`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `kniha_ibfk_2` FOREIGN KEY (`je_pujcena`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Omezení pro tabulku `ma`
--
ALTER TABLE `ma`
  ADD CONSTRAINT `ma_ibfk_2` FOREIGN KEY (`id_moznosti`) REFERENCES `moznosti_zapujceni` (`id_moznosti`),
  ADD CONSTRAINT `ma_ibfk_3` FOREIGN KEY (`id_knihy`) REFERENCES `kniha` (`id_knihy`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Omezení pro tabulku `obsahuje`
--
ALTER TABLE `obsahuje`
  ADD CONSTRAINT `obsahuje_ibfk_1` FOREIGN KEY (`id_knihy`) REFERENCES `kniha` (`id_knihy`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `obsahuje_ibfk_2` FOREIGN KEY (`id_zanru`) REFERENCES `zanr` (`id_zanru`);

--
-- Omezení pro tabulku `oznameni`
--
ALTER TABLE `oznameni`
  ADD CONSTRAINT `oznameni_ibfk_1` FOREIGN KEY (`id_uzivatele`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `oznameni_ibfk_2` FOREIGN KEY (`id_druheho_uzivatele`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `oznameni_ibfk_4` FOREIGN KEY (`id_typu_oznameni`) REFERENCES `typ_oznameni` (`id_typu_oznameni`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `oznameni_ibfk_5` FOREIGN KEY (`id_knihy`) REFERENCES `kniha` (`id_knihy`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Omezení pro tabulku `recenze`
--
ALTER TABLE `recenze`
  ADD CONSTRAINT `recenze_ibfk_2` FOREIGN KEY (`na`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `recenze_ibfk_3` FOREIGN KEY (`vytvoril`) REFERENCES `uzivatel` (`id_uzivatele`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
