-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 06, 2026 at 12:21 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `business_center`
--
CREATE DATABASE IF NOT EXISTS `business_center` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `business_center`;

-- --------------------------------------------------------

--
-- Table structure for table `korisnik`
--

CREATE TABLE `korisnik` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `ime` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `resurs`
--

CREATE TABLE `resurs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `naziv` varchar(100) NOT NULL,
  `tip` varchar(50) NOT NULL,
  `radno_vreme_pocetak` time NOT NULL DEFAULT '08:00:00',
  `radno_vreme_kraj` time NOT NULL DEFAULT '20:00:00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rezervaciona_serija`
--

CREATE TABLE `rezervaciona_serija` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `korisnik_id` int(11) NOT NULL,
  `resurs_id` int(11) NOT NULL,
  `frekvencija` varchar(20) NOT NULL COMMENT 'DNEVNO, RADNI_DANI, NEDELJNO, MESEČNO',
  `datum_pocetka` datetime NOT NULL,
  `datum_kraja` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_rezervaciona_serija_korisnik_idx` (`korisnik_id`),
  KEY `fk_rezervaciona_serija_resurs_idx` (`resurs_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rezervacija`
--

CREATE TABLE `rezervacija` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `korisnik_id` int(11) NOT NULL,
  `resurs_id` int(11) NOT NULL,
  `datum_pocetka` datetime NOT NULL,
  `datum_kraja` datetime NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'AKTIVNA' COMMENT 'AKTIVNA, OBRISANA, ZAVRSENA',
  `serija_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_rezervacija_korisnik_idx` (`korisnik_id`),
  KEY `fk_rezervacija_resurs_idx` (`resurs_id`),
  KEY `fk_rezervacija_serija_idx` (`serija_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Constraints for tables
--

--
-- Constraints for table `rezervacija`
--
ALTER TABLE `rezervacija`
  ADD CONSTRAINT `fk_rezervacija_korisnik` FOREIGN KEY (`korisnik_id`) REFERENCES `korisnik` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rezervacija_resurs` FOREIGN KEY (`resurs_id`) REFERENCES `resurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rezervacija_serija` FOREIGN KEY (`serija_id`) REFERENCES `rezervaciona_serija` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `rezervaciona_serija`
--
ALTER TABLE `rezervaciona_serija`
  ADD CONSTRAINT `fk_rezervaciona_serija_korisnik` FOREIGN KEY (`korisnik_id`) REFERENCES `korisnik` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rezervaciona_serija_resurs` FOREIGN KEY (`resurs_id`) REFERENCES `resurs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Kreiramo indekse za performanse
CREATE INDEX `idx_rezervacija_datum` ON `rezervacija` (`datum_pocetka`, `datum_kraja`);
CREATE INDEX `idx_rezervacija_status` ON `rezervacija` (`status`);
CREATE INDEX `idx_rezervacija_korisnik` ON `rezervacija` (`korisnik_id`);
CREATE INDEX `idx_rezervacija_resurs` ON `rezervacija` (`resurs_id`);

--
-- Unosimo test podatke
--
INSERT INTO `korisnik` (`username`, `password`, `ime`, `email`) VALUES
('admin', 'admin123', 'Administrator', 'admin@businesscenter.com'),
('korisnik1', 'korisnik123', 'Test Korisnik', 'korisnik1@businesscenter.com');

INSERT INTO `resurs` (`naziv`, `tip`, `radno_vreme_pocetak`, `radno_vreme_kraj`) VALUES
('Konferencijska sala A', 'PROSTORIJA', '08:00:00', '20:00:00'),
('Projektor', 'OPREMA', '09:00:00', '18:00:00'),
('Sala za sastanke', 'PROSTORIJA', '08:00:00', '22:00:00');

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;